/**
 * a light class to handle page table entries
 */
public class PageTableEntry {

	public int docLength = 0;
	public String url = "";
	public long offset;
	
	PageTableEntry(long offset, int docLength, String url){
		this.offset = offset;
		this.docLength = docLength;
		this.url = url;
	}

	public PageTableEntry() {
		// TODO Auto-generated constructor stub
	}
	
	public String toString() {
		String x = "" + offset + "\t" + "\t" + docLength + "\t" + url;
		return x;
		
	}
	
}
