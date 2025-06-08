# Loopin - Sosyal Etkinlikler Düzenlemek için Bir Android Uygulaması / An Android App to Organize Social Events

## Proje Ekibi / Project Team
**Işık Yıldız**, **Hasan Yıldız**,**Umut Şıbara**

---
## Proje Açıklaması

Loopin, kullanıcıların etkinlikler düzenleyip katılabildiği, arkadaşlar ekleyip sohbet edebildiği ve gruplar oluşturabildiği kapsamlı bir sosyal mobil uygulamadır. Gerçek zamanlı etkileşimi ve topluluk odaklı deneyimi bir araya getirerek sosyal bağlantıları güçlendirmeyi hedefler.

Loopin, modern sosyal etkileşimin ihtiyaçlarına yönelik tasarlanmış, hem birebir hem de grup tabanlı iletişimi destekleyen bir platformdur. Kullanıcılar, ilgi alanlarına göre etkinlikler oluşturabilir, mevcut etkinliklere katılarak yeni insanlarla tanışabilir ve arkadaş çevrelerini genişletebilir. Uygulama, kullanıcıların birbirleriyle kolayca bağlantı kurmasını, anlık mesajlaşmasını ve özel veya açık gruplar halinde organize olmasını sağlar.

## Özellikler

-   **Kullanıcı Kimlik Doğrulama:** Güvenli kayıt ve giriş sistemi. (Backend'de BCrypt ile şifreleme)
-   **Etkinlik Yönetimi:** Kullanıcılar kendi etkinliklerini düzenleyebilir ve yönetebilir.
-   **Etkinlik Katılımı:** Mevcut etkinliklere göz atma ve katılım.
-   **Arkadaş Yönetimi:** Diğer kullanıcıları arkadaş olarak ekleme, arkadaşlık isteklerini yönetme.
-   **Birebir Sohbet:** Arkadaşlarla özel mesajlaşma.
-   **Grup Sohbetleri:** Birden fazla kişiyle grup halinde iletişim kurma.
-   **Grup Üyeliği ve Yönetimi:**
    -   Grup üyelerini görüntüleme.
    -   Grup oluşturucularının grubu silme yeteneği.
    -   Admin rolündeki üyelerin arkadaş davet etme, üye çıkarma ve rol (admin/üye) atama yetenekleri.
-   **Gerçek Zamanlı İletişim:** Anlık mesajlaşma deneyimi.
-   **Bildirimler:** Etkinlik, arkadaşlık isteği ve mesaj bildirimleri.
-   **Kullanıcı Profilleri:** Özelleştirilebilir kullanıcı profilleri.
-   **Harita Entegrasyonu:** Etkinlik konumlarını görüntülemek için Google Haritalar kullanımı.

## Kullanılan Teknolojiler

### Frontend (Android Uygulaması)
-   **Kotlin:** Modern, güvenli ve özlü Android uygulama geliştirme dili.
-   **Android Jetpack:** Uygulama geliştirmeyi hızlandıran ve kolaylaştıran kütüphaneler.
    -   **ViewModel & LiveData:** Veri yaşam döngüsü yönetimi ve UI güncellemeleri için.
    -   **Navigation Components:** Uygulama içi navigasyonu yönetmek için.
-   **MVVM Mimarisi:** Model-View-ViewModel tasarım deseni ile temiz, test edilebilir ve sürdürülebilir kod yapısı.
-   **Dagger Hilt (v2.51.1):** Bağımlılık Enjeksiyonu (DI) için standartlaştırılmış ve güçlü bir çözüm.
-   **Retrofit (v2.9.0) & OkHttp:** RESTful API çağrıları için güçlü ve esnek HTTP istemcileri.
    -   `okhttp-logging-interceptor`: HTTP isteklerini loglamak için.
    -   `converter-gson`: JSON verilerini Kotlin/Java objelerine dönüştürmek için.
    -   `converter-moshi`: JSON verilerini Moshi ile dönüştürmek için (yedek veya alternatif).
-   **Kotlin Coroutines (v1.6.4):** Asenkron programlama ve arka plan işlemleri için.
-   **Glide (v4.15.1):** Etkin ve hızlı görsel yükleme ve önbellekleme için.
-   **Material Design (v1.9.0) & CardView (v1.0.0):** Modern ve tutarlı kullanıcı arayüzü bileşenleri.
-   **CalendarView:** Takvim özellikleri için özel kütüphane.
-   **Google Play Services Location & Maps SDK:** Konum tabanlı hizmetler ve harita görüntüleme için.

### Backend (API)
-   **Node.js:** Yüksek performanslı ve ölçeklenebilir sunucu tarafı uygulamaları için.
-   **Express.js:** Node.js için minimalist ve esnek web uygulama framework'ü.
-   **MySQL:** İlişkisel veritabanı yönetim sistemi.
-   **BCrypt:** Güvenli parola hash'leme için.

## Kurulum ve Çalıştırma

### 1. Ön Koşullar

-   **Node.js:** v14 veya üstü (Backend için)
-   **npm (Node Package Manager):** Node.js ile birlikte gelir
-   **MySQL:** Veritabanı sunucusu
-   **Java Development Kit (JDK):** JDK 11 veya üstü (Android Studio ve Gradle için)
-   **Android Studio:** Jellyfish veya en güncel sürüm
-   **Git:** Depoyu klonlamak için

### 2. Backend Kurulumu

1.  Depoyu klonlayın:
    ```bash
    git clone [https://github.com/IsikYildiz/Loopin.git](https://github.com/IsikYildiz/Loopin.git)
    cd Loopin/backend # Backend klasörünüzün yoluna göre düzeltin
    ```
2.  Bağımlılıkları yükleyin:
    ```bash
    npm install
    ```
3.  Veritabanı yapılandırması:
    -   Bir MySQL veritabanı oluşturun (örneğin `loopin_db`).
    -   `backend` dizininde bir `.env` dosyası oluşturun ve aşağıdaki değişkenleri doldurun. (Örnek: `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `JWT_SECRET` vb. Bunlar `.env.example` dosyanızda belirtilmiş olmalıdır.)
    ```
    DB_HOST=localhost
    DB_USER=root
    DB_PASSWORD=your_mysql_password
    DB_NAME=loopin_db
    ```
4.  Veritabanı tablolarını oluşturun ve başlangıç verilerini yükleyin (genellikle SQL scriptleri veya migrasyon araçları kullanılır):
    ```bash
    # Örnek SQL komutu (veritabanı schema'nızın olduğu varsayılarak):
    # mysql -u your_db_user -p your_db_name < path/to/your/schema.sql
    ```
5.  Backend sunucusunu başlatın:
    ```bash
    npm start
    ```
    API sunucusu genellikle `http://localhost:3000` adresinde çalışacaktır.

### 3. Android Uygulama Kurulumu

1.  Android Studio'yu açın.
2.  `File -> Open` seçeneğini kullanarak klonladığınız `Loopin` projesinin kök dizinini seçin. Android Studio otomatik olarak doğru modülü (`app`) tanıyacaktır.
3.  Gradle senkronizasyonunun tamamlanmasını bekleyin.
4.  Projenizin kök dizininde (`Loopin` klasörü, yani `build.gradle.kts` (Project-level) dosyasının olduğu yer) bir `local.properties` dosyası oluşturun.
5.  Bu dosyaya Google Maps API anahtarınızı ekleyin:
    ```properties
    Maps_API_KEY=YOUR_Maps_API_KEY
    ```
    (Google Maps API anahtarınızı [Google Cloud Console](https://console.cloud.google.com/)'dan almanız gerekmektedir.)
6.  `network/ApiClient.kt` dosyasını açın ve `BASE_URL`'i backend sunucunuzun doğru IP adresine veya emülatör için `10.0.2.2`'ye göre güncelleyin:
    ```kotlin
    // network/ApiClient.kt
    private const val BASE_URL = "[http://10.0.2.2:3000/api/](http://10.0.2.2:3000/api/)" // Android Emülatör için localhost
    // veya fiziksel cihazda test ediyorsanız (bilgisayarınızın yerel IP adresi):
    // private const val BASE_URL = "http://YOUR_LOCAL_IP_ADDRESS:3000/api/"
    ```
7.  Bir Android Emülatörü veya fiziksel cihaz bağlayın.
8.  Uygulamayı çalıştırın (`Run` butonu veya Shift+F10).
---

## Project Description

Loopin is a comprehensive social mobile application that allows users to organize and participate in events, add friends, chat, and create groups. It aims to strengthen social connections by bringing together real-time interaction and a community-focused experience.

Loopin is a platform designed to meet the needs of modern social interaction, supporting both one-on-one and group-based communication. Users can create events based on their interests, participate in existing events to meet new people, and expand their social circles. The application enables users to easily connect with each other, send instant messages, and organize into private or public groups.

## Features

-   **User Authentication:** Secure registration and login system. (Password hashing with BCrypt on the backend).
-   **Event Management:** Users can organize and manage their own events.
-   **Event Participation:** Browse and join existing events.
-   **Friend Management:** Add other users as friends and manage friend requests.
-   **One-on-One Chat:** Private messaging with friends.
-   **Group Chats:** Communicate with multiple people in groups.
-   **Group Membership and Management:**
    -   View group members.
    -   Ability for group creators to delete the group.
    -   Ability for "admin" role members to invite friends, remove members, and assign roles (admin/member).
-   **Real-time Communication:** Instant messaging experience.
-   **Notifications:** Event, friend request, and message notifications.
-   **User Profiles:** Customizable user profiles.
-   **Map Integration:** Uses Google Maps to display event locations.

## Technologies Used

### Frontend (Android Application)
-   **Kotlin:** Modern, safe, and concise language for Android app development.
-   **Android Jetpack:** Libraries that accelerate and simplify app development.
    -   **ViewModel & LiveData:** For data lifecycle management and UI updates.
    -   **Navigation Components:** To manage in-app navigation.
-   **MVVM Architecture:** For clean, testable, and maintainable code structure using the Model-View-ViewModel design pattern.
-   **Dagger Hilt (v2.51.1):** A standardized and powerful solution for Dependency Injection (DI).
-   **Retrofit (v2.9.0) & OkHttp:** Robust and flexible HTTP clients for RESTful API calls.
    -   `okhttp-logging-interceptor`: For logging HTTP requests.
    -   `converter-gson`: For converting JSON data to Kotlin/Java objects.
    -   `converter-moshi`: For converting JSON data using Moshi (fallback or alternative).
-   **Kotlin Coroutines (v1.6.4):** For asynchronous programming and background operations.
-   **Glide (v4.15.1):** For efficient and fast image loading and caching.
-   **Material Design (v1.9.0) & CardView (v1.0.0):** For modern and consistent user interface components.
-   **CalendarView:** A specific library for calendar features.
-   **Google Play Services Location & Maps SDK:** For location-based services and map display.

### Backend (API)
-   **Node.js:** For high-performance and scalable server-side applications.
-   **Express.js:** A minimalist and flexible web application framework for Node.js.
-   **MySQL:** Relational database management system.
-   **BCrypt:** For secure password hashing.

## Setup and Running

### 1. Prerequisites

-   **Node.js:** v14 or higher (for Backend).
-   **npm (Node Package Manager):** Comes with Node.js.
-   **MySQL:** Database server.
-   **Java Development Kit (JDK):** JDK 11 or higher (for Android Studio and Gradle).
-   **Android Studio:** Jellyfish or the latest version.
-   **Git:** To clone the repository.

### 2. Backend Setup

1.  Clone the repository:
    ```bash
    git clone [https://github.com/IsikYildiz/Loopin.git](https://github.com/IsikYildiz/Loopin.git)
    cd Loopin/backend # Adjust according to your backend folder path
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Database configuration:
    -   Create a MySQL database (e.g., `loopin_db`).
    -   Create a `.env` file in the `backend` directory and populate it with the following variables. (Example: `DB_HOST`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `JWT_SECRET`, etc. These should be specified in your `.env.example` file).
    ```
    DB_HOST=localhost
    DB_USER=root
    DB_PASSWORD=your_mysql_password
    DB_NAME=loopin_db
    JWT_SECRET=your_secret_key_for_jwt_tokens
    # Your other backend environment variables (e.g., API port)
    ```
4.  Create database tables and load initial data (typically using SQL scripts or migration tools):
    ```bash
    # Example SQL command (assuming you have a database schema):
    # mysql -u your_db_user -p your_db_name < path/to/your/schema.sql
    ```
5.  Start the backend server:
    ```bash
    npm start
    ```
    The API server will typically run at `http://localhost:3000`.

### 3. Android Application Setup

1.  Open Android Studio.
2.  Select `File -> Open` and choose the root directory of your cloned `Loopin` project. Android Studio will automatically detect the correct module (`app`).
3.  Wait for Gradle synchronization to complete.
4.  Create a `local.properties` file in your project's root directory (i.e., where the `build.gradle.kts` (Project-level) file is located).
5.  Add your Google Maps API key to this file:
    ```properties
    Maps_API_KEY=YOUR_Maps_API_KEY
    ```
    (You need to obtain your Google Maps API key from the [Google Cloud Console](https://console.cloud.google.com/)).
6.  Open `network/ApiClient.kt` and update the `BASE_URL` to your backend server's correct IP address or `10.0.2.2` for the emulator:
    ```kotlin
    // network/ApiClient.kt
    private const val BASE_URL = "[http://10.0.2.2:3000/api/](http://10.0.2.2:3000/api/)" // For Android Emulator (localhost)
    // or if testing on a physical device (your computer's local IP address):
    // private const val BASE_URL = "http://YOUR_LOCAL_IP_ADDRESS:3000/api/"
    ```
7.  Connect an Android Emulator or a physical device.
8.  Run the application (`Run` button or Shift+F10).
---
