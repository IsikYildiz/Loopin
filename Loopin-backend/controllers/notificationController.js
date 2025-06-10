const pool = require('../db');

// Dışarıdan kullanılabilir, req, res ile değil direkt parametre ile çağrılır
exports.createNotification = async (userId, type, content) => {
  // Validasyonlar
  const validTypes = ['friend_request','friend_request_accepted','friend_request_rejected', 'event_add','event_remove', 'message', 'reminder', 'system'];
  if (!userId || !type || !content) throw new Error('Eksik parametre');
  if (!validTypes.includes(type)) throw new Error('Geçersiz bildirim tipi');

  // Kullanıcı var mı kontrol et
  const [user] = await pool.query(`SELECT userId FROM users WHERE userId = ?`, [userId]);
  if (user.length === 0) throw new Error('User not found');

  // Bildirimi oluştur
  await pool.query(
    `INSERT INTO notifications (userId, type, content, isRead, sentAt) VALUES (?, ?, ?, 0, NOW())`,
    [userId, type, JSON.stringify(content)]
  );
};

// Kullanıcının bildirimlerini listeleme
exports.getUserNotifications = async (req, res) => {
  const userId = req.params.userId;
  const { isRead, type } = req.query;
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  try {
    // Kullanıcının var olup olmadığını kontrol et
    const [user] = await pool.query(`SELECT userId FROM users WHERE userId = ?`, [userId]);
    if (user.length === 0) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    let query = `SELECT * FROM notifications WHERE userId = ?`;
    let countQuery = `SELECT COUNT(*) as total FROM notifications WHERE userId = ?`;
    const params = [userId];
    const countParams = [userId];

    // Filtreleme
    if (isRead !== undefined) {
      query += ` AND isRead = ?`;
      countQuery += ` AND isRead = ?`;
      params.push(isRead === 'true' ? 1 : 0);
      countParams.push(isRead === 'true' ? 1 : 0);
    }
    if (type) {
      query += ` AND type = ?`;
      countQuery += ` AND type = ?`;
      params.push(type);
      countParams.push(type);
    }

    // Sıralama ve sayfalama
    query += ` ORDER BY sentAt DESC LIMIT ? OFFSET ?`;
    params.push(limit, offset);

    // Bildirimleri getir
    const [notifications] = await pool.query(query, params);

    // content JSON parse yap
    notifications.forEach(n => {
      try {
        n.content = JSON.parse(n.content);
      } catch {}
    });

    // Toplam bildirim sayısını getir
    const [[{ total }]] = await pool.query(countQuery, countParams);

    res.json({
      success: true,
      notifications,
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

// Bildirimi okundu olarak işaretleme
exports.markAsRead = async (req, res) => {
  const notificationId = req.params.notificationId;
  const { userId } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: 'User ID is required' });
  }

  try {
    // Bildirimin var olup olmadığını ve kullanıcıya ait olup olmadığını kontrol et
    const [notification] = await pool.query(
      `SELECT * FROM notifications 
       WHERE notificationId = ? AND userId = ?`,
      [notificationId, userId]
    );

    if (notification.length === 0) {
      return res.status(404).json({ success: false, message: 'Notification not found' });
    }

    // Bildirimi okundu olarak işaretle
    await pool.query(
      `UPDATE notifications SET isRead = 1 
       WHERE notificationId = ?`,
      [notificationId]
    );

    res.json({ success: true, message: 'Notification marked as read' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Tüm bildirimleri okundu olarak işaretleme
exports.markAllAsRead = async (req, res) => {
  const { userId } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: 'User ID is required' });
  }

  try {
    // Kullanıcının tüm bildirimlerini okundu olarak işaretle
    await pool.query(
      `UPDATE notifications SET isRead = 1 
       WHERE userId = ?`,
      [userId]
    );

    res.json({ success: true, message: 'All notifications marked as read' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Okunmamış bildirim sayısını getirme
exports.getUnreadCount = async (req, res) => {
  const userId = req.params.userId;

  try {
    // Kullanıcının var olup olmadığını kontrol et
    const [user] = await pool.query(`SELECT userId FROM users WHERE userId = ?`, [userId]);
    if (user.length === 0) {
      return res.status(404).json({ success: false, message: 'User not found' });
    }

    const [[{ count }]] = await pool.query(
      `SELECT COUNT(*) as count FROM notifications 
       WHERE userId = ? AND isRead = 0`,
      [userId]
    );

    res.json({ success: true, unreadCount: count });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Bildirimi silme
exports.deleteNotification = async (req, res) => {
  const notificationId = req.params.notificationId;
  const userId = req.params.userId;

  if (!userId) {
    return res.status(400).json({ success: false, message: 'User ID is required' });
  }

  try {
    // Bildirimin var olup olmadığını ve kullanıcıya ait olup olmadığını kontrol et
    const [notification] = await pool.query(
      `SELECT * FROM notifications 
       WHERE notificationId = ? AND userId = ?`,
      [notificationId, userId]
    );

    if (notification.length === 0) {
      return res.status(404).json({ success: false, message: 'Notification not found' });
    }

    // Bildirimi sil
    await pool.query(
      `DELETE FROM notifications 
       WHERE notificationId = ?`,
      [notificationId]
    );

    res.json({ success: true, message: 'Notification deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
