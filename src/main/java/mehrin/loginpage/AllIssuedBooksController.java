package mehrin.loginpage;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import mehrin.loginpage.Model.IssuedBook;
import mehrin.loginpage.Util.AutoCompleteHelper;
import mehrin.loginpage.Util.FileUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AllIssuedBooksController {

    @FXML private TextField issuedIdField;
    @FXML private TextField issuedIdInfo;
    @FXML private TextField bookIdInfo;
    @FXML private TextField studentIdInfo;
    @FXML private TextField studentNameInfo;
    @FXML private TextField lateFeeField;

    @FXML private TableView<IssuedBook> returnTable;
    @FXML private TableColumn<IssuedBook, String> issuedIdCol;
    @FXML private TableColumn<IssuedBook, String> bookIdCol;
    @FXML private TableColumn<IssuedBook, String> studentIdCol;
    @FXML private TableColumn<IssuedBook, String> studentNameCol;
    @FXML private TableColumn<IssuedBook, String> issueDateCol;
    @FXML private TableColumn<IssuedBook, String> dueDateCol;
    @FXML private TableColumn<IssuedBook, String> lateFeeCol;

    private static final String ISSUED_BOOKS_FILE = "issueBooks.csv";
    private static final String CART_DATA_FILE = "addToCart.csv";
    private static final String BOOKS_DATA_FILE = "books.csv";
    private static final String CLEARANCE_DATA_FILE = "clearance.csv";

    private IssuedBook selectedBook;
    private ObservableList<IssuedBook> allIssuedBooksList;

    @FXML
    public void initialize() {
        CartExpiryUtil.purgeExpiredCartEntries(CART_DATA_FILE, ISSUED_BOOKS_FILE);
        persistLateFees();

        setupTableColumns();
        allIssuedBooksList = loadIssuedBooks();
        returnTable.setItems(allIssuedBooksList);

        setupTableSelectionListener();
        setupSearchListener();
        setupAutoCompleteForSearch();
    }

    private void setupTableColumns() {
        issuedIdCol.setCellValueFactory(data -> data.getValue().issuedIdProperty());
        bookIdCol.setCellValueFactory(data -> data.getValue().bookIdProperty());
        studentIdCol.setCellValueFactory(data -> data.getValue().studentIdProperty());
        studentNameCol.setCellValueFactory(data -> data.getValue().studentNameProperty());
        issueDateCol.setCellValueFactory(data -> data.getValue().issuedDateProperty());
        dueDateCol.setCellValueFactory(data -> data.getValue().returnDateProperty());
        lateFeeCol.setCellValueFactory(data -> data.getValue().lateFeeProperty());
    }

    private void setupTableSelectionListener() {
        returnTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
                    selectedBook = newSelection;
                    if (newSelection != null) {
                        populateInfoPanel(newSelection);
                    }
                });
    }

    private void setupSearchListener() {
        issuedIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                returnTable.setItems(allIssuedBooksList);
                clearInfoPanel();
                selectedBook = null;
                return;
            }
            
            String query = newVal.trim().toLowerCase();
            ObservableList<IssuedBook> filteredBooks = FXCollections.observableArrayList();
            
            for (IssuedBook book : allIssuedBooksList) {
                if (book.getStudentName().toLowerCase().contains(query) ||
                    book.getStudentId().toLowerCase().contains(query) ||
                    book.getBookId().toLowerCase().contains(query) ||
                    getBookTitle(book.getBookId()).toLowerCase().contains(query)) {
                    filteredBooks.add(book);
                }
            }
            
            if (!filteredBooks.isEmpty()) {
                populateInfoPanel(filteredBooks.get(0));
                selectedBook = filteredBooks.get(0);
            } else {
                clearInfoPanel();
                selectedBook = null;
            }
            returnTable.setItems(filteredBooks);
        });
    }

    private void setupAutoCompleteForSearch() {
        AutoCompleteHelper.setupAutoComplete(issuedIdField, text ->
            FileUtil.readFile(ISSUED_BOOKS_FILE).stream()
                .filter(line -> {
                    String[] parts = line.split(",", -1);
                    return parts.length > 3 && parts[3].toLowerCase().contains(text.toLowerCase());
                })
                .map(line -> {
                    String[] parts = line.split(",", -1);
                    return parts.length > 3 ? parts[3] + " (" + parts[2] + ") - Issued ID: " + parts[0] : "";
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList()),
            chosen -> {
                try {
                    issuedIdField.setText(chosen.substring(0, chosen.indexOf(" (")));
                } catch (Exception ignored) { }
            }
        );
    }

    private void persistLateFees() {
        List<String> lines = FileUtil.readFile(ISSUED_BOOKS_FILE);
        List<String> updatedLines = new ArrayList<>();
        boolean hasChanged = false;

        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length != 7) {
                updatedLines.add(line);
                continue;
            }

            boolean isPendingCartRequest = parts[4].equalsIgnoreCase("N/A") || parts[0].startsWith("CART-");

            if (!isPendingCartRequest) {
                String correctFee = calculateLateFee(parts[5]);
                if (!parts[6].trim().equals(correctFee)) {
                    parts[6] = correctFee;
                    hasChanged = true;
                }
            }
            updatedLines.add(String.join(",", parts));
        }

        if (hasChanged) {
            FileUtil.writeFile(ISSUED_BOOKS_FILE, updatedLines,
                    "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
        }
    }

    private ObservableList<IssuedBook> loadIssuedBooks() {
        ObservableList<IssuedBook> list = FXCollections.observableArrayList();
        for (String line : FileUtil.readFile(ISSUED_BOOKS_FILE)) {
            String[] parts = line.split(",", -1);
            if (parts.length != 7) continue;

            boolean isPending = parts[4].equalsIgnoreCase("N/A") || parts[0].startsWith("CART-");
            String returnDate = parts[5];

            if (isPending) {
                String serial = parts[0].startsWith("CART-") ? parts[0].substring(5) : "";
                if (!serial.isEmpty()) {
                    returnDate = fetchReturnDateFromCart(serial, returnDate);
                }
            }

            String fee = isPending ? "0" : calculateLateFee(returnDate);
            list.add(new IssuedBook(parts[0], parts[1], parts[2], parts[3], parts[4], returnDate, fee));
        }
        return list;
    }

    private String fetchReturnDateFromCart(String cartSerial, String defaultDate) {
        for (String cartLine : FileUtil.readFile(CART_DATA_FILE)) {
            String[] cartParts = cartLine.split(",", -1);
            if (cartParts.length > 7 && cartParts[0].trim().equals(cartSerial)) {
                String cartStatus = cartParts[7].trim();
                return cartStatus.equalsIgnoreCase("Ready") ? cartParts[6].trim() : "Waiting";
            }
        }
        return defaultDate;
    }

    @FXML
    private void loadIssuedBookDetails() {
        String issuedId = issuedIdField.getText().trim();
        if (issuedId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing", "Enter Issued ID");
            return;
        }
        
        ObservableList<IssuedBook> filteredBooks = FXCollections.observableArrayList();
        for (IssuedBook book : loadIssuedBooks()) {
            if (book.getIssuedId().equalsIgnoreCase(issuedId)) {
                if (!isPendingCartRequest(book)) {
                    book.lateFeeProperty().set(calculateLateFee(book.getReturnDate()));
                }
                filteredBooks.add(book);
            }
        }
        
        if (filteredBooks.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Not Found", "No record found");
        }
        returnTable.setItems(filteredBooks);
    }

    private void populateInfoPanel(IssuedBook book) {
        if (issuedIdInfo != null) issuedIdInfo.setText(book.getIssuedId());
        if (bookIdInfo != null) bookIdInfo.setText(book.getBookId());
        if (studentIdInfo != null) studentIdInfo.setText(book.getStudentId());
        if (studentNameInfo != null) studentNameInfo.setText(book.getStudentName());
        
        String fee = isPendingCartRequest(book) ? "0" : calculateLateFee(book.getReturnDate());
        if (lateFeeField != null) {
            lateFeeField.setText(fee.equals("0") ? "No late fee" : fee + " Tk");
        }
        book.lateFeeProperty().set(fee);
    }

    @FXML
    private void submitBook(ActionEvent event) {
        if (selectedBook == null) {
            showAlert(Alert.AlertType.WARNING, "Error", "Select a book first");
            return;
        }
        if (isPendingCartRequest(selectedBook)) {
            showAlert(Alert.AlertType.WARNING, "Not Issued Yet",
                    "This book is a pending cart request and has not been issued yet.");
            return;
        }
        
        String currentLateFee = calculateLateFee(selectedBook.getReturnDate());
        selectedBook.lateFeeProperty().set(currentLateFee);
        
        if (lateFeeField != null) {
            lateFeeField.setText(currentLateFee);
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Confirm return?");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processBookReturn(currentLateFee);
        }
    }

    private void processBookReturn(String currentLateFee) {
        try {
            recordClearanceEntry(selectedBook);
            updateLateFeeInStorage(selectedBook);
            removeIssuedBookEntry();
            incrementBookInventory();

            long feeAmount = 0;
            try { 
                feeAmount = Long.parseLong(currentLateFee); 
            } catch (Exception ignored) { }
            
            if (feeAmount >= 150) {
                CartExpiryUtil.saveLateRestriction(selectedBook.getStudentId(), LocalDate.now().plusMonths(2));
            }

            allIssuedBooksList = loadIssuedBooks();
            returnTable.setItems(allIssuedBooksList);
            clearInfoPanel();
            
            String successMsg = "Book returned. Late fee: " + currentLateFee + " Tk";
            if (feeAmount >= 150) {
                successMsg += "\n⚠ Student restricted for 2 months due to excessive late fee.";
            }
            showAlert(Alert.AlertType.INFORMATION, "Success", successMsg);
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update files");
        }
    }

    private void recordClearanceEntry(IssuedBook book) throws IOException {
        String entry = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                book.getStudentName(), getBookTitle(book.getBookId()),
                book.getIssuedDate(), book.getReturnDate(),
                LocalDate.now(), "N/A",
                LocalTime.now(), book.getLateFee());
                
        List<String> lines = FileUtil.readFile(CLEARANCE_DATA_FILE);
        lines.add(entry);
        FileUtil.writeFile(CLEARANCE_DATA_FILE, lines,
                "StudentName,BookName,BorrowedDate,DueDate,ReturnDate,IssuedTime,ReturnTime,LateFee");
    }

    private String getBookTitle(String isbn) {
        if (isbn == null) return "Unknown Book";
        for (String line : FileUtil.readFile(BOOKS_DATA_FILE)) {
            String[] parts = line.split(",", -1);
            if (parts.length > 1 && parts[0].trim().equalsIgnoreCase(isbn.trim())) {
                return parts[1].trim();
            }
        }
        return "Unknown Book";
    }

    private void updateLateFeeInStorage(IssuedBook book) throws IOException {
        List<String> lines = FileUtil.readFile(ISSUED_BOOKS_FILE);
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            String[] parts = line.split(",", -1);
            if (parts.length != 7) continue;
            if (parts[0].equalsIgnoreCase(book.getIssuedId())) {
                parts[6] = book.getLateFee();
            }
            updatedLines.add(String.join(",", parts));
        }
        FileUtil.writeFile(ISSUED_BOOKS_FILE, updatedLines, "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    private void removeIssuedBookEntry() throws IOException {
        List<String> lines = FileUtil.readFile(ISSUED_BOOKS_FILE);
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith(selectedBook.getIssuedId() + ",")) {
                updatedLines.add(line);
            }
        }
        FileUtil.writeFile(ISSUED_BOOKS_FILE, updatedLines, "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    private void incrementBookInventory() throws IOException {
        List<String> books = FileUtil.readFile(BOOKS_DATA_FILE);
        List<String> updatedBooks = new ArrayList<>();
        
        for (String line : books) {
            String[] parts = line.split(",", -1);
            if (parts.length >= 8 && parts[0].equalsIgnoreCase(selectedBook.getBookId())) {
                parts[6] = String.valueOf(Integer.parseInt(parts[6].trim()) + 1);
                parts[7] = "Available";
            }
            updatedBooks.add(String.join(",", parts));
        }
        FileUtil.writeFile(BOOKS_DATA_FILE, updatedBooks, "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF");

        activateWaitingCartEntry(selectedBook.getBookId());
    }

    private void activateWaitingCartEntry(String bookIsbn) {
        File cartFile = new File("data/" + CART_DATA_FILE);
        if (!cartFile.exists()) return;

        List<String> lines = new ArrayList<>();
        boolean hasUpdated = false;

        try (BufferedReader br = new BufferedReader(new FileReader(cartFile))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { 
                    lines.add(line); 
                    isFirstLine = false; 
                    continue; 
                }
                String[] columns = line.split(",", -1);
                
                if (!hasUpdated && columns.length > 7 && columns[3].trim().equalsIgnoreCase(bookIsbn) && columns[7].trim().equalsIgnoreCase("Waiting")) {
                    columns[6] = LocalDate.now().plusDays(2).toString();
                    columns[7] = "Ready";
                    hasUpdated = true;
                }
                lines.add(String.join(",", columns));
            }
        } catch (Exception e) { 
            return; 
        }

        if (!hasUpdated) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(cartFile, false))) {
            for (String l : lines) { 
                bw.write(l); 
                bw.newLine(); 
            }
            bw.flush();
        } catch (Exception ignored) { }
    }

    private boolean isPendingCartRequest(IssuedBook book) {
        return book.getIssuedDate().equalsIgnoreCase("N/A")
                || book.getReturnDate().equalsIgnoreCase("N/A")
                || book.getIssuedId().startsWith("CART-");
    }

    private String calculateLateFee(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.equalsIgnoreCase("N/A")) return "0";
        try {
            LocalDate dueDate = LocalDate.parse(dueDateStr);
            LocalDate today = LocalDate.now();
            
            if (today.isAfter(dueDate)) {
                long fee = ChronoUnit.DAYS.between(dueDate, today) * 5;
                if (fee >= 150) fee += 300;
                return String.valueOf(fee);
            }
        } catch (Exception ignored) { }
        return "0";
    }

    @FXML 
    private void handleCancel() { 
        clearInfoPanel(); 
    }

    private void clearInfoPanel() {
        if (issuedIdField != null) issuedIdField.clear();
        if (issuedIdInfo != null) issuedIdInfo.clear();
        if (bookIdInfo != null) bookIdInfo.clear();
        if (studentIdInfo != null) studentIdInfo.clear();
        if (studentNameInfo != null) studentNameInfo.clear();
        if (lateFeeField != null) lateFeeField.clear();
        selectedBook = null;
        if (returnTable != null) returnTable.getSelectionModel().clearSelection();
    }

    private void loadPage(ActionEvent event, String fxmlPath) {
        Node node = (Node) event.getSource();
        new LoadStage(fxmlPath, node, true);
    }

    @FXML private void handleHome(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Dashboard.fxml"); }
    @FXML private void handleBooks(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Books.fxml"); }
    @FXML private void handleStudents(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Students.fxml"); }
    @FXML private void handleIssueBook(ActionEvent event) { loadPage(event, "/mehrin/loginpage/IssueBooks.fxml"); }
    @FXML private void handleAllIssuedBooks(ActionEvent event) { loadPage(event, "/mehrin/loginpage/AllIssuedBooks.fxml"); }
    @FXML private void handleAnnouncement(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Announcements.fxml"); }
    @FXML private void handleExport(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Export.fxml"); }
    @FXML private void handleClearance(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Clearance.fxml"); }
    @FXML private void logout(ActionEvent event) { loadPage(event, "/mehrin/loginpage/Login.fxml"); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title); 
        alert.setHeaderText(null);
        alert.setContentText(msg); 
        alert.showAndWait();
    }
}