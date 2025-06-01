const express = require('express');
const router = express.Router();
const friendController = require('../controllers/friendController');

// Arkadaşlık isteği gönderme
router.post('/requests', friendController.sendFriendRequest);

// Arkadaşlık isteğini kabul etme
router.put('/requests/accept', friendController.acceptFriendRequest);

// Arkadaşlık isteğini reddetme
router.put('/requests/reject', friendController.rejectFriendRequest);

// Arkadaşlığı sonlandırma
router.delete('/', friendController.removeFriend);

// Kullanıcının arkadaş listesini getirme
router.get('/user/:userId', friendController.getUserFriends);

// Bekleyen arkadaşlık isteklerini getirme
router.get('/requests/:userId', friendController.getPendingFriendRequests);

// Arkadaş arama
router.get('/search/:userId', friendController.searchFriends);

// Arkadaş önerileri getirme
router.get('/suggestions/:userId', friendController.getFriendSuggestions);

module.exports = router;