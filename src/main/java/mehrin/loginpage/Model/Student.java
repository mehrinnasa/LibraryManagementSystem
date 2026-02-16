package mehrin.loginpage.Model;

/**
 * Student Model Class
 * Represents a student in the library system
 */
public class Student {
    private String studentId;
    private String name;
    private String phone;
    private String email;

    public Student() {}

    public Student(String studentId, String name, String phone, String email) {
        this.studentId = studentId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // CSV conversion
    public String toCSV() {
        return studentId + "," + name + "," + phone + "," + email;
    }

    public static Student fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length >= 4) {
            return new Student(parts[0], parts[1], parts[2], parts[3]);
        }
        return null;
    }
}
