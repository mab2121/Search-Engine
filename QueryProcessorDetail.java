import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;

import indexer.IndexEntry;

public class QueryProcessorDetail {
    /**
     * These are helper functions to 
     * deal with index postings
     */
	
	/**
	 * 
	 * @param qi - ith entry from the query
	 * @return a index entry location object for that term
	 * @throws IOException 
	 */
	
	public static int MAX_DID = Integer.MAX_VALUE;
	
	
	
	public static IndexEntryLocation openList(String qi) throws IOException {
		// get the term qi's posting
		HashMap<String,String> temp = IndexLoader.lexicon;
		String fileptr =  IndexLoader.lexicon.get(qi);
		/**
		 * test
		 */
        if(fileptr == null) {
        	return null;
        }
 		StringTokenizer str = new StringTokenizer(fileptr);
		long offset = Long.parseLong(str.nextToken());
		long length = Long.parseLong(str.nextToken());
		
		byte [] brr = SEHelper.readLength(offset,length);
		Vector<Integer> v = IndexCompressor.processBinaryLine(brr,1);
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < v.size(); i++) {
    		sb.append(v.get(i) + " ");
    	}
    	
    	IndexEntry ie = IndexEntry.createFromLine(sb.toString());
    	ie.maxScore = Double.parseDouble(str.nextToken());
		IndexEntryLocation iel = new IndexEntryLocation(ie,0,0,ie.noOfDocs);
		return iel;
	}
	
	public static void closeList(IndexEntryLocation iel) {
		// set for garbage collection
		iel.ie = null;
		iel = null;
	}
	/**
	 * 
	 * @param iel
	 * @param did
	 * @return the docID of the first entry with doc id >= did in iel list
	 */
	public static int nextGEQ(IndexEntryLocation iel, int did) {
		IndexEntry ie = iel.ie;
		int blockListIdx = iel.blockIdx;
		//int docListIdx = iel.docIdx;
		
		// jump to the block first
		int blockOfInterest = blockListIdx;
		while(blockOfInterest < ie.noOfBlocks) {
			List<Integer> docAtBlock = ie.docIds.get(blockOfInterest);
			int n = docAtBlock.size() - 1;
			//if(did >= docAtBlock.get(0) && did <= docAtBlock.get(n)) {
			if(did <= docAtBlock.get(n)){
				//found the block of interest
				break;
			}
			else blockOfInterest++;
		}
		int docOfInterest = Integer.MAX_VALUE;
		if(blockOfInterest >= ie.noOfBlocks) {
			iel.blockIdx = blockOfInterest;
			iel.docIdx = Integer.MAX_VALUE;
			return docOfInterest;
		}
		List<Integer> docAtBlock = ie.docIds.get(blockOfInterest);
		for(int i = 0; i < docAtBlock.size(); i++) {
			if(docAtBlock.get(i) >= did) {
				iel.blockIdx = blockOfInterest;
				iel.docIdx = i;
				return docAtBlock.get(i);
			}
		}
		
	    return Integer.MAX_VALUE;			
	}
	
	public static double getScore(IndexEntryLocation iel) {
		if(iel.docIdx != Integer.MAX_VALUE) {
			 return BM25Score(iel.ie, iel.blockIdx, iel.docIdx, iel.N);
		}
		else return 0;
	}
	
	
	private static double BM25Score(IndexEntry ie, int blockIdx, int docIdx, int n) {
		// TODO Auto-generated method stub
		double k1 = 1.2;
	    double b = 0.75;
	    int df = ie.noOfDocs;
	    int avgDocLength = 942; //determined by data analysis
	    //Random random = new Random();
        // Generate a random number between 300 and 1500 (inclusive)
	    int docLength = IndexLoader.pageTable.get(ie.docIds.get(blockIdx).get(docIdx)).docLength;
        //int docLength = random.nextInt(1201) + 300;
	    int tf = ie.frequencies.get(blockIdx).get(docIdx);
	    int N = 3213836;
		double idf = Math.log((N - df + 0.5) / (df + 0.5));
	    double TF = (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * (docLength / avgDocLength)));
	    //double queryTermWeight = ((k1 + 1) * qf) / (k1 + qf);
	    double queryTermWeight = 1;
	    return idf * TF * queryTermWeight;
		//return 0;
	}

	public static List<ResultEntry> conjunctive(String query) throws IOException {
		// TODO Auto-generated method stub
		// PQ with a size 10 to hold top-10 results
		int initCapacity = 10;
		PriorityQueue<ResultEntry> pq = new PriorityQueue<ResultEntry>(initCapacity, new Comparator<ResultEntry>() {
		    public int compare(ResultEntry n1, ResultEntry n2) {
		        // compare n1 and n2
		    	if(n1.score > n2.score) return 1;
		    	else if(n1.score < n2.score) return -1;
		    	else return 0;
		    }
		});
		
		List<String> qis = new ArrayList<String>();
		List<IndexEntryLocation> iels = new ArrayList<IndexEntryLocation>();
		StringTokenizer str = new StringTokenizer(query);
		while(str.hasMoreTokens()) {
			String qi = SEHelper.regexRemoval(str.nextToken().toLowerCase());
			IndexEntryLocation iel = openList(qi);
			if(iel != null) {
				qis.add(qi);
				iels.add(iel);
			}
		}
		if(iels.size() == 0) return new ArrayList<ResultEntry>();
		Collections.sort(iels, new IndexEntryLocation.IELComparator());
		
		int did = iels.get(0).ie.docIds.get(0).get(0);
		while(did < MAX_DID) {
			boolean matched = true;
			did = nextGEQ(iels.get(0),did);
			for(int i = 1; i < iels.size(); i++) {
				int didp = nextGEQ(iels.get(i), did);
				if(didp > did) {
					did = didp;
					matched = false;
					break;
				}
			}
			
			if(!matched) continue;
			if(did != MAX_DID) {
				//did is a match
				double score = 0;
				for(int j = 0; j < iels.size(); j++) {
					score += getScore(iels.get(j));
				}
				ResultEntry re = new ResultEntry(IndexLoader.pageTable.get(did).url,"",score,did,qis);
				ResultEntry pqmin = pq.size() > 0 ? pq.peek() : null;
				if(pqmin == null) {
					pq.add(re);
				}
				else if(pq.size() == 10) {
					if(pqmin.score < re.score ) {
						pq.poll();
						pq.add(re);
					}
					else {
						did++;
						continue;
					}
				}
				else pq.add(re); // add to the heap
				did++;
			}
			else break;
		}
		
		for(int i = 0; i < iels.size(); i++) closeList(iels.get(i));
		
		List<ResultEntry> result = new ArrayList<ResultEntry>();
		int iterations = Math.min(10, pq.size());
		for(int i = 0; i < iterations; i++) {
		   result.add(pq.poll());
		}
		List<ResultEntry> resultSorted = new ArrayList<ResultEntry>();
		for(int i = iterations - 1; i >= 0; i--) {
			resultSorted.add(result.get(i));
		}
		pq.clear();
		result.clear();
		return resultSorted;
	}


	public static List<ResultEntry> disjunctive(String query) throws IOException {
		// TODO Auto-generated method stub
		int initCapacity = 10;
		PriorityQueue<ResultEntry> pq = new PriorityQueue<ResultEntry>(initCapacity, new Comparator<ResultEntry>() {
		    public int compare(ResultEntry n1, ResultEntry n2) {
		        // compare n1 and n2
		    	if(n1.score < n2.score) return -1;
		    	else if(n1.score > n2.score) return 1;
		    	else return 0;
		    }
		});
		
		List<String> qis = new ArrayList<String>();
		List<IndexEntryLocation> iels = new ArrayList<IndexEntryLocation>();
		StringTokenizer str = new StringTokenizer(query);
		while(str.hasMoreTokens()) {
			String qi = SEHelper.regexRemoval(str.nextToken().toLowerCase());
			IndexEntryLocation iel = openList(qi);
			if(iel != null) {
				qis.add(qi);
				iels.add(iel);
			}
		}
		
		Collections.sort(iels);
		
		int n = iels.size();
		double [] ub = new double[n];
		double [] sigma = new double[n];
		
		for(int i = 0; i < n ; i++) {
			sigma[i] = iels.get(i).ie.maxScore;
		}
		
		ub[0] = sigma[0];
		for(int i = 1; i < n; i++) ub[i] = ub[i - 1] + sigma[i];
		
		//double theta = ub[n/2];
		double theta = sigma[n - 1];
		List<IndexEntryLocation> essentials = new ArrayList<IndexEntryLocation>();
		List<IndexEntryLocation> nonEssentials = new ArrayList<IndexEntryLocation>();
		
		for(int i = 0; i < iels.size(); i++) {
			if(ub[i] <= theta) {
				nonEssentials.add(iels.get(i));
			}
			else {
				essentials.add(iels.get(i));
			}
		}
		
		if(essentials.size() == 0) {
			essentials.addAll(nonEssentials);
			nonEssentials.clear();
		}
		
		//score += getScore(iels.get(j));
		HashMap<Integer, Double> topK = new HashMap<Integer, Double>();
		
		for(int i = 0; i < essentials.size(); i++) {
			
			IndexEntryLocation iel = essentials.get(i);
			while(true) {
				int did = iel.nextDocId();
				if(did == MAX_DID) break;
				double score = getScore(iel);
				iel.next();
				if(topK.containsKey(did)) {
					score += topK.get(did);
				}
				topK.put(did, score);
			}
		}
		
		for(int i = 0; i < nonEssentials.size(); i++) {
			IndexEntryLocation iel = nonEssentials.get(i);
			for(int did: topK.keySet()) {
				int ndid = nextGEQ(iel, did);
				if(ndid == did) {
					double score = getScore(iel);
					topK.put(did, topK.get(did) + score);
					iel.reset();
				}
			}
		}
		
		for(int did: topK.keySet()) {
			
			double score = topK.get(did);
			if(pq.size() < 10) {
				ResultEntry re = new ResultEntry(IndexLoader.pageTable.get(did).url,"",score,did,qis);
				pq.add(re);
			}
			else {
				if(pq.peek().score < score) {
					pq.poll();
					ResultEntry re = new ResultEntry(IndexLoader.pageTable.get(did).url,"",score,did,qis);
					pq.add(re);
				}
			}	
		}
		
		topK.clear();
		
		for(int i = 0; i < iels.size(); i++) closeList(iels.get(i));
		
		List<ResultEntry> result = new ArrayList<ResultEntry>();
		int resultSize = Math.min(10, pq.size());
		for(int i = 0; i < resultSize; i++) {
		   result.add(pq.poll());
		}
		pq.clear();
		return result;
	
		
	}

}
