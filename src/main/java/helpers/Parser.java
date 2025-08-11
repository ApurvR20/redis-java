package helpers;
import store.KeyValueStore;

public class Parser {
    
  private int start = 0, end = 0, offset = 0;
  KeyValueStore kvStore = new KeyValueStore();

  public String commandExecutor(String req, String query){

    String res=query,key,value;
    if(query.equalsIgnoreCase("ping")){
      res = "+PONG";
    } else if(query.equalsIgnoreCase("echo")){
      value = bulkStringParser(req);
      res = "$"+value.length()+"\r\n"+value;
      offset += 1;
    } else if(query.equalsIgnoreCase("set")){
      key = bulkStringParser(req);
      value = bulkStringParser(req);
      kvStore.set(key,value);
      res = "+OK";
      offset += 2;
    } else if(query.equalsIgnoreCase("get")){
      key = bulkStringParser(req);
      offset += 1;
      if(kvStore.exists(key)){
        value = kvStore.get(key);
        res = "$"+value.length()+"\r\n"+value;
      } else {
        res = "$-1";
      }
    }

    return res;
  } 

  public int CRLFfinder(String req, int start){
    return req.indexOf("\r\n",start);
  }

  // [+/-/:]<string>\r\n
  public String simpleParser(String req){
    
    end = CRLFfinder(req, start);
    req = req.substring(start+1,end);
    start = end+2;
    return req;
  }

  // $<length>\r\n<data>\r\n
  public String bulkStringParser(String req){
    
    int len = 0; 
    String res="",query="",queryRes;
    end = CRLFfinder(req,start);
    len = Integer.parseInt(req.substring(start+1,end));
    if(len == -1) return "$-1\r\n";
    start = end+2;
    end = start+len;
    query = req.substring(start, end);
    start = end+2;

    queryRes = commandExecutor(req, query);
    if(queryRes.isEmpty()){
      res = "$"+res.length()+"\r\n"+res;
    } else {
      res = queryRes;
    }
    query = "";
    queryRes = "";

    return res;
  }

  // *<number-of-elements>\r\n<element-1>...<element-n>
  public String arrayParser(String req){
    end = CRLFfinder(req, start);
    int items = Integer.parseInt(req.substring(start+1,end));
    start = end+2;
    char ch;
    String res="";
    while(items > 0){
      ch = req.charAt(start);
      if(ch == '+' || ch == '-' || ch == ':'){
        res += (simpleParser(req)+"\r\n");
      } else if(req.charAt(start) == '$'){
        res += (bulkStringParser(req)+"\r\n");
        items -= offset;
        offset = 0;

      } else if(req.charAt(start) == '*'){
        res += arrayParser(req);
      }

      items--;
    }
    return res;
  }


  public String requestParser(String req){
    
    String res="";
    start = 0;
    end = 0;
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
    }
      catch (Exception e) {
      System.out.println("1 : IOException : "+e);
    }
    return res;
  }
}
