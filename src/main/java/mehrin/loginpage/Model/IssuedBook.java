package mehrin.loginpage.Model;

import javafx.beans.property.SimpleStringProperty;

public class IssuedBook {

    private SimpleStringProperty issuedId;
    private SimpleStringProperty studentId;
    private SimpleStringProperty bookId;
    private SimpleStringProperty issueDate;
    private SimpleStringProperty dueDate;
    private SimpleStringProperty lateFee;

    // Constructor used in AllIssuedBooksController
    public IssuedBook(String issuedId, String studentId, String bookId,
                      String issueDate, String dueDate, String lateFee) {
        this.issuedId = new SimpleStringProperty(issuedId);
        this.studentId = new SimpleStringProperty(studentId);
        this.bookId = new SimpleStringProperty(bookId);
        this.issueDate = new SimpleStringProperty(issueDate);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.lateFee = new SimpleStringProperty(lateFee);
    }

    // Getters
    public String getIssuedId() {
        return issuedId.get();
    }

    public String getStudentId() {
        return studentId.get();
    }

    public String getBookId() {
        return bookId.get();
    }

    public String getIssueDate() {
        return issueDate.get();
    }

    public String getDueDate() {
        return dueDate.get();
    }

    public String getLateFee() {
        return lateFee.get();
    }

    // Setters (optional, if you want to modify values later)
    public void setIssuedId(String issuedId) {
        this.issuedId.set(issuedId);
    }

    public void setStudentId(String studentId) {
        this.studentId.set(studentId);
    }

    public void setBookId(String bookId) {
        this.bookId.set(bookId);
    }

    public void setIssueDate(String issueDate) {
        this.issueDate.set(issueDate);
    }

    public void setDueDate(String dueDate) {
        this.dueDate.set(dueDate);
    }

    public void setLateFee(String lateFee) {
        this.lateFee.set(lateFee);
    }
}
