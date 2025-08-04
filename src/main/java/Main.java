import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


public class Main {
  
  public static class MyRunnable implements Runnable{
    private Socket clientSocket;
    private int start = 0, end = 0;
    private boolean echoCheck = false;
    long threadID;
    public MyRunnable(Socket clientSocket){
      this.clientSocket = clientSocket;
    }

    private int CRLFfinder(String req, int start){
      return req.indexOf("\r\n",start);
    }

    // +<string>\r\n
    private String simpleParser(String req){
      
      end = CRLFfinder(req, start);
      req = req.substring(start+1,end);
      start = end+2;
      return req;
    }

    // $<length>\r\n<data>\r\n
    private String bulkStringParser(String req){
      
      int len = 0; 
      String res="",query="";
      end = CRLFfinder(req,start);
      len = Integer.parseInt(req.substring(start+1,end));
      // System.out.println("len = "+ len);
      if(echoCheck){
        res = "$"+len+"\r\n";
      }
      start = end+2;
      end = start+len;
      query = req.substring(start, end);
      start = end+2;

      if(query.equalsIgnoreCase("ping")){
        res = "+PONG";
      } else if (query.equalsIgnoreCase("echo")){
        echoCheck = true;
        res += bulkStringParser(req);
      } else {
        res += query;
      }

      return res;
    }

    // *<number-of-elements>\r\n<element-1>...<element-n>
    private String arrayParser(String req){
      end = CRLFfinder(req, start);
      int items = Integer.parseInt(req.substring(start+1,end));
      start = end+2;
      // System.out.println("items,start,end = "+items+","+start+","+end);
      char ch;
      String res="";
      while(items > 0){
        ch = req.charAt(start);
        if(ch == '+' || ch == '-' || ch == ':'){
          res += (simpleParser(req)+"\r\n");
        } else if(req.charAt(start) == '$'){
          res += (bulkStringParser(req) + "\r\n");
          if (echoCheck) {
            items--;
          }
          echoCheck = false;
        } else if(req.charAt(start) == '*'){
          res += arrayParser(req);
        }

        items--;
      }
      return res;
    }


    public String requestParser(String req){
      
      String res="";
      try {

        //collecting req including CRLF
        char firstChar = req.charAt(0);
        //processing req into res as a RESP bulk string
        if(firstChar == '+' || firstChar == '-' || firstChar == ':'){
          res = simpleParser(req);
        } else if(firstChar == '$'){
          res = bulkStringParser(req);
        } else {
          res = arrayParser(req);
        }

        // System.out.println("res after processing : "+res);
      }
        catch (Exception e) {
        System.out.println("1 : IOException : "+e);
      }
      System.out.println("tID : "+threadID+"res kya hai "+res);
      return res;
    }

    public String readData(){

      String req="";
      start = 0;
      end = 0;
      echoCheck = false;
      try {
        InputStream in = clientSocket.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        bytesRead = in.read(data);

        if(bytesRead == -1){
          return "";
        }

        buffer.write(data, 0, bytesRead);
        req = buffer.toString(StandardCharsets.UTF_8);
        System.out.println("tID : "+threadID+"req now : "+req);
      } catch (Exception e) {
        System.out.println("2 : IOException : "+e);
      }
      return req;
    }

    @Override
    public void run(){
      threadID = Thread.currentThread().threadId();
      System.out.println("Running thread : "+threadID);
      OutputStream outputStream = null;
      String res,req="";

      try {
        outputStream = clientSocket.getOutputStream();
        while(true){
          //use parser here
          req = readData();
          if(req == ""){
            continue;
          }
          res = requestParser(req);
          outputStream.write(res.getBytes());
          outputStream.flush();
        }
      } catch (Exception e) {
        System.out.println("in thread 3 : IOException: " + e.getMessage());
      } finally {
        try {
            
          outputStream.close();
          clientSocket.close();
        } catch (Exception e) {
          System.out.println("Failed to close client conn");
        }        
      }
    }
  }

  public static void main(String[] args){
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    System.out.println("Logs from your program will appear here!");

    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    int port = 6379;
    try {
      serverSocket = new ServerSocket(port);
      serverSocket.setReuseAddress(true);

      while(true)
      {
        clientSocket = serverSocket.accept();
        Thread thread = new Thread(new MyRunnable(clientSocket));
        thread.start();
      }
    } catch (IOException e) {
        System.out.println("in main 4 : IOException: " + e.getMessage());
    } 
  }
}