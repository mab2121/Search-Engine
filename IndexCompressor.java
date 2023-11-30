
import java.io.*;
import java.lang.Byte;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

public class IndexCompressor {
   /**
    * compress a vector of integers to a vector of bytes using varByte
    * @param intArr
    * @return
    */
    public static Vector<Byte> vByteEncoder(Vector<Integer> intArr) {
        Vector<Byte> outputStream = new Vector<Byte>();
        for (int n : intArr) {
            int count = 0;
            if(n == 115333) {
            	int r = 1;
            }
            Vector<Byte> number = new Vector<Byte>();
            while (true) {
                int no = n % 128;
                number.add(0, (byte) no);
                if (n < 128)
                    break;
                n = n / 128;
                count++;
            }
            number.set(count, (byte) ((int) number.get(count) + 128));

            outputStream.addAll(number);
        }
        return outputStream;
    }
    
    /**
     * compress a single integer to a vector of bytes
     * @param n
     * @return
     */
    public static Vector<Byte> vByteEncoder(int n) {
        int count = 0;
        Vector<Byte> number = new Vector<Byte>();
        while (true) {
            int no = n % 128;
            number.add(0, (byte) no);
            if (n < 128)
                break;
            n = n / 128;
            count++;
        }
        number.set(count, (byte) ((int) number.get(count) + 128));
        return number;
    }

    /**
     * decode a list of integers from a byte stream compressed using varByte
     * @param byteStream
     * @return
     */
    public static Vector<Integer> vByteDecoder(Vector<Byte> byteStream) {
        Vector<Integer> numbers = new Vector<Integer>();
        int n=0;
        for (int i = 0; i < byteStream.size(); i++) {
            int byteNo = (int) byteStream.get(i)& 0xFF;
            if (byteNo < 128)
                n = n * 128 + byteNo;
            else {
                n = n * 128 + byteNo - 128;
                numbers.add(n);
                n=0;
            }
        }
        return numbers;
    }
    
    /**
     *  Updated index compression to log the posting length
     *  in the binary file as well, instead of just offset
     * @param filename
     * @param off
     * @return
     * @throws IOException
     */
    public static String compressIndexFileNew(String filename,HashMap<String,List<Long>> off) throws IOException {
		 String outFilePath = Constants.output_index_path + "/index-compressed-new.bin";
		 FileOutputStream os = new FileOutputStream(outFilePath, true);
		 File file = new File(filename); //input
	        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	          String line; int i = 0; long offset = 0; String key ="";               
	          while ((line = br.readLine()) != null) {
	        	  if(i%2 == 0) { // this is a key
	        		  if(off.size() > Constants.lexicon_flush_limit) IndexerWork.flushLexicon(off);
	        		  key = line;        		  
	                  i++;
	                  continue;
	        	  }
	        	  else {
	        		  // record the location
	        		  off.put(key, new ArrayList<Long>());
	        		  off.get(key).add(offset);
	        		 
	        		  Vector<Integer> post = new Vector<>();
	        		  StringTokenizer str = new StringTokenizer(line);
	        		  while(str.hasMoreTokens()) {
	        			  post.add(Integer.parseInt(str.nextToken()));
	        		  }
	                
	                  Vector<Byte> bytes3 = IndexCompressor.vByteEncoder(post);
	                  byte[] bArr3 = new byte[bytes3.size()];
	                  for (int ik = 0 ; ik < bArr3.length; ik++) {
	                    bArr3[ik] = bytes3.get(ik);
	                  }

	                  os.write(bArr3);
	                  long lineLength = bArr3.length;
	                  offset += lineLength;   
	                  off.get(key).add(lineLength);
	        	  }
	        	
	        	  i++;
	          }
	          
	          System.out.println("Compressed");
	          
	          } catch(Exception e) {
	        	  e.printStackTrace();
	          }
	        
	          return outFilePath;
	        }   
    
    
    
    
    /**
     * Compress the entire inverted index using varByte
     * @param filename
     * @param off
     * @return
     * @throws IOException
     */
    public static String compressIndexFile(String filename,HashMap<String,List<Long>> off) throws IOException {
		 String outFilePath = Constants.output_index_path + "/index-compressed.bin";
		 FileOutputStream os = new FileOutputStream(outFilePath, true);
		 File file = new File(filename); //input
	        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	          String line; int i = 0; long offset = 0; String key ="";               
	          while ((line = br.readLine()) != null) {
	        	  if(i%2 == 0) { // this is a key
	        		  if(off.size() > Constants.lexicon_flush_limit) IndexerWork.flushLexicon(off);
	        		  key = line;        		  
	                  i++;
	                  continue;
	        	  }
	        	  else {
	        		  // record the location
	        		  off.put(key, new ArrayList<Long>());
	        		  off.get(key).add(offset);
	        		 
	        		  IndexEntry ie = IndexEntry.createFromLine(line);
	        		  off.get(key).add((long) ie.noOfDocs);
	        		  ie.reduceDocIDSize();
	        		  String reducedPosting = ie.toFileFormat();
	        		  
	        		  Vector<Integer> post = new Vector<>();
	        		  StringTokenizer str = new StringTokenizer(reducedPosting);
	        		  while(str.hasMoreTokens()) {
	        			  post.add(Integer.parseInt(str.nextToken()));
	        		  }
	                
	                  Vector<Byte> bytes3 = IndexCompressor.vByteEncoder(post);
	                  byte[] bArr3 = new byte[bytes3.size()];
	                  for (int ik = 0 ; ik < bArr3.length; ik++) {
	                    bArr3[ik] = bytes3.get(ik);
	                  }

	                  os.write(bArr3);
	                  long lineLength = bArr3.length;
	                  offset += lineLength;   
	        	  }
	        	  
	        	  byte[] newline = "\n".getBytes();
	        	  os.write(newline);
	        	  offset += newline.length;
	        	  i++;
	          }
	          
	          System.out.println("Compressed");
	          
	          } catch(Exception e) {
	        	  e.printStackTrace();
	          }
	        
	          return outFilePath;
	        }   
    
    /**
     * read data from a file from a specific offset
     * @param fileName
     * @param offset
     * @return 
     * @throws IOException
     */
    public static Vector<Integer> readDataFromOffset(String fileName, long offset) throws IOException {
    	Vector<Integer> v = new Vector<Integer>();
        try (InputStream inputStream = new FileInputStream(fileName)) {
            long bytesToSkip = offset;
            while (bytesToSkip > 0) {
                long skipped = inputStream.skip(bytesToSkip);
                if (skipped == 0) {
                    // Unable to skip, reached end of file
                    return v;
                }
                bytesToSkip -= skipped;
            }

            // Read data from the specified offset
            ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
            int currentByte;
            while ((currentByte = inputStream.read()) != -1) {
                if (currentByte == 0x0A) { // Newline character
                    v = processBinaryLine(lineBuffer.toByteArray(),1);
                    lineBuffer.reset(); // Clear the buffer
                    break;
                } else {
                    lineBuffer.write(currentByte);
                }
            }
        }
        return v;
    }
    

    /**
     * decode a line represented by an array of bytes
     * @param data
     * @param k
     * @return
     */
    public static Vector<Integer> processBinaryLine(byte[] data, int k) {
        // Process the binary line here
    	Vector<Byte> inp = new Vector<Byte>();
    	for(int i = 0; i < data.length; i++) inp.add(data[i]);
    	Vector<Integer> v = IndexCompressor.vByteDecoder(inp);
    	return v;
    
    }

}
