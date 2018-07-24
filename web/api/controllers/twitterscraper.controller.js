const twitterService = require("../services/twitterscraper.service");

// Saving the context of this module inside the _the variable

_this = this;

exports.getTweets = async function(req, res, next) {
	// Check the existence of the query parameters, If the exists doesn't exists assign a default value

	const collection = req.params.collection;

	var page = req.query.page ? parseInt(req.query.page) : 1;
	var limit = req.query.limit ? parseInt(req.query.limit) : 100;

	try {
		var tweets = await twitterService.getTweets(collection, {}, page, limit);

		return res
			.status(200)
			.json({
				status: 200,
				data: tweets,
				message: "Succesfully Recieved Tweets"
			});
	} catch (e) {
		//Return an Error Response Message with Code and the Error Message.

		return res.status(400).json({ status: 400, message: e.message });
	}
};
