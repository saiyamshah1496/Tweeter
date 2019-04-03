/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lucene;
import java.io.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import static sun.security.krb5.Confounder.bytes;

/**
 *
 * @author DELL
 */
public class Lucene {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        
        String indexLocation="C:\\Users\\DELL\\Desktop\\index";
        HashMap<String,LinkedList<Integer>> invertedIndex= new HashMap<>();
        File indexFile=new File(args[0]);
        Directory indexDirectory = FSDirectory.open(indexFile.toPath());
        IndexReader indexReader = DirectoryReader.open(indexDirectory);
        Fields indexFields= MultiFields.getFields(indexReader);
        for (String field : indexFields) {
          
            if(!field.equals("id"))
            {
                
            Terms indexTerms = indexFields.terms(field);
            TermsEnum termsEnum = indexTerms.iterator();
            int count=0;
            BytesRef term=null;
            while ((term= termsEnum.next()) != null) {
                 count++;
                 
                PostingsEnum postings= MultiFields.getTermDocsEnum(indexReader, field, term , PostingsEnum.FREQS);
                LinkedList<Integer> documentIdList = new LinkedList<>();
                String termInUtf = term.utf8ToString();
                
                int docId;
                while (postings.nextDoc() != PostingsEnum.NO_MORE_DOCS)
					{
						docId = postings.docID();
						documentIdList.add(docId);
						
					}
               
                
                invertedIndex.put(termInUtf, documentIdList);
                
                
            }
         
           
        }
        }
     
