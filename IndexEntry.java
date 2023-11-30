// @author : mehran ali banka
// This is an object to hold a inverted index posting
// in memory temporarily

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;



public class IndexEntry {
	
	 /**
	  *  reduce the docIds in the block by storing
	  *  the difference from the previous docId in the block
	  */
	 public void reduceDocIDSize() {
		 
		 for(int i = 0; i < docIds.size(); i++) {
			 List<Integer> nextBlock = docIds.get(i);
			 int base = nextBlock.get(0);
			 for(int j = 1; j < nextBlock.size(); j++) {
				 int temp = nextBlock.get(j);
				 nextBlock.set(j, nextBlock.get(j) - base);
				 base = temp;
			 }
		 } 
	 }
	
      
	  public IndexEntry() {
		  this.lastBlockIds = new ArrayList<Integer>();
		  this.docIds = new ArrayList<List<Integer>>();
		  this.frequencies = new ArrayList<List<Integer>>();
	  }
	  
	  /**
	   * 
	   * @param posting
	   * @return
	   * Creates a Inverted Index object from a posting string
	   */
	  public static IndexEntry createFromLine(String posting) {
		  IndexEntry ie = new IndexEntry();
		  StringTokenizer str = new StringTokenizer(posting);
		  int blockSize = Integer.parseInt(str.nextToken());
		  int noOfBlocks = Integer.parseInt(str.nextToken());
		  int noOfDocs = Integer.parseInt(str.nextToken());
		  ie.blockSize = blockSize;
		  ie.noOfBlocks = noOfBlocks;
		  ie.noOfDocs = noOfDocs;
		  
		  
		  int fullBlocks = noOfDocs/blockSize;
		  int halfBlockSize = noOfDocs%blockSize;
		  int halfBlocks = halfBlockSize > 0 ? 1 : 0;
		  
		  int blocksPushed = 0;
		  
		  while(blocksPushed < fullBlocks) {
			  int i = 0;
			  List<Integer> d = new ArrayList<Integer>();
			  List<Integer> f = new ArrayList<Integer>();
			  while(i < blockSize && str.hasMoreTokens()) {
				  d.add(Integer.parseInt(str.nextToken()));
				  i++;
			  }
			  i = 0;
			  while(i < blockSize && str.hasMoreTokens()) {
				  f.add(Integer.parseInt(str.nextToken()));
				  i++;
			  }
			  ie.docIds.add(d);
			  ie.frequencies.add(f);
			  blocksPushed++;
		  }
		  
		  blocksPushed = 0;
		  while(blocksPushed < halfBlocks) {
			  int i = 0;
			  List<Integer> d = new ArrayList<Integer>();
			  List<Integer> f = new ArrayList<Integer>();
			  while(i < halfBlockSize && str.hasMoreTokens()) {
				  d.add(Integer.parseInt(str.nextToken()));
				  i++;
			  }
			  i = 0;
			  while(i < halfBlockSize && str.hasMoreTokens()) {
				  f.add(Integer.parseInt(str.nextToken()));
				  i++;
			  }
			  ie.docIds.add(d);
			  ie.frequencies.add(f);
			  blocksPushed++;
		  }
		  ie.current_list_idx = ie.docIds.size() - 1;
		  //ie.lastDocIdAbs = ie.docIds.get(ie.docIds.size() - 1).get(0);
		 // List<Integer> lastList = ie.docIds.get(ie.docIds.size() - 1);
		 // for(int i = 1; i < lastList.size(); i++) {
			//  ie.lastDocIdAbs += lastList.get(i);
		//  }
		  
	      return ie;  
	  }
	  
	
	  	  
	  // returns the string to add to file, when this posting is written
	  // not compressed at this stage
	  public String toFileFormat() {
		  
		  StringBuilder sb = new StringBuilder();
		  sb.append(blockSize + "\t");
		  sb.append(noOfBlocks + "\t");
		  sb.append(noOfDocs + "\t");
		  
		  for(int i = 0; i < docIds.size(); i++) {
			  
			  List<Integer> docList = docIds.get(i);
			  List<Integer> freqList = frequencies.get(i);
			  for(int j = 0; j < docList.size(); j++) {
				  sb.append(docList.get(j) + "\t");
			  }
			  for(int j = 0; j < freqList.size(); j++) {
				  sb.append(freqList.get(j) + "\t");
			  }  
		  }
		  
		  sb.append("\n");
		  return new String(sb);
	  }
	  /**
	   * merge two Index Entries
	   * @param other
	   * @return
	   */
	  
