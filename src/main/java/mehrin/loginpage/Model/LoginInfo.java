package mehrin.loginpage.Model;

public class LoginInfo {
    private String username;
    private String email;
    private String password;
    private String userType;
    private String status;
    public LoginInfo(String username, String email, String password, String userType, String status){//constructor call
        this.username=username;
        this.email=email;
        this.password=password;
        this.userType=userType;
        this.status=status;
    }
    public String getUsername() {//username get function
        return username;
    }
    public String getUserType() {//usertype get function
        return userType;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    @Override
    public String toString() {
        return "LoginInfo{"+
                "username='"+username+'\''+
                ", email='"+email+'\''+
                ", userType='"+userType+'\''+
                ", status='"+status+'\''+
                '}';
    }

}