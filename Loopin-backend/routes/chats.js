const express = require('express');
const router = express.Router();
const chatController = require('../controllers/chatController');

// Yeni sohbet başlatma
router.post('/', chatController.createChat);

// Kullanıcının sohbetlerini listeleme
router.get('/user/:userId', chatController.getUserChats);

// Belirli bir sohbetin detaylarını getirme
router.get('/:chatId', chatController.getChatById);

// Sohbet mesajlarını getirme
router.get('/:chatId/messages', chatController.getChatMessages);

// Sohbete mesaj gönderme
router.post('/:chatId/messages', chatController.sendMessage);

// Mesaj silme
router.delete('/:chatId/messages/:messageId', chatController.deleteMessage);

// Sohbeti silme
router.delete('/:userId/:chatId', chatController.deleteChat);

module.exports = router;