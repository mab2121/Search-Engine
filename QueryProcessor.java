import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

/**
 * @author mehran
 * This is the main class to do all the query processing
 */
public class QueryProcessor {
    
	private static final Object DISJUNCTIVE = "1";
	private static final Object CONJUNCTIVE = "2";

	public static List<ResultEntry> evaluate(String query, String type) throws IOException{
		
		  if(type.equals(QueryProcessor.CONJUNCTIVE)) {
			  return QueryProcessorDetail.conjunctive(query);
		  }
		  else if(type.equals(QueryProcessor.DISJUNCTIVE)) {
			  return QueryProcessorDetail.disjunctive(query);
		  }
		  return null;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		System.out.println("Start: " +  LocalTime.now());
		System.out.println("Initializing loader...");
		IndexLoader.initialize(Constants.page_table_path + "/page-table-final.bin", Constants.lexicon_path + "/lexicon-with-maxscore.bin","");
		System.out.println("Initializing loader -- DONE");
		System.out.println("End Loading: " +  LocalTime.now());
		
		
		Scanner scanner = new Scanner(System.in);
        while(true) {
        // Prompt the user for input
        System.out.println("Enter 1 for a query and 2 to exit: ");
 
        // Read the user's input
        String func = scanner.nextLine();
       
        if(func.equals("2")) break;
        
        if(func.equals("1")) {
        	System.out.println("Enter Query: ");
        	
        	String query = scanner.nextLine();
        	
        	System.out.println("Enter 1 for disjunctive(OR) and 2 for conjunctive (AND): ");
        	
        	String type = scanner.nextLine();
        	
        	System.out.println("Enter 1 for snippets and 2 for non-snippets: ");
        	
        	String snip = scanner.nextLine();
        	
        	if(snip.equals("1")) {
        		Constants.snippet_gen = true;
        	}
        	else if(snip.equals("2")) {
        		Constants.snippet_gen = false;
        	}
        	
        	long ts = System.currentTimeMillis();
        	List<ResultEntry> ans = evaluate(query,type);
        	long te = System.currentTimeMillis();
        	long elapsedTime = te - ts;
    		
    		for(int i = 0; i < ans.size(); i++) {
    			System.out.println(ans.get(i).printable());
    		}
    		System.out.println("Search Time(milliseconds): " +  elapsedTime);
        	ans.clear();
        }
        
        }
        // Close the Scanner to prevent resource leaks
        scanner.close();

	}

}
