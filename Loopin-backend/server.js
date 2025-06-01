const express = require('express');
const cors = require('cors');
require('dotenv').config();

const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const userRoutes = require('./routes/users');
const chatRoutes = require('./routes/chats');
const eventRoutes = require('./routes/events');
const groupRoutes = require('./routes/groups');
const friendRoutes = require('./routes/friends');
const notificationRoutes = require('./routes/notifications');
app.use('/api/users', userRoutes);
app.use('/api/events', eventRoutes);
app.use('/api/chats', chatRoutes);
app.use('/api/groups', groupRoutes);
app.use('/api/friends', friendRoutes);
app.use('/api/notifications', notificationRoutes);

app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});