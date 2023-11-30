import java.util.ArrayList;
import java.util.List;

/**
 * @author mehran
 * Search Engine result Entry
 */
public class ResultEntry {

	public String url;
	public String snippet;
	public double score;
	public int docID;
	public List<String> qi = new ArrayList<String>();
	
	ResultEntry(String url, String snippet, double score2,int did,List<String> qi ){
		this.url = url;
		this.snippet = snippet;
		this.score = score2;
		this.docID = did;
		this.qi = qi;
	}
	
	public String printable() {
		StringBuilder sb = new StringBuilder();
		sb.append("------------------------------------------------------------------------------------------\n");
		sb.append("URL: " + this.url + "  ||  ");
		sb.append("Score: " + this.score + "\n" + "");
		if(Constants.snippet_gen) sb.append(SnippetGeneratorNaive.genSnippet(docID, qi) + "\n");
		//if(this.snippet != null && !this.snippet.equals("")) sb.append(this.snippet + "\n");
		
		sb.append("------------------------------------------------------------------------------------------\n");
		return sb.toString();
	}
	
	
}
