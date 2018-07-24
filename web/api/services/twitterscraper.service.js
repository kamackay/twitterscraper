const twitter = require("../models/twitterscraper.model");

// Saving the context of this module inside the _the variable
_this = this;

exports.getTweets = async function(query, page, limit) {
	// Options setup for the mongoose paginate
	var options = {
		page,
		limit
	};

	// Try Catch the awaited promise to handle the error

	try {
		var tweets = await twitter.paginate(query, options);

		return tweets;
	} catch (e) {
		// return a Error message describing the reason
		throw Error("Error while Paginating Tweets");
	}
};