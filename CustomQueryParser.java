/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package project3;

import javax.management.Query;
import org.apache.lucene.analysis.Analyzer;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.ExtendedDismaxQParser;
import org.apache.solr.search.ExtendedDismaxQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.SyntaxError;


public class CustomQueryParser extends ExtendedDismaxQParserPlugin {
  private static final String[] defined_weights = {"text_en", "text_de", "text_ru", "tweet_hashtags"};
  
  public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    ModifiableSolrParams customParams = new ModifiableSolrParams();
    
    if (query.contains("lang:en")) {
        defined_weights[0] = "text_en^2.5";
        query = query.replace("lang:en", "");
      }
    if (query.contains("lang:de")) {
        defined_weights[1] = "text_de^2.5";
        query = query.replace("lang:de", "");
      }
    if (query.contains("text_ru")) {
        defined_weights[2] = "text_ru^2.5";
        query = query.replace("text_ru", "");
      }
    
      defined_weights[3] = "tweet_hashtags^1";
    
    
  
    
    customParams.add(DisMaxParams.QF,defined_weights);
    params = SolrParams.wrapAppended(params, customParams);
    return new ExtendedDismaxQParser(query, localParams, params, req);
  }
  
  
    

  
  
  
}