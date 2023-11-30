import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
/**
 * Naive Snippet generator. Should be improved
 */
public class SnippetGeneratorNaive {

	static int lines_max = 3;
	
	public static String genSnippet(int docID, List<String> qi) {
		
		   HashMap<Integer,List<String>> frq = new HashMap<Integer,List<String>>();
		   StringTokenizer str = new StringTokenizer(TaskRunner.getDocContent(Constants.input_corpus_unclean_path,docID),"$$");
		   
		   while(str.hasMoreTokens()) {
			   String line = SEHelper.regexRemoval(str.nextToken());
			   int key = countReleventTokens(line,qi);
			   if(!frq.containsKey(key)) {
				   frq.put(key,new ArrayList<String>());
			   }
			   frq.get(key).add(line);
		   }
		   
		   StringBuilder sb = new StringBuilder();
		   List<Integer> keys = new ArrayList<Integer>();
		   keys.addAll(frq.keySet());
		   Collections.sort(keys);
		   int count = 0;
		   for(int i = keys.size() - 1; i >=0; i--) {
			   List<String> vals = frq.get(keys.get(i));
			   for(int j = 0; j < vals.size(); j++) {
				   sb.append(vals.get(j) + "\n");
				   count++;
				   if(count == lines_max) break;
			   }
			   if(count == lines_max) break;
		   }
		   
		   return sb.toString();		
	}

	private static int countReleventTokens(String line, List<String> qi) {
		int count = 0;
		for(int i = 0; i < qi.size(); i++) {
			if(line.contains(qi.get(i))) count++;
		}
		return count;
	}
	
}
