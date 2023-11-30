import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SEHelper {
	/**
	 * @author mehran
	 * Utility class to handle various misc tasks for the SE
	 */

		public static void main(String[] args) {
			// TODO Auto-generated method stub


		}
		private static final String[] removalRegex= {
	            "(\\[[0-9]*\\])",
	            "(\\[edit\\])" ,
	            "[']",
	            "\\p{IsPunctuation}",
	            "[\\s]((the)|(or)|(and)|(be)|(of)|(for)|(to)|(is)|(was)|(it)|(has)|(had)|(etc)|(shall)|(a)|(but)|(him)|(his)|(if)|(an)|(in))[\\s]",
	            "[\\s][^\\s][\\s]"};

	    public static String regexRemoval(String _text){
	    	
	    	_text = _text.replaceAll(removalRegex[0]," ");
	    	_text = _text.replaceAll(removalRegex[1]," ");
	    	_text = _text.replaceAll(removalRegex[2],"");
	    	_text = _text.replaceAll(removalRegex[3]," ");
	    	_text = _text.replaceAll(removalRegex[4]," ");
	    	_text = _text.replaceAll(removalRegex[5]," ");
	    	
	    	
	    	String commonWordsRegex = "\\b(the|or|and|be|of|for|to|is|was|it|has|had|etc|shall|a|but|him|his|if|an|in)\\b";
	        Pattern pattern = Pattern.compile(commonWordsRegex, Pattern.CASE_INSENSITIVE);

	        // Use a Matcher to find and replace common words with an empty string
	        Matcher matcher = pattern.matcher(_text);
	        String result = matcher.replaceAll("");
	    	
	        return result;
	         
	    }
	    
	    public static byte[] readLength(long offset, long length) {
	        
	        String filePath = Constants.output_index_path + "/index-compressed-new.bin";
	        //long offset = 0; // Starting offset in bytes
	        //int length = 1050866;  // Length of the byte array to retrieve

	        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r")) {
	            randomAccessFile.seek(offset);

	            // Create a byte array to hold the retrieved data
	            byte[] byteArray = new byte[(int)length];

	            int bytesRead = randomAccessFile.read(byteArray);

	            if (bytesRead == length) {
	               
	                return byteArray;
	            } else {
	                System.err.println("Failed to retrieve the byte array.");
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return null;
	    }
}
