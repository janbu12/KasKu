const redis = require('redis');

const client = redis.createClient({
  url: process.env.REDIS_URL || 'redis://localhost:6379'
});

client.on('error', (err) => {
  console.error('Redis Client Error', err);
});

client.connect().then(() => {
  console.log('Redis client connected successfully');
}).catch((err) => {
  console.error('Failed to connect to Redis:', err);
});

module.exports = client;