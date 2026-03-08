package mehrin.loginpage.Model;

public class Announcement {

    private int    id;
    private String title;
    private String message;
    private String createdDateTime;   // when first posted
    private String updatedDateTime;   // last edited (empty if never updated)

    public Announcement() {}

    /** Full constructor */
    public Announcement(int id, String title, String message,
                        String createdDateTime, String updatedDateTime) {
        this.id              = id;
        this.title           = title;
        this.message         = message;
        this.createdDateTime = createdDateTime;
        this.updatedDateTime = updatedDateTime;
    }

    /** Legacy 4-arg constructor so nothing else breaks */
    public Announcement(int id, String title, String message, String createdDateTime) {
        this(id, title, message, createdDateTime, "");
    }

    // ── Getters & Setters ──────────────────────────────────────
    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public String getTitle()           { return title; }
    public void   setTitle(String t)   { this.title = t; }

    public String getMessage()         { return message; }
    public void   setMessage(String m) { this.message = m; }

    public String getCreatedDateTime()           { return createdDateTime; }
    public void   setCreatedDateTime(String dt)  { this.createdDateTime = dt; }

    /** Kept for backward compatibility with any code calling getDateTime() */
    public String getDateTime()        { return createdDateTime; }
    public void   setDateTime(String dt){ this.createdDateTime = dt; }

    public String getUpdatedDateTime()           { return updatedDateTime; }
    public void   setUpdatedDateTime(String dt)  { this.updatedDateTime = dt; }

    public boolean wasUpdated() {
        return updatedDateTime != null && !updatedDateTime.trim().isEmpty();
    }
}