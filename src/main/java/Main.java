import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.*;

public class Main {

  public static class MyRunnable implements Runnable{
    private Socket clientSocket;
    public MyRunnable(Socket clientSocket){
      this.clientSocket = clientSocket;
    }

    public String reqParser(String req){

      String[] info = req.split("\r\n");
      String res = "";
      int l = 0;
      Pattern pattern = Pattern.compile("(\\d+)$");
      Matcher matcher = pattern.matcher(info[0]);
      if(matcher.find()){
        l = Integer.parseInt(matcher.group());
      }

      for(int i = 1; i < l; i++){
        if(info[i].equalsIgnoreCase("echo")){
          res += (info[i+1] + "\r\n" + info[i+2] + "\r\n");
          i+=2;
        }
      }

      return res;
    }

    @Override
    public void run(){
      OutputStream outputStream = null;
      BufferedReader br = null;
      String req,res;
      try {
        InputStream input = clientSocket.getInputStream();
        br = new BufferedReader(new InputStreamReader(input));
        req = br.readLine();
      } catch (Exception e) {
       System.out.println(e); 
      }
      try {

        if(br != null){
        outputStream = clientSocket.getOutputStream();

        //i think the changes are supposed to be done here
        while((req = br.readLine()) != null){

          //use parser here
          res = reqParser(req);
          outputStream.write(res.getBytes());
          outputStream.flush();
      }}
      } catch (Exception e) {
        System.out.println("IOException: " + e.getMessage());
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

      //why do i have this while(true)
      while(true)
      {
        clientSocket = serverSocket.accept();
        Thread thread = new Thread(new MyRunnable(clientSocket));
        thread.start();
      }
    } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
    } 
  }
}