package mehrin.loginpage;



import mehrin.loginpage.Book;
import mehrin.loginpage.FileUtil;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    private static final String BOOKS_FILE = "books.csv";
    private static final String HEADER = "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Section,Availability";
    private List<Book> books;

    public BookService() {
        loadBooks();
    }

   /* private void loadBooks() {
        books = new ArrayList<>();
        List<String> lines = FileUtil.readFile(BOOKS_FILE);
        for (String line : lines) {
            Book book = Book.fromCSV(line);
            if (book != null) books.add(book);
        }
    }*/

    public List<Book> getAllBooks() {
        return new ArrayList<>(books);
    }

    public boolean addBook(Book book) {
        books.add(book);
        saveBooks();
        return true;
    }

    public boolean deleteBook(String isbn) {
        books.removeIf(book -> book.getIsbn().equals(isbn));
        saveBooks();
        return true;
    }

    public boolean updateBook(Book updatedBook) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getIsbn().equals(updatedBook.getIsbn())) {
                books.set(i, updatedBook);
                saveBooks();
                return true;
            }
        }
        return false;
    }

    public List<Book> searchBooks(String query) {
        List<Book> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(lowerQuery) ||
                    book.getAuthor().toLowerCase().contains(lowerQuery) ||
                    book.getIsbn().contains(query)) {
                results.add(book);
            }
        }
        return results;
    }

    public int getTotalBooks() {
        return books.size();
    }

    public int getTotalRemainingBooks() {
        int total = 0;
        for (Book book : books) total += book.getRemaining();
        return total;
    }

    public int getIssuedBooksCount() {
        int issued = 0;
        for (Book book : books) issued += (book.getQuantity() - book.getRemaining());
        return issued;
    }

    private void saveBooks() {
        List<String> lines = new ArrayList<>();
        for (Book book : books) lines.add(book.toCSV());
        //FileUtil.writeFile(BOOKS_FILE, lines, HEADER);
    }
}