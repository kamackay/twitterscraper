var mongoose = require("mongoose");
var mongoosePaginate = require("mongoose-paginate");

var tweetSchema = new mongoose.Schema({
	id: Number,
	username: String,
	tweetURL: String
});

tweetSchema.plugin(mongoosePaginate);

module.exports = {
	get: collectionName => {
		return mongoose.model("Tweets", tweetSchema, collectionName);
	}
};
