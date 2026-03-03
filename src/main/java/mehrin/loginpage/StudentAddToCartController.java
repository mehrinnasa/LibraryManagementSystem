package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Model.Student;
import mehrin.loginpage.Util.FileUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentAddToCartController {

    @FXML private TextField bookSearchField, studentSearchTextField;
    @FXML private Text bookName, bookAuthor, bookPublisher, availability;
    @FXML private Text studentName, studentEmail, contact;

    private static final String BOOKS_CSV = "data/books.csv";
    private static final String STUDENT_CSV = "data/students.csv";
    private static final String CART_CSV = "data/addToCart.csv";

    private Book selectedBook;
    private Student selectedStudent;

    // ================= SEARCH BOOK =================
    @FXML
    private void searchBook(KeyEvent event) {

        String isbn = bookSearchField.getText().trim();

        if (isbn.isEmpty()) {
            clearBookFields();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(BOOKS_CSV))) {

            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {

                Book book = Book.fromCSV(line);
                if (book != null && book.getIsbn().equals(isbn)) {

                    selectedBook = book;

                    bookName.setText(book.getTitle());
                    bookAuthor.setText(book.getAuthor());
                    bookPublisher.setText(book.getPublisher());
                    availability.setText(book.getAvailability());
                    return;
                }
            }

            clearBookFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEARCH STUDENT =================
    @FXML
    private void searchStudent(KeyEvent event) {

        String studentId = studentSearchTextField.getText().trim();

        if (studentId.isEmpty()) {
            clearStudentFields();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(STUDENT_CSV))) {

            br.readLine();
            String line;

            while ((line = br.readLine()) != null) {

                Student student = Student.fromCSV(line);
                if (student != null && student.getStudentId().equals(studentId)) {

                    selectedStudent = student;

                    studentName.setText(student.getName());
                    studentEmail.setText(student.getEmail());
                    contact.setText(student.getPhone());
                    return;
                }
            }

            clearStudentFields();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADD TO CART =================
    @FXML
    private void issueBook(ActionEvent event) {

        if (selectedBook == null || selectedStudent == null) {
            showAlert("Missing Info", "Please search both book and student", Alert.AlertType.WARNING);
            return;
        }

        // Duplicate check
        boolean alreadyInCart = FileUtil.readFile(CART_CSV).stream()
                .anyMatch(line -> {
                    String[] parts = line.split(",");
                    return parts.length > 2 &&
                            parts[1].equalsIgnoreCase(selectedBook.getIsbn()) &&
                            parts[2].equalsIgnoreCase(selectedStudent.getStudentId());
                });

        if (alreadyInCart) {
            showAlert("Already Added", "You already added this book to cart", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText(null);
        confirm.setContentText("Add this book to cart?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            addToCartFile();
            clearAllFields();
        }
    }

    // ================= FILE LOGIC =================
    private void addToCartFile() {

        List<String> cartList = new ArrayList<>(FileUtil.readFile(CART_CSV));

        // Generate Serial
        int lastId = 0;
        for (String line : cartList) {
            String[] parts = line.split(",");
            if (parts.length > 0 && !parts[0].equalsIgnoreCase("SerialID")) {
                try {
                    int id = Integer.parseInt(parts[0]);
                    if (id > lastId) lastId = id;
                } catch (Exception ignored) {}
            }
        }

        int newId = lastId + 1;

        LocalDate today = LocalDate.now();
        LocalDate expiry = today.plusDays(3);

        String status;

        if (selectedBook.getRemaining() > 0) {
            status = "Ready";
        } else {
            status = "Waiting";
        }

        String newLine = String.join(",",
                String.valueOf(newId),
                selectedBook.getIsbn(),
                selectedStudent.getStudentId(),
                selectedStudent.getName(),
                today.toString(),
                expiry.toString(),
                status
        );

        cartList.add(newLine);

        FileUtil.writeFile(CART_CSV, cartList,
                "SerialID,BookID,StudentID,StudentName,RequestDate,ExpiryDate,Status");

        showAlert("Success",
                "Book added to cart.\nSerial No: " + newId +
                        "\nValid Until: " + expiry,
                Alert.AlertType.INFORMATION);
    }

    // ================= CLEAR =================
    private void clearBookFields() {
        selectedBook = null;
        bookName.setText("-");
        bookAuthor.setText("-");
        bookPublisher.setText("-");
        availability.setText("-");
    }

    private void clearStudentFields() {
        selectedStudent = null;
        studentName.setText("-");
        studentEmail.setText("-");
        contact.setText("-");
    }

    private void clearAllFields() {
        clearBookFields();
        clearStudentFields();
    }

    @FXML
    private void cancel(ActionEvent event) {
        clearAllFields();
    }

    // ================= NAVIGATION =================
    @FXML private void loadHomePanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentDashboard.fxml",(Node)e.getSource(),true); }
    @FXML private void loadBooksPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentBooks.fxml",(Node)e.getSource(),true); }
    @FXML private void loadAllIssuedBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentAllIssuedBooks.fxml",(Node)e.getSource(),true); }
    @FXML private void loadAnnouncementPanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentAnnouncement.fxml",(Node)e.getSource(),true); }
    @FXML private void loadAddToCartBooksPanel(ActionEvent e) {new LoadStage("/mehrin/loginpage/StudentAddToCartBooks.fxml",(Node)e.getSource(), true);}
    @FXML private void loadClearancePanel(ActionEvent e){ new LoadStage("/mehrin/loginpage/StudentClearance.fxml",(Node)e.getSource(),true); }
    @FXML private void logout(ActionEvent e){ new LoadStage("/mehrin/loginpage/Login.fxml",(Node)e.getSource(),true); }

    // ================= ALERT =================
    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}