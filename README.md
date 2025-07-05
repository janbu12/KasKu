## KasKu: Manajemen Keuangan Personal Adaptif dengan Integrasi AI Multi-modal ✨💸🤖
**KasKu** merevolusi pencatatan keuangan pribadi harian Anda dengan memanfaatkan kekuatan Kecerdasan Buatan (AI) Gemini secara terintegrasi. Aplikasi ini dirancang sebagai solusi cerdas yang memungkinkan Anda mengelola pengeluaran dan pemasukan dengan efisiensi dan akurasi yang belum pernah ada sebelumnya.

Fitur Utama & Keunggulan Inovatif:
- 🧾 **Pemrosesan Struk Pintar dengan AI Gemini:**

  Fitur inti KasKu adalah kemampuannya untuk memproses struk belanja langsung dari foto. Berbeda dengan pendekatan tradisional, KasKu menggunakan AI Gemini multi-modal untuk secara langsung menganalisis gambar struk, mengekstrak teks relevan (OCR), dan sekaligus mengubahnya menjadi data transaksi yang terstruktur dan mudah diatur. Proses terpadu ini menyederhanakan pencatatan pengeluaran, menjadikan setiap detail, mulai dari nama item hingga total pembayaran, terekam secara otomatis dan akurat.

- 📊 **Analisis Keuangan Cerdas & Rekomendasi Adaptif:**

  Data transaksi yang terekam kemudian menjadi dasar bagi AI Gemini untuk memberikan wawasan keuangan yang mendalam. KasKu tidak hanya mencatat; ia menganalisis pola pengeluaran Anda dari waktu ke waktu. Jika teridentifikasi tren yang kurang efisien, misalnya dominasi pengeluaran pada kategori tertentu (seperti makanan yang signifikan dari total pemasukan), AI akan proaktif memberikan rekomendasi personal dan saran adaptif. Ini membantu Anda membuat keputusan finansial yang lebih baik dan mengoptimalkan anggaran secara cerdas.

- 📈 **Dashboard Keuangan Interaktif & Real-time:**

  Aplikasi ini dilengkapi dengan dashboard keuangan yang intuitif, menyajikan gambaran menyeluruh tentang finance balance Anda. Anda dapat dengan mudah membandingkan total pengeluaran dengan pemasukan, memungkinkan pemantauan kesehatan finansial secara real-time dan membantu Anda tetap di jalur tujuan keuangan.

- 🔒 **Sistem Keamanan & Fleksibilitas:**
  
  Untuk mendukung penggunaan personal yang aman dan memungkinkan akses multi-pengguna dan multi device, KasKu dilengkapi dengan sistem login yang terenkripsi, memastikan data keuangan Anda tetap pribadi dan terlindungi. Dengan arsitektur yang memisahkan backend dan frontend serta sistem API yang kuat, KasKu dirancang sebagai platform yang stabil, responsif, dan siap untuk skalabilitas serta pengembangan fitur di masa depan.

KasKu bukan sekadar aplikasi pencatat keuangan. ini adalah asisten finansial cerdas Anda yang didukung AI, yang membantu Anda mencapai stabilitas dan tujuan keuangan dengan cara yang lebih mudah dan cerdas. 🚀

---

## ⚙️ Cara Instalasi Backend

