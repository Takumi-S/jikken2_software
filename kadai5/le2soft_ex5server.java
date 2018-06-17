import java.net.*;
import java.io.*;
import java.util.regex.*;

class Server{
    public static void main(String[] args){
	if(args.length < 1){
	    System.out.println("ポート番号を入力してください");
	    return;
	}
	try{
	    ServerSocket serverS = new ServerSocket(Integer.parseInt(args[0]));
	    while(true){
		new ServerThread(serverS.accept()).start();
		System.out.println("New connection.");
	    }
	}catch(IOException e){
	    System.out.println("IO exception.");
	    System.exit(1);
	}
    }
}

class ServerThread extends Thread{
    
    Socket clientS;
    
    public ServerThread(Socket acceptedS){
	clientS = acceptedS;
    }
    
    public void run(){
	try {
	    PrintStream out = new PrintStream(clientS.getOutputStream(), true);
	    BufferedReader in = new BufferedReader(new InputStreamReader(clientS.getInputStream()));
	    String buf;
	    
	    // receive requests from client (until an empty line)
	    if(null == (buf = in.readLine())){
		System.out.println("リクエスト読み込み失敗");
	    }
	    
	    Pattern pat = Pattern.compile("GET /(.+) ");
	    Matcher mat = pat.matcher(buf);
	    mat.find();

	    try{	
		BufferedReader din = new BufferedReader(new FileReader(mat.group(1)));
		String s;
		
		out.print("HTTP/1.1 200 OK\r\n");
		out.print("Content-Type: text/html; charset=us-ascii\r\n");

		// send contents to client
		while((s = din.readLine()) != null){
		    out.println(s);
		}
	    }catch(FileNotFoundException e){
		out.print("HTTP/1.1 404 Not Found\r\n");
		out.print("Content-Type: text/html; charset=us-ascii\r\n");
		out.print("<HTML><HEAD>Not Found</HEAD>\r\n");
		out.print("<BODY>\r\n");
		out.print("The requested URL " + mat.group(1) + " was not found on this server.\r\n");
		out.print("</BODY></HTML>\r\n");
	    }

	    out.close();
	    in.close();
	    clientS.close();
	}catch(IOException e){
	    System.out.println("IO exception.");
	    System.exit(1);
	}
    }
}