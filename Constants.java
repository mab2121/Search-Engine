
public  class Constants {
  
	// the path to the working directory
	final static String root_work_dir = "";
	
	// the path to a temp directory
	final static String temp_work_dir = "C:/Search_Engines/Corpus/Temp";
	
	// the path of the root corpus 
	final static String input_corpus_path = "C:/Search_Engines/Corpus/fulldocs-new-clean-final.trec";
	//final static String input_corpus_path = "C:/Search_Engines/Corpus/fulldocs-new-1.txt";
	
	final static String input_corpus_unclean_path = "C:/Search_Engines/Corpus/fulldocs-new.trec";
	
	// the path of the compressed final index on disk
	final static String output_index_path = "C:/Search_Engines/Corpus/Output_Index";
	//final static String output_index_path = "C:/Search_Engines/Corpus/Backup/GOLD-V2/Output_Index";
	// the path to the final page table stored on disk 
	// can be completely loaded into main memory
	final static String page_table_path = "C:/Search_Engines/Corpus/Page_Table";
	//final static String page_table_path = "C:/Search_Engines/Corpus/Backup/GOLD-V2/Page_Table";
	
	// the path to the lexicon structure stored on disk
	// can be loaded completely into main memory 
	final static String lexicon_path = "C:/Search_Engines/Corpus/Lexicon";
	//final static String lexicon_path = "C:/Search_Engines/Corpus/Backup/GOLD-V2/Lexicon";
	// max size of the temp index file to be read and
	// sorted into main memory, default - 100Mb
	final static int part_index_file_size = 100 * 1024 * 1024;
	//final static int part_index_file_size = 512;
	
	// max number of entries in the lexicon before flushing to disk
	final static int lexicon_flush_limit = 10000;
	
	// size of the docID and freq block in the postings
	final static int posting_block_size = 128;
	
	//generate snippets or not
	static boolean snippet_gen = false;
	
	
}
