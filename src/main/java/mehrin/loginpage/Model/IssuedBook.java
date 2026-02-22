package mehrin.loginpage.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class IssuedBook {

    private StringProperty issuedId;
    private StringProperty bookId;
    private StringProperty studentId;
    private StringProperty studentName;
    private StringProperty issuedDate;
    private StringProperty returnDate;
    private StringProperty lateFee;

    public IssuedBook(String issuedId, String bookId,
                      String studentId, String studentName,
                      String issuedDate, String returnDate, String lateFee) {

        this.issuedId = new SimpleStringProperty(issuedId);
        this.bookId = new SimpleStringProperty(bookId);
        this.studentId = new SimpleStringProperty(studentId);
        this.studentName = new SimpleStringProperty(studentName);
        this.issuedDate = new SimpleStringProperty(issuedDate);
        this.returnDate = new SimpleStringProperty(returnDate);
        this.lateFee = new SimpleStringProperty(lateFee);
    }

    // Property getters for TableView
    public StringProperty issuedIdProperty() { return issuedId; }
    public StringProperty bookIdProperty() { return bookId; }
    public StringProperty studentIdProperty() { return studentId; }
    public StringProperty studentNameProperty() { return studentName; }
    public StringProperty issuedDateProperty() { return issuedDate; }
    public StringProperty returnDateProperty() { return returnDate; }
    public StringProperty lateFeeProperty() { return lateFee; }

    // Normal getters if needed
    public String getIssuedId() { return issuedId.get(); }
    public String getBookId() { return bookId.get(); }
    public String getStudentId() { return studentId.get(); }
    public String getStudentName() { return studentName.get(); }
    public String getIssuedDate() { return issuedDate.get(); }
    public String getReturnDate() { return returnDate.get(); }
    public String getLateFee() { return lateFee.get(); }
}