	  public IndexEntry mergePosting(IndexEntry other) {
		  // other should be the bigger one
		  List<Integer> d1 = this.docIds.get(current_list_idx);
		  List<Integer> f1 = this.frequencies.get(current_list_idx);
		  
		  List<List<Integer>> d2 = other.docIds;
		  List<List<Integer>> f2 = other.frequencies;
		  
		  for(int i = 0; i < d2.size(); i++) {
			  List<Integer> d2d = d2.get(i);
			  List<Integer> f2f = f2.get(i);
			  
			  for(int j = 0; j < d2d.size(); j++) {
				  // case of incomplete previous merge
				  if(d1.get(d1.size() -1) == d2d.get(j)) {
					  f1.set(f1.size() - 1, f1.get(f1.size() - 1) + f2f.get(j));
					  continue;
				  }
				  
				  if(d1.size() < this.blockSize) {
					  d1.add(d2d.get(j));
					  f1.add(f2f.get(j));
					  noOfDocs++; 
					  lastDocIdAbs = d2d.get(j);
				  }
				  else {
					  docIds.add(new ArrayList<Integer>());
					  frequencies.add(new ArrayList<Integer>());
					  current_list_idx++;
					  d1 = this.docIds.get(current_list_idx);
					  f1 = this.frequencies.get(current_list_idx);
					  d1.add(d2d.get(j));
					  f1.add(f2f.get(j));
					  noOfBlocks++;
					  noOfDocs++; 
					  lastDocIdAbs = d2d.get(j);
				  }
			  }	  
		  }
		  
		  return this;
	  }
	  
	  /**
	   * Add a docId to this IndexEntry
	   * @param docId
	   */
	  public void addDocID(Integer docId) {
		  //check if its already there
		  if(noOfDocs != 0) { // list have been initialized
			  List<Integer> curr = docIds.get(current_list_idx);
			  List<Integer> freq = frequencies.get(current_list_idx);
			  if(lastDocIdAbs == docId) {
				  int n = freq.size() -1;
				  freq.set(n, freq.get(n) + 1);
				  return;
			  }
			  else {
				  //check if there is capacity
				  if(curr.size() < blockSize) {
					  curr.add(docId); // store diff
					  freq.add(1);
					  noOfDocs++;
				  }
				  else { // initialize new list
					  lastBlockIds.add(curr.get(curr.size() - 1)); // record last doc ID
					  docIds.add(new ArrayList<Integer>());
					  frequencies.add(new ArrayList<Integer>());
					  noOfBlocks++;
					  noOfDocs++;
					  current_list_idx++;
					  docIds.get(current_list_idx).add(docId);
					  frequencies.get(current_list_idx).add(1);
				  }
			  } 
		  }
		  
		  else { // class is empty
			  docIds.add(new ArrayList<Integer>());
			  frequencies.add(new ArrayList<Integer>());
			  noOfBlocks++;
			  noOfDocs++;
			  docIds.get(current_list_idx).add(docId);
			  frequencies.get(current_list_idx).add(1);	  
		  }
		  
		  lastDocIdAbs = docId;
		 // lastBlockIds.set(noOfBlocks -1, docId); // set last block Id
	  }
	  
	  
	  public int blockSize = Constants.posting_block_size;
	  
	  List<Integer> lastBlockIds;
	  
	  // each list is of max size = blockSize
	  List<List<Integer>> docIds;
	  
	  // each list is of max size = blockSize
	  List<List<Integer>> frequencies;
	  
	  // no of docs in this inverted list
	  int noOfDocs = 0;
	  
	  // which doc/freq list is being used currently
	  int current_list_idx = 0;
	  
	  // no of blocks
	  int noOfBlocks = 0;
	  
	  // abs value of the last docID
	  int lastDocIdAbs;
	  
	  //max score of this index entry
	  public double maxScore = 0;

	public void computeImpactScores(String key) throws InterruptedException {
		// TODO Auto-generated method stub
		// load page table from index loader
		
		double maxScore = 0;
		for(int i = 0; i < this.noOfBlocks; i++) {
			List<Integer> nextBlock = this.docIds.get(i);
			for(int j = 0; j < nextBlock.size(); j++) {
				double score = BM25Score(i,j);
				maxScore = Math.max(maxScore, score);				
			}
		}
		if(IndexLoader.lexicon.containsKey(key)) {
			String oldVal = IndexLoader.lexicon.get(key);
			IndexLoader.lexicon.put(key, oldVal + "\t" + maxScore);
		}
	}
	
	private  double BM25Score(int blockIdx, int docIdx) {
		// TODO Auto-generated method stub
		double k1 = 1.2;
	    double b = 0.75;
	    int df = this.noOfDocs;
	    int avgDocLength = 942; //determined by data analysis
	    //Random random = new Random();
        // Generate a random number between 300 and 1500 (inclusive)
	    int docLength  = 0;
	    try {
	    docLength = IndexLoader.pageTable.get(this.docIds.get(blockIdx).get(docIdx)).docLength;
	    } catch(Exception e) {
	    	int r =1;
	    	
	    }
        //int docLength = random.nextInt(1201) + 300;
	    int tf = this.frequencies.get(blockIdx).get(docIdx);
	    int N = 3213836;
		double idf = Math.log((N - df + 0.5) / (df + 0.5));
	    double TF = (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * (docLength / avgDocLength)));
	    //double queryTermWeight = ((k1 + 1) * qf) / (k1 + qf);
	    double queryTermWeight = 1;
	    return idf * TF * queryTermWeight;
		//return 0;
	}

	
}