        BufferedWriter outputFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"UTF-8"));
        String inputString = "ateint feme";
   //     String[] queryTerms = inputString.split("\\s+", -1);
        
        
        
        Reader inputFileReader=new InputStreamReader(new FileInputStream(args[2]), Charset.forName("UTF-8"));//reading the input file
        BufferedReader inputFile=new BufferedReader(inputFileReader);
        skip(inputFileReader);
        String lineQuery=null;
        while((lineQuery=inputFile.readLine())!=null)
        {
          
            String[] queryTerms=lineQuery.split("\\s+", -1);
            getPostings(queryTerms, invertedIndex, outputFile ); 
            termAtATimeAnd(queryTerms, invertedIndex, outputFile ); 
            termAtATimeOr(queryTerms, invertedIndex, outputFile ); 
      //      DocumentAtATimeAnd(queryTerms, invertedIndex, outputFile);
      //      DocumentAtATimeOr(queryTerms, invertedIndex, outputFile);
            DocumentAtATimeAnd(queryTerms, invertedIndex, outputFile);
            DocumentAtATimeOr(queryTerms, invertedIndex, outputFile);
            
        }
        
        indexReader.close();
        outputFile.close();
        inputFileReader.close();
        inputFile.close();
            
        // TODO code application logic here
    }

    
    public static void getPostings(String[] queryTerms, HashMap<String,LinkedList<Integer>> invertedIndex, BufferedWriter outputFile) throws IOException
    {
        for(int termIterator=0;termIterator<queryTerms.length;termIterator++)
        {

      String queryTerm=queryTerms[termIterator];
     
      LinkedList<Integer> documents = new LinkedList<>();
      documents=invertedIndex.get(queryTerm);
      outputFile.write("GetPostings");
      outputFile.write("\r\n");
      outputFile.write(queryTerm);
      outputFile.write("\r\n");
   
    
    if(queryTerms.length==0)
    outputFile.write("empty");
    else
    {
     outputFile.write("Postings list: ");
      for(int i=0;i<documents.size();i++)
      {
         
          outputFile.write(documents.get(i) + " ");
      }
    }
      if(termIterator!=queryTerms.length-1)
      outputFile.write("\r\n");
        }
    }
    
    
    public static void termAtATimeOr(String[] queryTerms, HashMap<String,LinkedList<Integer>> invertedIndex, BufferedWriter outputFile) throws IOException 
    {
      
        LinkedList<Integer> intermediateResults= new LinkedList<>();
        
        intermediateResults=invertedIndex.get(queryTerms[0]);
        int size1,size2;
        int numberOfComparisonsTAATOr=0;
       
        for(int i=1;i<queryTerms.length;i++)
        {
            LinkedList<Integer> finalResults= new LinkedList<>();
            size1=0;
            size2=0;
            String queryTerm=queryTerms[i];
            LinkedList<Integer> queryTermList= new LinkedList<>();
            queryTermList=invertedIndex.get(queryTerm);

           
            while(intermediateResults != null && queryTermList  !=null && size1<intermediateResults.size() && size2< queryTermList.size())
            {
         
                if(Objects.equals(intermediateResults.get(size1), queryTermList.get(size2)))
                {
                   
                    finalResults.add(intermediateResults.get(size1));
                    size1++;
                    size2++;
                    numberOfComparisonsTAATOr++;
                   
                }
                
              else   if(intermediateResults.get(size1)<queryTermList.get(size2))
			{
                           
				finalResults.add(intermediateResults.get(size1));
				size1++;
				numberOfComparisonsTAATOr++;
                                  
                                
			}
			
              else if(intermediateResults.get(size1)>queryTermList.get(size2)) 
                    
			{
                           
				finalResults.add(queryTermList.get(size2));
				size2++;
				numberOfComparisonsTAATOr++;
                                 
			}

                            
             if(size1<intermediateResults.size() && size2>=queryTermList.size())
            {
                
                while(size1<intermediateResults.size()){
			finalResults.add(intermediateResults.get(size1));
			size1++;
                          
		}
            }
                         if(size1>=intermediateResults.size() && size2<queryTermList.size())
            {
                
		while(size2<queryTermList.size()){
			finalResults.add(queryTermList.get(size2));
			size2++;
                     
		}
            }
                            
               
               
            }

      
      
        intermediateResults=finalResults;
        }

 
       outputFile.write("\r\n" + "TaatOr" + "\r\n");
        for(String queryTerm : queryTerms)
        {
           
                  outputFile.write(queryTerm + " ");
                
        }
        
        outputFile.write("\r\n");
        outputFile.write("Results: ");
        if(intermediateResults.size()==0 || queryTerms.length==0)
            outputFile.write("empty");
        else
        {
        for (int i = 0; i < intermediateResults.size(); i++) {
  
            outputFile.write(intermediateResults.get(i) + " ");
        }
        }
        outputFile.write("\r\n" + "Number of documents in results: " + intermediateResults.size() + "\r\n");
        outputFile.write("Number of comparisons: " + numberOfComparisonsTAATOr);
        
                  
                  
    }
    
    
    
     public static void termAtATimeAnd(String[] queryTerms, HashMap<String,LinkedList<Integer>> invertedIndex, BufferedWriter outputFile) throws IOException 
    {
              

        LinkedList<Integer> intermediateResults= new LinkedList<>();
        if(queryTerms.length!=0)
        intermediateResults=invertedIndex.get(queryTerms[0]);
        int size1,size2;
        int numberOfComparisonsTAATAnd=0;
        for(int j=1;j<queryTerms.length;j++)
        {
            size1=0;
            size2=0;
             LinkedList<Integer> finalResults= new LinkedList<>();
                    String queryTerm=queryTerms[j];
                    LinkedList<Integer> queryTermList= new LinkedList<>();
                    queryTermList=invertedIndex.get(queryTerm);
                    int queryTermSkip = (int) Math.floor(Math.sqrt(Double.valueOf(queryTermList.size())));
                    boolean containsSkipQuery[] = new boolean[queryTermList.size()];
                    boolean containsSkipIntermediate[] = new boolean[intermediateResults.size()];
                    int intermediateTermSkip = (int) Math.floor(Math.sqrt(Double.valueOf(intermediateResults.size())));
                    
                    for(int i=0;i<queryTermList.size();i++)
                    {
                        if( queryTermList.size()> 3 && i%queryTermSkip==0)
                        {
                            containsSkipQuery[i] = true;
                        }
                        else
                            containsSkipQuery[i] = false;
                    }
                    
                    
                    for(int i=0;i<intermediateResults.size();i++)
                    {
                        if( intermediateResults.size()> 3 && i%intermediateTermSkip==0)
                        {
                            containsSkipIntermediate[i] = true;
     
                        }
                        else
                            containsSkipIntermediate[i] = false;
                    }
             int k=0;       
            while(intermediateResults != null && queryTermList  !=null && size1<intermediateResults.size() && size2< queryTermList.size())
            {
    System.out.println(intermediateResults.get(size1) + " " + queryTermList.get(size2));
                if(Objects.equals(intermediateResults.get(size1), queryTermList.get(size2)))
                {
                    System.out.println("here");
                    finalResults.add(intermediateResults.get(size1));
                    
                    size1++;
                    size2++;
                    numberOfComparisonsTAATAnd++;
                }
                
                else if(intermediateResults.get(size1)<queryTermList.get(size2))
			{
                            numberOfComparisonsTAATAnd++;
                          
                             if(containsSkipIntermediate[size1] == true)
                             {
                                 if((size1+intermediateTermSkip) < intermediateResults.size())
                                 {
                                    
                                       numberOfComparisonsTAATAnd++;
                                       if(intermediateResults.get(size1+intermediateTermSkip)< queryTermList.get(size2)  )
                                       {
                                           
                                           size1=size1+intermediateTermSkip;
                                           
                                       }
                                       
                                       else
                                       {
                                           size1++; 
                                           k++;
                                       }
                                 }
                                 
                                 else
                                      size1++; 
                                 k++;
                             }
                                                                   
                            
                                 else
                             {
                                 size1++;
                             }
			}
			
                else
                {
                    numberOfComparisonsTAATAnd++;
			
                          
                             if(containsSkipQuery[size2] == true)
                             {
                                 if((size2+queryTermSkip) < queryTermList.size())
                                 {
                                        numberOfComparisonsTAATAnd++;
                                     if(queryTermList.get(size2+queryTermSkip)< intermediateResults.get(size1))
                                     {
                                         size2=size2+queryTermSkip;
                                          
                                     }
                                     else
                                         size2++;
                                 }
                                 else
                                     size2++;
                             }
                                     
                                    
				
                             else
				size2++;
				
			}

                
            }
            intermediateResults=finalResults;
            System.out.println(intermediateResults);
        }
        
        
        outputFile.write("\r\n" + "TaatAnd" + "\r\n");
        for(String queryTerm : queryTerms)
        {
                  outputFile.write(queryTerm + " ");
                
        }
        
        outputFile.write("\r\n");
        outputFile.write("Results: ");
        if(intermediateResults.size()==0 || queryTerms.length==0)
            outputFile.write("empty");
        else
        {
        for (int i = 0; i < intermediateResults.size(); i++) {
            outputFile.write(intermediateResults.get(i) + " ");
        }
        }
        outputFile.write("\r\n" + "Number of documents in results: " + intermediateResults.size() + "\r\n");
        outputFile.write("Number of comparisons: " + numberOfComparisonsTAATAnd);
        
                  
                  
    }
     
     
     
    public static void DocumentAtATimeOr(String[] queryTerms, HashMap<String,LinkedList<Integer>> invertedIndex, BufferedWriter outputFile) throws IOException 
    {
              

        int numberOfComparisonsDaatOr=0;
        ArrayList<LinkedList<Integer>> parallelPostings = new ArrayList<>();
        for(String queryTerm: queryTerms)
        {
            LinkedList<Integer> queryPosting = invertedIndex.get(queryTerm);
            if(queryPosting!=null)
            parallelPostings.add(queryPosting);
        }
        int maximumSize = parallelPostings.get(0).size();
        int maxIndex = 0;
        ArrayList<Integer> minPointers= new ArrayList<Integer>();
        for(int i=0;i<parallelPostings.size();i++)
        {
            if(parallelPostings.get(i).size()> maximumSize)
            {
               
                maximumSize = parallelPostings.get(i).size();
                maxIndex= i;
            }
        }
        
        LinkedList<Integer> results = new LinkedList<Integer>();
        LinkedList<Integer> max = parallelPostings.get(maxIndex);
        LinkedList<Integer> temp = parallelPostings.get(0);

        int queryTermPostingPointers[]= new int[parallelPostings.size()];
        int queryTermPostingSize[]= new int[parallelPostings.size()];
        for(int j=0;j<parallelPostings.size();j++)
        {
            queryTermPostingSize[j]= parallelPostings.get(j).size();
            queryTermPostingPointers[j]=0;
        }
        boolean check=false;
        int pointerIterator=0,sizeIterator=0;
        int end=0;
        
         while(check==false)
         {
           
        for (int k = 0; k<parallelPostings.size(); k++)
			 {
                            
                                                          
				 if (queryTermPostingPointers[k] == queryTermPostingSize[k] ){
                                     end++;
					 check=true;
                                         break;
				 }
			 }
        
        
        
       if(check==false)
       {
           int r=0;
           
           int allEqual = 0;
           int sameValue=0;
			for (int i = 0; i < parallelPostings.size() - 1; i++) {
                     numberOfComparisonsDaatOr++;
                           System.out.println(parallelPostings.size());
				int a_Or = parallelPostings.get(i).get(queryTermPostingPointers[i]);
				int b_Or = parallelPostings.get(i + 1).get(queryTermPostingPointers[i + 1]);
				if (a_Or != b_Or) {
                                   
					
					allEqual = 0;
				
				} else {
                                      allEqual=1;
					//numberOfComparisonsDaatOr++;
                                        sameValue= parallelPostings.get(i).get(queryTermPostingPointers[i]);
				}
			}
           
           if(allEqual==1)
           {
               int docId= sameValue;
                boolean flag=true;
                   
                   for(int i=0; i<results.size(); i++){
							 
                       if(docId == results.get(i))
                       {
								 
                           flag = false;
                           
                        }
                   }
               if(flag==true)
               {
              
                   results.add(sameValue);
               }
               for (int p = 0; p < queryTermPostingPointers.length; p++) {
				queryTermPostingPointers[p]++;
				}
               
               
     
   
        }
           
           else
           {
            int minimum = parallelPostings.get(0).get(queryTermPostingPointers[0]);
               for(int i=0;i<parallelPostings.size();i++)
               {
               
               if(parallelPostings.get(i).get(queryTermPostingPointers[i]) < minimum)
               {
             
                   minimum= parallelPostings.get(i).get(queryTermPostingPointers[i]);
               }
               }
               
              
               
                for(int i=0;i<parallelPostings.size();i++)
               {
               
               if(parallelPostings.get(i).get(queryTermPostingPointers[i]) == minimum)
               {
                   minPointers.add(i);
               }
              
               boolean flag=true;
               for(int j=0; j<results.size(); j++){
							 
                       if(minimum == results.get(j))
                       {
								 
                           flag = false;
                           
                        }
                   }
               if(flag==true)
               {
                //   ++numberOfComparisonsDaatOr;
                   results.add(minimum);
               }
                  }
                
                for (int i = 0; i < minPointers.size(); ++i) {
			
					queryTermPostingPointers[minPointers.get(i)]++;
				}
               
           }
           
           
          
           
           
           
        
         }
       
       minPointers.clear();
         }
      
         if(check==true)
         {
         int count1;
          for (int i = 0; i < parallelPostings.size(); ++i) {
           
              
             //  if(queryTermPostingPointers[i] < queryTermPostingSize[i])
               //             count1++;
			int alreadyPrinted_Or = 0;
			while (queryTermPostingPointers[i] < queryTermPostingSize[i]) {
                               count1=0;
                             for (int k = 0; k < parallelPostings.size(); ++k) {
               if(queryTermPostingPointers[k] < queryTermPostingSize[k])
                            count1++;
               }
                           
                             if(count1>1)
                                    {
                                        for(int r=0;r<count1-1;r++)
                                        {
                                            numberOfComparisonsDaatOr++;   
                                        }
                                                      
                                }
				for (int j = 0; j < results.size(); j++) {
					
					int p = parallelPostings.get(i).get(queryTermPostingPointers[i]);
					int q = results.get(j);
					if (p == q) {
						alreadyPrinted_Or = 1;
						break;
					}
				}
				if (alreadyPrinted_Or == 0) {
                                   
					results.add(parallelPostings.get(i).get(queryTermPostingPointers[i]));
				}
				queryTermPostingPointers[i]++;
			}
		}
         }
         
         
         
         
         
         
         
         
         
         
         
         
          if(queryTerms.length==1)
        results=parallelPostings.get(0);
        
         
         
        Collections.sort(results);
        outputFile.write("DaatOr" + "\r\n");
        for(String queryTerm : queryTerms)
        {
                  outputFile.write(queryTerm + " ");
                
        }
        
        outputFile.write("\r\n");
        outputFile.write("Results: ");
        if(     results.isEmpty())
            outputFile.write("empty");
        
        
        else
        {
        for (int i = 0; i < results.size(); i++) {
            outputFile.write(results.get(i) + " ");
        }
        }
        outputFile.write("\r\n" + "Number of documents in results: " + results.size() + "\r\n");
        outputFile.write("Number of comparisons: " + numberOfComparisonsDaatOr + "\r\n");
        
                  
                  
    
    }
     
     
     
     
     
     
     
     
     
     
     
     
