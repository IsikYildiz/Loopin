const express = require('express');
const router = express.Router();
const notificationController = require('../controllers/notificationController');

// Kullanıcının bildirimlerini listeleme
router.get('/user/:userId', notificationController.getUserNotifications);

// Bildirimi okundu olarak işaretleme
router.put('/:notificationId/read', notificationController.markAsRead);

// Tüm bildirimleri okundu olarak işaretleme
router.put('/read-all', notificationController.markAllAsRead);

// Okunmamış bildirim sayısını getirme
router.get('/user/:userId/unread-count', notificationController.getUnreadCount);

// Bildirimi silme
router.delete('/:notificationId', notificationController.deleteNotification);

module.exports = router;