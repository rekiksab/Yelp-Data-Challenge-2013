import urllib
import json

response = urllib.urlopen("http://search.twitter.com/search.json?q=microsoft")
print json.load(response)