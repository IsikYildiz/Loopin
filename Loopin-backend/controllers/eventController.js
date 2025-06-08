const pool    = require('../db');  // pool objesi
const bcrypt  = require('bcrypt');
const notificationController = require('./notificationController');

const SALT_ROUNDS = 10;

// Event oluşturma
exports.createEvent = async (req, res) => {
  const {
    creatorId,
    eventName,
    eventLocation,
    startTime,
    endTime,
    description,
    maxParticipants,
    isPrivate,
    password
  } = req.body;

  // Gerekli alan kontrolü
  if (!creatorId || !eventName || !startTime || !endTime || !maxParticipants) {
    return res.status(400).json({ success: false, message: "Required fields missing" });
  }

  try {
    let hashedPassword = null;
    if (isPrivate && password) {
      hashedPassword = await bcrypt.hash(password, SALT_ROUNDS);
    }

    // Transaction başlat
    const connection = await pool.getConnection();
    await connection.beginTransaction();

    try {
      // Etkinliği oluştur
      const [eventResult] = await connection.query(
        `INSERT INTO events 
         (creatorId, eventName, eventLocation, startTime, endTime, description, maxParticipants, isPrivate, password)
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)`,
        [
          creatorId,
          eventName,
          eventLocation || null,
          startTime,
          endTime,
          description || null,
          parseInt(maxParticipants, 10),
          isPrivate ? 1 : 0,
          hashedPassword
        ]
      );

      // Oluşturanı etkinliğe katılımcı olarak ekle
      await connection.query(
        `INSERT INTO eventparticipants 
         (userId, eventId, status, joinedAt)
         VALUES (?, ?, 'joined', NOW())`,
        [creatorId, eventResult.insertId]
      );

      // Transaction'ı tamamla
      await connection.commit();
      connection.release();

      res.status(201).json({ 
        success: true, 
        message: 'Event created successfully',
        eventId: eventResult.insertId 
      });
    } catch (error) {
      // Hata durumunda rollback
      await connection.rollback();
      connection.release();
      throw error;
    }
  } catch (err) {
    console.error('Error creating event:', err);
    res.status(500).json({ 
      success: false, 
      message: err.message,
      error: process.env.NODE_ENV === 'development' ? err.stack : undefined
    });
  }
};

