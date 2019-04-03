# -*- coding: utf-8 -*-
import json
# if you are using python 3, you should 
#import urllib.request 
import urllib2,urllib
count = 1

with open('C:\\Users\Dell\\Desktop\\project3_data\\testqueries.txt') as f:
    for line in f:
        query = line.strip('\n').replace(':', '')
        u = urllib.quote(query)
        # change the url according to your own corename and query
        inurl = 'http://localhost:8983/solr/gettingstarted/select?defType=dismax&q=' + u + '&fl=id%2Cscore&wt=json&indent=true&rows=20&ps=3&qf=text_en^2.5%20text_de^2.5%20text_ru^2.5%20tweet_hashtags^0.9'
        outfn = 'C:\\Users\\Dell\Desktop\\' + str(count)+ '.txt'
        print inurl
        qid = str(count).zfill(3)
        # change query id and IRModel name accordingly
        IRModel='BM25'
        outf = open(outfn, 'a+')
        data = urllib.urlopen(inurl)
        # if you're using python 3, you should use
        # data = urllib.request.urlopen(inurl)
        docs = json.load(data)['response']['docs']
        # the ranking should start from 1 and increase
        rank = 1
        for doc in docs:
            outf.write(qid + ' ' + 'Q0' + ' ' + str(doc['id']) + ' ' + str(rank) + ' ' + str(doc['score']) + ' ' + IRModel + '\n')
            rank += 1
        outf.close()
        count += 1


