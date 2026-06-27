package mehrin.loginpage;
public class SessionManager {
    private static SessionManager instance;
    private String loggedInStudentId= "";
    private String loggedInStudentName= "";
    private String role="";// "student" na "admin"
    private SessionManager(){

    }
    public static SessionManager getInstance(){
        if (instance==null){
            instance=new SessionManager();
        }
        return instance;
    }
    public String getLoggedInStudentId(){
        return loggedInStudentId;
    }
    public String getLoggedInStudentName(){
        return loggedInStudentName;
    }
//    public String getRole()
//    {return role;
//    }
    public void setLoggedInStudentId(String id){
        this.loggedInStudentId=id;
    }
    public void setLoggedInStudentName(String name){
        this.loggedInStudentName=name;
    }
//    public void setRole(String role){ this.role= role; }
    //log out er time a automatic sob clear hoye jabe
    public void clear() {
        loggedInStudentId= "";
        loggedInStudentName= "";
       // role= "";
    }
}