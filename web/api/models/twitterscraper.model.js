var mongoose = require('mongoose')
var mongoosePaginate = require('mongoose-paginate')


var tweetSchema = new mongoose.Schema({
	id: Number,
	username: String,
	tweetURL: String
})

tweetSchema.plugin(mongoosePaginate)
const tweets = mongoose.model('Tweets', tweetSchema, "TrumpMentions")

module.exports = tweets;