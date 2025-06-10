const pool = require('../db');
const notificationController = require('./notificationController');

// Arkadaşlık isteği gönderme
exports.sendFriendRequest = async (req, res) => {
  const { senderId, receiverId } = req.body;

  if (!senderId || !receiverId) {
    return res.status(400).json({ success: false, message: 'Sender ID and receiver ID are required' });
  }

  if (senderId === receiverId) {
    return res.status(400).json({ success: false, message: 'Cannot send friend request to yourself' });
  }

  try {
    // Arkadaşlığın zaten var olup olmadığını kontrol et
    const [existingFriendship] = await pool.query(
      `SELECT * FROM friendships 
       WHERE (senderId = ? AND receiverId = ?) OR (senderId = ? AND receiverId = ?)`,
      [senderId, receiverId, receiverId, senderId]
    );

    if (existingFriendship.length > 0) {
      const status = existingFriendship[0].status;
      if (status === 'pending') {
        return res.status(400).json({ 
          success: false, 
          message: existingFriendship[0].senderId === parseInt(senderId) ? 
            'Friend request already sent' : 
            'You have a pending friend request from this user'
        });
      } else if (status === 'accepted') {
        return res.status(400).json({ success: false, message: 'Already friends' });
      } else if (status === 'rejected') {
        // Reddedilmiş bir istek varsa, durumu pending'e çevir
        await pool.query(
          `UPDATE friendships SET status = 'pending', senderId = ?, receiverId = ? 
           WHERE (senderId = ? AND receiverId = ?) OR (senderId = ? AND receiverId = ?)`,
          [senderId, receiverId, senderId, receiverId, receiverId, senderId]
        );
        
        // Bildirim gönder
        try {
          await notificationController.createNotification(
            receiverId,
            'friend_request',
            { senderId }
          );
        } catch (error) {
          console.error('Bildirim oluşturulurken hata:', error);
        }

        return res.json({ success: true, message: 'Friend request sent successfully' });
      }
    }

    // Yeni arkadaşlık isteği oluştur
    await pool.query(
      `INSERT INTO friendships (senderId, receiverId, status) VALUES (?, ?, 'pending')`,
      [senderId, receiverId]
    );

    // Bildirim gönder
    try {
      await notificationController.createNotification(
        receiverId,
        'friend_request',
        { senderId }
      );
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.status(201).json({ success: true, message: 'Friend request sent successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};


// Arkadaşlık isteğini kabul etme
exports.acceptFriendRequest = async (req, res) => {
  const { senderId, receiverId } = req.body;

  if (!senderId || !receiverId) {
    return res.status(400).json({ success: false, message: 'Sender ID and receiver ID are required' });
  }

  try {
    // Arkadaşlık isteğini bul
    const [friendship] = await pool.query(
      `SELECT * FROM friendships 
       WHERE senderId = ? AND receiverId = ? AND status = 'pending'`,
      [senderId, receiverId]
    );

    if (friendship.length === 0) {
      return res.status(404).json({ success: false, message: 'Friend request not found' });
    }

    // İsteği kabul et
    await pool.query(
      `UPDATE friendships SET status = 'accepted' 
       WHERE senderId = ? AND receiverId = ?`,
      [senderId, receiverId]
    );

    // Bildirim gönder (isteği gönderen kişiye)
    try {
      await notificationController.createNotification(
        senderId,             // Bildirim alacak kişi
        'friend_request_accepted', // Bildirim tipi
        { receiverId }        // Bildirim içeriği (kabul eden kişi)
      );
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.json({ success: true, message: 'Friend request accepted' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Arkadaşlık isteğini reddetme
exports.rejectFriendRequest = async (req, res) => {
  const { senderId, receiverId } = req.body;

  if (!senderId || !receiverId) {
    return res.status(400).json({ success: false, message: 'Sender ID and receiver ID are required' });
  }

  try {
    // Arkadaşlık isteğini bul
    const [friendship] = await pool.query(
      `SELECT * FROM friendships 
       WHERE senderId = ? AND receiverId = ? AND status = 'pending'`,
      [senderId, receiverId]
    );

    if (friendship.length === 0) {
      return res.status(404).json({ success: false, message: 'Friend request not found' });
    }

    // İsteği reddet
    await pool.query(
      `UPDATE friendships SET status = 'rejected' 
       WHERE senderId = ? AND receiverId = ?`,
      [senderId, receiverId]
    );

    // Bildirim gönder (isteği gönderen kişiye)
    try {
      await notificationController.createNotification(
        senderId,               // Bildirim alacak kişi
        'friend_request_rejected', // Bildirim tipi
        { receiverId }          // Bildirim içeriği (reddeden kişi)
      );
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.json({ success: true, message: 'Friend request rejected' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Arkadaşlığı sonlandırma
exports.removeFriend = async (req, res) => {
  const userId = req.params.userId;
  const friendId = req.params.friendId;

  if (!userId || !friendId) {
    return res.status(400).json({ success: false, message: 'User ID and friend ID are required' });
  }

  try {
    // Arkadaşlık ilişkisini bul
    const [friendship] = await pool.query(
      `SELECT * FROM friendships 
       WHERE ((senderId = ? AND receiverId = ?) OR (senderId = ? AND receiverId = ?)) 
       AND status = 'accepted'`,
      [userId, friendId, friendId, userId]
    );

    if (friendship.length === 0) {
      return res.status(404).json({ success: false, message: 'Friendship not found' });
    }

    // Arkadaşlığı sil
    await pool.query(
      `DELETE FROM friendships 
       WHERE ((senderId = ? AND receiverId = ?) OR (senderId = ? AND receiverId = ?))`,
      [userId, friendId, friendId, userId]
    );

    res.json({ success: true, message: 'Friendship removed successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Kullanıcının arkadaş listesini getirme
exports.getUserFriends = async (req, res) => {
  const userId = req.params.userId;
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  try {
    // Arkadaşları getir
    const [friends] = await pool.query(
      `SELECT u.userId, u.fullName, u.userName, u.profileImage, 
              CASE 
                WHEN f.senderId = ? THEN 'outgoing'
                ELSE 'incoming'
              END as friendshipDirection
       FROM friendships f
       JOIN users u ON (f.senderId = u.userId OR f.receiverId = u.userId) AND u.userId != ?
       WHERE (f.senderId = ? OR f.receiverId = ?) AND f.status = 'accepted'
       ORDER BY u.fullName ASC
       LIMIT ? OFFSET ?`,
      [userId, userId, userId, userId, limit, offset]
    );

    // Toplam arkadaş sayısını getir
    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) as total 
       FROM friendships 
       WHERE (senderId = ? OR receiverId = ?) AND status = 'accepted'`,
      [userId, userId]
    );

    res.json({
      success: true,
      friends,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Bekleyen arkadaşlık isteklerini getirme
exports.getPendingFriendRequests = async (req, res) => {
  const userId = req.params.userId;
  const type = req.query.type || 'incoming'; // 'incoming' veya 'outgoing'
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  try {
    let query, countQuery, params, countParams;

    if (type === 'incoming') {
      // Gelen istekler
      query = `SELECT u.userId, u.fullName, u.userName, u.profileImage, f.senderId, f.receiverId
               FROM friendships f
               JOIN users u ON f.senderId = u.userId
               WHERE f.receiverId = ? AND f.status = 'pending'
               ORDER BY f.senderId ASC
               LIMIT ? OFFSET ?`;
      countQuery = `SELECT COUNT(*) as total FROM friendships WHERE receiverId = ? AND status = 'pending'`;
      params = [userId, limit, offset];
      countParams = [userId];
    } else {
      // Giden istekler
      query = `SELECT u.userId, u.fullName, u.userName, u.profileImage, f.senderId, f.receiverId
               FROM friendships f
               JOIN users u ON f.receiverId = u.userId
               WHERE f.senderId = ? AND f.status = 'pending'
               ORDER BY f.receiverId ASC
               LIMIT ? OFFSET ?`;
      countQuery = `SELECT COUNT(*) as total FROM friendships WHERE senderId = ? AND status = 'pending'`;
      params = [userId, limit, offset];
      countParams = [userId];
    }

    const [requests] = await pool.query(query, params);
    const [[{ total }]] = await pool.query(countQuery, countParams);

    res.json({
      success: true,
      requests,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Arkadaş arama
exports.searchFriends = async (req, res) => {
  const userId = req.params.userId;
  const { query } = req.query;
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  if (!query) {
    return res.status(400).json({ success: false, message: 'Search query is required' });
  }

  try {
    // Arkadaşlarda arama yap
    const [friends] = await pool.query(
      `SELECT u.userId, u.fullName, u.userName, u.profileImage
       FROM friendships f
       JOIN users u ON (f.senderId = u.userId OR f.receiverId = u.userId) AND u.userId != ?
       WHERE (f.senderId = ? OR f.receiverId = ?) AND f.status = 'accepted'
       AND (u.fullName LIKE ? OR u.userName LIKE ?)
       ORDER BY u.fullName ASC
       LIMIT ? OFFSET ?`,
      [userId, userId, userId, `%${query}%`, `%${query}%`, limit, offset]
    );

    // Toplam sonuç sayısını getir
    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) as total
       FROM friendships f
       JOIN users u ON (f.senderId = u.userId OR f.receiverId = u.userId) AND u.userId != ?
       WHERE (f.senderId = ? OR f.receiverId = ?) AND f.status = 'accepted'
       AND (u.fullName LIKE ? OR u.userName LIKE ?)`,
      [userId, userId, userId, `%${query}%`, `%${query}%`]
    );

    res.json({
      success: true,
      friends,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Arkadaş önerileri getirme (ortak arkadaşlara göre)
exports.getFriendSuggestions = async (req, res) => {
  const userId = req.params.userId;
  const limit = parseInt(req.query.limit) || 10;

  try {
    // Ortak arkadaşlara sahip olan kullanıcıları öner
    const [suggestions] = await pool.query(
      `SELECT u.userId, u.fullName, u.userName, u.profileImage, 
              COUNT(*) as mutualFriends
       FROM users u
       JOIN friendships f1 ON (f1.senderId = u.userId OR f1.receiverId = u.userId) AND 
                             (f1.senderId != ? AND f1.receiverId != ?)
       JOIN friendships f2 ON ((f2.senderId = ? AND f2.receiverId = f1.senderId) OR 
                              (f2.senderId = ? AND f2.receiverId = f1.receiverId) OR
                              (f2.senderId = f1.senderId AND f2.receiverId = ?) OR
                              (f2.senderId = f1.receiverId AND f2.receiverId = ?))
       WHERE u.userId != ? 
       AND NOT EXISTS (
         SELECT 1 FROM friendships f 
         WHERE (f.senderId = ? AND f.receiverId = u.userId) OR 
               (f.senderId = u.userId AND f.receiverId = ?)
       )
       AND f1.status = 'accepted' AND f2.status = 'accepted'
       GROUP BY u.userId
       ORDER BY mutualFriends DESC
       LIMIT ?`,
      [userId, userId, userId, userId, userId, userId, userId, userId, userId, limit]
    );

    res.json({ success: true, suggestions });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};