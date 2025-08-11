import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import helpers.Parser;

public class Main {
  
  public static class MyRunnable implements Runnable{
    private Socket clientSocket;
    long threadID;

    public MyRunnable(Socket clientSocket){
      this.clientSocket = clientSocket;
    }


    public String readData(){

      String req="";
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
      Parser parser = new Parser();
      try {
        outputStream = clientSocket.getOutputStream();
      
        while(true){
          req = readData();
          if(req.isEmpty()){
            continue;
          }
          res = parser.requestParser(req);
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