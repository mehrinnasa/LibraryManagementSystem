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
    private String registration;
    private String session;
    private String year;
    private String semester;
    private String password;
    private String status;

    public Student() {}

    public Student(String studentId, String name, String phone, String email) {
        this.studentId = studentId;
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    public Student(String studentId, String name, String phone, String email,
                   String registration, String session, String year, String semester) {
        this.studentId = studentId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.registration = registration;
        this.session = session;
        this.year = year;
        this.semester = semester;
    }

    public Student(String studentId, String name, String phone, String email,
                   String registration, String session, String year, String semester,
                   String password, String status) {
        this.studentId = studentId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.registration = registration;
        this.session = session;
        this.year = year;
        this.semester = semester;
        this.password = password;
        this.status = status;
    }

    // ================= GETTERS =================
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getRegistration() { return registration; }
    public String getSession() { return session; }
    public String getYear() { return year; }
    public String getSemester() { return semester; }
    public String getPassword() { return password; }
    public String getStatus() { return status; }

    // ================= SETTERS =================
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setRegistration(String registration) { this.registration = registration; }
    public void setSession(String session) { this.session = session; }
    public void setYear(String year) { this.year = year; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setPassword(String password) { this.password = password; }
    public void setStatus(String status) { this.status = status; }

    // ================= CSV CONVERSION =================
    public String toCSV() {
        return studentId + "," + name + "," + phone + "," + email + "," +
                registration + "," + session + "," + year + "," + semester;
    }

    public static Student fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length >= 8) {
            return new Student(
                    parts[0].trim(),  // studentId
                    parts[1].trim(),  // name
                    parts[2].trim(),  // phone
                    parts[3].trim(),  // email
                    parts[4].trim(),  // registration
                    parts[5].trim(),  // session
                    parts[6].trim(),  // year
                    parts[7].trim()   // semester
            );
        }
        return null;
    }

    public static Student fromLoginInfoCSV(String csvLine) {
        String[] fields = csvLine.split(",");
        if (fields.length >= 5) {
            Student student = new Student();
            student.setStudentId(fields[0].trim());
            student.setEmail(fields[1].trim());
            student.setPassword(fields[2].trim());
            student.setStatus(fields[4].trim());
            return student;
        }
        return null;
    }

    // ================= VALIDATION =================
    public boolean validateCredentials(String username, String inputPassword) {
        boolean usernameMatches = this.studentId.equals(username) ||
                this.email.equalsIgnoreCase(username);
        boolean passwordMatches = this.password.equals(inputPassword);
        return usernameMatches && passwordMatches && "Active".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", registration='" + registration + '\'' +
                ", session='" + session + '\'' +
                ", year='" + year + '\'' +
                ", semester='" + semester + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}