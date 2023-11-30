import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author mehran
 * A one-off class to run miscellenous
 * tasks that can be run standalone
 * like re-creating the page table with some 
 * modifications, re-creating index etc.
 * After some QA, this can be merged in the
 * main code
 */
public class TaskRunner {
	
	static HashMap<Integer,PageTableEntry> pageTableMap = new HashMap<Integer,PageTableEntry>();
	static String line_sep = " $$ ";

	public static void main(String [] args) throws Exception {
		
		
		String toCompress = Constants.temp_work_dir + "/index-level-" + 8 + "/0.bin";
		compressIndexFileWithScore(toCompress);
	}
	
	/**
     * logs the lexicon and also stores pre-computed
     * bm 25 score rather than frequencies - for max score implementation
     * @param filename
     * @param off
     * @return
     * @throws IOException
	 * @throws InterruptedException 
     */
    public static void compressIndexFileWithScore(String filename) throws IOException, InterruptedException {
    	IndexLoader.initialize(Constants.page_table_path + "/page-table-final.bin", Constants.lexicon_path + "/lexicon.bin","his");
		 File file = new File(filename); //input
	        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	          String line; int i = 0; String key ="";               
	          while ((line = br.readLine()) != null) {
	        	  if(i%1000000 == 0) System.out.println(i);
	        	  if(i%2 == 0) { // this is a key
	        		  key = line;        		  
	                  i++;	                
	                  continue;
	                  
	        	  }
	        	  else {
	        		 
	        		  IndexEntry ie = IndexEntry.createFromLine(line);
	        		  ie.computeImpactScores(key);
	        		  
	        	  }
	     
	        	  i++;
	          }
	          
	          logNewLexiconWithScores();
	          System.out.println("Compressed");
	          
	          } catch(Exception e) {
	        	  e.printStackTrace();
	          }
	        
	 }
    


	private static void logNewLexiconWithScores() {
		// TODO Auto-generated method stub
		 HashMap<String,String> lexicon = IndexLoader.lexicon;
		 String outFilePath = Constants.lexicon_path + "/lexicon-with-maxscore.bin";
	     List<String> terms = new ArrayList<>();
	     terms.addAll(lexicon.keySet());

	        try (FileOutputStream fileOutputStream = new FileOutputStream(outFilePath,true)) {
	            // Convert the string to bytes and write it to the file
	        	for (String term: terms) {
	        		 String termw = term;
	        		 term = term + "\n";
	        		 byte[] bytes = term.getBytes();
	                 fileOutputStream.write(bytes);
	                 String lexiconData = lexicon.get(termw) + "\n";
	                 bytes = lexiconData.getBytes();
	                 fileOutputStream.write(bytes);
	        	 }
	             System.out.println("Lexicon has been written as binary to the file. - along with maxscores");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
    

	public static String getDocContent(String filePath,int docID) {
		long offset = IndexLoader.pageTable.get(docID).offset;
		//long offset = 10222925725L;

		StringBuilder sb = new StringBuilder();
		//long endLine = IndexLoader.pageTable.get(docID).line_end;    
	    try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r")) {
	        randomAccessFile.seek(offset);
            while(true) {
            	String s = randomAccessFile.readLine();
            	if(isEndDocument(s)) break;
            	sb.append(s + TaskRunner.line_sep + "\n");
           }
	      
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return sb.toString();
	}
    
	/*
	 * Recored offsets using buffered reader - less control , faster
	 */

    private static void recordLineOffsets2(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            long offset = 0;
            String line;
            //long offset = 0;
            //String line;
            int line_num = 0;
            PageTableEntry pte = new PageTableEntry();
            int docId = 0; String url = ""; int docLength = 0; long corpusLength = 0;
            while ((line = reader.readLine()) != null) {
            	if(line_num%10000 == 0) System.out.println(line_num);
            	line_num++;
            	if(isNewDocument(line)) {
            		docId++;
            		offset = offset + line.getBytes().length + 1;
            		line = reader.readLine();
            		pte = new PageTableEntry();
            		pte.offset = offset;
            		offset = offset + line.getBytes().length + 1;
            		continue;	
            	}
            	else if(isNewURL(line)) {
            		 offset = offset + line.getBytes().length + 1;
	        		 url = reader.readLine();
	        		 offset = offset + url.getBytes().length + 1;
	        		 pte.offset = offset;
	        		 pte.url = url;
	        		 docLength = 0;
	        		 continue;
	        	 }
            	else if(isEndDocument(line)) {
            		 offset = offset + line.getBytes().length + 1;
            		 line = reader.readLine();
            		 offset = offset + line.getBytes().length + 1;
	        		 pte.docLength = docLength;
	        		 pageTableMap.put(docId,pte);
	        		 continue;
	        	 }
            	 offset = offset + line.getBytes().length + 1;
            	 long linelength = line.split("\\s+").length;
	        	 docLength += linelength;
	        	 corpusLength += linelength;
            }
            System.out.println("Avg Doc length: " + corpusLength/docId);
            logPageTable(pageTableMap);
        }
    }
    
    
    private static void recordLineOffsets3(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            long offset = 0;
            String line;
            //long offset = 0;
            //String line;
            int line_num = 0;
            PageTableEntry pte = new PageTableEntry();
            int docId = 0; String url = ""; int docLength = 0; long corpusLength = 0;
            while ((line = reader.readLine()) != null) {
            	if(line_num%10000 == 0) System.out.println(line_num);
            	line_num++;
            	if(isNewDocument(line)) {
            		docId++;
            		//offset = offset + line.getBytes().length + 1;
            		//line = reader.readLine();
            		pte = new PageTableEntry();
            		//pte.offset = offset;
            		//offset = offset + line.getBytes().length + 1;
            		//continue;	
            	}
            	else if(isNewURL(line)) {
            		 offset = offset + line.getBytes().length + 1;
	        		 url = reader.readLine();
	        		 offset = offset + url.getBytes().length + 1;
	        		 pte.offset = offset;
	        		 pte.url = url;
	        		 docLength = 0;
	        		 continue;
	        	 }
            	
            	 offset = offset + line.getBytes().length + 1;
            	 long linelength = line.split("\\s+").length;
	        	 docLength += linelength;
	        	 corpusLength += linelength;
	        	 if(isEndDocument(line)) {
            		 //offset = offset + line.getBytes().length + 1;
            		 //line = reader.readLine();
            		 //offset = offset + line.getBytes().length + 1;
	        		 pte.docLength = docLength;
	        		 pageTableMap.put(docId,pte);
	        		 //continue;
	        	 }
            }
            System.out.println("Avg Doc length: " + corpusLength/docId);
            logPageTable(pageTableMap);
        }
    }
    
    
    
    /**
	 * Record offsets using RandomAccessFile - more control, slower
	 * @param filePath
	 */
	private static void recordLineOffsets(String fileName) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(fileName, "r")) {
            long offset = 0;
            String line;
            int line_num = 0;
            PageTableEntry pte = new PageTableEntry();
            int docId = 0; String url = ""; int docLength = 0; long corpusLength = 0;
            while ((line = file.readLine()) != null) {
            	if(line_num%10000 == 0) System.out.println(line_num);
            	line_num++;
            	if(isNewDocument(line)) {
            		docId++;
            		offset = file.getFilePointer();
            		line = file.readLine();
            		pte = new PageTableEntry();
            		pte.offset = offset;
            		offset = file.getFilePointer();
            		continue;	
            	}
            	else if(isNewURL(line)) {
            		 offset = file.getFilePointer();
	        		 url = file.readLine();
	        		 offset = file.getFilePointer();
	        		 pte.offset = offset;
	        		 pte.url = url;
	        		 docLength = 0;
	        		 continue;
	        	 }
            	else if(isEndDocument(line)) {
            		 offset = file.getFilePointer();
	        		 pte.docLength = docLength;
	        		 pageTableMap.put(docId,pte);
	        		 continue;
	        	 }
            	 offset = file.getFilePointer();
            	 long linelength = line.split("\\s+").length;
	        	 docLength += linelength;
	        	 corpusLength += linelength;
            	 
            }
            System.out.println("Avg Doc length: " + corpusLength/docId);
            logPageTable(pageTableMap);
        }
    }
	    
	 
	 private static void logPageTable(HashMap<Integer,PageTableEntry> pageTable) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			String outFilePath = Constants.page_table_path + "/page-table-final.bin";
			List<Integer> terms = new ArrayList<>();
			terms.addAll(pageTable.keySet());
			// sort the terms lexicographically
		    //Collections.sort(terms);

		    try (FileOutputStream fileOutputStream = new FileOutputStream(outFilePath)) {
				// Convert the string to bytes and write it to the file
				for (Integer term: terms) {
				        String next_term = "" + term + "\n";
				        byte[] bytes = next_term.getBytes();
				        fileOutputStream.write(bytes);
				        PageTableEntry pte = pageTable.get(term);
				        String posting = pte.toString()  + "\n";
				        bytes = posting.getBytes();
				        fileOutputStream.write(bytes);
				}
				System.out.println("Page Table has been written to the file.");
			} catch (IOException e) {
			    e.printStackTrace();
			}
		}
	 
		private static boolean isNewDocument(String token) {
			// TODO Auto-generated method stub
			if(token.contains("<DOC>")) return true;
			return false;
		}

		private static boolean isNewURL(String token) {
			// TODO Auto-generated method stub
			if(token.contains("<TEXT>")) return true;
			return false;
		}

		private static boolean isEndDocument(String token) {
			// TODO Auto-generated method stub
			//if(token.contains("< text>")) return true;
			if(token.contains("</TEXT>")) return true;
			return false;
		}

	
}
