package mehrin.loginpage;

/**
 * Singleton that holds the currently logged-in user's info.
 * Set this when the student logs in, read it anywhere.
 */
public class SessionManager {

    private static SessionManager instance;

    private String loggedInStudentId   = "";
    private String loggedInStudentName = "";
    private String role                = ""; // "student" or "admin"

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // ── Getters & Setters ──────────────────────────────────────
    public String getLoggedInStudentId()   { return loggedInStudentId; }
    public String getLoggedInStudentName() { return loggedInStudentName; }
    public String getRole()                { return role; }

    public void setLoggedInStudentId(String id)     { this.loggedInStudentId   = id; }
    public void setLoggedInStudentName(String name) { this.loggedInStudentName = name; }
    public void setRole(String role)                { this.role                = role; }

    public void clear() {
        loggedInStudentId   = "";
        loggedInStudentName = "";
        role                = "";
    }
}