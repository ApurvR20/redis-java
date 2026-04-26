/*
 - do diff clients have diff kvstore? should they?
- 

- check if I'm correctly parsing time
- check if im correctly rolling back if px is not listed
- if im using a single queue to process all, how tf im supposed to insert the expiry at the correct location? say i have an expiry at 500 ms, and right now time is 200 ms. how do i schedule it so it doesnt block other requests between 200 and 500 ms?
- then comes the main processing, how do i handle that in case of event loop solution?
- can i let the keys delete themselves, bu letting them have a thread (?). (suicide bomb approach?) ?
- another idea : keys keep track of the expiry, then submit request to the main thread to be deleted (prison approach)
- do i need to change the logic of how CRLF is parsed over? Seems too complicated imo.
 */

package helpers;
import store.KeyValueStore;

public class Parser {
    
  private int start = 0, end = 0;
  private KeyValueStore kvStore;

  public Parser(KeyValueStore kvstore){
    this.kvStore = kvstore;
  }

  public String commandExecutor(String req, String query){

    String res=query,key,value,next;
    boolean willExpire;
    int expiryTime;
    if(query.equalsIgnoreCase("ping")){
      res = "+PONG";
    }
    else if(query.equalsIgnoreCase("echo")){
      value = bulkStringParser(req);
      res = "$"+value.length()+"\r\n"+value;
    }
    else if(query.equalsIgnoreCase("set")){
      willExpire = false;
      expiryTime = 0;
      key = bulkStringParser(req);
      value = bulkStringParser(req);

      if(start < req.length()){
        next = bulkStringParser(req);
        willExpire = true;
        if(next.equalsIgnoreCase("PX")){
          expiryTime = Integer.parseInt(bulkStringParser(req));
        }
      }
      kvStore.set(key,value,willExpire,expiryTime);
      res = "+OK";
    }
    else if(query.equalsIgnoreCase("get")){
      key = bulkStringParser(req);
      value = kvStore.get(key);
      if(value.isEmpty()){
        res = "$-1";
      } else {
        res = "$"+value.length()+"\r\n"+value;
      }
    }

    return res;
  } 

  public int CRLFfinder(String req, int start) {
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
    start = end+2;
    char ch;
    String res="";
    int strLen = req.length();
    while(start < strLen){
      ch = req.charAt(start);
      if(ch == '+' || ch == '-' || ch == ':'){
        res += (simpleParser(req)+"\r\n");
      } else if(req.charAt(start) == '$'){
        res += (bulkStringParser(req)+"\r\n");
      } else if(req.charAt(start) == '*'){
        res += arrayParser(req);
      }
    }
    return res;
  }


  public String requestParser(String req){

    String res="";
    start = 0;
    end = 0;
    char firstChar = req.charAt(0);

    //processing req into res as a RESP bulk string
    if(firstChar == '+' || firstChar == '-' || firstChar == ':'){
      res = simpleParser(req);
    } else if(firstChar == '$'){
      res = bulkStringParser(req);
    } else {
      res = arrayParser(req);
    }

    return res;
  }
}
