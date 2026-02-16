package mehrin.loginpage.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Announcement Model Class
 * Represents an announcement in the RUET Library Management System
 */
public class Announcement {
    private int id;
    private String title;
    private String message;
    private String date;

    // Default constructor
    public Announcement() {
        this.date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Constructor with all fields
    public Announcement(int id, String title, String message, String date) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    // Convert to CSV format (replace commas in message to avoid CSV issues)
    public String toCSV() {
        return id + "," + title + "," + message.replace(",", ";") + "," + date;
    }

    // Create Announcement from CSV line
    public static Announcement fromCSV(String csvLine) {
        String[] parts = csvLine.split(",", 4); // Split into max 4 parts
        if (parts.length >= 4) {
            return new Announcement(
                    Integer.parseInt(parts[0]),
                    parts[1],
                    parts[2].replace(";", ","), // Convert back semicolons to commas
                    parts[3]
            );
        }
        return null;
    }

    @Override
    public String toString() {
        return title + " - " + date;
    }
}