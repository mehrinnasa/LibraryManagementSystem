package mehrin.loginpage;

/**
 * Book Model Class
 * Represents a book in the library system
 */
public class Book {

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int edition;
    private int quantity;
    private int remaining;
    private String section;
    private String availability;

    // Default constructor
    public Book() {
        this.availability = "Available";
    }

    // Full constructor (used by CSV + services)
    public Book(String isbn, String title, String author, String publisher,
                int edition, int quantity, int remaining,
                String section, String availability) {

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.edition = edition;
        this.quantity = quantity;
        this.remaining = remaining;
        this.section = section;
        this.availability = availability;
    }

    // ================== STANDARD GETTERS & SETTERS ==================

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getEdition() {
        return edition;
    }

    public void setEdition(int edition) {
        this.edition = edition;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
        this.availability = remaining > 0 ? "Available" : "Not Available";
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    // ================== UI ALIAS METHODS (VERY IMPORTANT) ==================
    // These methods make JavaFX TableView work WITHOUT changing CSV logic

    // Alias for bookId (used in Books.fxml & BooksController)
    public String getBookId() {
        return isbn;
    }

    public void setBookId(String bookId) {
        this.isbn = bookId;
    }

    // Alias for status (used in UI)
    public String getStatus() {
        return availability;
    }

    public void setStatus(String status) {
        this.availability = status;
    }

    // ================== CSV SUPPORT ==================

    public String toCSV() {
        return isbn + "," +
                title + "," +
                author + "," +
                publisher + "," +
                edition + "," +
                quantity + "," +
                remaining + "," +
                section + "," +
                availability;
    }

    public static Book fromCSV(String csvLine) {
        String[] parts = csvLine.split(",");
        if (parts.length >= 9) {
            return new Book(
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3],
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    parts[7],
                    parts[8]
            );
        }
        return null;
    }
}

