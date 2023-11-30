import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author mehran
 * This is a class with various static methods
 * to load a pre-computed compressed index,
 * lexicon and page table from disk into memory
 * lexicon and page table will be loaded completely
 * index entries will be loaded as needed.
 */
public class IndexLoader {

	    // docID -> URL mapping - in memory
		public static HashMap<Integer, PageTableEntry> pageTable = new HashMap<Integer, PageTableEntry>();

		// term -> begin offset in the disk based file
		// and the number of docs containing the word
		public static HashMap<String, String> lexicon = new HashMap<String, String>();
		
		// load a page table
		public static void initialize(String pageTablePath, String lexiconPath,String s) throws InterruptedException {

			    System.out.println("Page Table Loaded ...");
				loadPageTable(pageTablePath);
			
				loadLexicon(lexiconPath);

				System.out.println("Lexicon Loaded ...");   
		}
		
		public static long getFreeMemory() {
	        Runtime runtime = Runtime.getRuntime();
	        return runtime.freeMemory();
	    }

		private static void loadLexicon(String lexiconPath) {
			// TODO Auto-generated method stub
			HashMap<String, String> temp = lexicon;
	        try (BufferedReader reader = new BufferedReader(new FileReader(lexiconPath))) {
	            String line;
	            int lineNumber = 0;
                String key = "";
	            while ((line = reader.readLine()) != null) {
	            	//if(lineNumber < 30000000) {
	            		//lineNumber++;
	            		//continue;
	            	//}
	            	//if(lineNumber%10000 == 0) System.out.println(lineNumber);
	                if (lineNumber % 2 == 0) {
	                    // add key to the lexicon
	                	//lexicon.put(line, new long[2]);
	                	key = line;
	                } else {
	                    try {
	                    	//StringTokenizer str = new StringTokenizer(line);
	                    	//Long fileOffset = Long.parseLong(str.nextToken());
	                    	//Long NoOfDocs = Long.parseLong(str.nextToken());
	                    	
	                    	lexicon.put(key,line);
	                    	//lexicon.get(key)[0] = fileOffset;
	                    	//lexicon.get(key)[1] = NoOfDocs;
	                    } catch (NumberFormatException e) {
	                        System.err.println("Error parsing long from line " + lineNumber + ": " + line);
	                    }
	                }
	                lineNumber++;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
		}
		
		
		

		private static void loadPageTable(String pageTablePath) {
			// TODO Auto-generated method stub
			HashMap<Integer, PageTableEntry> temp = pageTable;
			try (BufferedReader reader = new BufferedReader(new FileReader(pageTablePath))) {
	            String line;
	            int lineNumber = 0;
                Integer key = -1;
                
	            while ((line = reader.readLine()) != null) {
	                if (lineNumber % 2 == 0) {
	                    // add key to the page table
	                	key = Integer.parseInt(line);
	                	//pageTable.put(key,"");
	                } else {
	                    try {
	                    	StringTokenizer str = new StringTokenizer(line);
	                    	PageTableEntry pte = new PageTableEntry();
	                    	pte.offset = str.hasMoreTokens() ? Long.parseLong(str.nextToken()) : 0;
	                    	//pte.line_end = str.hasMoreTokens() ?Long.parseLong(str.nextToken()) : 0;
	                    	pte.docLength = str.hasMoreTokens() ? Integer.parseInt(str.nextToken()) : 0;
	                    	pte.url = str.hasMoreTokens() ? str.nextToken() : "";
	                    	pageTable.put(key,pte);
	                    } catch (Exception e) {
	                        System.err.println("Error parsing long from line " + lineNumber + ": " + line);
	                    }
	                }
	                lineNumber++;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	
}
