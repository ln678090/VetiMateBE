package com.graduation.project.clinic.service.impl;

import com.graduation.project.clinic.dto.req.CancelRequestReq;
import com.graduation.project.clinic.dto.req.CheckoutRequest;
import com.graduation.project.clinic.dto.req.POSCheckoutRequest;
import com.graduation.project.clinic.dto.req.ProcessCancelReq;
import com.graduation.project.clinic.dto.req.ReviewOrderReq;
import com.graduation.project.clinic.dto.req.ReviewProductReq;
import com.graduation.project.clinic.dto.resp.OrderItemResponse;
import com.graduation.project.clinic.dto.resp.OrderResponse;
import com.graduation.project.clinic.entity.Customer;
import com.graduation.project.clinic.entity.Invoice;
import com.graduation.project.clinic.entity.InvoiceItem;
import com.graduation.project.clinic.entity.InvoiceReview;
import com.graduation.project.clinic.repository.CustomerRepository;
import com.graduation.project.clinic.repository.InvoiceRepository;
import com.graduation.project.clinic.repository.InvoiceReviewRepository;
import com.graduation.project.clinic.service.OrderService;
import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.loyalty.entity.DiscountType;
import com.graduation.project.loyalty.entity.UserVoucher;
import com.graduation.project.loyalty.entity.Voucher;
import com.graduation.project.loyalty.repository.UserVoucherRepository;
import com.graduation.project.loyalty.service.LoyaltyService;
import com.graduation.project.notification.service.NotificationService;
import com.graduation.project.product.entity.Product;
import com.graduation.project.product.repository.ProductRepository;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private static final int MAX_NOTE_LENGTH = 500;

  private final InvoiceRepository invoiceRepository;
  private final CustomerRepository customerRepository;
  private final ProductRepository productRepository;
  private final NotificationService notificationService;
  private final LoyaltyService loyaltyService;
  private final UserVoucherRepository userVoucherRepository;
  private final InvoiceReviewRepository invoiceReviewRepository;
  private final UserRepository userRepository;

  /**
   * Xử lý luồng đặt hàng trực tuyến (Online Checkout) từ phía khách hàng. - Lấy thông tin User và
   * Customer tương ứng. - Tạo mới Invoice trạng thái DRAFT. - Thêm các sản phẩm vào hóa đơn và tính
   * tổng tiền (subtotal). - Áp dụng voucher giảm giá (nếu có) và tính số tiền cuối cùng. - Lưu hóa
   * đơn và tạo thông báo (notification) cho nhân viên shop.
   *
   * @param currentUserId ID của người dùng đang thực hiện đặt hàng
   * @param request Dữ liệu giỏ hàng và thông tin thanh toán, giao hàng
   * @return OrderResponse thông tin chi tiết đơn hàng sau khi tạo
   */
  @Override
  @Transactional
  public OrderResponse checkout(UUID currentUserId, CheckoutRequest request) {
    // 1. Kiểm tra tài khoản người dùng có tồn tại không
    User account = requireUser(currentUserId);

    // 2. Tìm hoặc tạo mới hồ sơ Khách hàng (Customer) gắn với User này
    Customer customer =
        customerRepository.findByUser_Id(currentUserId).orElseGet(() -> createCustomer(account));

    // 3. Chuẩn hóa chuỗi địa chỉ giao hàng từ các trường (số nhà, phường, quận...)
    String shippingAddress = buildShippingAddress(request);

    // 4. Khởi tạo đối tượng Hóa đơn (Invoice) mới với loại SHOP và trạng thái nháp (DRAFT)
    Invoice invoice =
        Invoice.builder()
            .customer(customer)
            .type("SHOP")
            .status("DRAFT")
            .items(new ArrayList<>())
            .discountAmount(BigDecimal.ZERO)
            .build();

    // 5. Ghi nhận phương thức thanh toán khách hàng chọn (VD: COD, BANKING)
    invoice.setPaymentMethod(normalizePaymentMethod(request.getPaymentMethod()));

    // 6. Tạo mã hóa đơn tự động có tiền tố ORD- kèm timestamp để đảm bảo tính duy nhất
    invoice.setInvoiceCode("ORD-" + System.currentTimeMillis());

    // 7. Lưu thông tin địa chỉ, số điện thoại và ghi chú khách hàng vào trường Note của hóa đơn
    invoice.setNote(buildOrderNote(shippingAddress, request.getPhone(), request.getNote()));

    // 8. Duyệt qua từng sản phẩm khách hàng đặt, kiểm tra tồn kho, thêm vào Invoice và tính tổng
    // tiền hàng
    BigDecimal subtotal = addOrderItems(invoice, request.getItems());

    // 9. Lưu tạm tổng tiền hàng (chưa trừ giảm giá)
    invoice.setSubtotal(subtotal);

    // 10. Kiểm tra và áp dụng Voucher nếu có, trả về số tiền được giảm
    BigDecimal discount =
        applyVoucher(invoice, currentUserId, request.getUserVoucherId(), subtotal);

    // 11. Cập nhật số tiền được giảm vào hóa đơn
    invoice.setDiscountAmount(discount);

    // 12. Tính toán số tiền cuối cùng khách phải trả (tổng tiền hàng - giảm giá, đảm bảo >= 0)
    invoice.setTotalAmount(subtotal.subtract(discount).max(BigDecimal.ZERO));

    // 13. Lưu đối tượng hóa đơn và các InvoiceItem liên quan vào database
    Invoice savedInvoice = invoiceRepository.save(invoice);

    // 14. Gửi thông báo hệ thống (Notification) tới nhân viên Shop báo hiệu có đơn hàng mới
    notificationService.createNotification(
        null, // null nghĩa là gửi cho role Admin/Staff
        "Đơn hàng mới",
        "Có đơn hàng mới: "
            + invoice.getInvoiceCode()
            + " với tổng tiền "
            + invoice.getTotalAmount().toString()
            + "đ",
        "/staff/shop/orders");

    // 15. Ánh xạ (Map) thực thể Invoice sang DTO OrderResponse để trả về cho Client
    return mapToResponse(savedInvoice);
  }

  /**
   * Xử lý thanh toán tại quầy (POS - Point of Sale) cho nhân viên bán hàng. - Tạo trực tiếp Invoice
   * với trạng thái PAID (đã thanh toán). - Mã đơn hàng được đánh dấu bằng tiền tố POS-. - Tính tổng
   * tiền các sản phẩm. - Trừ số lượng tồn kho (inventory) ngay lập tức do khách hàng lấy hàng luôn.
   *
   * @param currentUserId ID của nhân viên đang thực hiện giao dịch POS
   * @param request Danh sách sản phẩm được quét/chọn tại quầy
   * @return OrderResponse thông tin hóa đơn POS sau khi lưu
   */
  @Override
  @Transactional
  public OrderResponse posCheckout(UUID currentUserId, POSCheckoutRequest request) {
    // 1. Khởi tạo đối tượng Hóa đơn mới cho POS.
    // Vì bán trực tiếp tại quầy, trạng thái sẽ là PAID (đã thanh toán) luôn.
    Invoice invoice =
        Invoice.builder()
            .type("SHOP")
            .status("PAID")
            .paidAt(Instant.now()) // Ghi nhận luôn thời điểm thanh toán
            .items(new ArrayList<>())
            .discountAmount(BigDecimal.ZERO) // POS không hỗ trợ voucher, mặc định giảm giá = 0
            .build();

    // 2. Ghi nhận hình thức thanh toán (CASH, CREDIT CARD...) do nhân viên chọn
    invoice.setPaymentMethod(normalizePaymentMethod(request.getPaymentMethod()));

    // 3. Đánh mã hóa đơn tiền tố POS- để phân biệt với đơn đặt Online
    invoice.setInvoiceCode("POS-" + System.currentTimeMillis());

    // 4. Lưu ghi chú của nhân viên nếu có (ví dụ: tên khách, lưu ý...)
    invoice.setNote(truncateNote(request.getNote()));

    // Biến tạm để cộng dồn tổng tiền các món hàng
    BigDecimal subtotal = BigDecimal.ZERO;

    // 5. Lặp qua danh sách sản phẩm trong giỏ hàng POS
    for (POSCheckoutRequest.CartItemReq itemRequest : request.getItems()) {
      // 5.1 Tìm sản phẩm trong DB, ném lỗi nếu sản phẩm bị xóa hoặc sai ID
      Product product =
          productRepository
              .findById(itemRequest.getProductId())
              .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

      // 5.2 Tạo đối tượng InvoiceItem (Chi tiết hóa đơn)
      InvoiceItem item = new InvoiceItem();
      item.setInvoice(invoice);
      item.setProduct(product);
      item.setNameSnapshot(
          product.getName()); // Lưu lại tên tại thời điểm bán phòng khi đổi tên sau này
      item.setQuantity(BigDecimal.valueOf(itemRequest.getQuantity()));
      item.setUnitPrice(product.getPrice());

      // 5.3 Tính tổng giá trị của mục này (số lượng * đơn giá)
      BigDecimal itemTotal = product.getPrice().multiply(item.getQuantity());

      item.setTotal(itemTotal);

      // 5.4 Thêm chi tiết vào hóa đơn
      invoice.getItems().add(item);

      subtotal = subtotal.add(itemTotal);
    }

    invoice.setSubtotal(subtotal);
    invoice.setTotalAmount(subtotal);

    // Trừ kho ngay lập tức cho POS
    deductInventory(invoice);

    Invoice savedInvoice = invoiceRepository.save(invoice);

    return mapToResponse(savedInvoice);
  }

  /**
   * Lấy danh sách đơn hàng mua online của khách hàng (phục vụ chức năng xem lịch sử đơn hàng của
   * User).
   *
   * @param currentUserId ID của khách hàng
   * @return Danh sách các đơn hàng loại SHOP sắp xếp mới nhất lên đầu
   */
  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getMyOrders(UUID currentUserId) {
    // 1. Lọc trong Database tất cả hóa đơn thuộc về userId của khách hàng
    // và sắp xếp theo ngày tạo mới nhất (giảm dần)
    return invoiceRepository.findByCustomer_User_IdOrderByCreatedAtDesc(currentUserId).stream()
        // 2. Chỉ giữ lại những hóa đơn của chức năng SHOP (loại trừ các hóa đơn CLINIC nếu có)
        .filter(invoice -> "SHOP".equals(invoice.getType()))
        // 3. Map/Chuyển đổi từng thực thể Invoice thành DTO OrderResponse
        .map(this::mapToResponse)
        // 4. Gom kết quả lại thành một List và trả về
        .toList();
  }

  /**
   * Lấy chi tiết một đơn hàng cụ thể của khách hàng (có kiểm tra quyền sở hữu).
   *
   * @param invoiceId ID của hóa đơn cần xem
   * @param currentUserId ID của khách hàng đang request
   * @return OrderResponse thông tin chi tiết hóa đơn
   */
  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrderById(UUID invoiceId, UUID currentUserId) {
    // 1. Tìm hóa đơn trong DB dựa trên ID, báo lỗi nếu không tìm thấy
    Invoice invoice = requireInvoice(invoiceId);

    // 2. Kiểm tra xem người đang request (currentUserId) có thực sự là chủ sở hữu đơn hàng này
    // không.
    // Chặn người dùng khác lấy ID đơn hàng để xem trộm.
    requireOrderOwner(invoice, currentUserId);

    // 3. Nếu qua được bước kiểm tra, chuyển hóa đơn sang DTO và trả về
    return mapToResponse(invoice);
  }

  /**
   * Lấy lịch sử các giao dịch POS trong một khoảng thời gian (dùng cho báo cáo, thống kê nhân
   * viên).
   *
   * @param startDate Thời gian bắt đầu
   * @param endDate Thời gian kết thúc
   * @return Danh sách giao dịch POS đã thanh toán
   */
  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getPosHistory(Instant startDate, Instant endDate) {
    // 1. Tìm các hóa đơn thỏa mãn:
    // - Loại là SHOP
    // - Trạng thái đã thanh toán (PAID)
    // - Thời gian thanh toán nằm trong khoảng [startDate, endDate]
    // - Sắp xếp thời gian thanh toán giảm dần (mới nhất lên đầu)
    return invoiceRepository
        .findByTypeAndStatusAndPaidAtBetweenOrderByPaidAtDesc("SHOP", "PAID", startDate, endDate)
        .stream()
        // 2. Chuyển đổi List<Invoice> sang List<OrderResponse>
        .map(this::mapToResponse)
        .toList();
  }

  /**
   * Lấy toàn bộ danh sách đơn hàng online của Shop (dùng cho màn hình Quản lý đơn hàng của nhân
   * viên). - Chỉ lấy các đơn có type = SHOP và có tiền tố ORD- (loại trừ POS-). - Yêu cầu hóa đơn
   * phải có thông tin user mua hàng (loại trừ khách vãng lai).
   *
   * @return Danh sách đơn hàng sắp xếp theo thời gian tạo giảm dần
   */
  @Override
  @Transactional(readOnly = true)
  public List<OrderResponse> getAllShopOrders() {
    // 1. Lấy toàn bộ Invoice từ Database
    // (Đây là cách đơn giản để xử lý logic filter phức tạp trên Java stream thay vì viết câu Query
    // quá dài)
    return invoiceRepository.findAll().stream()
        // 2. Lọc ra những đơn hàng thuộc phần SHOP
        .filter(invoice -> "SHOP".equals(invoice.getType()))
        // 3. Lọc ra các đơn hàng online dựa vào mã ORD-
        .filter(
            invoice ->
                invoice.getInvoiceCode() != null && invoice.getInvoiceCode().startsWith("ORD-"))
        // 4. Đảm bảo đơn hàng này có thông tin Khách hàng (loại bỏ dữ liệu rác hoặc khách vãng lai)
        .filter(invoice -> invoice.getCustomer() != null && invoice.getCustomer().getUser() != null)
        // 5. Sắp xếp đơn hàng mới nhất lên đầu tiên theo thời gian tạo
        .sorted((first, second) -> second.getCreatedAt().compareTo(first.getCreatedAt()))
        // 6. Chuyển thành DTO
        .map(this::mapToResponse)
        // 7. Chuyển Stream sang List
        .toList();
  }

  // Phương thức helper: Xử lý hoàn lại Voucher cho khách nếu đơn hàng bị hủy
  private void refundVoucherIfAny(Invoice invoice) {
    // Nếu hóa đơn có liên kết với 1 UserVoucher (mã giảm giá của khách)
    if (invoice.getUserVoucher() != null) {
      com.graduation.project.loyalty.entity.UserVoucher uv = invoice.getUserVoucher();
      // Đánh dấu Voucher này trở lại trạng thái CHƯA SỬ DỤNG
      uv.setIsUsed(false);
      // Xóa thời gian sử dụng
      uv.setUsedAt(null);
      // Lưu lại vào DB để khách có thể dùng lại cho đơn khác
      userVoucherRepository.save(uv);
    }
  }

  /**
   * Cập nhật trạng thái của đơn hàng (Xác nhận, Giao hàng, Đã giao, Hủy đơn). - Nếu Hủy đơn: Hoàn
   * lại voucher, ghi chú lý do hủy, hoàn lại số lượng tồn kho. - Nếu Xác nhận đơn: Trừ số lượng tồn
   * kho. - Nếu Đã giao: Cộng điểm thưởng (loyalty points) cho khách hàng.
   *
   * @param invoiceId ID của hóa đơn cần cập nhật
   * @param newStatus Trạng thái mới
   * @param cancelReason Lý do hủy (nếu có, do nhân viên shop nhập)
   * @return OrderResponse thông tin đơn hàng sau khi cập nhật
   */
  @Override
  @Transactional
  public OrderResponse updateOrderStatus(UUID invoiceId, String newStatus, String cancelReason) {
    // 1. Tìm hóa đơn trong DB, ném lỗi nếu không tồn tại
    Invoice invoice = requireInvoice(invoiceId);

    // 2. Đảm bảo hóa đơn thuộc loại SHOP (nhân viên shop không được sửa đơn CLINIC)
    requireShopInvoice(invoice);

    // 3. Lưu lại trạng thái hiện tại (oldStatus) để lát nữa so sánh logic hoàn kho/trừ kho
    String oldStatus = invoice.getStatus();

    // 4. Gán trạng thái mới cho hóa đơn
    invoice.setStatus(newStatus);

    // 5. Nếu trạng thái mới là HỦY (CANCELLED) và nhân viên có nhập lý do hủy
    if ("CANCELLED".equals(newStatus) && cancelReason != null && !cancelReason.trim().isEmpty()) {
      // 5.1. Lấy ghi chú cũ (nếu null thì coi như rỗng)
      String note = invoice.getNote() != null ? invoice.getNote() : "";

      // 5.2. Nối thêm lý do hủy do nhân viên nhập vào
      note = note + " | [SHOP_CANCELLED]: " + cancelReason.trim();

      // 5.3. Đảm bảo độ dài note không vượt quá giới hạn của DB (thường là 500 ký tự)
      if (note.length() > 500) {
        note = note.substring(0, 497) + "...";
      }

      // 5.4. Gán lại chuỗi ghi chú mới cho hóa đơn
      invoice.setNote(note);
    }

    // 6. Xử lý nghiệp vụ liên quan đến Kho và Voucher dựa trên sự thay đổi trạng thái
    if ("CANCELLED".equals(newStatus)) {
      // 6.1. Nếu đơn hàng chuyển sang Hủy: Hoàn lại mã giảm giá cho khách
      refundVoucherIfAny(invoice);

      // 6.2. Nếu trạng thái trước đó đã đi qua bước "Xác nhận" (CONFIRMED) trở đi
      // nghĩa là trước đó hàng đã bị trừ kho, thì bây giờ hủy phải cộng trả lại kho
      // (restoreInventory)
      if ("CONFIRMED".equals(oldStatus)
          || "SHIPPING".equals(oldStatus)
          || "DELIVERED".equals(oldStatus)
          || "PAID".equals(oldStatus)) {
        restoreInventory(invoice);
      }
    } else if ("CONFIRMED".equals(newStatus)
        && ("DRAFT".equals(oldStatus) || "PENDING".equals(oldStatus))) {
      // 6.3. Nếu đơn hàng chuyển sang Xác nhận (từ Trạng thái Nháp hoặc Chờ xử lý)
      // thì tiến hành trừ số lượng hàng trong kho (deductInventory)
      deductInventory(invoice);
    }

    // 7. Lưu đối tượng hóa đơn với các thay đổi vào DB
    Invoice savedInvoice = invoiceRepository.save(invoice);

    // 8. Lấy ID của Khách hàng (User) từ hóa đơn
    UUID ownerUserId = getCustomerUserId(savedInvoice);

    // 9. Nếu đơn hàng vừa chuyển sang ĐÃ GIAO (DELIVERED) thành công, tiến hành cộng điểm thưởng
    // (Loyalty Point)
    if ("DELIVERED".equals(newStatus) && ownerUserId != null) {
      loyaltyService.earnPoints(ownerUserId, savedInvoice.getTotalAmount(), savedInvoice.getId());
    }

    // 10. Gửi thông báo Notification về điện thoại/web cho khách hàng báo hiệu đơn hàng đổi trạng
    // thái
    if (ownerUserId != null) {
      sendOrderStatusNotification(savedInvoice, ownerUserId, newStatus, cancelReason);
    }

    // 11. Ánh xạ về DTO và trả về
    return mapToResponse(savedInvoice);
  }

  /**
   * Khách hàng yêu cầu hủy đơn hàng. Yêu cầu này sẽ được thêm vào note của đơn hàng với cú pháp
   * [CANCEL_REQUEST]. Đơn hàng sẽ không tự động chuyển sang trạng thái CANCELLED mà cần nhân viên
   * duyệt.
   *
   * @param invoiceId ID của đơn hàng
   * @param currentUserId ID của khách hàng
   * @param request Yêu cầu hủy (chứa lý do hủy)
   * @return OrderResponse thông tin đơn hàng sau khi lưu yêu cầu
   */
  @Override
  @Transactional
  public OrderResponse cancelRequest(UUID invoiceId, UUID currentUserId, CancelRequestReq request) {
    // 1. Tìm đơn hàng trong DB
    Invoice invoice = requireInvoice(invoiceId);

    // 2. Phải đảm bảo người gửi yêu cầu hủy chính là chủ nhân của đơn hàng này
    requireOrderOwner(invoice, currentUserId);

    // 3. Nếu đơn hàng Đã Giao hoặc Đã Hủy trước đó thì báo lỗi không cho phép thao tác
    if ("DELIVERED".equals(invoice.getStatus()) || "CANCELLED".equals(invoice.getStatus())) {
      throw new IllegalStateException("Không thể hủy đơn đã giao hoặc đã hủy");
    }

    // 4. Lấy nội dung ghi chú cũ của đơn hàng
    String currentNote = invoice.getNote() == null ? "" : invoice.getNote();

    // 5. Nối thêm 1 chuỗi flag [CANCEL_REQUEST] kèm lý do vào Note để báo hiệu cho Admin/Shop thấy
    // có yêu cầu hủy
    String newNote = currentNote + " | [CANCEL_REQUEST]: " + normalizeText(request.getReason());

    // 6. Giới hạn độ dài note, tránh lỗi quá tải ký tự trong Database
    invoice.setNote(truncateNote(newNote));

    // 7. Lưu lại hóa đơn, lúc này status vẫn chưa đổi thành CANCELLED ngay mà đợi Shop duyệt
    Invoice savedInvoice = invoiceRepository.save(invoice);

    // 8. Trả về DTO cập nhật mới cho Frontend
    return mapToResponse(savedInvoice);
  }

  /**
   * Nhân viên Shop xử lý (Chấp nhận/Từ chối) yêu cầu hủy đơn của khách hàng. - Nếu chấp nhận:
   * Chuyển status sang CANCELLED, hoàn voucher, hoàn tồn kho. - Nếu từ chối: Xóa chuỗi
   * [CANCEL_REQUEST] khỏi note của đơn hàng. - Gửi thông báo kết quả cho khách hàng.
   *
   * @param invoiceId ID của đơn hàng
   * @param request Quyết định của nhân viên (true/false)
   * @return OrderResponse thông tin đơn hàng sau khi xử lý
   */
  @Override
  @Transactional
  public OrderResponse processCancelRequest(UUID invoiceId, ProcessCancelReq request) {
    // 1. Lấy thông tin hóa đơn từ Database, ném ngoại lệ nếu không tồn tại
    Invoice invoice = requireInvoice(invoiceId);

    // 2. Kiểm tra xem hóa đơn có đúng loại SHOP không (chỉ nhân viên shop mới xử lý đơn SHOP)
    requireShopInvoice(invoice);

    // 3. Nếu nhân viên shop đồng ý hủy đơn (request.getAccept() == true)
    if (Boolean.TRUE.equals(request.getAccept())) {
      // 3.1. Lưu lại trạng thái cũ để kiểm tra xem có cần hoàn lại số lượng tồn kho không
      String oldStatus = invoice.getStatus();

      // 3.2. Cập nhật trạng thái hóa đơn thành Đã Hủy
      invoice.setStatus("CANCELLED");

      // 3.3. Hoàn lại Voucher (nếu khách có sử dụng) để khách có thể dùng lại sau
      refundVoucherIfAny(invoice);

      // 3.4. Nếu đơn hàng đã được xác nhận (trừ kho), đang giao hoặc đã giao, ta phải hoàn lại tồn
      // kho cho shop
      if ("CONFIRMED".equals(oldStatus)
          || "SHIPPING".equals(oldStatus)
          || "DELIVERED".equals(oldStatus)
          || "PAID".equals(oldStatus)) {
        restoreInventory(invoice);
      }
    } else {
      // 4. Nếu nhân viên TỪ CHỐI hủy đơn (không đồng ý), thì ta xóa dòng chữ [CANCEL_REQUEST] khỏi
      // ghi chú (note)
      // Việc này giúp hóa đơn trở lại trạng thái bình thường và nhân viên có thể tiếp tục xử lý
      removeCancellationRequest(invoice);
    }

    // 5. Lưu lại những thay đổi (về trạng thái, ghi chú) vào Database
    Invoice savedInvoice = invoiceRepository.save(invoice);

    // 6. Lấy ID của Khách hàng để gửi thông báo
    UUID ownerUserId = getCustomerUserId(savedInvoice);

    if (ownerUserId != null) {
      // 6.1. Xây dựng nội dung thông báo trả về cho khách (tùy thuộc vào việc Chấp nhận hay Từ
      // chối)
      String message =
          Boolean.TRUE.equals(request.getAccept())
              ? "Yêu cầu hủy đơn hàng của bạn đã được chấp nhận."
              : "Yêu cầu hủy đơn hàng của bạn đã bị từ chối.";

      // 6.2. Tạo và gửi Notification tới hệ thống để báo cho khách
      notificationService.createNotification(
          ownerUserId,
          "Phản hồi yêu cầu hủy đơn " + savedInvoice.getInvoiceCode(),
          message,
          "/profile/orders?orderId=" + savedInvoice.getId());
    }

    // 7. Trả về thông tin hóa đơn mới để Frontend cập nhật UI
    return mapToResponse(savedInvoice);
  }

  @Override
  @Transactional
  public OrderResponse reviewOrder(UUID invoiceId, UUID currentUserId, ReviewOrderReq request) {
    // 1. Tìm đơn hàng cần đánh giá trong CSDL
    Invoice invoice = requireInvoice(invoiceId);

    // 2. Phải đảm bảo người đánh giá là chủ nhân của đơn hàng
    requireOrderOwner(invoice, currentUserId);

    // 3. Chỉ cho phép đánh giá khi đơn hàng đã được giao thành công
    if (!"DELIVERED".equals(invoice.getStatus())) {
      throw new IllegalStateException("Chỉ có thể đánh giá đơn hàng đã giao thành công");
    }

    // 4. Mỗi đơn hàng chỉ được đánh giá 1 lần, nếu đã đánh giá rồi thì báo lỗi
    if (Boolean.TRUE.equals(invoice.getIsReviewed())) {
      throw new IllegalStateException("Đơn hàng này đã được đánh giá");
    }

    // 5. Cập nhật cờ (flag) isReviewed = true để ngăn không cho đánh giá lại lần sau
    invoice.setIsReviewed(true);

    // 6. Lưu trạng thái hóa đơn mới
    Invoice savedInvoice = invoiceRepository.save(invoice);

    // 7. Lặp qua từng sản phẩm mà người dùng đã submit đánh giá
    for (ReviewProductReq reviewRequest : request.getReviews()) {
      // 7.1. Lưu đánh giá cho từng sản phẩm (bao gồm số sao, nội dung comment)
      saveProductReview(savedInvoice, reviewRequest);
    }

    // 8. Thưởng thêm 50 điểm Loyalty Point cho User như một món quà khích lệ vì đã viết đánh giá
    loyaltyService.addPoints(
        currentUserId,
        50,
        "Đánh giá đơn hàng " + savedInvoice.getInvoiceCode(),
        savedInvoice.getId());

    // 9. Trả về thông tin hóa đơn sau khi đã đổi trạng thái Review
    return mapToResponse(savedInvoice);
  }

  // --- CÁC PHƯƠNG THỨC HELPER (HỖ TRỢ) NỘI BỘ ---

  // Lấy thông tin User, nếu không có ném lỗi
  private User requireUser(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
  }

  // Khởi tạo Customer profile nếu User chưa từng mua hàng
  private Customer createCustomer(User account) {
    Customer customer = Customer.builder().user(account).build();
    return customerRepository.save(customer);
  }

  // Lấy thông tin Hóa đơn
  private Invoice requireInvoice(UUID invoiceId) {
    return invoiceRepository
        .findById(invoiceId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
  }

  // Bắt buộc hóa đơn phải là loại SHOP
  private void requireShopInvoice(Invoice invoice) {
    if (!"SHOP".equals(invoice.getType())) {
      throw new IllegalArgumentException("Chỉ đơn hàng SHOP được phép xử lý");
    }
  }

  // Bắt buộc User request phải là chủ nhân của đơn hàng
  private void requireOrderOwner(Invoice invoice, UUID currentUserId) {
    UUID ownerUserId = getCustomerUserId(invoice);
    if (ownerUserId == null || !ownerUserId.equals(currentUserId)) {
      throw new SecurityException("Bạn không có quyền truy cập đơn hàng");
    }
  }

  // Trích xuất UUID của người mua từ Invoice
  private UUID getCustomerUserId(Invoice invoice) {
    if (invoice.getCustomer() == null || invoice.getCustomer().getUser() == null) {
      return null;
    }
    return invoice.getCustomer().getUser().getId();
  }

  // Ghép các thành phần địa chỉ thành 1 chuỗi hoàn chỉnh
  private String buildShippingAddress(CheckoutRequest request) {
    return String.join(
        ", ",
        normalizeText(request.getSpecificAddress()),
        normalizeText(request.getDistrict()),
        normalizeText(request.getCity()));
  }

  // Tạo chuỗi ghi chú đơn hàng từ các thông tin khách hàng cung cấp
  private String buildOrderNote(String shippingAddress, String phone, String customerNote) {
    // 1. Nối các thông tin lại với nhau thành 1 chuỗi dài
    String note =
        "Shipping Address: "
            + shippingAddress
            + " | Phone: "
            + normalizeText(phone)
            + " | Note: "
            + normalizeText(customerNote);

    // 2. Cắt ngắn chuỗi nếu nó quá dài (vượt quá giới hạn database)
    return truncateNote(note);
  }

  // Cắt ngắn chuỗi ghi chú để tránh lỗi tràn bộ đệm cơ sở dữ liệu
  private String truncateNote(String value) {
    // 1. Trả về null nếu giá trị ban đầu là null
    if (value == null) {
      return null;
    }
    // 2. Nếu chuỗi nhỏ hơn mức tối đa cho phép thì trả về nguyên bản
    if (value.length() <= MAX_NOTE_LENGTH) {
      return value;
    }

    // 3. Nếu dài hơn, cắt bớt và thêm "..." vào cuối
    return value.substring(0, MAX_NOTE_LENGTH - 3) + "...";
  }

  // Chuẩn hóa chuỗi văn bản (cắt bỏ khoảng trắng thừa ở 2 đầu)
  private String normalizeText(String value) {
    return value == null ? "" : value.trim();
  }

  // Chuẩn hóa tên phương thức thanh toán
  private String normalizePaymentMethod(String paymentMethod) {
    // Nếu truyền vào COD (thanh toán khi nhận hàng), ta chuyển thành CASH (Tiền mặt) cho đồng nhất
    return "COD".equals(paymentMethod) ? "CASH" : paymentMethod;
  }

  // Helper: Xử lý thêm danh sách sản phẩm từ giỏ hàng vào thực thể Hóa đơn
  private BigDecimal addOrderItems(Invoice invoice, List<CheckoutRequest.CartItemReq> requests) {
    // 1. Khởi tạo biến tạm tính tổng tiền hàng
    BigDecimal subtotal = BigDecimal.ZERO;

    // 2. Duyệt qua từng sản phẩm khách mua
    for (CheckoutRequest.CartItemReq itemRequest : requests) {
      // 2.1 Tìm sản phẩm thực tế trong CSDL
      Product product =
          productRepository
              .findById(itemRequest.getProductId())
              .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

      // 2.2 Tạo dòng chi tiết hóa đơn (InvoiceItem)
      InvoiceItem item = new InvoiceItem();
      item.setInvoice(invoice); // Gắn với hóa đơn cha
      item.setProduct(product); // Gắn với sản phẩm
      item.setNameSnapshot(product.getName()); // Lưu lại tên sản phẩm tại thời điểm này
      item.setQuantity(BigDecimal.valueOf(itemRequest.getQuantity())); // Ghi nhận số lượng mua
      item.setUnitPrice(product.getPrice()); // Ghi nhận đơn giá

      // 2.3 Tính tổng tiền cho dòng sản phẩm này = đơn giá * số lượng
      BigDecimal itemTotal = product.getPrice().multiply(item.getQuantity());
      item.setTotal(itemTotal);

      // 2.4 Thêm dòng chi tiết vào danh sách của Hóa đơn
      invoice.getItems().add(item);

      // 2.5 Cộng dồn vào biến tính tổng tiền hàng
      subtotal = subtotal.add(itemTotal);
    }

    // 3. Trả về tổng tiền hàng (chưa có voucher)
    return subtotal;
  }

  // Helper: Áp dụng mã giảm giá và tính toán số tiền được giảm
  private BigDecimal applyVoucher(
      Invoice invoice, UUID currentUserId, UUID userVoucherId, BigDecimal subtotal) {
    // 1. Nếu khách không chọn voucher thì trả về số tiền giảm là 0
    if (userVoucherId == null) {
      return BigDecimal.ZERO;
    }

    // 2. Tìm UserVoucher trong ví của khách hàng
    UserVoucher userVoucher =
        userVoucherRepository
            .findById(userVoucherId)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));

    // 3. Kiểm tra xem mã này đã từng được sử dụng trước đây chưa
    if (Boolean.TRUE.equals(userVoucher.getIsUsed())) {
      throw new IllegalStateException("Voucher đã được sử dụng");
    }

    // 4. Xác nhận mã giảm giá này đúng là của người dùng hiện tại
    if (userVoucher.getUser() == null || !userVoucher.getUser().getId().equals(currentUserId)) {
      throw new SecurityException("Voucher không thuộc tài khoản hiện tại");
    }

    // 5. Trích xuất thông tin Voucher gốc (thể lệ giảm giá)
    Voucher voucher = userVoucher.getVoucher();

    // 6. Kiểm tra xem tổng tiền đơn hàng có đạt giá trị tối thiểu không
    if (voucher.getMinOrderAmount() != null
        && subtotal.compareTo(voucher.getMinOrderAmount()) < 0) {
      throw new IllegalStateException("Đơn hàng chưa đạt giá trị tối thiểu");
    }

    BigDecimal discount;

    // 7. Tính số tiền giảm giá dựa vào loại voucher
    if (voucher.getDiscountType() == DiscountType.FIXED) {
      // 7.1 Nếu voucher loại FIX: Giảm trực tiếp 1 số tiền cụ thể (ví dụ 50.000đ)
      discount = voucher.getDiscountValue();
    } else {
      // 7.2 Nếu voucher loại PERCENT: Tính % của tổng tiền (ví dụ giảm 10% của 200k = 20k)
      discount = subtotal.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));

      // 7.3 Áp dụng giới hạn giảm tối đa (ví dụ giảm 10% nhưng tối đa 30k)
      if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
        discount = voucher.getMaxDiscount();
      }
    }

    // 8. Đánh dấu UserVoucher là ĐÃ SỬ DỤNG và cập nhật thời gian
    userVoucher.setIsUsed(true);
    userVoucher.setUsedAt(LocalDateTime.now());

    // 9. Lưu vào database
    userVoucherRepository.save(userVoucher);

    // 10. Gắn voucher vào hóa đơn hiện tại
    invoice.setUserVoucher(userVoucher);

    // 11. Trả về số tiền giảm, tối đa không vượt quá tổng tiền hóa đơn
    return discount.min(subtotal);
  }

  // Helper: Gửi thông báo cho khách hàng khi đơn hàng đổi trạng thái
  private void sendOrderStatusNotification(
      Invoice invoice, UUID ownerUserId, String newStatus, String cancelReason) {
    // 1. Dịch trạng thái hệ thống sang tiếng Việt để hiển thị cho người dùng
    String statusText =
        switch (newStatus) {
          case "CONFIRMED" -> "đã được xác nhận";
          case "SHIPPING" -> "đang được giao";
          case "DELIVERED" -> "đã giao thành công";
          case "CANCELLED" -> "đã bị hủy";
          default -> "được cập nhật trạng thái";
        };

    // 2. Xây dựng nội dung tin nhắn cơ bản
    String message = "Đơn hàng của bạn " + statusText + ".";

    // 3. Nếu đơn bị hủy và có lý do hủy, thì nối thêm lý do vào tin nhắn
    if ("CANCELLED".equals(newStatus) && cancelReason != null && !cancelReason.trim().isEmpty()) {
      message += " Lý do: " + cancelReason.trim();
    }

    // 4. Tạo và gửi Notification thông qua NotificationService
    notificationService.createNotification(
        ownerUserId, // Gửi cho ai
        "Cập nhật đơn hàng " + invoice.getInvoiceCode(), // Tiêu đề
        message, // Nội dung
        "/profile/orders?orderId=" + invoice.getId()); // Link chuyển hướng khi click vào thông báo
  }

  // Helper: Xóa cờ [CANCEL_REQUEST] khỏi ghi chú (khi nhân viên từ chối yêu cầu hủy)
  private void removeCancellationRequest(Invoice invoice) {
    // 1. Lấy ghi chú hiện tại
    String note = invoice.getNote();

    // 2. Nếu không có cờ này thì bỏ qua không làm gì cả
    if (note == null || !note.contains("| [CANCEL_REQUEST]:")) {
      return;
    }

    // 3. Tìm vị trí bắt đầu của chuỗi [CANCEL_REQUEST]
    int markerIndex = note.indexOf("| [CANCEL_REQUEST]:");

    // 4. Cắt chuỗi để lấy phần trước đó (loại bỏ phần yêu cầu hủy) và lưu lại
    invoice.setNote(note.substring(0, markerIndex).trim());
  }

  // Helper: Lưu một đánh giá sản phẩm vào CSDL và tính toán lại sao trung bình của sản phẩm
  private void saveProductReview(Invoice invoice, ReviewProductReq request) {
    // 1. Tìm sản phẩm được đánh giá
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Không tìm thấy sản phẩm: " + request.getProductId()));

    // 2. Tạo đối tượng Review mới (liên kết với hóa đơn, khách hàng, và sản phẩm)
    InvoiceReview review =
        InvoiceReview.builder()
            .invoice(invoice)
            .customer(invoice.getCustomer())
            .product(product)
            .rating(request.getRating())
            .comment(request.getComment())
            .build();

    // 3. Lưu review vào DB
    invoiceReviewRepository.save(review);

    // 4. Lấy tất cả các đánh giá của sản phẩm này để tính toán lại điểm đánh giá trung bình
    List<InvoiceReview> reviews =
        invoiceReviewRepository.findByProduct_SlugOrderByCreatedAtDesc(product.getSlug());

    // 5. Đếm tổng số lượt đánh giá
    int reviewCount = reviews.size();

    // 6. Tính số sao trung bình (nếu không có thì mặc định là 5.0)
    double averageRating =
        reviews.stream().mapToInt(InvoiceReview::getRating).average().orElse(5.0);

    // 7. Cập nhật lại số lượt và sao trung bình vào thực thể Sản phẩm
    product.setReviewCount(reviewCount);
    product.setRating(BigDecimal.valueOf(averageRating));

    // 8. Lưu sản phẩm
    productRepository.save(product);
  }

  // Helper: Trừ tồn kho sản phẩm khi đơn hàng được bán ra
  private void deductInventory(Invoice invoice) {
    // 1. Duyệt qua từng mặt hàng trong hóa đơn
    for (InvoiceItem item : invoice.getItems()) {
      if (item.getProduct() != null) {
        Product product = item.getProduct();

        // 2. Lấy số lượng tồn kho hiện tại (nếu null coi như 0)
        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

        // 3. Lấy số lượng khách mua
        int qty = item.getQuantity().intValue();

        // 4. Nếu số lượng mua lớn hơn tồn kho thì ném lỗi (Chống bán âm kho)
        if (currentStock < qty) {
          throw new IllegalStateException(
              "Sản phẩm " + product.getName() + " không đủ số lượng tồn kho");
        }

        // 5. Trừ đi lượng đã bán
        product.setStockQuantity(currentStock - qty);

        // 6. Lưu lại cập nhật kho
        productRepository.save(product);
      }
    }
  }

  // Helper: Hoàn lại tồn kho sản phẩm (dùng khi hủy đơn hàng đã trừ kho)
  private void restoreInventory(Invoice invoice) {
    // 1. Duyệt qua từng mặt hàng trong hóa đơn bị hủy
    for (InvoiceItem item : invoice.getItems()) {
      if (item.getProduct() != null) {
        Product product = item.getProduct();

        // 2. Lấy số lượng tồn kho hiện tại
        int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

        // 3. Lấy số lượng khách đã mua trong đơn hàng này
        int qty = item.getQuantity().intValue();

        // 4. Cộng trả lại số lượng đó vào tồn kho
        product.setStockQuantity(currentStock + qty);

        // 5. Lưu lại cập nhật kho
        productRepository.save(product);
      }
    }
  }

  private OrderResponse mapToResponse(Invoice invoice) {
    String frontendStatus = mapFrontendStatus(invoice.getStatus());

    List<OrderItemResponse> items =
        invoice.getItems().stream()
            .map(
                item ->
                    OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct() == null ? null : item.getProduct().getId())
                        .productName(item.getNameSnapshot())
                        .productImage(
                            item.getProduct() == null ? null : item.getProduct().getImageUrl())
                        .price(item.getUnitPrice())
                        .quantity(item.getQuantity().intValue())
                        .stockQuantity(
                            item.getProduct() != null ? item.getProduct().getStockQuantity() : 0)
                        .isActive(
                            item.getProduct() != null ? item.getProduct().getIsActive() : false)
                        .build())
            .toList();

    String paymentMethod =
        "CASH".equals(invoice.getPaymentMethod()) ? "COD" : invoice.getPaymentMethod();

    User account = invoice.getCustomer() == null ? null : invoice.getCustomer().getUser();

    return OrderResponse.builder()
        .id(invoice.getId())
        .code(invoice.getInvoiceCode())
        .status(frontendStatus)
        .totalAmount(invoice.getSubtotal())
        .discountAmount(invoice.getDiscountAmount())
        .shippingFee(BigDecimal.ZERO)
        .finalAmount(invoice.getTotalAmount())
        .createdAt(invoice.getCreatedAt())
        .updatedAt(invoice.getUpdatedAt())
        .paymentMethod(paymentMethod)
        .shippingAddress(account == null ? null : account.getAddress())
        .note(invoice.getNote())
        .customerName(account == null ? null : account.getFullName())
        .customerPhone(account == null ? null : account.getPhone())
        .isReviewed(invoice.getIsReviewed())
        .items(items)
        .build();
  }

  private String mapFrontendStatus(String status) {
    return switch (status) {
      case "DRAFT" -> "PENDING";
      case "CONFIRMED" -> "CONFIRMED";
      case "SHIPPING" -> "SHIPPING";
      case "DELIVERED", "PAID" -> "DELIVERED";
      case "CANCELLED" -> "CANCELLED";
      default -> "PENDING";
    };
  }
}
