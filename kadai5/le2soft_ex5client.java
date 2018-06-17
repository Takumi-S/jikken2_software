import java.net.*;
import java.io.*;
import java.util.regex.*;
import java.util.Arrays;

class Client{
    public static void main(String[] args){

	try{
	    Browse browse = new Browse();
	    while(true){
		
		browse.gethost_port_path();
		Socket s = new Socket(browse.host_port_path[0], Integer.parseInt(browse.host_port_path[1]));
		BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		PrintWriter out = new PrintWriter(s.getOutputStream(),true);
		String buf;
		
		// send request to server
		out.print("GET /" + browse.host_port_path[2] + " HTTP/1.1\r\n");
		out.print("Host: " + browse.host_port_path[0] + "\r\n");
		out.print("\r\n");
		out.flush();
		
		
		// receive contents from server
		while(null != (buf = in.readLine())){
		    browse.collectlinks(buf);
		    System.out.println(buf);
		}
		browse.printlinks();
		browse.getlink();
		in.close();
		out.close();
		s.close();
	    }
	}catch(UnknownHostException e){
	    System.out.println("Unknown host.");
	    System.exit(1);
	}catch(IOException e){
	    System.out.println("IO Exception");
	    System.exit(1);
	}
    }
}



class Browse{
    
    //instance variables
    public String browseurl;
    public String[] links = new String[64];
    public int urlnum = 0;
    public String[] host_port_path = new String[3];

    //constructor
    public Browse(){
	try{
	    System.out.println("Input URL:");
	    this.browseurl = new BufferedReader(new InputStreamReader(System.in)).readLine();
	    Arrays.fill(links, "");
	}catch(IOException e){
	    System.out.println("IO Exception");
	    System.exit(1);
	}
    }
    
    //methods
    public void gethost_port_path(){

	Pattern pat1 = Pattern.compile("http://(.+?):(\\d+)/(.+)");
	Matcher mat1 = pat1.matcher(this.browseurl);
	if(mat1.find()){ //ポート番号が指定されている
	    host_port_path[0] = mat1.group(1);
	    host_port_path[1] = mat1.group(2);
	    host_port_path[2] = mat1.group(3);
	}else{ //ポート番号が指定されていない
	    Pattern pat2 = Pattern.compile("http://(.+?)/(.+)");
	    Matcher mat2 = pat2.matcher(this.browseurl);
	    mat2.find();
	    host_port_path[0] = mat2.group(1);
	    host_port_path[1] = "80";
	    host_port_path[2] = mat2.group(2);
	}
    }

    public void collectlinks(String buf){

	Pattern pat1 = Pattern.compile("a(.+)href=\"(.+)\"");
	Matcher mat1 = pat1.matcher(buf);
	Pattern pat2 = Pattern.compile("A(.+)HREF=\"(.+)\"");
	Matcher mat2 = pat2.matcher(buf);
	Pattern abspat = Pattern.compile("http://(.+)"); //絶対パスパターン
	Pattern relpat = Pattern.compile("(http://(.+))/(.+)"); //url補完用パターン
	Matcher relmat = relpat.matcher(this.browseurl);

	if(mat1.find()){
	    Matcher absmat = abspat.matcher(mat1.group(2));
	    if(absmat.find()){ //絶対パス
		this.links[urlnum] =  mat1.group(2);
		urlnum++;
	    }else if(relmat.find()){
		this.links[urlnum] = relmat.group(1) + "/" + mat1.group(2);
		urlnum++;
	    }
	}
	
	if(mat2.find()){
	    Matcher absmat = abspat.matcher(mat2.group(2));
            if(absmat.find()){ //絶対パス        
                this.links[urlnum] = mat2.group(2);
                urlnum++;
            }else if(relmat.find()){
                this.links[urlnum] = relmat.group(1) + "/" + mat2.group(2);
		urlnum++;
	    }
	}
    }

    public void printlinks(){
	int i = 0;
	if(this.links[0] != ""){
	    System.out.println("[list of links]");
	    for(i=0; this.links[i] != ""; i++){
		System.out.printf("%d.", i+1);
		System.out.println(this.links[i]);
	    }
	}else{
	    System.out.println("リンクはありません");
	    System.exit(1);
	}
    }
    
    public void getlink(){

	try{
	    if(this.links[0] != ""){ 
		System.out.print("リンク番号を入力:");
		String num = new BufferedReader(new InputStreamReader(System.in)).readLine();
		if(0 <= Integer.parseInt(num) && Integer.parseInt(num) <= urlnum){
		    this.browseurl =  this.links[Integer.parseInt(num)-1];
		    urlnum = 0; //links配列番号初期化
		    Arrays.fill(links, ""); //links初期化
		}else{
		    System.out.println("リンクがありません");
		    System.exit(1);
		}
	    }
	}catch(IOException e){
	    System.out.println("IO Exception");
	    System.exit(1);
	}
    }
}