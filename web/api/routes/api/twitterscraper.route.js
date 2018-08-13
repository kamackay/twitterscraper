const express = require('express')

const router = express.Router()

const TwitterController = require('../../controllers/twitterscraper.controller');

router.get('/:collection', TwitterController.getTweets)
router.get('/:collection/count', TwitterController.count);

module.exports = router;