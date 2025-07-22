import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

  public static class MyRunnable implements Runnable{
    private Socket clientSocket;
    public MyRunnable(Socket clientSocket){
      this.clientSocket = clientSocket;
    }

    @Override
    public void run(){
      OutputStream outputStream = null;
      BufferedReader br = null;
      try {
        InputStream input = clientSocket.getInputStream();
        br = new BufferedReader(new InputStreamReader(input));
      } catch (Exception e) {
       System.out.println(e); 
      }
      try {

        if(br != null){
        String ping;
        outputStream = clientSocket.getOutputStream();

        while((ping = br.readLine()) != null){

          if(ping.equals("PING")){
          System.out.println(ping);
          outputStream.write("+PONG\r\n".getBytes());
          outputStream.flush();}
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
      while(true)
      {
        clientSocket = serverSocket.accept();
        Thread thread = new Thread(new MyRunnable(clientSocket));
        thread.start();

        // try {
        //   // thread.join();
        // } catch (Exception e) {
        //   System.out.println(e);
        // }
      }
    } catch (IOException e) {
        System.out.println("IOException: " + e.getMessage());
    } 
  }
}