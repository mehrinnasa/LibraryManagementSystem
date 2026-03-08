package mehrin.loginpage.Model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClearanceRecord {
    private final StringProperty studentName;
    private final StringProperty bookName;
    private final StringProperty borrowedDate;
    private final StringProperty dueDate;
    private final StringProperty returnDate;
    private final StringProperty issuedTime;
    private final StringProperty returnTime;
    private final StringProperty lateFee;

    public ClearanceRecord(String studentName, String bookName, String borrowedDate, String dueDate, String returnDate,
                           String issuedTime, String returnTime, String lateFee) {
        this.studentName = new SimpleStringProperty(studentName);
        this.bookName = new SimpleStringProperty(bookName);
        this.borrowedDate = new SimpleStringProperty(borrowedDate);
        this.dueDate = new SimpleStringProperty(dueDate);
        this.returnDate = new SimpleStringProperty(returnDate);
        this.issuedTime = new SimpleStringProperty(issuedTime);
        this.returnTime = new SimpleStringProperty(returnTime);
        this.lateFee = new SimpleStringProperty(lateFee);
    }

    public String getStudentName() {
        return studentName.get();
    }

    public StringProperty studentNameProperty() {
        return studentName;
    }

    public String getBookName() {
        return bookName.get();
    }

    public StringProperty bookNameProperty() {
        return bookName;
    }

    public String getBorrowedDate() {
        return borrowedDate.get();
    }

    public StringProperty borrowedDateProperty() {
        return borrowedDate;
    }

    public String getDueDate() {
        return dueDate.get();
    }

    public StringProperty dueDateProperty() {
        return dueDate;
    }

    public String getReturnDate() {
        return returnDate.get();
    }

    public StringProperty returnDateProperty() {
        return returnDate;
    }

    public String getIssuedTime() {
        return issuedTime.get();
    }

    public StringProperty issuedTimeProperty() {
        return issuedTime;
    }

    public String getReturnTime() {
        return returnTime.get();
    }

    public StringProperty returnTimeProperty() {
        return returnTime;
    }

    public String getLateFee() {
        return lateFee.get();
    }

    public StringProperty lateFeeProperty() {
        return lateFee;
    }
}
