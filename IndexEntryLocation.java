import java.util.Comparator;

import indexer.IndexEntry;

public class IndexEntryLocation implements Comparable<IndexEntryLocation>{
    
	public IndexEntry ie;
	public int blockIdx;
	public int docIdx;
	public int N;
	
	public IndexEntryLocation(IndexEntry ie, int i, int j, int noOfDocs) {
		// TODO Auto-generated constructor stub
		this.ie = ie;
		this.blockIdx = i;
		this.docIdx = j;
		this.N = noOfDocs;
	}
	
	public int nextDocId() {
		int docNo = (blockIdx + 1)*128 + docIdx + 1;
		if(docNo >= ie.noOfDocs) return Integer.MAX_VALUE;
		int did = ie.docIds.get(blockIdx).get(docIdx);
		return did;	
	}
	
	public void next() {
		if(this.docIdx == 127) {
			blockIdx++;
			docIdx = 0;
		}
		else {
			docIdx++;
		}
	}

	public void reset() {
		// TODO Auto-generated method stub
		this.blockIdx = 0;
		this.docIdx = 0;
		
	}

	@Override
	public int compareTo(IndexEntryLocation o) {
		// TODO Auto-generated method stub
		if(this.ie.maxScore < o.ie.maxScore) return -1;
		else if(this.ie.maxScore > o.ie.maxScore) return 1;
		return 0;
	}
	
	public static class IELComparator implements Comparator<IndexEntryLocation> {
	    @Override
	    public int compare(IndexEntryLocation iel1, IndexEntryLocation iel2) {
	        return Integer.compare(iel1.N, iel2.N);
	    }
	}
	

}
