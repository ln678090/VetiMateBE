package com.graduation.project.loyalty.service.impl;

import com.graduation.project.common.exception.ResourceNotFoundException;
import com.graduation.project.loyalty.dto.*;
import com.graduation.project.loyalty.entity.*;
import com.graduation.project.loyalty.repository.*;
import com.graduation.project.loyalty.service.LoyaltyService;
import com.graduation.project.user.entity.User;
import com.graduation.project.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoyaltyServiceImpl implements LoyaltyService {

  private final VoucherRepository voucherRepository;
  private final UserLoyaltyPointsRepository pointsRepository;
  private final PointTransactionRepository transactionRepository;
  private final UserVoucherRepository userVoucherRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public PointsResponse getMyPoints(UUID userId) {
    UserLoyaltyPoints points =
        pointsRepository
            .findById(userId)
            .orElse(UserLoyaltyPoints.builder().totalPoints(0).availablePoints(0).build());
    return PointsResponse.builder()
        .totalPoints(points.getTotalPoints())
        .availablePoints(points.getAvailablePoints())
        .totalSpending(points.getTotalSpending())
        .tier(points.getTier())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TransactionDto> getMyTransactions(UUID userId) {
    return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::mapToTransactionDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<VoucherDto> getAvailableVouchers() {
    return voucherRepository.findAvailableVouchers(LocalDateTime.now()).stream()
        .map(this::mapToVoucherDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserVoucherDto> getMyVouchers(UUID userId) {
    return userVoucherRepository.findByUserIdOrderByRedeemedAtDesc(userId).stream()
        .map(this::mapToUserVoucherDto)
        .collect(Collectors.toList());
  }

  /**
   * Tính toán và cộng điểm thưởng cho khách hàng sau khi mua hàng thành công. Tỷ lệ quy đổi: 20,000
   * VND = 10 điểm (tương đương 1 VND = 1/2000 điểm). - Cập nhật điểm hiện tại và tổng điểm. - Cộng
   * dồn tổng chi tiêu (totalSpending). - Tính toán lại hạng thành viên (Tier) dựa trên tổng chi
   * tiêu mới. - Ghi lại lịch sử giao dịch cộng điểm.
   *
   * @param userId ID của khách hàng
   * @param orderAmount Tổng tiền của đơn hàng
   * @param orderId ID của đơn hàng (dùng để tra cứu lịch sử)
   */
  @Override
  @Transactional
  public void earnPoints(UUID userId, BigDecimal orderAmount, UUID orderId) {
    // 1. Kiểm tra số tiền đơn hàng hợp lệ (phải lớn hơn 0)
    if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) <= 0) return;

    // 2. Tính số điểm đạt được theo tỷ lệ: 20,000 VND = 10 points => 1 VND = 1/2000 points
    // Sử dụng RoundingMode.DOWN để làm tròn xuống (ví dụ 19,999 đ không được tính điểm)
    int earnedPoints = orderAmount.divide(new BigDecimal("2000"), RoundingMode.DOWN).intValue();

    if (earnedPoints > 0) {
      // 3. Tìm User trong DB để đảm bảo khách hàng tồn tại
      User user =
          userRepository
              .findById(userId)
              .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      // 4. Lấy thông tin Ví điểm (LoyaltyPoints) của khách hàng.
      // Nếu chưa từng có ví điểm thì tạo mới với các giá trị = 0
      UserLoyaltyPoints points =
          pointsRepository
              .findById(userId)
              .orElseGet(
                  () -> {
                    UserLoyaltyPoints newPoints =
                        UserLoyaltyPoints.builder()
                            .user(user)
                            .userId(userId)
                            .totalPoints(0) // Tổng số điểm tích luỹ từ trước đến nay
                            .availablePoints(0) // Số điểm hiện có thể dùng
                            .build();
                    return pointsRepository.save(newPoints);
                  });

      // 5. Cộng số điểm vừa đạt được vào Tổng điểm tích lũy và Điểm khả dụng
      points.setTotalPoints(points.getTotalPoints() + earnedPoints);
      points.setAvailablePoints(points.getAvailablePoints() + earnedPoints);

      // 6. Cộng dồn số tiền đơn hàng vào Tổng chi tiêu của User
      points.setTotalSpending(points.getTotalSpending().add(orderAmount));

      // 7. Dựa vào Tổng chi tiêu mới, tính toán và cập nhật lại Hạng thành viên (MEMBER, SILVER,
      // GOLD...)
      points.setTier(CustomerTier.calculateTier(points.getTotalSpending()));

      // 8. Lưu lại thông tin Ví điểm vào Database
      pointsRepository.save(points);

      // 9. Tạo một bản ghi Lịch sử giao dịch điểm (PointTransaction) để khách hàng có thể xem lại
      PointTransaction transaction =
          PointTransaction.builder()
              .user(user)
              .points(earnedPoints)
              .type(TransactionType.EARN) // Loại giao dịch là TÍCH LŨY
              .description("Earned points from online order")
              .orderId(orderId) // Lưu lại ID đơn hàng sinh ra số điểm này
              .build();

      // 10. Lưu Lịch sử giao dịch vào Database
      transactionRepository.save(transaction);
    }
  }

  /**
   * Cộng điểm thưởng thủ công cho khách hàng (ví dụ: khi họ đánh giá đơn hàng, hoặc được shop
   * tặng). - Khởi tạo ví điểm nếu khách hàng chưa có. - Cộng số điểm tương ứng vào ví. - Ghi lại
   * lịch sử giao dịch cộng điểm.
   *
   * @param userId ID của khách hàng
   * @param pointsAmount Số điểm cần cộng
   * @param description Lý do/Mô tả giao dịch cộng điểm
   * @param orderId ID đơn hàng liên quan (nếu có, có thể null)
   */
  @Override
  @Transactional
  public void addPoints(UUID userId, int pointsAmount, String description, UUID orderId) {
    if (pointsAmount <= 0) return;

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    UserLoyaltyPoints points =
        pointsRepository
            .findById(userId)
            .orElseGet(
                () -> {
                  UserLoyaltyPoints newPoints =
                      UserLoyaltyPoints.builder()
                          .user(user)
                          .userId(userId)
                          .totalPoints(0)
                          .availablePoints(0)
                          .build();
                  return pointsRepository.save(newPoints);
                });

    points.setTotalPoints(points.getTotalPoints() + pointsAmount);
    points.setAvailablePoints(points.getAvailablePoints() + pointsAmount);
    pointsRepository.save(points);

    PointTransaction transaction =
        PointTransaction.builder()
            .user(user)
            .points(pointsAmount)
            .type(TransactionType.EARN)
            .description(description)
            .orderId(orderId)
            .build();
    transactionRepository.save(transaction);
  }

  /**
   * Xử lý luồng đổi điểm lấy Voucher của khách hàng. - Kiểm tra tính hợp lệ của Voucher (đang hoạt
   * động, còn hạn, còn lượt sử dụng). - Kiểm tra xem khách hàng đã sở hữu voucher này (chưa sử
   * dụng, chưa hết hạn) chưa. - Kiểm tra điều kiện điểm hiện có và hạng thành viên (Tier) yêu cầu.
   * - Trừ điểm trong ví của khách hàng. - Cộng thêm 1 vào số lượt đã sử dụng của Voucher gốc
   * (usedCount). - Ghi lại lịch sử giao dịch trừ điểm. - Tạo và lưu UserVoucher (phiếu giảm giá của
   * riêng khách hàng).
   *
   * @param userId ID của khách hàng
   * @param voucherId ID của Voucher gốc muốn đổi
   * @return UserVoucherDto thông tin phiếu giảm giá đã thuộc về khách hàng
   */
  @Override
  @Transactional
  public UserVoucherDto redeemVoucher(UUID userId, UUID voucherId) {
    // 1. Kiểm tra tài khoản User có tồn tại không
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // 2. Lấy thông tin Voucher gốc từ hệ thống, báo lỗi nếu không tìm thấy
    Voucher voucher =
        voucherRepository
            .findById(voucherId)
            .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

    // 3. Kiểm tra xem Voucher có đang mở cho phép đổi (isActive) hay bị vô hiệu hóa
    if (!voucher.getIsActive()) {
      throw new IllegalArgumentException("Voucher is not active");
    }

    // 4. Kiểm tra thời gian: Voucher đã đến ngày bắt đầu chưa? Đã quá hạn chưa?
    LocalDateTime now = LocalDateTime.now();
    if (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) {
      throw new IllegalArgumentException("Voucher is not yet valid");
    }
    if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
      throw new IllegalArgumentException("Voucher is expired");
    }

    // 5. Kiểm tra giới hạn số lượng Voucher có thể phát hành (usageLimit)
    if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
      throw new IllegalArgumentException("Voucher usage limit reached");
    }

    // 6. Kiểm tra xem User này đã từng đổi Voucher này và ĐANG CÒN HẠN SỬ DỤNG hay không.
    // Tránh trường hợp 1 user ôm quá nhiều voucher giống nhau cùng lúc.
    boolean hasActiveVoucher =
        userVoucherRepository.findAllByUserIdAndVoucherId(userId, voucherId).stream()
            .anyMatch(
                uv ->
                    !uv.getIsUsed() // Chưa dùng
                        && (uv.getVoucher().getEndDate() == null
                            || !uv.getVoucher().getEndDate().isBefore(now))); // Chưa hết hạn
    if (hasActiveVoucher) {
      throw new IllegalArgumentException("Bạn đã đổi voucher này rồi");
    }

    // 7. Lấy thông tin Ví điểm của User
    UserLoyaltyPoints points =
        pointsRepository
            .findById(userId)
            .orElseGet(
                () -> {
                  UserLoyaltyPoints newPoints =
                      UserLoyaltyPoints.builder()
                          .user(user)
                          .userId(userId)
                          .totalPoints(0)
                          .availablePoints(0)
                          .totalSpending(BigDecimal.ZERO)
                          .tier(CustomerTier.MEMBER)
                          .build();
                  return newPoints;
                });

    // 8. Đối chiếu Số điểm hiện có của User với Số điểm yêu cầu của Voucher
    if (points.getAvailablePoints() < voucher.getPointsRequired()) {
      throw new IllegalArgumentException("Not enough points to redeem this voucher");
    }

    // 9. Kiểm tra Hạng thành viên (Tier). Một số voucher chỉ dành cho GOLD hoặc PLATINUM
    if (voucher.getRequiredTier() != null) {
      if (voucher.getRequiredTier() != CustomerTier.MEMBER
          && points.getTier() != voucher.getRequiredTier()) {
        throw new IllegalArgumentException(
            "Voucher is only available for " + voucher.getRequiredTier() + " tier");
      }
    }

    // 10. Đủ điều kiện -> Trừ điểm khả dụng của User và lưu lại vào DB
    points.setAvailablePoints(points.getAvailablePoints() - voucher.getPointsRequired());
    pointsRepository.save(points);

    // 11. Tăng số lượng đã đổi (usedCount) của Voucher gốc lên 1 và lưu DB
    voucher.setUsedCount(voucher.getUsedCount() + 1);
    voucherRepository.save(voucher);

    // 12. Ghi nhận giao dịch trừ điểm vào Lịch sử (PointTransaction)
    PointTransaction transaction =
        PointTransaction.builder()
            .user(user)
            .points(voucher.getPointsRequired())
            .type(TransactionType.REDEEM) // Loại giao dịch là ĐỔI THƯỞNG
            .description("Redeemed voucher " + voucher.getCode())
            .voucher(voucher)
            .build();
    transactionRepository.save(transaction);

    // 13. Tạo đối tượng UserVoucher (phiếu giảm giá cá nhân) và lưu trữ
    // Object này đại diện cho Voucher mà User đang sở hữu trong ví
    UserVoucher userVoucher = UserVoucher.builder().user(user).voucher(voucher).build();
    userVoucherRepository.save(userVoucher);

    // 14. Trả về thông tin voucher cá nhân vừa tạo
    return mapToUserVoucherDto(userVoucher);
  }

  /**
   * Lấy danh sách toàn bộ Voucher trong hệ thống. Dùng cho màn hình Quản lý Voucher của Admin/Quản
   * lý cửa hàng.
   *
   * @return Danh sách VoucherDto
   */
  @Override
  @Transactional(readOnly = true)
  public List<VoucherDto> getAllVouchers() {
    return voucherRepository.findAll().stream()
        .map(this::mapToVoucherDto)
        .collect(Collectors.toList());
  }

  /**
   * Tạo mới một Voucher. Chức năng dành cho Quản lý cửa hàng để tạo các chương trình khuyến mãi/đổi
   * điểm.
   *
   * @param req Dữ liệu đầu vào của Voucher (mã, loại giảm, số điểm cần, thời gian...)
   * @return VoucherDto thông tin Voucher sau khi tạo
   */
  @Override
  @Transactional
  public VoucherDto createVoucher(CreateVoucherReq req) {
    Voucher voucher =
        Voucher.builder()
            .code(req.getCode().toUpperCase())
            .description(req.getDescription())
            .discountType(req.getDiscountType())
            .discountValue(req.getDiscountValue())
            .minOrderAmount(
                req.getMinOrderAmount() != null ? req.getMinOrderAmount() : BigDecimal.ZERO)
            .maxDiscount(req.getMaxDiscount())
            .pointsRequired(req.getPointsRequired())
            .usageLimit(req.getUsageLimit())
            .startDate(req.getStartDate())
            .endDate(req.getEndDate())
            .isActive(req.getIsActive() != null ? req.getIsActive() : true)
            .requiredTier(req.getRequiredTier())
            .build();
    return mapToVoucherDto(voucherRepository.save(voucher));
  }

  /**
   * Cập nhật thông tin của một Voucher đã tồn tại. Cho phép Quản lý cửa hàng thay đổi điều kiện, số
   * điểm yêu cầu, thời hạn sử dụng.
   *
   * @param id ID của Voucher cần cập nhật
   * @param req Dữ liệu cập nhật mới
   * @return VoucherDto thông tin Voucher sau khi cập nhật
   */
  @Override
  @Transactional
  public VoucherDto updateVoucher(UUID id, CreateVoucherReq req) {
    Voucher voucher =
        voucherRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Voucher not found"));

    voucher.setCode(req.getCode().toUpperCase());
    voucher.setDescription(req.getDescription());
    voucher.setDiscountType(req.getDiscountType());
    voucher.setDiscountValue(req.getDiscountValue());
    if (req.getMinOrderAmount() != null) voucher.setMinOrderAmount(req.getMinOrderAmount());
    voucher.setMaxDiscount(req.getMaxDiscount());
    voucher.setPointsRequired(req.getPointsRequired());
    voucher.setUsageLimit(req.getUsageLimit());
    voucher.setStartDate(req.getStartDate());
    voucher.setEndDate(req.getEndDate());
    if (req.getIsActive() != null) voucher.setIsActive(req.getIsActive());
    voucher.setRequiredTier(req.getRequiredTier());

    return mapToVoucherDto(voucherRepository.save(voucher));
  }

  /**
   * Xóa một Voucher khỏi hệ thống.
   *
   * @param id ID của Voucher cần xóa
   */
  @Override
  @Transactional
  public void deleteVoucher(UUID id) {
    voucherRepository.deleteById(id);
  }

  private VoucherDto mapToVoucherDto(Voucher voucher) {
    if (voucher == null) return null;
    return VoucherDto.builder()
        .id(voucher.getId())
        .code(voucher.getCode())
        .description(voucher.getDescription())
        .discountType(voucher.getDiscountType())
        .discountValue(voucher.getDiscountValue())
        .minOrderAmount(voucher.getMinOrderAmount())
        .maxDiscount(voucher.getMaxDiscount())
        .pointsRequired(voucher.getPointsRequired())
        .usageLimit(voucher.getUsageLimit())
        .usedCount(voucher.getUsedCount())
        .startDate(voucher.getStartDate())
        .endDate(voucher.getEndDate())
        .isActive(voucher.getIsActive())
        .requiredTier(voucher.getRequiredTier())
        .createdAt(voucher.getCreatedAt())
        .build();
  }

  private TransactionDto mapToTransactionDto(PointTransaction t) {
    if (t == null) return null;
    return TransactionDto.builder()
        .id(t.getId())
        .points(t.getPoints())
        .type(t.getType())
        .description(t.getDescription())
        .orderId(t.getOrderId())
        .voucher(mapToVoucherDto(t.getVoucher()))
        .createdAt(t.getCreatedAt())
        .build();
  }

  private UserVoucherDto mapToUserVoucherDto(UserVoucher uv) {
    if (uv == null) return null;
    return UserVoucherDto.builder()
        .id(uv.getId())
        .voucher(mapToVoucherDto(uv.getVoucher()))
        .redeemedAt(uv.getRedeemedAt())
        .usedAt(uv.getUsedAt())
        .isUsed(uv.getIsUsed())
        .build();
  }
}
