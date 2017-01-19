# RssFilter
Rss filter with white list keywords

# description
host: `https://ww-rssfilter-akka.herokuapp.com`
endpoint: `filter`
queryParameters: 
  `feed`, String
  `keywords`, String array split by `,`

# sample
https://ww-rssfilter-akka.herokuapp.com/filter?feed=http://japanese.engadget.com/rss.xml&keywords=Android,iOS
