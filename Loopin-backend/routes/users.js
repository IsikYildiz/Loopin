const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

// Post /api/users/register,login...
router.post('/register', userController.register);
router.post('/login', userController.login);
router.post('/check-username', userController.checkUsername);
router.post('/check-email', userController.checkEmail);

// Profil güncelleme
router.patch('/update-profile', userController.updateProfile); // userId body'den alınıyor, bu OK.

// Profil getirme ve silme için path parametresi ekleyin
router.get('/get-profile/:id', userController.getUserProfile);      
router.delete('/delete-profile/:id', userController.deleteAccount); 

router.patch('/change-password', userController.changePassword);
router.post('/update-token', userController.updateFcmToken);

module.exports = router;