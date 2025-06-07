const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

// Post /api/users/register,login...
router.post('/register', userController.register);
router.post('/login', userController.login);
router.post('/check-username', userController.checkUsername);
router.post('/check-email', userController.checkEmail);
// Dikkat bunlar post değil
router.patch('/update-profile', userController.updateProfile);
router.get('/get-profile', userController.getUserProfile);
router.delete('/delete-profile', userController.deleteAccount);
router.patch('/change-password', userController.changePassword);

module.exports = router;