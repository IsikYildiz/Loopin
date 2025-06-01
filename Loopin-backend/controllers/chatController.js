const pool = require('../db');
const notificationController = require('./notificationController');

// Kullanıcılar arasında yeni bir sohbet başlatma
exports.createChat = async (req, res) => {
  const { user1Id, user2Id } = req.body;

  if (!user1Id || !user2Id) {
    return res.status(400).json({ success: false, message: 'Both user IDs are required' });
  }

  if (user1Id === user2Id) {
    return res.status(400).json({ success: false, message: 'Cannot create chat with yourself' });
  }

  try {
    // Önce böyle bir sohbetin zaten var olup olmadığını kontrol et
    const [existingChats] = await pool.query(
      `SELECT * FROM chats 
       WHERE (user1Id = ? AND user2Id = ?) OR (user1Id = ? AND user2Id = ?)`,
      [user1Id, user2Id, user2Id, user1Id]
    );

    if (existingChats.length > 0) {
      return res.json({ 
        success: true, 
        message: 'Chat already exists', 
        chatId: existingChats[0].chatId 
      });
    }

    // Yeni sohbet oluştur
    const [result] = await pool.query(
      `INSERT INTO chats (user1Id, user2Id) VALUES (?, ?)`,
      [user1Id, user2Id]
    );

    res.status(201).json({ 
      success: true, 
      message: 'Chat created successfully', 
      chatId: result.insertId 
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Kullanıcının tüm sohbetlerini listeleme
exports.getUserChats = async (req, res) => {
  const userId = req.params.userId;

  try {
    const [chats] = await pool.query(
      `SELECT c.chatId, 
              c.user1Id, 
              c.user2Id,
              u1.fullName as user1Name,
              u2.fullName as user2Name,
              u1.profileImage as user1Image,
              u2.profileImage as user2Image,
              (SELECT content FROM messages m 
               JOIN chatmessages cm ON m.messageId = cm.messageId 
               WHERE cm.chatId = c.chatId 
               ORDER BY m.sentAt DESC LIMIT 1) as lastMessage,
              (SELECT sentAt FROM messages m 
               JOIN chatmessages cm ON m.messageId = cm.messageId 
               WHERE cm.chatId = c.chatId 
               ORDER BY m.sentAt DESC LIMIT 1) as lastMessageTime
       FROM chats c
       JOIN users u1 ON c.user1Id = u1.userId
       JOIN users u2 ON c.user2Id = u2.userId
       WHERE c.user1Id = ? OR c.user2Id = ?
       ORDER BY lastMessageTime DESC`,
      [userId, userId]
    );

    res.json({ success: true, chats });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Belirli bir sohbetin detaylarını getirme
exports.getChatById = async (req, res) => {
  const chatId = req.params.chatId;

  try {
    const [chats] = await pool.query(
      `SELECT c.*, 
              u1.fullName as user1Name,
              u2.fullName as user2Name,
              u1.profileImage as user1Image,
              u2.profileImage as user2Image
       FROM chats c
       JOIN users u1 ON c.user1Id = u1.userId
       JOIN users u2 ON c.user2Id = u2.userId
       WHERE c.chatId = ?`,
      [chatId]
    );

    if (chats.length === 0) {
      return res.status(404).json({ success: false, message: 'Chat not found' });
    }

    res.json({ success: true, chat: chats[0] });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Sohbet mesajlarını getirme
exports.getChatMessages = async (req, res) => {
  const chatId = req.params.chatId;
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  try {
    // Mesajları getir
    const [messages] = await pool.query(
      `SELECT m.messageId, m.senderId, m.content, m.sentAt, u.fullName as senderName, u.profileImage as senderImage
       FROM messages m
       JOIN chatmessages cm ON m.messageId = cm.messageId
       JOIN users u ON m.senderId = u.userId
       WHERE cm.chatId = ?
       ORDER BY m.sentAt DESC
       LIMIT ? OFFSET ?`,
      [chatId, limit, offset]
    );

    // Toplam mesaj sayısını getir
    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) as total 
       FROM chatmessages 
       WHERE chatId = ?`,
      [chatId]
    );

    res.json({
      success: true,
      messages: messages.reverse(), // En yeni mesaj en altta olacak şekilde
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

// Sohbete mesaj gönderme
exports.sendMessage = async (req, res) => {
  const chatId = req.params.chatId;
  const { senderId, content } = req.body;

  if (!senderId || !content) {
    return res.status(400).json({ success: false, message: 'Sender ID and content are required' });
  }

  try {
    // Önce sohbetin var olup olmadığını kontrol et
    const [chats] = await pool.query(`SELECT * FROM chats WHERE chatId = ?`, [chatId]);
    if (chats.length === 0) {
      return res.status(404).json({ success: false, message: 'Chat not found' });
    }

    // Kullanıcının bu sohbette olup olmadığını kontrol et
    const chat = chats[0];
    if (chat.user1Id !== parseInt(senderId) && chat.user2Id !== parseInt(senderId)) {
      return res.status(403).json({ success: false, message: 'You are not a participant of this chat' });
    }

    // Mesajı oluştur
    const [messageResult] = await pool.query(
      `INSERT INTO messages (senderId, content) VALUES (?, ?)`,
      [senderId, content]
    );

    // Mesajı sohbete bağla
    await pool.query(
      `INSERT INTO chatmessages (chatId, messageId) VALUES (?, ?)`,
      [chatId, messageResult.insertId]
    );

    // Bildirim gönderilecek kişinin ID'sini tespit et (diğer kullanıcı)
    let receiverId;
    if (chat.user1Id === parseInt(senderId)) {
      receiverId = chat.user2Id;
    } else {
      receiverId = chat.user1Id;
    }

    // Bildirim gönder
    try {
      await notificationController.createNotification(receiverId, 'message', { senderId, message: content, chatId });
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.status(201).json({ 
      success: true, 
      message: 'Message sent successfully', 
      messageId: messageResult.insertId 
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Sohbeti silme
exports.deleteChat = async (req, res) => {
  const chatId = req.params.chatId;
  const { userId } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: 'User ID is required' });
  }

  try {
    // Sohbetin var olup olmadığını ve kullanıcının bu sohbette olup olmadığını kontrol et
    const [chats] = await pool.query(`SELECT * FROM chats WHERE chatId = ?`, [chatId]);
    if (chats.length === 0) {
      return res.status(404).json({ success: false, message: 'Chat not found' });
    }

    const chat = chats[0];
    if (chat.user1Id !== parseInt(userId) && chat.user2Id !== parseInt(userId)) {
      return res.status(403).json({ success: false, message: 'You are not a participant of this chat' });
    }

    // 1. chatmessages'tan messageId'leri al
    const [messageRows] = await pool.query(`SELECT messageId FROM chatmessages WHERE chatId = ?`, [chatId]);
    const messageIds = messageRows.map(row => row.messageId);
    
    // 2. ilişkileri sil
    await pool.query(`DELETE FROM chatmessages WHERE chatId = ?`, [chatId]);
    
    // 3. mesajları sil
    if (messageIds.length > 0) {
        await pool.query(`DELETE FROM messages WHERE messageId IN (?)`, [messageIds]);
    }

    // Son olarak sohbeti sil
    await pool.query(`DELETE FROM chats WHERE chatId = ?`, [chatId]);

    res.json({ success: true, message: 'Chat deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

exports.deleteMessage = async (req, res) => {
  const { chatId, messageId } = req.params;
  const { userId } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: 'User ID is required' });
  }

  try {
    // Mesajın ve sohbetin varlığını kontrol et
    const [messages] = await pool.query(
      `SELECT m.senderId, c.user1Id, c.user2Id
       FROM messages m
       JOIN chatmessages cm ON m.messageId = cm.messageId
       JOIN chats c ON cm.chatId = c.chatId
       WHERE m.messageId = ? AND c.chatId = ?`,
      [messageId, chatId]
    );

    if (messages.length === 0) {
      return res.status(404).json({ success: false, message: 'Message not found in this chat' });
    }

    const message = messages[0];

    // Yalnızca mesajı gönderen kullanıcı silebilir
    if (parseInt(userId) !== message.senderId) {
      return res.status(403).json({ success: false, message: 'You can only delete your own messages' });
    }

    // Önce chatmessages ilişki tablosundan sil
    await pool.query(`DELETE FROM chatmessages WHERE chatId = ? AND messageId = ?`, [chatId, messageId]);

    // Ardından mesajı tamamen sil
    await pool.query(`DELETE FROM messages WHERE messageId = ?`, [messageId]);

    res.json({ success: true, message: 'Message deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};