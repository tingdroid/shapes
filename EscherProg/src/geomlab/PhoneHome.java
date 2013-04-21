package geomlab;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PhoneHome {
    private static final String svnid =
	"$Id";
    
    private static final String home =
	"http://spivey.oriel.ox.ac.uk/mike/geomlab-home/version.txt";
    
    private String result = null;
    
    public void request() {
	Thread thread = new Thread() {
	    public void run() {
		try {
		    URL url = new URL(home);
		    Object reply = url.getContent();
		    InputStream in = (InputStream) reply;
		    BufferedReader bufin = 
			new BufferedReader(new InputStreamReader(in));
		    setResult(bufin.readLine());
		    bufin.close();
		}
		catch (Exception _) {
		    setResult("unknown");
		}
	    }
	};
	
	thread.start();
    }
    
    protected synchronized void setResult(String result){
	this.result = result;
    }
    
    public synchronized String getResult() {
	return result;
    }
}
