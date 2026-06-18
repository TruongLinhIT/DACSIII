# HƯỚNG DẪN CÀI ĐẶT VÀ TÍCH HỢP FIREBASE (FCM)

Tài liệu này hướng dẫn bạn cách tích hợp Firebase Cloud Messaging (FCM) vào dự án Android và Backend từ A đến Z.

---

## 1. Cấu hình trên Firebase Console

### Bước 1: Tạo dự án Firebase
1. Truy cập [Firebase Console](https://console.firebase.google.com/).
2. Nhấn **Add project** (Tạo dự án mới).
3. Đặt tên cho dự án (ví dụ: `DeliveryPro`) và làm theo các bước hướng dẫn để hoàn tất.

### Bước 2: Thêm ứng dụng Android vào dự án Firebase
1. Ở màn hình tổng quan dự án, nhấn vào biểu tượng **Android** để thêm app Android.
2. Nhập **Android package name**: `com.example.dacsiii_v2` (phải trùng khớp chính xác với package name trong code Android của bạn).
3. Nhập tên ứng dụng (tùy chọn).
4. Nhập mã băm **SHA-1** của bạn (quan trọng đối với các chức năng xác thực qua OTP nếu có):
   - Để lấy mã SHA-1, mở terminal tại thư mục gốc dự án Android và chạy lệnh:
     ```bash
     ./gradlew signingReport
     ```
   - Copy mã SHA-1 của task `:app:signingReport` (ở build variant `debug`) và dán vào ô SHA-1 trên Firebase Console.
5. Nhấn **Register app**.

### Bước 3: Tải file cấu hình Android
1. Tải file `google-services.json`.
2. Di chuyển file này vào thư mục **`app/`** trong dự án Android của bạn:
   - Đường dẫn chính xác: `DACSIII-V2/app/google-services.json`.

---

## 2. Tạo Service Account Key cho Backend (Node.js)

Để backend Node.js có quyền gửi thông báo đẩy (Push Notification) đến các thiết bị, bạn cần tạo khóa tài khoản dịch vụ (Service Account Key).

### Bước 1: Tạo và tải file Service Account JSON
1. Trên Firebase Console, nhấn vào biểu tượng **Cài đặt (răng cưa)** ở góc trên bên trái bên cạnh "Project Overview" -> chọn **Project settings**.
2. Chọn tab **Service accounts**.
3. Tại mục **Firebase Admin SDK**, nhấn nút **Generate new private key** (Tạo khóa riêng tư mới).
4. Xác nhận tải file bằng cách nhấn **Generate key**. Một file `.json` chứa thông tin xác thực bảo mật cao sẽ được tải xuống máy tính của bạn.

### Bước 2: Cấu hình trên Backend
1. Đổi tên file vừa tải xuống thành tên dễ nhớ hoặc giữ nguyên (ví dụ: `firebase-adminsdk.json`).
2. Di chuyển file này vào thư mục **`backend/`** của bạn.
   - Ví dụ: `DACSIII-V2/backend/firebase-adminsdk.json`.
3. Mở file **`backend/.env`** của bạn và cấu hình đường dẫn đến file JSON này bằng biến môi trường `FIREBASE_SERVICE_ACCOUNT_PATH`.
   - Nếu bạn để file JSON ở thư mục gốc của backend:
     ```env
     FIREBASE_SERVICE_ACCOUNT_PATH=firebase-adminsdk.json
     ```
   - Bạn cũng có thể dùng đường dẫn tuyệt đối nếu cần:
     ```env
     FIREBASE_SERVICE_ACCOUNT_PATH=D:\Lap Trinh Di Dong\DACSIII-V2\backend\firebase-adminsdk.json
     ```

> [!CAUTION]
> **Bảo mật quan trọng**: Không bao giờ commit file Service Account JSON này lên GitHub public hoặc các kênh chia sẻ mã nguồn công khai, vì bất kỳ ai có file này đều có quyền can thiệp vào tài nguyên Firebase của bạn. Hãy đảm bảo file này đã được thêm vào `.gitignore` của backend.

---

## 3. Cách thức Hoạt động & Các Loại Thông Báo

Sau khi tích hợp thành công, hệ thống thông báo sẽ hoạt động tự động dựa trên các sự kiện trong dự án:

| Sự kiện | Trigger | Người nhận | Tiêu đề thông báo | Loại (`type`) |
| :--- | :--- | :--- | :--- | :--- |
| **Đơn hàng mới** | Tạo đơn hàng mới | Tài xế trong bán kính ≤ 3km | `Có đơn hàng mới gần bạn` | `new_order` |
| **Tài xế nhận đơn** | Tài xế ấn nhận đơn | Khách hàng đặt đơn | `Đơn hàng đã có tài xế` | `order_accepted` |
| **Giao hàng thành công** | Tài xế hoàn thành đơn | Khách hàng đặt đơn | `Đơn hàng đã hoàn thành` | `order_completed` |
| **Gửi hồ sơ định danh** | Người dùng gửi ảnh eKYC | Tất cả Admin | `Hồ sơ eKYC mới cần duyệt` | `identity_submitted` |

### Cơ chế tự động đồng bộ Token FCM:
1. Khi người dùng đăng nhập thành công vào ứng dụng Android, ứng dụng sẽ lấy token FCM hiện tại của thiết bị thông qua Firebase Messaging.
2. Ứng dụng Android tự động gửi API `POST /users/me/device-token` để lưu token này vào cơ sở dữ liệu MySQL của backend.
3. Nếu thiết bị thay đổi token FCM mới (sự kiện `onNewToken`), Service FCM trong ứng dụng Android sẽ tự động phát hiện và gửi request cập nhật lại token lên backend (nếu người dùng đang đăng nhập).
4. Khi backend gửi thông báo, nó sẽ lấy danh sách token FCM từ cơ sở dữ liệu và gửi đẩy qua Firebase Admin SDK (`sendEachForMulticast`).
