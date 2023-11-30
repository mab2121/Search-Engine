import java.io.IOException;

public class IndexMain {
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        IndexerWork iw = new IndexerWork();
        try {
			iw.startIndexing();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}

}
