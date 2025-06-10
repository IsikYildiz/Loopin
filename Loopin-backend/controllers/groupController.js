const pool = require('../db');
const notificationController = require('./notificationController');

// Yeni grup oluşturma
exports.createGroup = async (req, res) => {
  // YENİ: eventId'yi de request body'sinden alıyoruz.
  const { groupName, groupDescription, createdBy, groupImage, eventId } = req.body;

  if (!groupName || !createdBy) {
    return res.status(400).json({ success: false, message: 'Group name and creator ID are required' });
  }

  try {
    // YENİ: Veritabanı sorgusunu ve parametreleri güncelliyoruz.
    const [groupResult] = await pool.query(
      `INSERT INTO usergroups (groupName, groupDescription, createdBy, groupImage, eventId) 
       VALUES (?, ?, ?, ?, ?)`,
      [groupName, groupDescription || null, createdBy, groupImage || null, eventId || null] // eventId varsa ekle, yoksa null ekle
    );

    // Oluşturanı gruba admin olarak ekle (Bu kısım aynı kalıyor)
    await pool.query(
      `INSERT INTO groupmembers (groupId, userId, role, joinedAt) 
       VALUES (?, ?, 'admin', NOW())`,
      [groupResult.insertId, createdBy]
    );

    res.status(201).json({ 
      success: true, 
      message: 'Group created successfully', 
      groupId: groupResult.insertId 
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};


// Grup bilgilerini güncelleme
exports.updateGroup = async (req, res) => {
  const groupId = req.params.groupId;
  const { groupName, groupDescription, groupImage, userId } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: 'User ID is required' });
  }

  try {
    const [membership] = await pool.query(
      `SELECT role FROM groupmembers WHERE groupId = ? AND userId = ?`,
      [groupId, userId]
    );

    if (membership.length === 0 || membership[0].role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Only group admins can update group info' });
    }

    const fields = [];
    const params = [];

    if (groupName !== undefined) {
      fields.push('groupName = ?');
      params.push(groupName);
    }
    if (groupDescription !== undefined) {
      fields.push('groupDescription = ?');
      params.push(groupDescription);
    }
    if (groupImage !== undefined) {
      fields.push('groupImage = ?');
      params.push(groupImage);
    }

    if (fields.length === 0) {
      return res.status(400).json({ success: false, message: 'No valid fields provided for update' });
    }

    params.push(groupId);
    const query = `UPDATE usergroups SET ${fields.join(', ')} WHERE groupId = ?`;
    const [result] = await pool.query(query, params);

    if (result.affectedRows === 0) {
      return res.status(404).json({ success: false, message: 'Group not found' });
    }

    res.json({ success: true, message: 'Group updated successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};


// Grubu silme
exports.deleteGroup = async (req, res) => {
  const groupId = req.params.groupId;
  const { userId } = req.body;

  if (!userId) {
    return res.status(400).json({ success: false, message: 'User ID is required' });
  }

  try {
    // Kullanıcının bu grubun yaratıcısı olup olmadığını kontrol et
    const [group] = await pool.query(
      `SELECT createdBy FROM usergroups WHERE groupId = ?`,
      [groupId]
    );

    if (group.length === 0) {
      return res.status(404).json({ success: false, message: 'Group not found' });
    }

    if (group[0].createdBy !== parseInt(userId)) {
      return res.status(403).json({ success: false, message: 'Only group creator can delete the group' });
    }

    // Önce groupmessages tablosundaki ilişkileri sil
    await pool.query(`DELETE FROM groupmessages WHERE groupId = ?`, [groupId]);

    // Sonra mesajları sil (eğer başka bir grupta kullanılmıyorsa)
    await pool.query(
      `DELETE FROM messages WHERE messageId IN 
       (SELECT messageId FROM groupmessages WHERE groupId = ?)`,
      [groupId]
    );

    // Grup üyelerini sil
    await pool.query(`DELETE FROM groupmembers WHERE groupId = ?`, [groupId]);

    // Son olarak grubu sil
    await pool.query(`DELETE FROM usergroups WHERE groupId = ?`, [groupId]);

    res.json({ success: true, message: 'Group deleted successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Grup detaylarını getirme
exports.getGroupById = async (req, res) => {
  const groupId = req.params.groupId;

  try {
    const [groups] = await pool.query(
      `SELECT g.*, u.fullName as creatorName, u.profileImage as creatorImage
       FROM usergroups g
       JOIN users u ON g.createdBy = u.userId
       WHERE g.groupId = ?`,
      [groupId]
    );

    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: 'Group not found' });
    }

    const group = groups[0];

    // Üye sayısını al
    const [[{ memberCount }]] = await pool.query(
      `SELECT COUNT(*) as memberCount FROM groupmembers WHERE groupId = ?`,
      [groupId]
    );

    res.json({ 
      success: true, 
      group: {
        ...group,
        memberCount
      }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Kullanıcının gruplarını listeleme
exports.getUserGroups = async (req, res) => {
  const userId = req.params.userId;
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 10;
  const offset = (page - 1) * limit;

  try {
    // Kullanıcının üye olduğu grupları getir
    const [groups] = await pool.query(
      `SELECT g.groupId, g.groupName, g.groupDescription, g.groupImage, 
              g.createdBy, gm.role, gm.joinedAt,
              (SELECT content FROM messages m 
               JOIN groupmessages gm ON m.messageId = gm.messageId 
               WHERE gm.groupId = g.groupId 
               ORDER BY m.sentAt DESC LIMIT 1) as lastMessage,
              (SELECT sentAt FROM messages m 
               JOIN groupmessages gm ON m.messageId = gm.messageId 
               WHERE gm.groupId = g.groupId 
               ORDER BY m.sentAt DESC LIMIT 1) as lastMessageTime,
              (SELECT COUNT(*) FROM groupmembers WHERE groupId = g.groupId) as memberCount
       FROM usergroups g
       JOIN groupmembers gm ON g.groupId = gm.groupId
       WHERE gm.userId = ?
       ORDER BY lastMessageTime DESC
       LIMIT ? OFFSET ?`,
      [userId, limit, offset]
    );

    // Toplam grup sayısını getir
    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) as total 
       FROM groupmembers 
       WHERE userId = ?`,
      [userId]
    );

    res.json({
      success: true,
      groups,
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

// Gruba üye ekleme
exports.addGroupMember = async (req, res) => {
  const groupId = req.params.groupId;
  const { userId, requesterId } = req.body;

  if (!userId || !requesterId) {
    return res.status(400).json({ success: false, message: 'User ID and requester ID are required' });
  }

  try {
    // Grubun var olup olmadığını kontrol et
    const [groups] = await pool.query(`SELECT * FROM usergroups WHERE groupId = ?`, [groupId]);
    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: 'Group not found' });
    }

    // İstek yapanın grupta admin olup olmadığını kontrol et
    const [requesterMembership] = await pool.query(
      `SELECT role FROM groupmembers 
       WHERE groupId = ? AND userId = ?`,
      [groupId, requesterId]
    );

    if (requesterMembership.length === 0 || requesterMembership[0].role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Only group admins can add members' });
    }

    // Kullanıcının zaten grupta olup olmadığını kontrol et
    const [existingMember] = await pool.query(
      `SELECT * FROM groupmembers 
       WHERE groupId = ? AND userId = ?`,
      [groupId, userId]
    );

    if (existingMember.length > 0) {
      return res.status(400).json({ success: false, message: 'User is already a member of this group' });
    }

    // Kullanıcıyı gruba ekle
    await pool.query(
      `INSERT INTO groupmembers (groupId, userId, role, joinedAt) 
       VALUES (?, ?, 'member', NOW())`,
      [groupId, userId]
    );

    // Bildirim gönder
    try {
      await notificationController.createNotification(
        userId, 
        'group_join', 
        { groupId, type: "event_add", message: 'You have been added to a group.' }
      );
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.status(201).json({ success: true, message: 'User added to group successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Gruptan üye çıkarma
exports.removeGroupMember = async (req, res) => {
  const groupId = req.params.groupId;
  const { userId, requesterId } = req.body;

  if (!userId || !requesterId) {
    return res.status(400).json({ success: false, message: 'User ID and requester ID are required' });
  }

  try {
    // Grubun var olup olmadığını kontrol et
    const [groups] = await pool.query(`SELECT * FROM usergroups WHERE groupId = ?`, [groupId]);
    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: 'Group not found' });
    }

    // İstek yapanın grupta admin olup olmadığını kontrol et
    const [requesterMembership] = await pool.query(
      `SELECT role FROM groupmembers 
       WHERE groupId = ? AND userId = ?`,
      [groupId, requesterId]
    );

    if (requesterMembership.length === 0 || requesterMembership[0].role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Only group admins can remove members' });
    }

    // Kullanıcının grupta olup olmadığını kontrol et
    const [existingMember] = await pool.query(
      `SELECT * FROM groupmembers 
       WHERE groupId = ? AND userId = ?`,
      [groupId, userId]
    );

    if (existingMember.length === 0) {
      return res.status(404).json({ success: false, message: 'User is not a member of this group' });
    }

    // Kendini çıkarmaya çalışıyorsa
    if (parseInt(userId) === parseInt(requesterId)) {
      return res.status(400).json({ success: false, message: 'Admins cannot remove themselves from the group' });
    }

    // Kullanıcıyı gruptan çıkar
    await pool.query(
      `DELETE FROM groupmembers 
       WHERE groupId = ? AND userId = ?`,
      [groupId, userId]
    );

    // Bildirim gönder
    try {
      await notificationController.createNotification(
        userId,
        'group_remove',
        { groupId, type: "event_remove", message: 'You have been removed from the group.' }
      );
    } catch (error) {
      console.error('Bildirim oluşturulurken hata:', error);
    }

    res.json({ success: true, message: 'User removed from group successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Grup üyelerini listeleme
exports.getGroupMembers = async (req, res) => {
  const groupId = req.params.groupId;
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  try {
    // Üyeleri getir
    const [members] = await pool.query(
      `SELECT u.userId, u.fullName, u.userName, u.profileImage, 
              gm.role, gm.joinedAt
       FROM groupmembers gm
       JOIN users u ON gm.userId = u.userId
       WHERE gm.groupId = ?
       ORDER BY gm.role DESC, gm.joinedAt ASC
       LIMIT ? OFFSET ?`,
      [groupId, limit, offset]
    );

    // Toplam üye sayısını getir
    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) as total 
       FROM groupmembers 
       WHERE groupId = ?`,
      [groupId]
    );

    res.json({
      success: true,
      members,
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

// Grup mesajlarını getirme
exports.getGroupMessages = async (req, res) => {
  const groupId = req.params.groupId;
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;

  try {
    // Mesajları getir
    const [messages] = await pool.query(
      `SELECT m.messageId, m.senderId, m.content, m.sentAt, 
              u.fullName as senderName, u.profileImage as senderImage
       FROM messages m
       JOIN groupmessages gm ON m.messageId = gm.messageId
       JOIN users u ON m.senderId = u.userId
       WHERE gm.groupId = ?
       ORDER BY m.sentAt DESC
       LIMIT ? OFFSET ?`,
      [groupId, limit, offset]
    );

    // Toplam mesaj sayısını getir
    const [[{ total }]] = await pool.query(
      `SELECT COUNT(*) as total 
       FROM groupmessages 
       WHERE groupId = ?`,
      [groupId]
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

// Gruba mesaj gönderme
exports.sendGroupMessage = async (req, res) => {
  const groupId = req.params.groupId;
  const { senderId, content } = req.body;

  if (!senderId || !content) {
    return res.status(400).json({ success: false, message: 'Sender ID and content are required' });
  }

  try {
    // Grubun var olup olmadığını kontrol et
    const [groups] = await pool.query(`SELECT * FROM usergroups WHERE groupId = ?`, [groupId]);
    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: 'Group not found' });
    }

    // Kullanıcının bu grupta olup olmadığını kontrol et
    const [membership] = await pool.query(
      `SELECT * FROM groupmembers 
       WHERE groupId = ? AND userId = ?`,
      [groupId, senderId]
    );

    if (membership.length === 0) {
      return res.status(403).json({ success: false, message: 'You are not a member of this group' });
    }

    // Mesajı oluştur
    const [messageResult] = await pool.query(
      `INSERT INTO messages (senderId, content) VALUES (?, ?)`,
      [senderId, content]
    );

    // Mesajı gruba bağla
    await pool.query(
      `INSERT INTO groupmessages (groupId, messageId) VALUES (?, ?)`,
      [groupId, messageResult.insertId]
    );

    // Bildirim gönderilecek diğer grup üyelerinin ID'lerini al (sender hariç)
    const [groupMembers] = await pool.query(
      `SELECT userId FROM groupmembers WHERE groupId = ? AND userId != ?`,
      [groupId, senderId]
    );

    // Her üyeye bildirim gönder
    for (const member of groupMembers) {
      try {
        await notificationController.createNotification(
          member.userId,
          'group_message',
          { groupId, senderId, message: content }
        );
      } catch (error) {
        console.error(`Bildirim gönderilemedi: userId=${member.userId}`, error);
      }
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


// Üye rolünü güncelleme (admin/member)
exports.updateMemberRole = async (req, res) => {
  const groupId = req.params.groupId;
  const { userId, newRole, requesterId } = req.body;

  if (!userId || !newRole || !requesterId) {
    return res.status(400).json({ success: false, message: 'User ID, new role and requester ID are required' });
  }

  if (!['admin', 'member'].includes(newRole)) {
    return res.status(400).json({ success: false, message: 'Invalid role value' });
  }

  try {
    // Grup var mı?
    const [groups] = await pool.query(`SELECT * FROM usergroups WHERE groupId = ?`, [groupId]);
    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: 'Group not found' });
    }

    // İstek yapan admin mi?
    const [requesterMembership] = await pool.query(
      `SELECT role FROM groupmembers WHERE groupId = ? AND userId = ?`,
      [groupId, requesterId]
    );

    if (requesterMembership.length === 0 || requesterMembership[0].role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Only group admins can change roles' });
    }

    // Güncellenecek kullanıcı grup üyesi mi?
    const [userMembership] = await pool.query(
      `SELECT * FROM groupmembers WHERE groupId = ? AND userId = ?`,
      [groupId, userId]
    );

    if (userMembership.length === 0) {
      return res.status(404).json({ success: false, message: 'User is not a member of this group' });
    }

    // Kendi rolünü değiştirmeye çalışıyorsa engelle
    if (parseInt(userId) === parseInt(requesterId)) {
      return res.status(400).json({ success: false, message: 'You cannot change your own role' });
    }

    // Rol güncelle
    const [result] = await pool.query(
      `UPDATE groupmembers SET role = ? WHERE groupId = ? AND userId = ?`,
      [newRole, groupId, userId]
    );

    if (result.affectedRows === 0) {
      return res.status(404).json({ success: false, message: 'Failed to update member role' });
    }

    res.json({ success: true, message: 'Member role updated successfully' });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};

// Bir etkinliğe bağlı grubu getirme
exports.getGroupByEventId = async (req, res) => {
  const eventId = req.params.eventId;

  try {
    // Veritabanında eventId ile eşleşen grubu bul
    const [groups] = await pool.query(
      `SELECT g.*, u.fullName as creatorName, u.profileImage as creatorImage,
              (SELECT COUNT(*) FROM groupmembers WHERE groupId = g.groupId) as memberCount
       FROM usergroups g
       JOIN users u ON g.createdBy = u.userId
       WHERE g.eventId = ?`,
      [eventId]
    );

    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: 'No group found for this event' });
    }

    // getGroupById ile aynı formatta bir cevap dönüyoruz.
    res.json({ 
      success: true, 
      group: groups[0]
    });
  } catch (error) {
    res.status(500).json({ success: false, message: error.message });
  }
};
