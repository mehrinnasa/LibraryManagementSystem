package mehrin.loginpage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import mehrin.loginpage.Model.Book;
import mehrin.loginpage.Util.FileUtil;
import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.util.List;

public class ExportController {
    @FXML private TextField bookSearchField;
    @FXML private TextField driveUrlField;
    @FXML private Text bookName;
    @FXML private Text bookAuthor;
    @FXML private Text bookPublisher;
    @FXML private Text availability;
    @FXML private Label currentLinkLabel;

    public static String prefilledIsbn=null;
    private Book selectedBook=null;
    @FXML public void initialize() {
        if (prefilledIsbn != null) {
            bookSearchField.setText(prefilledIsbn);
            fillBookInfo(prefilledIsbn);
            prefilledIsbn = null;
        }
        else {
            clearBookInfo();
        }
    }
    @FXML private void searchBook(){
        String query=bookSearchField.getText().trim().toLowerCase();
        if (query.isEmpty()){
            clearBookInfo();
            return;
        }
        fillBookInfoByQuery(query);
    }
    private void fillBookInfo(String isbn) {
        fillBookInfoByQuery(isbn.toLowerCase());
    }
    private void fillBookInfoByQuery(String query){
        List<String> lines=FileUtil.readFile("books.csv");
        for (String line:lines){
            Book book=Book.fromCSV(line);
            if(book==null)
                continue;
            //book search ta isbn or title 2 ta diyei hoite pare
            if(book.getIsbn().toLowerCase().contains(query)||book.getTitle().toLowerCase().contains(query)) {
                selectedBook=book;
                bookName.setText(book.getTitle());
                bookAuthor.setText(book.getAuthor());
                bookPublisher.setText(book.getPublisher());
                availability.setText(book.getAvailability());

                if(book.hasPdf()){
                    driveUrlField.setText(book.getPdf());
                    currentLinkLabel.setText("✅ Current link:"+book.getPdf());
                    currentLinkLabel.setStyle("-fx-text-fill: #2D6A4F; -fx-font-size: 11px;");
                }
                else{
                    driveUrlField.clear();
                    currentLinkLabel.setText("No link saved yet.");
                    currentLinkLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
                }
                return;
            }
        }
        clearBookInfo();
    }
    @FXML private void handleSaveLink(ActionEvent event) {
        if (selectedBook==null) {
            showAlert(Alert.AlertType.WARNING, "No Book", "Search and select a book first."); return;
        }
        String url=driveUrlField.getText().trim();
        if(url.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No URL", "Paste a Google Drive link first."); return;
        }
        if(!url.startsWith("http")) {
            showAlert(Alert.AlertType.WARNING, "Invalid URL", "URL must start with https://\nGo to Google Drive → Share → Copy link."); return;
        }
        url=normalizeDriveUrl(url);
        if(saveLinkToCSV(selectedBook.getIsbn(), url)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "PDF added successfully for \"" + selectedBook.getTitle() + "\"!");
            bookSearchField.clear();
            clearBookInfo();
        }
        else{
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update books.csv.");
        }
    }
    @FXML private void handleClearBookInfo() {
        bookSearchField.clear();
        clearBookInfo();
    }

    @FXML private void handleClearLink(ActionEvent event) {
        if(selectedBook==null) {
            showAlert(Alert.AlertType.WARNING, "No Book", "Search and select a book first.");
            return;
        }
        if (!selectedBook.hasPdf()) {
            showAlert(Alert.AlertType.INFORMATION, "Nothing to Clear", "This book has no link saved.");
            return;
        }
        Alert confirm=new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText(null);
        confirm.setContentText("Remove the PDF link from \"" + selectedBook.getTitle() + "\"?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                if (saveLinkToCSV(selectedBook.getIsbn(), "")) {
                    selectedBook.setPdf("");
                    driveUrlField.clear();
                    currentLinkLabel.setText("Link cleared.");
                    currentLinkLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 11px;");
                }
            }
        });
    }

    @FXML private void handleView(ActionEvent event) {
        String url=driveUrlField.getText().trim();
        if (url.isEmpty()||!url.startsWith("http")) {
            showAlert(Alert.AlertType.WARNING, "No Link", "Paste or save a link first.");
            return;
        }
        openUrl(url);
    }
    private String normalizeDriveUrl(String url) {
        if (url.contains("drive.google.com/file/d/")&&url.contains("?")) {
            return url.substring(0,url.indexOf("?"));
        }
        return url;
    }
    private void openUrl(String url) {
        try{
            Desktop.getDesktop().browse(new URI(url));
        }
        catch(Exception e){
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open link: " + e.getMessage()); }
    }

    private static final String BOOKS_HEADER="ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF";
    private boolean saveLinkToCSV(String isbn, String url) {
        List<String> lines=FileUtil.readFile("books.csv");
        if(lines.isEmpty())
            return false;
        boolean changed=false;
        for(int i=0; i<lines.size(); i++){
            Book book=Book.fromCSV(lines.get(i));
            if(book==null)
                continue;
            if(book.getIsbn().equalsIgnoreCase(isbn)){
                book.setPdf(url);
                lines.set(i, book.toCSV());
                changed=true;
            }
            else{
                lines.set(i, book.toCSV()); // malformed row gulao clean kore dey
            }
        }
        if(!changed)
            return false;
        FileUtil.writeFile("books.csv", lines, BOOKS_HEADER);
        return true;
    }
    private void clearBookInfo() {
        selectedBook = null;
        bookName.setText("-"); bookAuthor.setText("-");
        bookPublisher.setText("-"); availability.setText("-");
        driveUrlField.clear();
        if (currentLinkLabel!=null){
            currentLinkLabel.setText("No link saved yet.");
            currentLinkLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        }
    }
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title);
        a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    @FXML private void handleHome(ActionEvent e){ new LoadStage("/mehrin/loginpage/Dashboard.fxml",(Node)e.getSource(), true); }
    @FXML private void handleBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/Books.fxml",(Node)e.getSource(), true); }
    @FXML private void handleStudents(ActionEvent e){ new LoadStage("/mehrin/loginpage/Students.fxml",(Node)e.getSource(), true); }
    @FXML private void handleIssueBook(ActionEvent e){ new LoadStage("/mehrin/loginpage/IssueBooks.fxml",(Node)e.getSource(), true); }
    @FXML private void handleAllIssuedBooks(ActionEvent e){ new LoadStage("/mehrin/loginpage/AllIssuedBooks.fxml",(Node)e.getSource(), true); }
    @FXML private void handleAnnouncement(ActionEvent e){ new LoadStage("/mehrin/loginpage/Announcements.fxml",(Node)e.getSource(), true); }
    @FXML private void handleExport(ActionEvent e){ new LoadStage("/mehrin/loginpage/Export.fxml",(Node)e.getSource(), true); }
    @FXML private void handleClearance(ActionEvent e){ new LoadStage("/mehrin/loginpage/Clearance.fxml",(Node)e.getSource(), true); }
    @FXML private void logout(ActionEvent e){ new LoadStage("/mehrin/loginpage/Login.fxml",(Node)e.getSource(), true); }
}