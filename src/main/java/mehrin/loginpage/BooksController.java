package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Service.BookService;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BooksController implements Initializable {

    @FXML private TableView<Book> booksTable;
    @FXML private TableColumn<Book, String> bookId;
    @FXML private TableColumn<Book, String> title;
    @FXML private TableColumn<Book, String> author;
    @FXML private TableColumn<Book, String> status;
    @FXML private TableColumn<Book, String> publisher;
    @FXML private TableColumn<Book, String> edition;
    @FXML private TableColumn<Book, String> quantity;
    @FXML private TableColumn<Book, String> remainingBooks;
    @FXML private TableColumn<Book, Void> pdfCol;

    @FXML private TextField searchField;
    @FXML private TextField bookIdField;
    @FXML private TextField bookTitleField;
    @FXML private TextField authorField;
    @FXML private TextField publisherField;
    @FXML private TextField editionField;
    @FXML private TextField quantityField;
    @FXML private ComboBox<String> statusComboBox;

    private final ObservableList<Book> booksList = FXCollections.observableArrayList();
    private BookService bookService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        bookService = new BookService();
        statusComboBox.setItems(FXCollections.observableArrayList("Available", "Not Available"));

        setupTableColumns();
        setupPdfColumn();
        loadBooks();
        setupSearch();
        setupTableClick();
    }

    private void setupTableColumns() {
        bookId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getIsbn()));
        title.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        author.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        status.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAvailability()));
        publisher.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPublisher()));
        edition.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getEdition())));
        quantity.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getQuantity())));
        remainingBooks.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getRemaining())));
    }

    private void setupPdfColumn() {
        pdfCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setStyle("-fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                btn.setOnAction(e -> {
                    Book book = getTableView().getItems().get(getIndex());
                    if (book.hasPdf()) {
                        openUrl(book.getPdf());
                    } else {
                        ExportController.prefilledIsbn = book.getIsbn();
                        new LoadStage("/mehrin/loginpage/Export.fxml", btn.getScene().getRoot(), true);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { 
                    setGraphic(null); 
                    return; 
                }
                
                Book book = getTableView().getItems().get(getIndex());
                if (book.hasPdf()) {
                    btn.setText("View PDF");
                    btn.setStyle("-fx-background-color: #2D6A4F; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                } else {
                    btn.setText("Add PDF");
                    btn.setStyle("-fx-background-color: #143F73; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-background-radius: 4;");
                }
                setGraphic(btn);
            }
        });
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open link:\n" + e.getMessage());
        }
    }

    private void loadBooks() {
        booksList.setAll(bookService.getAllBooks());
        booksTable.setItems(booksList);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) { 
                booksTable.setItems(booksList); 
                return; 
            }
            
            String query = newVal.toLowerCase();
            ObservableList<Book> filteredBooks = FXCollections.observableArrayList();
            
            for (Book book : booksList) {
                if (book.getTitle().toLowerCase().contains(query) || 
                    book.getAuthor().toLowerCase().contains(query) || 
                    book.getIsbn().contains(newVal)) {
                    filteredBooks.add(book);
                }
            }
            booksTable.setItems(filteredBooks);
        });
    }

    private void setupTableClick() {
        booksTable.getSelectionModel().selectedItemProperty().addListener((obs, oldBook, book) -> {
            if (book != null) {
                bookIdField.setText(book.getIsbn());
                bookTitleField.setText(book.getTitle());
                authorField.setText(book.getAuthor());
                publisherField.setText(book.getPublisher());
                editionField.setText(String.valueOf(book.getEdition()));
                quantityField.setText(String.valueOf(book.getQuantity()));
                statusComboBox.setValue(book.getAvailability());
            }
        });
    }

    @FXML
    private void handleSave() {
        if (areAnyFieldsEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Book ID, Title, Author and Status are required.");
            return;
        }

        int editionValue = parseIntField(editionField.getText(), 1, "Edition");
        if (editionValue == -1) return;
        
        int quantityValue = parseIntField(quantityField.getText(), 1, "Quantity");
        if (quantityValue == -1) return;

        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        
        if (selectedBook != null) {
            updateExistingBook(selectedBook, editionValue, quantityValue);
        } else {
            addNewBook(editionValue, quantityValue);
        }
        
        clearForm();
        loadBooks();
    }

    private boolean areAnyFieldsEmpty() {
        return bookIdField.getText().trim().isEmpty() || 
               bookTitleField.getText().trim().isEmpty() || 
               authorField.getText().trim().isEmpty() || 
               statusComboBox.getValue() == null;
    }

    private int parseIntField(String valueStr, int defaultValue, String fieldName) {
        if (valueStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(valueStr.trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", fieldName + " must be a valid number.");
            return -1;
        }
    }

    private void updateExistingBook(Book book, int newEdition, int newQuantity) {
        int quantityDiff = newQuantity - book.getQuantity();
        int newRemaining = Math.max(0, book.getRemaining() + quantityDiff);

        String publisherVal = publisherField.getText().trim();

        book.setIsbn(bookIdField.getText().trim());
        book.setTitle(bookTitleField.getText().trim());
        book.setAuthor(authorField.getText().trim());
        book.setPublisher(publisherVal.isEmpty() ? book.getPublisher() : publisherVal);
        book.setEdition(newEdition);
        book.setQuantity(newQuantity);
        book.setRemaining(newRemaining);
        book.setAvailability(statusComboBox.getValue());

        writeBookToCSV(book);
    }

    private void addNewBook(int edition, int quantity) {
        String publisherVal = publisherField.getText().trim();
        Book newBook = new Book(
            bookIdField.getText().trim(),
            bookTitleField.getText().trim(),
            authorField.getText().trim(),
            publisherVal.isEmpty() ? "Unknown" : publisherVal,
            edition, 
            quantity, 
            quantity, 
            "General", 
            statusComboBox.getValue()
        );
        writeBookToCSV(newBook);
    }

    private boolean writeBookToCSV(Book book) {
        File csvFile = new File("data/books.csv");
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    lines.add("ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF");
                    isFirstLine = false;
                    continue;
                }
                
                Book existingBook = Book.fromCSV(line);
                if (existingBook != null && existingBook.getIsbn().equalsIgnoreCase(book.getIsbn())) {
                    book.setPdf(existingBook.getPdf());
                    lines.add(book.toCSV());
                    found = true;
                } else {
                    lines.add(existingBook != null ? existingBook.toCSV() : line);
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Read Error", "Cannot read books.csv: " + e.getMessage());
            return false;
        }

        if (!found) {
            lines.add(book.toCSV());
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(csvFile, false))) {
            for (String l : lines) { 
                bw.write(l); 
                bw.newLine(); 
            }
            bw.flush();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Write Error", "Cannot write books.csv: " + e.getMessage());
            return false;
        }

        return true;
    }

    @FXML
    private void handleDelete() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        
        if (selectedBook == null) { 
            showAlert(Alert.AlertType.ERROR, "Error", "Please select a book to delete."); 
            return; 
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("Are you sure you want to delete \"" + selectedBook.getTitle() + "\"?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) { 
            bookService.deleteBook(selectedBook.getIsbn()); 
            loadBooks(); 
            clearForm(); 
        }
    }

    @FXML 
    private void handleCancel() { 
        clearForm(); 
        booksTable.getSelectionModel().clearSelection(); 
    }

    private void clearForm() {
        bookIdField.clear(); 
        bookTitleField.clear(); 
        authorField.clear();
        publisherField.clear(); 
        editionField.clear(); 
        quantityField.clear();
        statusComboBox.setValue(null);
    }

    // Navigation handlers
    private void loadPage(ActionEvent event, String fxmlPath) {
        Node node = (Node) event.getSource();
        new LoadStage(fxmlPath, node, true);
    }

    @FXML private void loadHomePanel(ActionEvent e) { loadPage(e, "/mehrin/loginpage/Dashboard.fxml"); }
    @FXML private void loadBooksPanel(ActionEvent e) { loadPage(e, "/mehrin/loginpage/Books.fxml"); }
    @FXML private void loadStudentPanel(ActionEvent e) { loadPage(e, "/mehrin/loginpage/Students.fxml"); }
    @FXML private void loadIssueBooksPanel(ActionEvent e) { loadPage(e, "/mehrin/loginpage/IssueBooks.fxml"); }
    @FXML private void viewAllIssuedBooks(ActionEvent e) { loadPage(e, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void loadSendAnnouncementsPanel(ActionEvent e) { loadPage(e, "/mehrin/loginpage/Announcements.fxml"); }
    @FXML private void loadExportDataPanel(ActionEvent e) { loadPage(e, "/mehrin/loginpage/Export.fxml"); }
    @FXML private void loadClearancePanel(ActionEvent e) { loadPage(e, "/mehrin/loginpage/Clearance.fxml"); }
    @FXML private void logout(ActionEvent e) { loadPage(e, "/mehrin/loginpage/Login.fxml"); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type); 
        alert.setTitle(title); 
        alert.setHeaderText(null); 
        alert.setContentText(msg); 
        alert.showAndWait();
    }
}