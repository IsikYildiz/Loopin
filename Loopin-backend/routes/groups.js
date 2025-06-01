const express = require('express');
const router = express.Router();
const groupController = require('../controllers/groupController');

// Grup oluşturma
router.post('/', groupController.createGroup);

// Grup bilgilerini güncelleme
router.patch('/:groupId', groupController.updateGroup);

// Grubu silme
router.delete('/:groupId', groupController.deleteGroup);

// Grup detaylarını getirme
router.get('/:groupId', groupController.getGroupById);

// Kullanıcının gruplarını listeleme
router.get('/user/:userId', groupController.getUserGroups);

// Gruba üye ekleme
router.post('/:groupId/members', groupController.addGroupMember);

// Gruptan üye çıkarma
router.delete('/:groupId/members', groupController.removeGroupMember);

// Grup üyelerini listeleme
router.get('/:groupId/members', groupController.getGroupMembers);

// Grup mesajlarını getirme
router.get('/:groupId/messages', groupController.getGroupMessages);

// Gruba mesaj gönderme
router.post('/:groupId/messages', groupController.sendGroupMessage);

// Üye rolünü güncelleme
router.patch('/:groupId/members/role', groupController.updateMemberRole);

module.exports = router;