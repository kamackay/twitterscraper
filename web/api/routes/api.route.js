const express = require('express')

const router = express.Router()
const tweets = require('./api/twitterscraper.route')

router.use('/tweets', tweets);

module.exports = router;