1. **Clone repository ini**
   ```bash
   git clone <repo-url>
   cd backend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Buat file konfigurasi environment**
   - Salin file `.env.example` (jika ada) menjadi `.env` dan sesuaikan isinya, atau buat `.env` baru sesuai kebutuhan:
     ```
     PORT=1234
     GEMINI_API_KEY=...
     FIREBASE_SERVICE_ACCOUNT_PATH=../serviceAccountKey.json
     FIREBASE_API_KEY=...
     FIREBASE_AUTH_DOMAIN=...
     FIREBASE_PROJECT_ID=...
     FIREBASE_STORAGE_BUCKET=...
     FIREBASE_MESSAGING_SENDER_ID=...
     FIREBASE_APP_ID=...
     FIREBASE_MEASUREMENT_ID=...
     ```

4. **Letakkan file service account Firebase**
   - Simpan file `serviceAccountKey.json` di lokasi yang sesuai dengan variabel `FIREBASE_SERVICE_ACCOUNT_PATH` pada `.env`.

5. **Jalankan server backend**
   ```bash
   npm start
   ```
   Server akan berjalan di port yang ditentukan pada `.env` (default: 1234).

---

## 📚 API Documentation

### 🔑 Authentication

#### 📝 Register
- **POST** `/auth/register`
- **Request Body:**
  ```json
  {
    "username": "string",
    "email": "string",
    "password": "string"
  }
  ```
- **Response:**
  - **201 Created**
    ```json
    {
      "message": "User registered successfully!",
      "userId": "string",
      "email": "string",
      "username": "string"
    }
    ```
  - **Error:** 400/409/500

#### 🔓 Login
- **POST** `/auth/login`
- **Request Body:**
  ```json
  {
    "email": "string",
    "password": "string"
  }
  ```
- **Response:**
  - **200 OK**
    ```json
    {
      "message": "Login successful!",
      "uid": "string",
      "email": "string",
      "token": "string"
    }
    ```
  - **Error:** 400/401/500

#### 🚪 Logout
- **POST** `/auth/logout`
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
  - **200 OK**
    ```json
    { "message": "Logout successful!" }
    ```
  - **Error:** 401/500

---

### 👤 User

#### 👁️ Get Profile User
- **GET** `/api/users/:uid`
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
  - **200 OK**
    ```json
    {
      "uid": "string",
      "username": "string",
      "email": "string",
      "createdAt": "date",
      "profileCompleted": true,
      "userProfile": { ... }
    }
    ```
  - **Error:** 400/403/404/500

#### ✏️ Update Profile User
- **PUT** `/api/users/:uid/profile`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "occupation": "string",
    "income": 0,
    "financialGoals": "string",
    "currency": "string",
    // ...additional fields
  }
  ```
- **Response:**
  - **200 OK**
    ```json
    {
      "message": "User profile updated successfully!",
      "updatedProfile": { ... }
    }
    ```
  - **Error:** 400/403/404/500

---

### 🧾 Receipt (Struct)

#### 📂 Get My Receipts
- **GET** `/api/structs`
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
  - **200 OK**
    ```json
    [
      {
        "id": "string",
        "merchant_name": "string",
        "transaction_date": "YYYY-MM-DD",
        "transaction_time": "HH:MM",
        "items": [
          {
            "item_name": "string",
            "quantity": 0,
            "unit_price": 0,
            "total_price_item": 0
          }
        ],
        "subtotal": 0,
        "discount_amount": 0,
        "additional_charges": 0,
        "tax_amount": 0,
        "final_total": 0,
        "tender_type": "string",
        "amount_paid": 0,
        "change_given": 0
      }
    ]
    ```
  - **Error:** 404/500

#### ➕ Add Receipt
- **POST** `/api/struct/add`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "receipt": { ... }
  }
  ```
- **Response:**
  - **200 OK**
    ```json
    {
      "message": "Receipt added successfully.",
      "id": "string"
    }
    ```
  - **Error:** 400/404/500

#### 📝 Edit Receipt
- **PUT** `/api/struct/edit/:id`
- **Headers:** `Authorization: Bearer <token>`
- **Request Body:**
  ```json
  {
    "receipt": { ... }
  }
  ```
- **Response:**
  - **200 OK**
    ```json
    { "message": "Receipt updated successfully." }
    ```
  - **Error:** 400/404/500

#### 🗑️ Delete Receipt
- **DELETE** `/api/struct/delete/:id`
- **Headers:** `Authorization: Bearer <token>`
- **Response:**
  - **200 OK**
    ```json
    { "message": "Receipt deleted successfully." }
    ```
  - **Error:** 400/404/500

---

### 🤖 Upload Receipt Image (AI Extraction)

#### 📤 Upload Struk Image
- **POST** `/api/upload/struct`
- **Headers:** `Authorization: Bearer <token>`
- **Form Data:** `struk_image` (file, required)
- **Response:**
  - **200 OK**
    ```json
    {
      "message": "Image processed successfully and data extracted!",
      "fileName": "string",
      "structuredData": { ... },
      "rawGeminiText": "string"
    }
    ```
  - **Error:** 400/500

---

**ℹ️ Note:**  
All endpoints requiring authentication must include the header:  
`Authorization: Bearer <Firebase ID Token>`