//     
//     
//        
//     
//     
//     
//      public static void DocumentAtATimeOr(String[] queryTerms, HashMap<String,LinkedList<Integer>> invertedIndex, BufferedWriter outputFile) throws IOException 
//    {
//                
//
//        int numberOfComparisonsDaatOr=0;
//        ArrayList<LinkedList<Integer>> parallelPostings = new ArrayList<>();
//        for(String queryTerm: queryTerms)
//        {
//            LinkedList<Integer> queryPosting = invertedIndex.get(queryTerm);
//            if(queryPosting!=null)
//            parallelPostings.add(queryPosting);
//        }
//        
//        int maximumSize = parallelPostings.get(0).size();
//        int maxIndex = 0;
//        for(int i=0;i<parallelPostings.size();i++)
//        {
//            if(parallelPostings.get(i).size()> maximumSize)
//            {
//                maximumSize = parallelPostings.get(i).size();
//                maxIndex= i;
//            }
//        }
//        
//        LinkedList<Integer> results = new LinkedList<>();
//        LinkedList<Integer> max = parallelPostings.get(maxIndex);
//        LinkedList<Integer> temp = parallelPostings.get(0);
//
//        int queryTermPostingPointers[]= new int[parallelPostings.size()];
//        int queryTermPostingSize[]= new int[parallelPostings.size()];
//        for(int j=0;j<parallelPostings.size();j++)
//        {
//            queryTermPostingSize[j]= parallelPostings.get(j).size();
//            queryTermPostingPointers[j]=0;
//        }
//        boolean check=false;
//        int pointerIterator=0,sizeIterator=0;
//        int end=0;
//        
//         while(check==false)
//         {
//            
//        for (int k = 0; k<parallelPostings.size(); k++)
//			 {
//				 if (queryTermPostingPointers[k] == queryTermPostingSize[k]){
//					 end++;
//				 }
//			 }
//        
//        if(end==parallelPostings.size() || end>parallelPostings.size())
//            check=true;
//        
//       if(check==false)
//       {
//           int r=0;
//           while(r<parallelPostings.size())
//           {
//              
//               if(queryTermPostingPointers[r] < queryTermPostingSize[r])
//               {
//                   int documentId= parallelPostings.get(r).get(queryTermPostingPointers[r]);
//                   boolean flag=true;
//                   
//                   for(int i=0; i<results.size(); i++){
//							 
//                       if(documentId == results.get(i))
//                       {
//								 
//                           flag = false;
//                           queryTermPostingPointers[r]++;
//                        }
//
//               }
//                   if(flag==true)
//                   {
//                       numberOfComparisonsDaatOr++;
//                      
//                       results.add(parallelPostings.get(r).get(queryTermPostingPointers[r]));			 
//                       queryTermPostingPointers[r]++;
//                   }
//           }
//               r++;
//       }
//           
//        
//        
//        }
//        
//         }
//        Collections.sort(results);
//        outputFile.write("\r\n" + "DaatOr" + "\r\n");
//        for(String queryTerm : queryTerms)
//        {
//                  outputFile.write(queryTerm + " ");
//                
//        }
//        
//        outputFile.write("\r\n");
//        outputFile.write("Results: ");
//        if(     results.isEmpty() || queryTerms.length==0)
//            outputFile.write("empty");
//        else
//        {
//        for (int i = 0; i < results.size(); i++) {
//            outputFile.write(results.get(i) + " ");
//        }
//        }
//        outputFile.write("\r\n" + "Number of documents in results: " + results.size() + "\r\n");
//        outputFile.write("Number of comparisons: " + numberOfComparisonsDaatOr + "\r\n");
//        
//                  
//                  
//    }
     
