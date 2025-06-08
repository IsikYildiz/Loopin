const express = require('express');
const router = express.Router();
const eventController = require('../controllers/eventController');

// Yeni etkinlik oluşturma
router.post('/', eventController.createEvent);

// Genel (public, gizli olmayan ve bitmemiş) etkinlikleri getirme
router.get('/public', eventController.getPublicEvents);

// Etkinlik silme (eventId, userId genelde body veya query'de gönderilir)
router.delete('/:eventId', eventController.deleteEvent);

// Etkinlik güncelleme
router.patch('/:eventId', eventController.updateEvent);

// Belirli bir etkinliği getirme
router.get('/:eventId', eventController.getEventById);

// Kullanıcının oluşturduğu etkinlikleri getirme
router.get('/creator/:userId', eventController.getEventsCreatedByUser);

// Kullanıcının geçmişte katıldığı etkinlikler
router.get('/participants/past/:userId', eventController.getEventsUserParticipates);

// Kullanıcının gelecekte katılacağı etkinlikler
router.get('/participants/upcoming/:userId', eventController.getUpcomingEventsUserParticipates);

// Etkinliğe katılma (katılımcı ekleme)
router.post('/:eventId/join', eventController.joinEvent);

// Etkinlikten ayrılma (katılımcı çıkarma)
router.post('/:eventId/leave', eventController.leaveEvent);

// Belirli etkinliğin katılımcılarını listeleme
router.get('/:eventId/participants', eventController.getEventParticipants);

// Etkinlik katılımcı sayısını alma
router.get('/:eventId/participant-count', eventController.getEventParticipantCount);

// Katılım durumunu güncelleme (status değiştirme)
router.put('/:eventId/participants/status', eventController.updateParticipantStatus);

// Etkinlik arama ve filtreleme (query parametreleri ile)
router.get('/search', eventController.searchEvents);

module.exports = router;
