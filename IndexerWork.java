import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import indexer.IndexEntry;



public class IndexerWork {

	/**
	 * Should read the input corpus certain MB's a time
	 * build a temp index, sort it and save it to disk
	 * repeat till the entire index is read
	 */
	public void buildSortedPartialIndexes() {

		//start reading files x MB chunk at a time
		String filePath = Constants.input_corpus_path; // Replace with the path to your file
		int chunkSize = Constants.part_index_file_size; // 10 MB chunk size

		try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
		     byte[] buffer = new byte[chunkSize];
		     //StringBuilder stringBuilder = new StringBuilder();

		     int bytesRead;
		     while ((bytesRead = fileInputStream.read(buffer)) != -1) {
		           String chunk = new String(buffer, 0, bytesRead);
		           // generate the next sorted temp index file
                   generateNextSortedTempIndex(chunk);

		            // Clear the buffer for the next chunk
		            buffer = new byte[chunkSize];
		      }

		  }
		  catch (IOException e) {
		      e.printStackTrace();
		  }

	}

	/**
	 * write the intermediate index to disk
	 */

	private void writeIndexMapToDisk() {
		// TODO Auto-generated method stub
		// Specify the output file path
        String outFilePath = createTempFilePath();
        List<String> terms = new ArrayList<>();
        terms.addAll(indexMap.keySet());
        // sort the terms lexicographically
        Collections.sort(terms);

        try (FileOutputStream fileOutputStream = new FileOutputStream(outFilePath)) {
            // Convert the string to bytes and write it to the file
        	for (String term: terms) {
        		 String termw = term + "\n";
        		 byte[] bytes = termw.getBytes();
                 fileOutputStream.write(bytes);
                 String posting = indexMap.get(term).toFileFormat();
                 bytes = posting.getBytes();
                 fileOutputStream.write(bytes);
        	 }
        } catch (IOException e) {
            e.printStackTrace();
        }
		tempFileID++;
		indexMap.clear();
	}
	/**
	 * merge two partially created sorted inverted indexes
	 * @param f1
	 * @param f2
	 * @param outDir
	 * @param n
	 * @throws IOException
	 */

	public void mergeTwoFiles(File f1, File f2, String outDir, int n) throws IOException {

	    // File name of the new merged file
	    /**
	     *
	     * TODO make this clean
	     *
	     */
	    String tempFile = outDir + "/" + f1.getName();
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

	    BufferedReader fh1 = new BufferedReader(new FileReader(f1));
	    BufferedReader fh2 = new BufferedReader(new FileReader(f2));
        // line in file f1
	    String line1 = fh1.readLine();
	    // line in file f2
	    String line2 = fh2.readLine();

	    //track offset to store in lexicon
	    long offset = 0;

	    while ((line1 != null) && (line2 != null)) {

	      int comp = line1.compareTo(line2);
	      if (comp < 0) {
	    	//convert to binary and write
	    	line1 += "\n";
	    	byte[] bytes = line1.getBytes();
	    	fileOutputStream.write(bytes);
	        line1 = fh1.readLine();
	        line1 += "\n";
	        bytes = line1.getBytes();
	    	fileOutputStream.write(bytes);
            line1 = fh1.readLine();
	      }

	      else if (comp > 0) {
	    	 //convert to binary and write
	    	  line2 += "\n";
	    	  byte[] bytes = line2.getBytes();
		      fileOutputStream.write(bytes);
		      line2 = fh2.readLine();
		      line2 += "\n";
		      bytes = line2.getBytes();
		      fileOutputStream.write(bytes);
              line2 = fh2.readLine();
	      }
	      else { // both keys are the same

	    	line1 += "\n";
	    	byte[] bytes = line1.getBytes();
	    	fileOutputStream.write(bytes);

	        line1 = fh1.readLine();
	        line2 = fh2.readLine();
	        IndexEntry e1 = IndexEntry.createFromLine(line1);
	        IndexEntry e2 = IndexEntry.createFromLine(line2);
	        e1 = e1.mergePosting(e2);
	        String mergePosting = e1.toFileFormat();

	        bytes = mergePosting.getBytes();
	        fileOutputStream.write(bytes);

	        line1 = fh1.readLine();
	        line2 = fh2.readLine();
	      }

	    }

	    while (line1 != null) {
	    	line1 += "\n";
	    	byte[] bytes = line1.getBytes();
		    fileOutputStream.write(bytes);
		    line1 = fh1.readLine();
		    line1 += "\n";
		    bytes = line1.getBytes();
		    fileOutputStream.write(bytes);
            line1 = fh1.readLine();
	    }

	    while (line2 != null) {
	    	line2 += "\n";
	    	  byte[] bytes = line2.getBytes();
		      fileOutputStream.write(bytes);
		      line2 = fh2.readLine();
		      line2 += "\n";
		      bytes = line2.getBytes();
		      fileOutputStream.write(bytes);
              line2 = fh2.readLine();
	    }

	    fh1.close();
	    fh2.close();
	    fileOutputStream.close();
    }


	/**
	 * @param buffer
	 * Reads the next chunk(e.g of 10Mb), creates a
	 * in memory inverted index from it and persists it to disk
	 */
	public void generateNextSortedTempIndex(String buffer) {
		// reset the temp index map for the new file
		indexMap = new HashMap<String, IndexEntry>();
		try (Scanner scanner = new Scanner((buffer))) {
			while (scanner.hasNext()) {
				String token = scanner.next();

				if (isNewDocument(token)) {
					docID++;
					continue;
				} else if (isNewURL(token)) {
					// add the entry to the page table
					token = scanner.next();
					pageTable.put(docID, token);
					continue;
				}
				// token = stemmer.stem(token);

				if (isBadToken(token))
					continue;
				addToIndexMap(token, docID);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		writeIndexMapToDisk();
	}

	private void addToIndexMap(String token, int docId) {
		// TODO Auto-generated method stub
		if(!indexMap.containsKey(token)) {
			indexMap.put(token, new IndexEntry());
		}
		indexMap.get(token).addDocID(docId);
	}


	private String createTempFilePath() {
		// base out put path
		String outputPath = Constants.temp_work_dir + "/index-level-" + 0;
        // create the new path
        if(!basePathCreated) createDir(outputPath);
		basePathCreated = true;
		String path = outputPath + "/" + tempFileID + ".bin";
		return path;
	}

	private boolean isNewDocument(String token) {
		// TODO Auto-generated method stub
		if(token.contains("<DOC>")) return true;
		return false;
	}

	private boolean isNewURL(String token) {
		// TODO Auto-generated method stub
		if(token.contains("<TEXT>")) return true;
		return false;
	}

	private boolean isEndDocument(String token) {
		// TODO Auto-generated method stub
		if(token.contains("</DOC>")) return true;
		return false;
	}

	private boolean isBadToken(String token) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * trigger function to start indexing
	 * @throws IOException
	 */
	public void startIndexing() throws IOException {
		System.out.println("Start: " +  LocalTime.now());

		// build **sorted** part index files of a fixed size
		// see Constants.part_index_file_size
		buildSortedPartialIndexes();

		// perform I/O efficient merge sort on the partailly built index files
		mergePartialIndexes();

		// Log the page table
		logPageTable();

		// compress the built Index and build the lexicon
		compressCompleteIndex();

		System.out.println("End: " +  LocalTime.now());
	}

	private static String getLexiconMetdada(List<Long> list) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		for (Long element : list) {
			sb.append(String.valueOf(element) + "\t");
		}
		sb.append("\n");
		return new String(sb);
	}

	private static void logLexicon() {
		// TODO Auto-generated method stub
		 String outFilePath = Constants.lexicon_path + "/lexicon.bin";
	     List<String> terms = new ArrayList<>();
	     terms.addAll(lexicon.keySet());

	        try (FileOutputStream fileOutputStream = new FileOutputStream(outFilePath,true)) {
	            // Convert the string to bytes and write it to the file
	        	for (String term: terms) {
	        		 String termw = term;
	        		 term = term + "\n";
	        		 byte[] bytes = term.getBytes();
	                 fileOutputStream.write(bytes);
	                 String lexiconData = getLexiconMetdada(lexicon.get(termw));
	                 bytes = lexiconData.getBytes();
	                 fileOutputStream.write(bytes);
	        	 }
	             System.out.println("Lexicon has been written as binary to the file.");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

	private void logPageTable() {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		String outFilePath = Constants.page_table_path + "/page-table.bin";
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
			        String posting = pageTable.get(term) + "\n";
			        bytes = posting.getBytes();
			        fileOutputStream.write(bytes);
			}
			System.out.println("Page Table has been written to the file.");
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

	private void compressCompleteIndex() throws IOException {
		// TODO Auto-generated method stub
		String toCompress = Constants.temp_work_dir + "/index-level-" + mergeIterations + "/0.bin";
		IndexCompressor.compressIndexFileNew(toCompress,lexicon);
	}

	private void mergePartialIndexes() throws IOException {
		// TODO Auto-generated method stub
		int currentLevelIdx = 0;

		// keep merging two at a time till we get one file
		while(true) {
			String directoryPath = Constants.temp_work_dir + "/index-level-" + currentLevelIdx;
			File directory = new File(directoryPath);

	        // Check if the path exists and is a directory
	        if (directory.exists() && directory.isDirectory()) {
	            // List all files in the directory
	            File[] files = directory.listFiles();

	            //sort files by name - important to keep docIds ascending
	            Arrays.sort(files,new FileNumberComparator());

	            // if its only one file, we have merged everything - so quit
	            if(files.length == 1) {
	            	System.out.println("Files Merged , level: " + currentLevelIdx);
	            	break;
	            }
	            // else start the merge from this dir to next index dir
	            currentLevelIdx += 1;
	            String outputPath = Constants.temp_work_dir + "/index-level-" + currentLevelIdx;
	            // create the new path
	            createDir(outputPath);
	            for(int i = 0; i < files.length; i+=2) {

	            	File f1 = files[i];
	            	File f2 = i + 1 < files.length ? files[i+1] : null;
	            	if(f2 == null) {
	            		// just place f1 into output path
	            		copyFile(f1.getAbsolutePath(),outputPath,f1.getName());
	            	}
	            	else {
	            		mergeTwoFiles(f1,f2,outputPath,0);
	            	}

	            }
	            System.out.println("Finished Merge on level: " + currentLevelIdx);
	            deleteDir(directoryPath);

	        } else {
	            System.out.println("The specified path is not a directory or does not exist.");
	        }
		}

		this.mergeIterations = currentLevelIdx;
	}

	private void deleteDir(String directoryPath) throws IOException {
		// TODO Auto-generated method stub
		Path directory = Paths.get(directoryPath);

	    if (Files.exists(directory))
	    {
	        Files.walkFileTree(directory, new SimpleFileVisitor<Path>()
	        {
	            @Override
	            public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException
	            {
	                Files.delete(path);
	                return FileVisitResult.CONTINUE;
	            }

	            @Override
	            public FileVisitResult postVisitDirectory(Path directory, IOException ioException) throws IOException
	            {
	                Files.delete(directory);
	                return FileVisitResult.CONTINUE;
	            }
	        });
	    }

	}

	private void copyFile(String sourceFilePath, String outputPath, String name) {
		// TODO Auto-generated method stub
        String destinationFilePath = outputPath + "/" + name;

        // Create Path objects for the source and destination files
        Path sourcePath = Paths.get(sourceFilePath);
        Path destinationPath = Paths.get(destinationFilePath);

        try {
            // Copy the file with the REPLACE_EXISTING option
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File copied from " + sourceFilePath + " to " + destinationFilePath);
        } catch (IOException e) {
            System.err.println("Failed to copy the file: " + e.getMessage());
        }
	}

	private void createDir(String outputPath) {
		// TODO Auto-generated method stub
		Path directory = Paths.get(outputPath);
        // Create the directory
        try {
            Files.createDirectories(directory);
            System.out.println("Directory created: " + directory);
        } catch (IOException e) {
            System.err.println("Failed to create the directory: " + e.getMessage());
        }
	}

	static class FileNumberComparator implements Comparator<File> {
        @Override
        public int compare(File file1, File file2) {
            int number1 = extractNumber(file1.getName());
            int number2 = extractNumber(file2.getName());
            return Integer.compare(number1, number2);
        }

        private int extractNumber(String fileName) {
            String[] parts = fileName.split("\\D+"); // Split by non-digit characters
            if (parts.length > 0) {
                return Integer.parseInt(parts[0]);
            }
            return 0; // Default value if no number is found
        }
    }

	public static void flushLexicon(HashMap<String, List<Long>> off) {
		// TODO Auto-generated method stub
		IndexerWork.logLexicon();
		IndexerWork.lexicon.clear();
	}

	IndexerWork(){}
    // intermediate indexMap in memory. Flush after each buffer is read
	public HashMap<String,IndexEntry> indexMap;

	// docID -> URL mapping - in memory
	public HashMap<Integer,String> pageTable = new HashMap<>();

	// term -> begin offset in the disk based file
	// and the number of docs containing the word
	public static HashMap<String,List<Long>> lexicon = new HashMap<>();

	// doc ID
	int docID = 0;

	// temp file ID
	int tempFileID = 0;

	// to get started on creating sub dirs for index files
	boolean basePathCreated = false;

	private int mergeIterations;
	
	public StringBuilder load = new StringBuilder();
}
