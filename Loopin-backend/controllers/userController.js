const db = require('../db');
const bcrypt = require('bcrypt');

// Kayıt işlemi
exports.register = async (req, res) => {
  const { fullName, username, email, password } = req.body;

  try {
    const hashedPassword = await bcrypt.hash(password, 10);
    const [result] = await db.execute(
      `INSERT INTO Users (fullName, username, email, passwordHash, joinDate)
       VALUES (?, ?, ?, ?, NOW())`,
      [fullName, username, email, hashedPassword]
    );

    res.status(201).json({ success: true, userId: result.insertId });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
};

// Giriş işlemi
exports.login = async (req, res) => {
  const { email, password } = req.body;

  try {
    const [rows] = await db.execute(
      `SELECT * FROM Users WHERE email = ?`,
      [email]
    );

    if (rows.length === 0) {
      return res.status(401).json({ success: false, message: 'Incorrect user or password' });
    }

    const user = rows[0];
    const match = await bcrypt.compare(password, user.passwordHash);

    if (!match) {
      return res.status(401).json({ success: false, message: 'Incorrect user or password' });
    }

    res.json({ success: true, user: { userId: user.userId, username: user.username } });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
};

// Kullanıcı adı zaten kayıtlı mı kontrol eder
exports.checkUsername = async (req, res) => {
  const { username } = req.body;
  try {
    const [rows] = await db.execute(
      'SELECT userId FROM Users WHERE username = ?',
      [username]
    );
    res.json({ exists: rows.length > 0 });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// E-posta zaten kayıtlı mı kontrol eder
exports.checkEmail = async (req, res) => {
  const { email } = req.body;
  try {
    const [rows] = await db.execute(
      'SELECT userId FROM Users WHERE email = ?',
      [email]
    );
    res.json({ exists: rows.length > 0 });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

// Kullanıcının tüm özelliklerini güncelleyebilen fonksiyon. Şifre hariç!!
exports.updateProfile = async (req, res) => {
  const { userId, fullName, username, email, phoneNumber, location, bio } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: "userId is required" });
  }

  try {
    if (email) {
      const [emailRows] = await db.execute(
        'SELECT userId FROM Users WHERE email = ? AND userId != ?',
        [email, userId]
      );
      if (emailRows.length > 0) {
        return res.status(409).json({ success: false, message: "Email already in use" });
      }
    }
    if (username) {
      const [usernameRows] = await db.execute(
        'SELECT userId FROM Users WHERE username = ? AND userId != ?',
        [username, userId]
      );
      if (usernameRows.length > 0) {
        return res.status(409).json({ success: false, message: "Username already in use" });
      }
    }

    const fields = [];
    const values = [];
    
    if (fullName !== undefined) {
      fields.push("fullName = ?");
      values.push(fullName);
    }
    if (username !== undefined) {
      fields.push("username = ?");
      values.push(username);
    }
    if (email !== undefined) {
      fields.push("email = ?");
      values.push(email);
    }
    if (phoneNumber !== undefined) {
      fields.push("phoneNumber = ?");
      values.push(phoneNumber);
    }
    if (location !== undefined) {
      fields.push("location = ?");
      values.push(location);
    }
    if (bio !== undefined) {
      fields.push("bio = ?");
      values.push(bio);
    }

    if (fields.length === 0) {
      return res.status(400).json({ success: false, message: "No fields provided to update" });
    }

    values.push(userId);
    const sql = `UPDATE Users SET ${fields.join(', ')} WHERE userId = ?`;

    const [result] = await db.execute(sql, values);
    res.json({ success: true, affectedRows: result.affectedRows });

  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
};

// Kullanıcı profil bilgilerini döndürür
exports.getUserProfile = async (req, res) => {
  const userId = req.params.id;

  try {
    const [rows] = await db.execute(
      `SELECT userId, fullName, username, email, phoneNumber, location, bio, profileImage
       FROM Users WHERE userId = ?`,
      [userId]
    );

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "User not found" });
    }

    res.json({ success: true, user: rows[0] });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
};

// Kullanıcı idsinden hesabı siler
exports.deleteAccount = async (req, res) => {
  const userId = req.params.id;

  try {
    const [result] = await db.execute(
      `DELETE FROM Users WHERE userId = ?`,
      [userId]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ success: false, message: "User not found" });
    }

    res.json({ success: true, message: "Account deleted" });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
};

// --- BU FONKSİYON LOGLAMA İÇİN GÜNCELLENDİ ---
// Şifre değiştirme fonksiyonu
exports.changePassword = async (req, res) => {
  const { userId, currentPassword, newPassword } = req.body;
  
  // 1. Adım: Gelen verileri kontrol edelim ve loglayalım
  console.log("Şifre değiştirme isteği geldi:", { userId, currentPassword: Boolean(currentPassword), newPassword: Boolean(newPassword) });

  if (!userId || !currentPassword || !newPassword) {
    return res.status(400).json({ success: false, message: "Gerekli alanlar eksik" });
  }

  try {
    // 2. Adım: Kullanıcıyı ve mevcut şifresini veritabanından alalım
    const [rows] = await db.execute('SELECT passwordHash FROM Users WHERE userId = ?', [userId]);

    if (rows.length === 0) {
      console.error(`Kullanıcı bulunamadı: userId=${userId}`);
      return res.status(404).json({ success: false, message: "Kullanıcı bulunamadı" });
    }

    // 3. Adım: Mevcut şifrenin doğruluğunu karşılaştıralım
    const user = rows[0];
    console.log("Veritabanındaki hash:", user.passwordHash);
    const isMatch = await bcrypt.compare(currentPassword, user.passwordHash);
    
    if (!isMatch) {
      console.warn(`Şifre eşleşmedi: userId=${userId}`);
      return res.status(401).json({ success: false, message: "Mevcut şifre yanlış" });
    }

    // 4. Adım: Yeni şifreyi hash'leyip veritabanını güncelleyelim
    const newHashed = await bcrypt.hash(newPassword, 10);
    console.log("Yeni hash oluşturuldu:", newHashed);
    
    await db.execute('UPDATE Users SET passwordHash = ? WHERE userId = ?', [newHashed, userId]);
    console.log(`Şifre başarıyla güncellendi: userId=${userId}`);

    // 5. Adım: Başarılı cevabı gönderelim
    res.json({ success: true, message: "Şifre başarıyla değiştirildi" });

  } catch (err) {
    // Eğer herhangi bir adımda hata olursa, bunu konsola yazdıralım
    console.error("Şifre değiştirme sırasında KRİTİK HATA:", err);
    res.status(500).json({ success: false, error: "Sunucuda bir hata oluştu." });
  }
};

