---
description: Quy tắc bắt buộc khi làm việc với Flyway migration và Database schema
---

# Flyway Migration Rules

Bất cứ khi nào có yêu cầu thay đổi cấu trúc Database (thêm cột, xóa cột, tạo bảng, sửa kiểu dữ liệu...), AI phải tuân thủ tuyệt đối các quy tắc sau:

1. **KHÔNG BAO GIỜ sửa trực tiếp cấu trúc DB**: Không được dùng lệnh SQL chọc thẳng vào DB để sửa cấu trúc mà không thông qua Flyway.
2. **KHÔNG BAO GIỜ sửa file Flyway cũ đã chạy thành công**: Nếu một file migration (ví dụ `V1__init.sql`) đã được chạy thành công trước đó (Spring Boot chạy không báo lỗi checksum), thì tuyệt đối không được sửa lại file đó.
3. **LUÔN LUÔN tạo file mới (Create New File)**: Khi có nhu cầu CRUD cấu trúc DB, bắt buộc phải tạo file Flyway mới (ví dụ `V19__add_new_column.sql`) và khai báo lệnh sửa trong file đó để Flyway tự động chạy.
4. **Chỉ sửa file cũ khi bị lỗi**: Nếu Spring Boot chạy và báo lỗi chính tại file đó (file đang phát triển, chưa được chạy thành công), thì mới được phép sửa trực tiếp file đó để fix lỗi.
