package mehrin.loginpage.Model;

public class Book {

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int edition;
    private int quantity;
    private int remaining;
    private String availability; // Status
    private String pdf;          // PDF

    public Book() {
        this.availability = "Available";
    }

    public Book(String isbn, String title, String author, String publisher,
                int edition, int quantity, int remaining,
                String availability, String pdf) {

        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.edition = edition;
        this.quantity = quantity;
        this.remaining = remaining;
        this.availability = availability;
        this.pdf = pdf;
    }

    // ===== Getters & Setters =====
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getPublisher() { return publisher; }
    // public void setPublisher(String publisher) { this.publisher = publisher; }//csv te update kori

    public int getEdition() { return edition; }
    //public void setEdition(int edition) { this.edition = edition; }//csv te update kori

    public int getQuantity() { return quantity; }
    //public void setQuantity(int quantity) { this.quantity = quantity; }//csv te update kori

    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) {
        this.remaining = remaining;
        this.availability = remaining > 0 ? "Available" : "Not Available";
    }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getPdf() { return pdf; }
    public void setPdf(String pdf) { this.pdf = pdf; }

    // ===== CSV SUPPORT =====
    public String toCSV() {
        return isbn + "," + title + "," + author + "," + publisher + "," +
                edition + "," + quantity + "," + remaining + "," +
                availability + "," + pdf;
    }

    public static Book fromCSV(String line) {
        String[] p = line.split(",", -1);
        if (p.length < 9) return null;

        return new Book(
                p[0],
                p[1],
                p[2],
                p[3],
                Integer.parseInt(p[4]),
                Integer.parseInt(p[5]),
                Integer.parseInt(p[6]),
                p[7], // Availability
                p[8]  // PDF / Loan type
        );
    }
}