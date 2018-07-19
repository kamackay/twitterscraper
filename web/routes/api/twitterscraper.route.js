const express = require('express')

const router = express.Router()

const TwitterController = require('../../controllers/twitterscraper.controller');

router.get('/', TwitterController.getTweets)

module.exports = router;