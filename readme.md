# Twitter Scraper

## config.json
Contains query configurations that are read into the program every time it runs. You don't have to restart to see the changes take effect.
- At the root is a list of queries that the Scraper will go through and execute. Each query has the following attributes. Note that mentions, quotes, and hashtags have an AND association.
    - queryName - The name of the query. Also is saved into the database under this name.
    - includeRetweets - Whether or not to also query retweets of tweets that meet the given parameters. Defaults to false.
    - updateExisting - Whether to allow results to include tweets that have been retrieved before. If false, retweet and favorite counts in the database will be more incorrect than they would be otherwise (may still not be correct otherwise). Defaults to true.
    - mentions - List of requested users mentioned in tweets
    - hashtags -  List of requested hashtags in tweets
    - quotes - List of exact quotes to be requested in tweets
    
## twitter4j.properties
Properties file that houses twitter API credentials. Won't exist in a clean project, needs to be created for the project to work.
Credentials can be created [here](https://apps.twitter.com/). The contents of the file should match this format:
```properties
debug=false
oauth.consumerKey=<consumerKey>
oauth.consumerSecret=<consumerSecret>
oauth.accessToken=<accessToken>
oauth.accessTokenSecret=<accessTokenSecret>
```

### TODO:
- Save results to database &#x2714;
- Run as continuous service &#x2714;
- Webpage to view data