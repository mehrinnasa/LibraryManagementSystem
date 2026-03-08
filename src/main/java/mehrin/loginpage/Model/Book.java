package mehrin.loginpage.Model;

public class Book {

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private int    edition;
    private int    quantity;
    private int    remaining;
    private String section;
    private String availability;
    private String pdf;

    public Book() { this.availability = "Available"; this.pdf = ""; }

    public Book(String isbn, String title, String author, String publisher,
                int edition, int quantity, int remaining,
                String section, String availability) {
        this(isbn, title, author, publisher, edition, quantity, remaining, section, availability, "");
    }

    public Book(String isbn, String title, String author, String publisher,
                int edition, int quantity, int remaining,
                String section, String availability, String pdf) {
        this.isbn         = isbn;
        this.title        = title;
        this.author       = author;
        this.publisher    = publisher;
        this.edition      = edition;
        this.quantity     = quantity;
        this.remaining    = remaining;
        this.section      = section;
        this.availability = availability;
        this.pdf          = (pdf == null) ? "" : pdf.trim();
    }

    // ── Getters & Setters ────────────────────────────────────────
    public String getIsbn()               { return isbn; }
    public void   setIsbn(String v)       { this.isbn = v; }

    public String getTitle()              { return title; }
    public void   setTitle(String v)      { this.title = v; }

    public String getAuthor()             { return author; }
    public void   setAuthor(String v)     { this.author = v; }

    public String getPublisher()          { return publisher; }
    public void   setPublisher(String v)  { this.publisher = v; }

    public int  getEdition()              { return edition; }
    public void setEdition(int v)         { this.edition = v; }

    public int  getQuantity()             { return quantity; }
    public void setQuantity(int v)        { this.quantity = v; }

    public int  getRemaining()            { return remaining; }
    public void setRemaining(int v)       { this.remaining = v; this.availability = v > 0 ? "Available" : "Not Available"; }

    public String getSection()            { return section; }
    public void   setSection(String v)    { this.section = v; }

    public String getAvailability()       { return availability; }
    public void   setAvailability(String v) { this.availability = v; }

    public String getPdf()                { return pdf; }
    public void   setPdf(String v)        { this.pdf = (v == null) ? "" : v.trim(); }

    public boolean hasPdf()               { return pdf != null && !pdf.isEmpty(); }

    // ── UI aliases ───────────────────────────────────────────────
    public String getBookId()             { return isbn; }
    public void   setBookId(String v)     { this.isbn = v; }
    public String getStatus()             { return availability; }
    public void   setStatus(String v)     { this.availability = v; }

    // ── CSV: 9 columns — ISBN,Title,Author,Publisher,Edition,Qty,Remaining,Availability,PDF
    // toCSV always writes clean 9-col rows, removing any old junk duplicate column
    public String toCSV() {
        return isbn + "," + title + "," + author + "," + publisher + ","
                + edition + "," + quantity + "," + remaining + ","
                + availability + "," + pdf;
    }

    /**
     * Reads a CSV line that may be in one of these layouts:
     *   8-col : ...Remaining, Availability                          (old, no PDF col)
     *   9-col : ...Remaining, Availability, PDF-or-junk            (standard or malformed)
     *  10-col : ...Remaining, Availability, Available(junk), URL   (current broken rows)
     *
     * Rule: p[7] is always Availability.
     *   p[8] starts with "http" → it IS the PDF link (clean row).
     *   p[9] starts with "http" → p[8] was junk duplicate, p[9] is the PDF link.
     *   Otherwise               → no PDF yet.
     */
    public static Book fromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) return null;
        String[] p = csvLine.split(",", -1);
        if (p.length < 8) return null;
        try {
            String availability = p[7].trim();
            String pdfVal       = "";

            if (p.length >= 9) {
                String p8 = p[8].trim();
                if (p8.startsWith("http")) {
                    pdfVal = p8;                          // clean 9-col row with URL
                } else if (p.length >= 10 && p[9].trim().startsWith("http")) {
                    pdfVal = p[9].trim();                 // malformed 10-col: skip junk p[8]
                }
                // else p[8] = "Available" duplicate with no URL → pdfVal stays ""
            }

            return new Book(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
                    Integer.parseInt(p[4].trim()),
                    Integer.parseInt(p[5].trim()),
                    Integer.parseInt(p[6].trim()),
                    "General", availability, pdfVal);
        } catch (Exception e) { return null; }
    }
}