// Event güncelleme
exports.updateEvent = async (req, res) => {
  const eventId = req.params.eventId  ;
  const {
    eventName,
    eventLocation,
    startTime,
    endTime,
    description,
    maxParticipants,
    isPrivate,
    password
  } = req.body;

  try {
    const fields = [];
    const params = [];

    if (eventName !== undefined) {
      fields.push('eventName = ?');
      params.push(eventName);
    }
    if (eventLocation !== undefined) {
      fields.push('eventLocation = ?');
      params.push(eventLocation);
    }
    if (startTime !== undefined) {
      fields.push('startTime = ?');
      params.push(startTime);
    }
    if (endTime !== undefined) {
      fields.push('endTime = ?');
      params.push(endTime);
    }
    if (description !== undefined) {
      fields.push('description = ?');
      params.push(description);
    }
    if (maxParticipants !== undefined) {
      fields.push('maxParticipants = ?');
      params.push(parseInt(maxParticipants, 10));
    }
    if (isPrivate !== undefined) {
      fields.push('isPrivate = ?');
      params.push(isPrivate ? 1 : 0);
      if (password !== undefined) {
        const hashed = password ? await bcrypt.hash(password, SALT_ROUNDS) : null;
        fields.push('password = ?');
        params.push(hashed);
      }
    } else if (password !== undefined) {
      // Sadece password güncellenecekse
      const hashed = password ? await bcrypt.hash(password, SALT_ROUNDS) : null;
      fields.push('password = ?');
      params.push(hashed);
    }

    if (fields.length === 0) {
      return res.status(400).json({ success: false, message: 'No valid fields provided for update' });
    }

    params.push(eventId);
    const query = `UPDATE events SET ${fields.join(', ')} WHERE eventId = ?`;

    const [result] = await pool.query(query, params);

    if (result.affectedRows === 0) {
      return res.status(404).json({ success: false, message: 'Event not found' });
    }

    res.json({ success: true, message: 'Event updated successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Event silme
exports.deleteEvent = async (req, res) => {
  const eventId = req.params.eventId;
  const { userId } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: "User ID required" });
  }

  try {
    const [rows] = await pool.query(`SELECT * FROM events WHERE eventId = ?`, [eventId]);

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Event not found" });
    }

    const event = rows[0];
    if (event.creatorId !== parseInt(userId, 10)) {
      return res.status(403).json({ success: false, message: "You are not the owner of this event" });
    }

    await pool.query(`DELETE FROM events WHERE eventId = ?`, [eventId]);

    res.json({ success: true, message: "Event deleted successfully" });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Etkinlik detayını getirme
exports.getEventById = async (req, res) => {
  const eventId = req.params.eventId;

  try {
    const [rows] = await pool.query(`SELECT * FROM events WHERE eventId = ?`, [eventId]);

    if (rows.length === 0) {
      return res.status(404).json({ success: false, message: "Event not found" });
    }

    res.json({ success: true, event: rows[0] });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Public etkinlikleri listeleme (kullanıcının katıldıkları hariç)
exports.getPublicEvents = async (req, res) => {
  // 1. Adım: İstekten page, limit ve opsiyonel olarak userId'yi al
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 10;
  const userId = req.query.userId; // userId, string olarak gelebilir
  const offset = (page - 1) * limit;

  // Temel sorgu ve parametreleri hazırla
  let mainQuery = '';
  let countQuery = '';
  let queryParams = [];

  // 2. Adım: userId varsa sorguyu ve parametreleri değiştir
  if (userId) {
    // Kullanıcının katılmadığı etkinlikleri bulmak için LEFT JOIN kullan
    // Bir etkinliğin karşılığında ep.userId null ise, kullanıcı o etkinliğe katılmamıştır.
    mainQuery = `
      SELECT e.*
      FROM events e
      LEFT JOIN eventparticipants ep ON e.eventId = ep.eventId AND ep.userId = ?
      WHERE e.isPrivate = 0 AND e.endTime >= NOW() AND ep.userId IS NULL
      ORDER BY e.createdAt DESC
      LIMIT ? OFFSET ?
    `;
    countQuery = `
      SELECT COUNT(*) AS total
      FROM events e
      LEFT JOIN eventparticipants ep ON e.eventId = ep.eventId AND ep.userId = ?
      WHERE e.isPrivate = 0 AND e.endTime >= NOW() AND ep.userId IS NULL
    `;
    // Parametre dizisine userId'yi başa ekle
    queryParams = [parseInt(userId, 10)];

  } else {
    // userId yoksa (örneğin kullanıcı giriş yapmamışsa), orijinal sorguyu kullan
    mainQuery = `
      SELECT *
       FROM events
       WHERE isPrivate = 0 AND endTime >= NOW()
       ORDER BY createdAt DESC
       LIMIT ? OFFSET ?
    `;
    countQuery = `
      SELECT COUNT(*) AS total
       FROM events
       WHERE isPrivate = 0 AND endTime >= NOW()
    `;
  }

  try {
    // Etkinlikleri al
    const [rows] = await pool.query(mainQuery, [...queryParams, limit, offset]);

    // Toplam sayıyı al
    const [[{ total }]] = await pool.query(countQuery, queryParams);

    res.json({
      success: true,
      events: rows,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Kullanıcının oluşturduğu etkinlikler
exports.getEventsCreatedByUser = async (req, res) => {
  const userId = req.params.userId;
  const page   = parseInt(req.query.page) || 1;
  const limit  = parseInt(req.query.limit) || 10;
  const offset = (page - 1) * limit;

  try {
    const [rows] = await pool.query(
      `SELECT * 
       FROM events 
       WHERE creatorId = ? 
       ORDER BY createdAt DESC 
       LIMIT ? OFFSET ?`,
      [userId, limit, offset]
    );

    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) AS total 
       FROM events 
       WHERE creatorId = ?`,
      [userId]
    );

    res.json({
      success: true,
      events: rows,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Kullanıcının katıldığı geçmiş etkinlikler
exports.getEventsUserParticipates = async (req, res) => {
  const userId = req.params.userId;
  const page   = parseInt(req.query.page) || 1;
  const limit  = parseInt(req.query.limit) || 10;
  const offset = (page - 1) * limit;

  try {
    const [rows] = await pool.query(
      `SELECT e.* 
       FROM events e
       JOIN eventParticipants ep ON e.eventId = ep.eventId
       WHERE ep.userId = ? AND e.endTime < NOW()
       ORDER BY e.createdAt DESC
       LIMIT ? OFFSET ?`,
      [userId, limit, offset]
    );

    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) AS total 
       FROM events e
       JOIN eventParticipants ep ON e.eventId = ep.eventId
       WHERE ep.userId = ? AND e.endTime < NOW()`,
      [userId]
    );

    res.json({
      success: true,
      events: rows,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Kullanıcının katıldığı gelecek etkinlikler
exports.getUpcomingEventsUserParticipates = async (req, res) => {
  const userId = req.params.userId;
  const page   = parseInt(req.query.page) || 1;
  const limit  = parseInt(req.query.limit) || 10;
  const offset = (page - 1) * limit;

  try {
    const [rows] = await pool.query(
      `SELECT e.* 
       FROM events e
       JOIN eventParticipants ep ON e.eventId = ep.eventId
       WHERE ep.userId = ? AND e.endTime >= NOW()
       ORDER BY e.startTime ASC
       LIMIT ? OFFSET ?`,
      [userId, limit, offset]
    );

    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) AS total 
       FROM events e
       JOIN eventParticipants ep ON e.eventId = ep.eventId
       WHERE ep.userId = ? AND e.endTime >= NOW()`,
      [userId]
    );

    res.json({
      success: true,
      events: rows,
      pagination: {
        total,
        page,
        limit,
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Etkinliğe katılma (birini etkinliğe eklemek için de kullanılabilir)
exports.joinEvent = async (req, res) => {
  const { eventId, userId, password } = req.body;

  if (!eventId || !userId) {
    return res.status(400).json({ success: false, message: "Event ID and User ID are required" });
  }

  try {
    const [events] = await pool.query(`SELECT * FROM events WHERE eventId = ?`, [eventId]);

    if (events.length === 0) {
      return res.status(404).json({ success: false, message: "Event not found" });
    }
    const event = events[0];

    if (event.isPrivate) {
      if (!password) {
        return res.status(400).json({ success: false, message: "Password required for private event" });
      }
      const passwordMatch = await bcrypt.compare(password, event.password);
      if (!passwordMatch) {
        return res.status(403).json({ success: false, message: "Incorrect password" });
      }
    }

    const [participants] = await pool.query(
      `SELECT COUNT(*) AS count FROM eventParticipants WHERE eventId = ?`,
      [eventId]
    );
    if (participants[0].count >= event.maxParticipants) {
      return res.status(400).json({ success: false, message: "Event is full" });
    }

    const [existing] = await pool.query(
      `SELECT * FROM eventParticipants WHERE eventId = ? AND userId = ?`,
      [eventId, userId]
    );
    if (existing.length > 0) {
      return res.status(400).json({ success: false, message: "User already joined this event" });
    }

    await pool.query(
      `INSERT INTO eventParticipants (eventId, userId, status, joinedAt) VALUES (?, ?, 'joined', NOW())`,
      [eventId, userId]
    );

    // Katılım bildirimi gönder
    try {
      await notificationController.createNotification(userId, 'event_invite_accept', { userId, message: "Succesfully joined event", eventId});
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.json({ success: true, message: "Successfully joined the event" });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Etkinlikten ayrılma (birini etkinlikten çıkarmak için de kullanılabilir)
exports.leaveEvent = async (req, res) => {
  const { eventId, userId } = req.body;

  if (!eventId || !userId) {
    return res.status(400).json({ success: false, message: "Event ID and User ID are required" });
  }

  try {
    const [existing] = await pool.query(
      `SELECT * FROM eventParticipants WHERE eventId = ? AND userId = ?`,
      [eventId, userId]
    );

    if (existing.length === 0) {
      return res.status(404).json({ success: false, message: "User is not a participant of this event" });
    }

    const [eventResult] = await pool.query(`SELECT eventName FROM events WHERE eventId = ?`, [eventId]);
    const eventTitle = eventResult.length > 0 ? eventResult[0].eventName : "the event";

    await pool.query(
      `DELETE FROM eventParticipants WHERE eventId = ? AND userId = ?`,
      [eventId, userId]
    );

    // Ayrılma bildirimi gönder
    try {
      await notificationController.createNotification(userId, 'event_invite_decline', { userId, message: "Succesfully left "+eventTitle+"", eventId});
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.json({ success: true, message: "Successfully left the event" });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Etkinlik katılımcılarını listeleme
exports.getEventParticipants = async (req, res) => {
  const eventId = req.params.eventId;

  try {
    const [participants] = await pool.query(
      `SELECT u.userId AS id, u.fullName AS name, ep.status, ep.joinedAt
       FROM eventParticipants ep
       JOIN Users u ON ep.userId = u.userId
       WHERE ep.eventId = ?`,
      [eventId]
    );

    res.json({ success: true, participants });
  } catch (err) {
    console.error(`Error fetching participants for event ${eventId}:`, err);
    res.status(500).json({ success: false, message: err.message });
  }
};


// Etkinlik katılımcı sayısını alma
exports.getEventParticipantCount = async (req, res) => {
  const eventId = req.params.eventId;

  try {
    const [result] = await pool.query(
      `SELECT COUNT(*) AS participantCount 
       FROM eventParticipants 
       WHERE eventId = ?`,
      [eventId]
    );

    res.json({ success: true, participantCount: result[0].participantCount });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Katılım durumu güncelleme
exports.updateParticipantStatus = async (req, res) => {
  const { eventId, userId, status } = req.body;
  const validStatuses = ['pending', 'joined', 'declined', 'cancelled'];

  if (!eventId || !userId || !status) {
    return res.status(400).json({ success: false, message: "Event ID, User ID and status are required" });
  }

  if (!validStatuses.includes(status)) {
    return res.status(400).json({ success: false, message: "Invalid status value" });
  }

  try {
    const [result] = await pool.query(
      `UPDATE eventParticipants 
       SET status = ? 
       WHERE eventId = ? AND userId = ?`,
      [status, eventId, userId]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ success: false, message: "Participant not found" });
    }

    res.json({ success: true, message: "Participant status updated successfully" });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};

// Etkinlik arama ve filtreleme
exports.searchEvents = async (req, res) => {
  const { name, location, startDate, endDate, isPrivate, page = 1, limit = 10 } = req.query;

  let query = `SELECT * FROM events WHERE 1=1`;
  const params = [];

  if (name) {
    query += ` AND eventName LIKE ?`;
    params.push(`%${name}%`);
  }
  if (location) {
    query += ` AND eventLocation LIKE ?`;
    params.push(`%${location}%`);
  }
  if (startDate) {
    query += ` AND startTime >= ?`;
    params.push(startDate);
  }
  if (endDate) {
    query += ` AND endTime <= ?`;
    params.push(endDate);
  }
  if (isPrivate !== undefined) {
    query += ` AND isPrivate = ?`;
    params.push(isPrivate === 'true' ? 1 : 0);
  }

  // Sayfalama hesaplaması
  const offset = (parseInt(page) - 1) * parseInt(limit);
  query += ` LIMIT ? OFFSET ?`;
  params.push(parseInt(limit), offset);

  try {
    const [rows] = await pool.query(query, params);

    // Toplam kayıt sayısını almak istersen:
    let countQuery = `SELECT COUNT(*) AS total FROM events WHERE 1=1`;
    const countParams = [];

    if (name) {
      countQuery += ` AND eventName LIKE ?`;
      countParams.push(`%${name}%`);
    }
    if (location) {
      countQuery += ` AND eventLocation LIKE ?`;
      countParams.push(`%${location}%`);
    }
    if (startDate) {
      countQuery += ` AND startTime >= ?`;
      countParams.push(startDate);
    }
    if (endDate) {
      countQuery += ` AND endTime <= ?`;
      countParams.push(endDate);
    }
    if (isPrivate !== undefined) {
      countQuery += ` AND isPrivate = ?`;
      countParams.push(isPrivate === 'true' ? 1 : 0);
    }

    const [countResult] = await pool.query(countQuery, countParams);
    const total = countResult[0].total;

    res.json({
      success: true,
      events: rows,
      pagination: {
        total,
        page: parseInt(page),
        limit: parseInt(limit),
        totalPages: Math.ceil(total / limit)
      }
    });
  } catch (err) {
    res.status(500).json({ success: false, message: err.message });
  }
};