//    
//    @SuppressWarnings("empty-statement")
//    public static void DocumentAtATimeAnd(String[] queryTerms, HashMap<String,LinkedList<Integer>> invertedIndex, BufferedWriter outputFile) throws IOException 
//    {
//               
//
//        int numberOfComparisonsDaatAnd=0;
//        ArrayList<LinkedList<Integer>> parallelPostings = new ArrayList<>();
//        for(String queryTerm: queryTerms)
//        {
//            LinkedList<Integer> queryPosting = invertedIndex.get(queryTerm);
//            if(queryPosting!=null)
//            parallelPostings.add(queryPosting);
//        }
//        int minimumSize = parallelPostings.get(0).size();
//        int minIndex = 0;
//        for(int i=0;i<parallelPostings.size();i++)
//        {
//            if(parallelPostings.get(i).size()< minimumSize)
//            {
//                minimumSize = parallelPostings.get(i).size();
//                minIndex= i;
//            }
//        }
//        
//        LinkedList<Integer> results = new LinkedList<>();
//        LinkedList<Integer> max = parallelPostings.get(minIndex);
//        
//        boolean skipStatus[]= new boolean[parallelPostings.size()];
//        int queryTermSkipPostingSize[]= new int[parallelPostings.size()];
//        int x=0;
//        int count;
//        for(int i=0;i<parallelPostings.size();i++)
//        {
//            queryTermSkipPostingSize[i]= (int) Math.floor(Math.sqrt(Double.valueOf(parallelPostings.get(i).size())));;
//           
//        }
//       
//        
//        for(int i=0;i<parallelPostings.get(0).size();i++)
//        {
//            count=1;
//            int docId=parallelPostings.get(0).get(i);
//            for(int j=1;j<parallelPostings.size();j++)
//            {
//                
//                for(int k=x;k<parallelPostings.get(j).size();k++)
//                {
//                    
//                  
//                    numberOfComparisonsDaatAnd++;
//                   
//                    if(docId==parallelPostings.get(j).get(k))
//                    {
//                        count++;
//                       
//                        break;
//                    }
//                    
//                    else if(docId<parallelPostings.get(j).get(k)){
//                       
//						x=k;
//						break;
//					}
//                   
//
//                        
//                }
//                    
//            }
//            
//            if(count==parallelPostings.size())
//            {
//                results.add(parallelPostings.get(0).get(i));
//            }
//            
//        }
//         Collections.sort(results);
//        outputFile.write("\r\n" + "DaatAnd" + "\r\n");
//        for(String queryTerm : queryTerms)
//        {
//                  outputFile.write(queryTerm + " ");
//                
//        }
//        
//        outputFile.write("\r\n");
//        outputFile.write("Results: ");
//        if(     results.isEmpty() || queryTerms.length==0)
//            outputFile.write("empty");
//        else
//        {
//        for (int i = 0; i < results.size(); i++) {
//            outputFile.write(results.get(i) + " ");
//        }
//        }
//        outputFile.write("\r\n" + "Number of documents in results: " + results.size() + "\r\n");
//        outputFile.write("Number of comparisons: " + numberOfComparisonsDaatAnd);
//        
//
//        
//                  
//                  
//    }  
    
    
    
    
     public static void DocumentAtATimeAnd(String[] queryTerms, HashMap<String,LinkedList<Integer>> invertedIndex, BufferedWriter outputFile) throws IOException 
    {
              

        int numberOfComparisonsDaatAnd=0;
        ArrayList<LinkedList<Integer>> parallelPostings = new ArrayList<>();
        for(String queryTerm: queryTerms)
        {
            LinkedList<Integer> queryPosting = invertedIndex.get(queryTerm);
            if(queryPosting!=null)
            parallelPostings.add(queryPosting);
        }
        int maximumSize = parallelPostings.get(0).size();
        int maxIndex = 0;
        ArrayList<Integer> minPointers= new ArrayList<>();
        for(int i=0;i<parallelPostings.size();i++)
        {
            if(parallelPostings.get(i).size()> maximumSize)
            {
                maximumSize = parallelPostings.get(i).size();
                maxIndex= i;
            }
        }
        
        
        
        
        boolean skipStatus[][]= new boolean[parallelPostings.size()][];
        
        for(int i=0;i<parallelPostings.size();i++)
        {
            
               skipStatus[i] = new boolean[parallelPostings.get(i).size()];
            
        }
        int queryTermSkipPostingSize[]= new int[parallelPostings.size()];
        int x=0;
        int count;
        for(int i=0;i<parallelPostings.size();i++)
        {
            queryTermSkipPostingSize[i]= (int) Math.floor(Math.sqrt(Double.valueOf(parallelPostings.get(i).size())));;
         
        }
        
         for(int i=0;i<parallelPostings.size();i++)
                    {
                        for(int j=0;j<parallelPostings.get(i).size();j++)
                        {
                           
                            skipStatus[i][j] = parallelPostings.get(i).size()> 3 && j%queryTermSkipPostingSize[i]==0;
                        }
                        }
        
        
        
        
        
        
        LinkedList<Integer> results = new LinkedList<Integer>();
        LinkedList<Integer> max = parallelPostings.get(maxIndex);
        LinkedList<Integer> temp = parallelPostings.get(0);

        int queryTermPostingPointers[]= new int[parallelPostings.size()];
        int queryTermPostingSize[]= new int[parallelPostings.size()];
        for(int j=0;j<parallelPostings.size();j++)
        {
            queryTermPostingSize[j]= parallelPostings.get(j).size();
            queryTermPostingPointers[j]=0;
        }
        boolean check=false;
        int pointerIterator=0,sizeIterator=0;
        int end=0;
        
         while(check==false)
         {
           
        for (int k = 0; k<parallelPostings.size(); k++)
			 {
                            
                                                          
				 if (queryTermPostingPointers[k] == queryTermPostingSize[k] ){
					 check=true;
                                         break;
				 }
			 }
        
        
        
       if(check==false)
       {
           int r=0;
           
           int allEqual = 1;
           int sameValue=0;
			for (int i = 0; i < parallelPostings.size() - 1; i++) {
                      
                            
				int a_Or = parallelPostings.get(i).get(queryTermPostingPointers[i]);
				int b_Or = parallelPostings.get(i + 1).get(queryTermPostingPointers[i + 1]);
				if (a_Or != b_Or) {
                                   
					numberOfComparisonsDaatAnd++;
					allEqual = 0;
					break;
				} else {
                                      
					numberOfComparisonsDaatAnd++;
                                        sameValue= parallelPostings.get(i).get(queryTermPostingPointers[i]);
				}
			}
           
           if(allEqual==1)
           {
               int docId= sameValue;
                boolean flag=true;
                   
                   for(int i=0; i<results.size(); i++){
							 
                       if(docId == results.get(i))
                       {
								 
                           flag = false;
                           
                        }
                   }
               if(flag==true)
               {
              //     ++numberOfComparisonsDaatAnd;
                   results.add(sameValue);
               }
               for (int p = 0; p < queryTermPostingPointers.length; p++) {
				queryTermPostingPointers[p]++;
				}
               
               
     
   
        }
           
           else
           {
            int minimum = parallelPostings.get(0).get(queryTermPostingPointers[0]);
               for(int i=0;i<parallelPostings.size();i++)
               {
               
               if(parallelPostings.get(i).get(queryTermPostingPointers[i]) < minimum)
               {
                   minimum= parallelPostings.get(i).get(queryTermPostingPointers[i]);
               }
               }
               
               
               
                int maximum = parallelPostings.get(0).get(queryTermPostingPointers[0]);
                
               for(int i=0;i<parallelPostings.size();i++)
               {
               
              
               if(parallelPostings.get(i).get(queryTermPostingPointers[i]) > maximum)
               {
                   
                   maximum= parallelPostings.get(i).get(queryTermPostingPointers[i]);
               }
               }
              
               
                for(int i=0;i<parallelPostings.size();i++)
               {
               
               if(parallelPostings.get(i).get(queryTermPostingPointers[i]) == minimum)
               {
                 if(skipStatus[i][queryTermPostingPointers[i]])
                 {
                    
                     int j=queryTermPostingPointers[i];
                     if(j+queryTermSkipPostingSize[i] < parallelPostings.get(i).size())
                     {
                         for(int k=0;k<parallelPostings.size()-1;k++){
                             numberOfComparisonsDaatAnd++;
                         }
                         if(parallelPostings.get(i).get(j+queryTermSkipPostingSize[i]) <  maximum)
                         {
                             
                             queryTermPostingPointers[i] = queryTermPostingPointers[i] + queryTermSkipPostingSize[i];
                         }
                         else
                         {
                             queryTermPostingPointers[i]++;
                         }   
                             
                     }
                     
                     else
                     {
                     queryTermPostingPointers[i]++;
                     }
                     
                     
                 }
                 
                 else 
                   queryTermPostingPointers[i]++;

                   
               }
                                  
                  }
               
           
           
          
           
           
           
        
         }
       
       minPointers.clear();
         }
         
         }
        if(queryTerms.length==1)
        results=parallelPostings.get(0); 
        Collections.sort(results);
        outputFile.write("\r\n" + "DaatAnd" + "\r\n");
        for(String queryTerm : queryTerms)
        {
                  outputFile.write(queryTerm + " ");
                
        }
        
        outputFile.write("\r\n");
        outputFile.write("Results: ");
        if(results.size()==0)
            outputFile.write("empty");
        else if(queryTerms.length==1)
        for (int i = 0; i < parallelPostings.get(0).size(); i++) {
            outputFile.write(parallelPostings.get(0).get(i) + " ");
        }
        
        else
        {
        for (int i = 0; i < results.size(); i++) {
            outputFile.write(results.get(i) + " ");
        }
        }
        outputFile.write("\r\n" + "Number of documents in results: " + results.size() + "\r\n");
        outputFile.write("Number of comparisons: " + numberOfComparisonsDaatAnd + "\r\n");
        
                  
                  
    
    
    
    
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

 
      
        public static void skip(Reader reader) throws IOException
	{
		
		char[] possibleBOM = new char[1];
		reader.read(possibleBOM);

		if (possibleBOM[0] != '\ufeff')
		{
			reader.reset();
		}
	}
      
      
      
      
      
      
      
      
      
      
      
}
