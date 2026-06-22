# Team Contribution Document
## RUET Library Management System — CSE 2100: Software Development Project I

> **Purpose:** This document details each team member's individual contribution to the project — which files they worked on, what logic they implemented, how it works step by step, and how to explain it during viva.

---

## Technologies Used & Why We Used Them

| Technology | Version | Why We Used It |
|:---|:---|:---|
| **Java** | JDK 24 | Core programming language. Taught in Years 1–2 at RUET. Statically typed, strong OOP support (classes, inheritance, interfaces), cross-platform via JVM, and has a rich standard library for file I/O and date operations. |
| **JavaFX** | 21.0.6 (LTS) | Modern Java GUI framework. Provides ready-made UI controls like `TableView`, `PieChart`, `ComboBox`, `ListView`, `ContextMenu`. Chosen because it integrates natively with Java without any bridge layer. The team self-learned it for this project. |
| **FXML** | JavaFX 21 | XML-based declarative UI layout language for JavaFX. Separates UI design from controller code — enforcing the MVC pattern. Each screen has its own `.fxml` file (e.g., `Login.fxml`, `Dashboard.fxml`). Allows parallel development of UI and logic. |
| **JavaFX CSS** | JavaFX built-in | Used to style all screens (colors, fonts, sidebar hover effects, button styles). Kept in `loginStyle.css` and `mainStyle.css`. Separation of styling from controller code follows clean code principles. |
| **Apache Maven** | 3.x (mvnw) | Build automation and dependency management tool. Manages JavaFX library downloads, compiles all Java files, and runs the app via `mvn clean javafx:run`. The `mvnw` wrapper ensures all team members use the same Maven version. |
| **CSV (java.io)** | Java built-in | Lightweight flat-file database. No database server required — the system works fully offline. Each entity (Book, Student, LoginInfo, etc.) has `fromCSV()` / `toCSV()` methods. Files are stored in the `data/` folder and read/written using `BufferedReader` / `BufferedWriter`. |
| **IntelliJ IDEA** | 2025 (Latest) | Primary IDE. Provides JavaFX FXML visual editor, Maven integration, Git integration, code refactoring tools, and Java debugging. |
| **Git & GitHub** | — | Version control and team collaboration. Hosted at `github.com/mehrinnasa/LibraryManagementSystem`. Used for branching, merging, and coordinating code across team members. |
| **JUnit Jupiter** | 5.12.1 | Unit testing framework declared in `pom.xml` for testing service and utility classes independently of the JavaFX UI. |

---

## Project Architecture — MVC Pattern

The project follows the **Model-View-Controller (MVC)** design pattern:

```
Model     → data/          (CSV files: books.csv, students.csv, loginInfo.csv, etc.)
View      → resources/     (.fxml files + .css files: Login.fxml, Dashboard.fxml, etc.)
Controller → java/         (.java controller files: LoginController, BooksController, etc.)
```

- **Model classes** (`Book.java`, `Student.java`, etc.) represent real-world entities and handle CSV serialization.
- **FXML files** define all UI screens declaratively (buttons, tables, labels, text fields).
- **Controller classes** handle user events (button clicks, key presses, table selections) and call service/utility methods.

### CSV Data Files (in `data/` folder)

| File | Purpose |
|:---|:---|
| `books.csv` | All book records: ISBN, Title, Author, Publisher, Edition, Quantity, Remaining, Availability, PDF |
| `students.csv` | Registered students: StudentID, Name, Phone, Email, Registration, Session, Year, Semester |
| `loginInfo.csv` | Authentication data: Username, Email, Password, UserType, Status, LastPasswordChange, AccountCreated |
| `issueBooks.csv` | Active checkouts: IssuedID, BookID, StudentID, StudentName, IssuedDate, ReturnDate, LateFee |
| `addToCart.csv` | Reservation queue: Serial, StudentID, StudentName, BookISBN, BookName, RequestDate, ExpiryDate, Status |
| `clearance.csv` | Return history for clearance: StudentName, BookName, BorrowedDate, DueDate, ReturnDate, IssuedTime, ReturnTime, LateFee |
| `cartRestrictions.csv` | Per-student/per-book bans for uncollected reservations: StudentID, BookISBN, RestrictedUntil |
| `lateRestrictions.csv` | 2-month borrowing bans for excessive late fees: StudentID, RestrictedUntil |
| `announcements.csv` | Library notice board: id, title, message, createdDateTime, updatedDateTime |

---

---

# Member 1 — Meherin Nasa Mom (Roll No: 2303008)
### Role: Team Lead & Admin UI Developer

---

## Files Worked On

| File | Type | Purpose |
|:---|:---|:---|
| `LoginController.java` | Controller | Handles the login screen — validates credentials and navigates to admin or student dashboard |
| `SessionManager.java` | Singleton Utility | Stores the currently logged-in user's ID and name in memory |
| `ResetPassController.java` | Controller | Handles password change — validates old password and writes new one to CSV |
| `DashboardController.java` | Controller | Admin home screen — shows statistics and pie chart |
| `BooksController.java` | Controller | Admin Books screen — full CRUD (Create, Read, Update, Delete) for books + search + PDF links |
| `StudentsController.java` | Controller | Admin Students screen — full CRUD for student records + live search |
| `ExportController.java` | Controller | Attaches Google Drive PDF links to books |
| `LoadStage.java` | Utility | Reusable helper to switch between FXML screens |
| `MyAlert.java` | Utility | Reusable JavaFX alert dialog wrapper |
| `LoginApplication.java` | Entry Point | Launches the JavaFX application and loads Login.fxml |
| `Login.fxml` | UI Layout | Login screen design |
| `ResetPassword.fxml` | UI Layout | Password change screen design |
| `Dashboard.fxml` | UI Layout | Admin dashboard screen design |
| `Books.fxml` | UI Layout | Admin books management screen design |
| `Students.fxml` | UI Layout | Admin students registry screen design |
| `Export.fxml` | UI Layout | PDF link export screen design |
| `loginStyle.css` | Stylesheet | Login screen styling |
| `mainStyle.css` | Stylesheet | Main application styling (sidebar, buttons, tables) |

---

## 1. Authentication Logic — `LoginController.java` + `SessionManager.java`

### What it does:
When a user enters their username/email and password and clicks Login, this code checks the credentials against `loginInfo.csv` and navigates to either the admin dashboard or student dashboard.

### How I implemented it (step by step):

**Step 1 — Read credentials from the form:**
```java
String username = usernameField.getText().trim();
String password = getEnteredPassword(); // reads from either PasswordField or visible TextField
```

**Step 2 — Search `loginInfo.csv` for a match:**
```java
private LoginInfo findValidAccount(String username, String password) {
    for (String line : FileUtil.readFile(LOGIN_DATA_FILE)) {
        String[] fields = line.split(",", -1);
        // fields[0]=StudentID, fields[1]=Email, fields[2]=Password, fields[3]=UserType, fields[4]=Status
        boolean matchesUsername = fields[0].trim().equals(username) 
                               || fields[1].trim().equalsIgnoreCase(username);
        boolean matchesPassword = fields[2].trim().equals(password) 
                               || fields[3].trim().equalsIgnoreCase(password);
        if (matchesUsername && matchesPassword) {
            return new LoginInfo(fields[0], fields[1], fields[2], fields[3], fields[4]);
        }
    }
    return null; // no match
}
```

**Step 3 — Route based on user role:**
```java
if (userRole.equals("student")) {
    setupStudentSessionAndNavigate(userAccount, sourceNode);
} else if (userRole.equals("admin")) {
    new LoadStage("/mehrin/loginpage/Dashboard.fxml", sourceNode, true);
}
```

**Step 4 — For students, store session data in `SessionManager` (singleton):**
```java
SessionManager.getInstance().setLoggedInStudentId(studentId);
SessionManager.getInstance().setLoggedInStudentName(fullName);
```
The `SessionManager` is a **singleton** — there's only one instance in the entire app (`instance == null` check). This means any controller anywhere can call `SessionManager.getInstance().getLoggedInStudentId()` and get the same value.

**Password Visibility Toggle:**
The FXML has both a `PasswordField` (masked) and a plain `TextField` (visible). When Show/Hide is clicked:
```java
if (isPasswordVisible) {
    passwordVisibility.setText(passwordField.getText()); // copy text
    passwordVisibility.setVisible(true);
    passwordField.setVisible(false); // hide the masked field
    show.setText("Hide");
}
```

### How to explain in viva:
> "I implemented the login using a CSV file as the user database. The `LoginController` reads each line of `loginInfo.csv` using `FileUtil.readFile()`, splits it by comma, and checks if the entered username or email matches column 0 or column 1, and the password matches column 2. If found, it checks the role in column 3 — if 'student' it saves the student ID and name into `SessionManager` singleton and navigates to the student dashboard; if 'admin' it goes directly to the admin dashboard. I also implemented a password show/hide toggle using two overlapping fields — one `PasswordField` and one plain `TextField` — swapping visibility on button click."

---

## 2. Password Change Algorithm — `ResetPassController.java`

### What it does:
Allows any user to change their password by verifying their current password first, then writing the new one to `loginInfo.csv`.

### How I implemented it:

**Step 1 — Validate all form fields:**
```java
if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) { ... }
if (!newPassword.equals(confirmPassword)) { ... }    // passwords must match
if (newPassword.equals(oldPassword)) { ... }          // can't reuse same password
if (newPassword.length() < 3) { ... }                 // minimum 3 characters
```

**Step 2 — Read CSV, find the matching row, and rewrite it:**
```java
while ((line = reader.readLine()) != null) {
    String[] fields = line.split(",", -1);
    // fields: [0]=Username, [1]=Email, [2]=Password, [3]=UserType, [4]=Status, [5]=LastPwdChange, [6]=AccountCreated
    
    boolean match = (csvUsername.equals(username) || csvEmail.equalsIgnoreCase(username))
                 && csvPassword.equals(oldPassword)
                 && "Active".equalsIgnoreCase(status);
    
    if (match) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String accountCreatedDate = (fields.length >= 7) ? fields[6].trim() : today;
        
        // Write the new row — update password (col 2) and LastPasswordChange (col 5)
        // Preserve AccountCreatedDate (col 6) exactly as before
        lines.add(username + "," + email + "," + newPassword + "," + userType 
                + "," + status + "," + today + "," + accountCreatedDate);
        found = true;
    }
}
```

**Key design decision:** Column 5 (`LastPasswordChange`) is updated to today's date. Column 6 (`AccountCreatedDate`) is **preserved unchanged** by reading the original value from the CSV before rewriting.

**Step 3 — Write the updated list back to the file:**
```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath, StandardCharsets.UTF_8))) {
    for (String line : lines) { writer.write(line); writer.newLine(); }
}
```

### How to explain in viva:
> "The reset password flow reads the entire `loginInfo.csv` into a list, scans for the row where the username or email matches AND the current password matches AND the status is Active. When found, it builds a new CSV row with the updated password in column 2 and today's date in column 5, but copies the original account creation date from column 6 unchanged. Then it rewrites the whole file with the modified list. I used `LocalDate.now()` from `java.time` to get today's date."

---

## 3. Admin Dashboard Statistics — `DashboardController.java`

### What it does:
When the admin logs in, the dashboard shows: total books in library, total issued books, total registered students, and a pie chart showing Available vs. Issued copies.

### How I implemented it:

**Statistics from `FileUtil`:**
```java
int totalBooks    = FileUtil.getTotalBooks();    // sums Quantity column in books.csv
int issuedBooks   = FileUtil.getIssuedBooks();   // counts rows in issueBooks.csv
int totalStudents = FileUtil.getTotalStudents(); // counts rows in students.csv

totalBooksLabel.setText(String.valueOf(totalBooks));
issuedBooksLabel.setText(String.valueOf(issuedBooks));
totalStudentsLabel.setText(String.valueOf(totalStudents));
```

**Pie Chart (JavaFX `PieChart`):**
```java
Map<String, Integer> status = FileUtil.BookStatusCount();
// BookStatusCount() reads books.csv, for each row:
//   int issued = quantity - remaining;
//   available += remaining;
//   issued += (quantity - remaining);

PieChart pie = new PieChart();
pie.getData().addAll(
    new PieChart.Data("Available", status.get("Available")),
    new PieChart.Data("Issued",    status.get("Issued"))
);
pie.setPrefSize(460, 460); // fixed size to prevent resizing
chartContainer.getChildren().add(pie);
```

**Navigation sidebar:** Each sidebar button calls `new LoadStage(...)` to switch screens:
```java
@FXML private void loadBooksPanel(ActionEvent event) {
    new LoadStage("/mehrin/loginpage/Books.fxml", (Node) event.getSource(), true);
}
```

### How to explain in viva:
> "The admin dashboard implements `Initializable` and runs setup in `initialize()`. I call `FileUtil.getTotalBooks()` which reads books.csv and sums the Quantity column, `getIssuedBooks()` which counts rows in issueBooks.csv, and `getTotalStudents()` which counts rows in students.csv. For the pie chart, `BookStatusCount()` calculates available and issued totals and returns a Map. I then create a JavaFX `PieChart` with two data points and add it to a VBox container."

---

## 4. Books Management Screen — `BooksController.java`

### What it does:
Full CRUD for the book catalog: view all books in a table, add new books, edit existing books, delete books, and open/add PDF links.

### Key implementation details:

**Live search** — reacts to every character typed in the search field:
```java
searchField.textProperty().addListener((observable, oldText, newText) -> {
    String needle = newText.toLowerCase();
    ObservableList<Book> results = FXCollections.observableArrayList();
    for (Book b : booksList) {
        if (b.getTitle().toLowerCase().contains(needle) 
         || b.getAuthor().toLowerCase().contains(needle) 
         || b.getIsbn().contains(newText)) {
            results.add(b);
        }
    }
    booksTable.setItems(results);
});
```

**Save (Add or Update):**
- If a row is selected in the table → **Update** that book
- If nothing is selected → **Add** new book
- Quantity change adjusts `remaining` proportionally: `newRemain = Math.max(0, current.getRemaining() + diff)`

**PDF column button (dynamic):**
```java
if (bk.hasPdf()) {
    btn.setText("View PDF");  // green button → opens link in browser
} else {
    btn.setText("Add PDF");   // blue button → navigates to Export screen
}
```

---

---

# Member 2 — Maisum Maliha (Roll No: 2303007)
### Role: Student UI Developer

---

## Files Worked On

| File | Type | Purpose |
|:---|:---|:---|
| `StudentAddToCartController.java` | Controller | Student reservation screen — lets students add books to queue |
| `StudentDashboardController.java` | Controller | Student home screen — shows personal info + stats |
| `StudentClearanceController.java` | Controller | Student clearance history view — shows their own borrowing records |
| `StudentBooksController.java` | Controller | Student book browser — read-only view of catalog with PDF links |
| `StudentAllIssuedBooksController.java` | Controller | Student's personal issued books list with late fee display |
| `StudentAnnouncementController.java` | Controller | Student view of library announcements |
| `StudentAddToCart.fxml` | UI Layout | Cart/reservation screen design |
| `StudentDashboard.fxml` | UI Layout | Student home screen design |
| `StudentClearance.fxml` | UI Layout | Student clearance history screen design |
| `StudentBooks.fxml` | UI Layout | Student books browser screen design |
| `StudentAllIssuedBooks.fxml` | UI Layout | Student issued books list screen design |
| `StudentAnnouncement.fxml` | UI Layout | Student announcements screen design |

---

## 1. Student Reservation Limits — `StudentAddToCartController.java`

### What it does:
When a student searches for a book and clicks "Add to Cart", multiple validation checks run **before** the reservation is accepted.

### How I implemented it (step by step):

**Step 1 — Check if student has a cart restriction for THIS book:**
```java
java.time.LocalDate restrictedUntil =
    CartExpiryUtil.getRestrictionExpiry(loggedInStudent.getStudentId(), selectedBook.getIsbn());

if (restrictedUntil != null) {
    showAlert("Restricted",
        "You did not collect this book on time when it was reserved for you.\n"
        + "You cannot add this book to cart until: " + restrictedUntil,
        Alert.AlertType.WARNING);
    return; // block the reservation
}
```
This calls `CartExpiryUtil.getRestrictionExpiry()` which reads `cartRestrictions.csv`. If a record exists and has not expired yet, the student is blocked.

**Step 2 — Scan `addToCart.csv` for this book's queue status:**
```java
for (String line : FileUtil.readFile(CART_FILE)) {
    String[] p = line.split(",", -1);
    // p[0]=Serial, p[1]=StudentID, p[3]=BookISBN, p[6]=ExpiryDate, p[7]=Status
    
    if (!p[3].equalsIgnoreCase(selectedBook.getIsbn())) continue;
    
    if (p[1].equalsIgnoreCase(loggedInStudent.getStudentId())) {
        myCartExpiry = p[6].trim(); // student already has this book in cart
    } else {
        if (p[7].equalsIgnoreCase("Waiting")) waitingByOther = true;
        if (p[7].equalsIgnoreCase("Ready"))   { readyByOther = true; otherExpiry = p[6]; }
    }
}
```

**Step 3 — Block based on scan results:**
```java
// Already in cart?
if (myCartExpiry != null) {
    showAlert("Already Added", "You already have this book in your cart.", ...);
    return;
}

// Book unavailable AND someone is waiting?
if (selectedBook.getRemaining() <= 0 && waitingByOther) {
    showAlert("Not Available", "This book is already reserved by another student.", ...);
    return;
}

// Book unavailable AND someone has a Ready 2-day window?
if (selectedBook.getRemaining() <= 0 && readyByOther) {
    showAlert("Not Available", "Reservation expires on: " + otherExpiry, ...);
    return;
}
```

**Step 4 — If all checks pass, write to `addToCart.csv`:**
```java
// Determine status: "Ready" if books are available, "Waiting" if stock is 0
String status    = (selectedBook.getRemaining() > 0) ? "Ready" : "Waiting";
String expiryStr = status.equals("Ready") ? today.plusDays(2).toString() : "N/A";

cartList.add(newId + "," + studentId + "," + studentName + "," 
           + isbn + "," + title + "," + today + "," + expiryStr + "," + status);
```
Also writes a placeholder row in `issueBooks.csv` with `CART-<serial>` as the ID and `N/A` for dates (pending until librarian processes the issue).

### How to explain in viva:
> "In `StudentAddToCartController`, before allowing a reservation I run three checks: First, I call `CartExpiryUtil.getRestrictionExpiry()` to check if the student has a ban for this specific book in `cartRestrictions.csv`. Second, I scan `addToCart.csv` to check if the student already has this book in their cart. Third, I check if the book is at 0 remaining and another student already has a Waiting or Ready status for it — if so, I block the new reservation. If all checks pass, I assign a serial number, determine if status is Ready (book available) or Waiting (book out of stock), set the expiry to today+2 days for Ready or N/A for Waiting, and write both to `addToCart.csv` and `issueBooks.csv`."

---

## 2. Dashboard Information Filtration — `StudentDashboardController.java`

### What it does:
Displays the logged-in student's personal profile (name, roll, registration, session, year, semester, phone) and library statistics.

### How I implemented it:

**Loading personal info using `SessionManager`:**
```java
@Override
public void initialize(URL location, ResourceBundle resources) {
    studentService = new StudentService();
    if (staticStudentId != null && !staticStudentId.isEmpty()) {
        setStudentId(staticStudentId); // restore from static variable on screen reload
    }
    setHeader();   // load book stats
    setPieChart(); // load pie chart
    FileUtil.syncBooks(); // sync book availability
}

private void loadStudentInfo() {
    Student student = studentService.getStudentById(StudentId);
    if (student != null) {
        nameLabel.setText(student.getName());
        rollLabel.setText(student.getStudentId());
        regLabel.setText(student.getRegistration());
        sessionLabel.setText(student.getSession());
        // ...etc
    }
}
```

A `staticStudentId` variable preserves the ID across screen navigations (since each screen reload creates a new controller instance).

---

## 3. Student Clearance History — `StudentClearanceController.java`

### What it does:
Loads only THIS student's clearance records (borrowing history after return) from `clearance.csv`, filtered by the logged-in student's name from `SessionManager`.

### How I implemented it:

```java
private void loadMyRecords() {
    // Get the logged-in student's name from session
    String myName = SessionManager.getInstance().getLoggedInStudentName();
    
    List<String> lines = FileUtil.readFile("clearance.csv");
    // clearance.csv columns:
    // 0=StudentName, 1=BookName, 2=BorrowedDate, 3=DueDate, 4=ReturnDate,
    // 5=IssuedTime, 6=ReturnTime, 7=LateFee
    
    for (String line : lines) {
        String[] p = line.split(",", -1);
        
        // Filter: only show rows where StudentName matches the logged-in student
        if (!p[0].trim().equalsIgnoreCase(myName.trim())) continue;
        
        myRecords.add(new ClearanceRecord(
            p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
    }
}
```

**Live search** by book name:
```java
searchField.textProperty().addListener((obs, oldVal, newVal) -> {
    String q = newVal.trim().toLowerCase();
    ObservableList<ClearanceRecord> filtered = FXCollections.observableArrayList();
    for (ClearanceRecord r : myRecords) {
        if (r.getBookName().toLowerCase().contains(q)) {
            filtered.add(r);
        }
    }
    clearanceTable.setItems(filtered);
});
```

### How to explain in viva:
> "The `StudentClearanceController` reads `clearance.csv` and filters it to show only records where column 0 (StudentName) matches the name stored in `SessionManager`. This is important because `clearance.csv` contains all students' borrowing records, not just the logged-in student's. The filtering uses `equalsIgnoreCase` for case-insensitive comparison. The filtered records are displayed in a JavaFX `TableView` with columns for book name, borrowed date, due date, return date, and late fee."

---

---

# Member 3 — Sawal Islam (Roll No: 2303005)
### Role: Student & Book Data Layer Developer

---

## Files Worked On

| File | Type | Purpose |
|:---|:---|:---|
| `IssueBookController.java` | Controller | Librarian checkout screen — processes book checkouts from cart queue |
| `Model/Book.java` | Model | Book data class with CSV serialization |
| `Model/Student.java` | Model | Student data class with CSV serialization |
| `Model/IssuedBook.java` | Model | Issued book record data class |
| `Model/LoginInfo.java` | Model | Login credentials data class |
| `Util/AutoCompleteHelper.java` | Utility | Real-time autocomplete dropdown for search fields |
| `Service/BookService.java` | Service | Book CRUD operations (read, add, update, delete from `books.csv`) |
| `Service/StudentService.java` | Service | Student CRUD operations (read, add, update, delete from `students.csv`) |
| `Service/CartService.java` | Service | Cart/reservation helper queries |
| `IssueBooks.fxml` | UI Layout | Issue book screen design |

---

## 1. Queue-Based Book Checkout — `IssueBookController.java`

### What it does:
When a librarian types a CART serial number (e.g., `CART-3`) and clicks Issue, the system:
1. Looks up the student and book from `addToCart.csv`
2. Decrements the book's remaining count in `books.csv`
3. Replaces the placeholder `CART-3` row in `issueBooks.csv` with real issue data (today's date + 14-day due date)
4. Removes the entry from `addToCart.csv`

### How I implemented it (step by step):

**Step 1 — Search `addToCart.csv` by serial number:**
```java
private boolean tryFillFromCart(String serial) {
    // serial is the number part only, e.g., "3" if user typed "CART-3"
    for (String line : FileUtil.readFile(CART_FILE)) {
        String[] p = line.split(",", -1);
        // p[0]=Serial, p[1]=StudentID, p[3]=BookISBN
        if (p.length >= 8 && p[0].trim().equals(serial)) {
            bookSearchField.setText(p[3].trim());   // fill book ISBN field
            searchBook(null);                        // load book info
            studentSearchTextField.setText(p[1].trim()); // fill student ID field
            searchStudent(null);                     // load student info
            return true;
        }
    }
    return false;
}
```

**Step 2 — Decrement `remaining` in `books.csv`:**
```java
for (String line : books) {
    String[] p = line.split(",", -1);
    if (p[0].equalsIgnoreCase(bookSearchField.getText().trim())) {
        int remaining = Integer.parseInt(p[6].trim());
        if (remaining <= 0) { showAlert("Not Available", ...); return; }
        remaining--;                           // decrement remaining count
        p[6] = String.valueOf(remaining);
        p[7] = (remaining == 0) ? "Not Available" : "Available"; // update status
    }
    updatedBooks.add(String.join(",", p));
}
FileUtil.writeFile("books.csv", updatedBooks, "ISBN,Title,Author,Publisher,...");
```

**Step 3 — Replace `CART-<serial>` row in `issueBooks.csv` with real data:**
```java
LocalDate today   = LocalDate.now();
LocalDate dueDate = today.plusDays(14); // 14-day loan period

for (String line : issuedLines) {
    String[] p = line.split(",", -1);
    if (p[0].equalsIgnoreCase(cartKey)) {  // cartKey = "CART-3"
        p[0] = String.valueOf(newIssuedId);  // assign real numeric ID
        p[4] = today.toString();             // IssuedDate
        p[5] = dueDate.toString();           // ReturnDate / DueDate
        p[6] = "0";                          // LateFee starts at 0
        pendingUpdated = true;
    }
    updatedIssued.add(String.join(",", p));
}
```

**Step 4 — Remove entry from `addToCart.csv`:**
```java
private void removeFromCart(String serial) {
    List<String> updated = new ArrayList<>();
    for (String line : FileUtil.readFile(CART_FILE)) {
        String[] p = line.split(",", -1);
        if (!p[0].trim().equals(serial)) { // keep all rows except this serial
            updated.add(line);
        }
    }
    FileUtil.writeFile(CART_FILE, updated, "Serial,StudentID,...");
}
```

**Direct issue (no cart):** If the librarian manually enters student and book without a cart serial, the code detects `cartSerial` is empty (`!pendingUpdated`) and appends a brand new row directly to `issueBooks.csv`.

### How to explain in viva:
> "The `IssueBookController` works by the librarian entering a CART serial number. The `tryFillFromCart()` method reads `addToCart.csv` and finds the row whose first column matches the serial, then auto-fills the book ISBN and student ID search fields. When Issue is confirmed, `issueBookInFile()` does three things: first it decrements the `remaining` count in `books.csv` and sets status to 'Not Available' if zero; second it finds the matching `CART-<serial>` row in `issueBooks.csv` and replaces it with the real issue data — today's date and 14 days from now as the due date; third it removes the cart entry. If there was no cart entry (direct issue), it simply appends a new row to `issueBooks.csv`."

---

## 2. Data Models and CSV Serialization — `Book.java` and `Student.java`

### What it does:
These model classes represent real-world entities and handle converting between Java objects and CSV strings.

### Book.java — how I implemented it:

**Fields:**
```java
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
```

**`toCSV()` — converts the object to a CSV string:**
```java
public String toCSV() {
    return isbn + "," + title + "," + author + "," + publisher + ","
         + edition + "," + quantity + "," + remaining + ","
         + availability + "," + pdf;
}
// Output example: 978-0-13-468599-1,Clean Code,Robert C. Martin,Prentice Hall,1,3,2,Available,
```

**`fromCSV()` — parses a CSV line back into a Book object:**
```java
public static Book fromCSV(String csvLine) {
    String[] p = csvLine.split(",", -1);
    if (p.length < 8) return null;
    
    // Handle old 8-col rows (no PDF), standard 9-col, and malformed 10-col rows
    String availability = p[7].trim();
    String pdfVal = "";
    if (p.length >= 9) {
        String p8 = p[8].trim();
        if (p8.startsWith("http")) {
            pdfVal = p8;       // clean 9-col row with URL
        } else if (p.length >= 10 && p[9].trim().startsWith("http")) {
            pdfVal = p[9].trim(); // malformed 10-col: skip junk p[8]
        }
    }
    
    return new Book(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(),
        Integer.parseInt(p[4].trim()),  // edition
        Integer.parseInt(p[5].trim()),  // quantity
        Integer.parseInt(p[6].trim()),  // remaining
        "General", availability, pdfVal);
}
```

**Student.java** works the same way:
```java
// toCSV():
return studentId + "," + name + "," + phone + "," + email + ","
     + registration + "," + session + "," + year + "," + semester;

// fromCSV():
String[] parts = csvLine.split(",");
if (parts.length >= 8) {
    return new Student(parts[0].trim(), parts[1].trim(), parts[2].trim(),
                       parts[3].trim(), parts[4].trim(), parts[5].trim(),
                       parts[6].trim(), parts[7].trim());
}
```

`Student.java` also has a `validateCredentials()` method and a `fromLoginInfoCSV()` factory for the login system.

### How to explain in viva:
> "The `Book` and `Student` model classes follow the JavaBean pattern with private fields and public getters/setters. The `toCSV()` method converts the object to a comma-separated string using string concatenation. The `fromCSV()` static factory method takes a CSV line, splits it by comma using `split(",", -1)` — the `-1` limit ensures trailing empty fields are included — parses integer fields with `Integer.parseInt()`, and returns a new object. For `Book`, I added special handling for old CSV rows that had 8, 9, or 10 columns by checking which column index starts with 'http' to find the PDF field."

---

## 3. Dynamic Auto-Completion UI — `AutoCompleteHelper.java`

### What it does:
Attaches a real-time suggestion dropdown to any `TextField`. As the user types, matching suggestions appear below the field. Clicking a suggestion fills the field.

### How I implemented it:

```java
public static void setupAutoComplete(TextField textField,
        Function<String, List<String>> fetchSuggestions,  // called with current text, returns matches
        Consumer<String> onSuggestionChosen) {            // called when user selects a suggestion

    ContextMenu popup = new ContextMenu();

    textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue == null || newValue.trim().isEmpty()) { popup.hide(); return; }
        if (!newValue.equals(oldValue)) {               // only re-fetch if text actually changed
            List<String> suggestions = fetchSuggestions.apply(newValue);
            if (suggestions == null || suggestions.isEmpty()) { popup.hide(); }
            else {
                popup.getItems().clear();
                int count = 0;
                for (String suggestion : suggestions) {
                    if (count++ >= 10) break;           // max 10 suggestions shown
                    Label label = new Label(suggestion);
                    label.prefWidthProperty().bind(textField.widthProperty().subtract(10));
                    
                    CustomMenuItem item = new CustomMenuItem(label, true);
                    item.setOnAction(e -> {
                        textField.setText(suggestion);
                        popup.hide();
                        if (onSuggestionChosen != null) onSuggestionChosen.accept(suggestion);
                    });
                    popup.getItems().add(item);
                }
                if (!popup.isShowing()) popup.show(textField, Side.BOTTOM, 0, 0);
            }
        }
    });

    // Hide when field loses focus
    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
        if (!newVal) popup.hide();
    });
}
```

**How it's called in `IssueBookController` for book search:**
```java
AutoCompleteHelper.setupAutoComplete(bookSearchField,
    text -> FileUtil.readFile("books.csv").stream()
        .skip(1)                           // skip header row
        .filter(line -> line.toLowerCase().contains(text.toLowerCase()))
        .map(line -> {
            String[] p = line.split(",", -1);
            return p.length > 1 ? p[1] + " (" + p[0] + ")" : ""; // "Title (ISBN)"
        })
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList()),
    chosen -> {
        // Extract ISBN from "Title (ISBN)" format
        String isbn = chosen.substring(chosen.lastIndexOf("(") + 1, chosen.lastIndexOf(")"));
        bookSearchField.setText(isbn);
        searchBook(null); // trigger full book info load
    });
```

### How to explain in viva:
> "The `AutoCompleteHelper` uses the **Strategy pattern** via functional interfaces. It takes two lambdas: `fetchSuggestions` (a `Function<String, List<String>>`) which is called with the current typed text to get matching items, and `onSuggestionChosen` (a `Consumer<String>`) which is called when the user clicks a suggestion. Inside, I attach a `textProperty().addListener()` to the TextField. Every time the text changes, I call the fetch function, clear and rebuild the `ContextMenu` with up to 10 `CustomMenuItem` entries, and show it below the text field using `popup.show(textField, Side.BOTTOM, 0, 0)`. When a user clicks a suggestion, the callback is invoked — for books, this extracts the ISBN from the suggestion string format 'Title (ISBN)' and triggers a full book info lookup."

---

---

# Member 4 — Md Mominul Islam Moon (Roll No: 2303001)
### Role: Clearance, Restrictions & Integration Developer

---

## Files Worked On

| File | Type | Purpose |
|:---|:---|:---|
| `AllIssuedBooksController.java` | Controller | Admin screen — view all issued books, calculate late fees, process returns, enforce bans |
| `ClearanceController.java` | Controller | Admin digital clearance screen — view all return history records |
| `CartExpiryUtil.java` | Utility | Handles all borrowing restrictions, cart expiry, and late restriction logic |
| `Service/AnnouncementService.java` | Service | CRUD operations for library announcements saved in `announcements.csv` |
| `AnnouncementController.java` | Controller | Admin announcements management screen — post, edit, delete announcements |
| `Model/ClearanceRecord.java` | Model | Clearance history record data class |
| `Model/Announcement.java` | Model | Announcement data class |
| `AllIssuedBooks.fxml` | UI Layout | All issued books admin screen design |
| `Clearance.fxml` | UI Layout | Admin clearance screen design |
| `Announcements.fxml` | UI Layout | Admin announcements screen design |

---

## 1. Dynamic Late Fee Calculation — `AllIssuedBooksController.java`

### What it does:
Calculates how much a student owes for an overdue book, live, at the moment it is viewed or returned. The fee is not stored until return — it's computed each time from the due date.

### How I implemented it:

```java
private String calculateLateFee(String dueDateStr) {
    if (dueDateStr == null || dueDateStr.equalsIgnoreCase("N/A")) return "0";
    try {
        LocalDate dueDate = LocalDate.parse(dueDateStr);  // parse "2026-06-15"
        LocalDate today   = LocalDate.now();
        
        if (today.isAfter(dueDate)) {                     // book is overdue
            long fee = ChronoUnit.DAYS.between(dueDate, today) * 5; // 5 Tk per overdue day
            if (fee >= 150) fee += 300; // penalty surcharge if 30+ days overdue (150 Tk = 30 days × 5)
            return String.valueOf(fee);
        }
    } catch (Exception ignored) { }
    return "0";  // not yet overdue
}
```

**Example:** If due date was 20 days ago:
- Base fee = 20 × 5 = 100 Tk → returned as "100"

**Example:** If due date was 35 days ago:
- Base fee = 35 × 5 = 175 Tk → 175 >= 150 → total = 175 + 300 = **475 Tk**

**Persist fees on load** (`persistLateFees()`): Every time the screen opens, it recalculates and writes the correct fee back to `issueBooks.csv` so the CSV is always up-to-date.

### How to explain in viva:
> "I use `ChronoUnit.DAYS.between(dueDate, today)` from `java.time.temporal` to get the number of days overdue. I multiply by 5 to get the base fee in Taka. If the base fee reaches 150 Taka (which means 30 days overdue), I add a penalty surcharge of 300 Tk — making the total 450+ Tk. This is enforced as a surcharge, not a replacement. The method returns '0' if the book is not overdue. I also run `persistLateFees()` on screen initialization which recalculates and saves fees for all non-pending rows in `issueBooks.csv`."

---

## 2. Borrowing Restriction Enforcement — `CartExpiryUtil.java`

### What it does:
When a book is returned with a fee >= 150 Tk, the student is automatically banned from checking out any books for 2 months. This ban is written to `lateRestrictions.csv`.

### How I implemented it:

**On book return (in `AllIssuedBooksController.processBookReturn()`):**
```java
long feeAmount = Long.parseLong(currentLateFee);

if (feeAmount >= 150) {
    // Ban student for 2 months from today
    CartExpiryUtil.saveLateRestriction(
        selectedBook.getStudentId(), 
        LocalDate.now().plusMonths(2)
    );
}
```

**`saveLateRestriction()` in `CartExpiryUtil.java`:**
```java
public static void saveLateRestriction(String studentId, LocalDate until) {
    List<String> lines   = FileUtil.readFile("lateRestrictions.csv");
    List<String> updated = new ArrayList<>();
    boolean found = false;
    
    for (String line : lines) {
        String[] p = line.split(",", -1);
        if (p[0].trim().equalsIgnoreCase(studentId)) {
            updated.add(studentId + "," + until); // update existing entry
            found = true;
        } else { 
            updated.add(line); // keep other students' entries
        }
    }
    if (!found) updated.add(studentId + "," + until); // add new entry
    
    FileUtil.writeFile("lateRestrictions.csv", updated, "StudentID,RestrictedUntil");
}
```

**`isLateRestricted()` check (used when a student tries to check out):**
```java
public static boolean isLateRestricted(String studentId) {
    // Case 1: currently 30+ days overdue on an active book
    for (String line : FileUtil.readFile("issueBooks.csv")) {
        // check if any issued book for this student is 30+ days past due date
        long daysLate = ChronoUnit.DAYS.between(LocalDate.parse(retDate), today);
        if (daysLate >= 30) return true;
    }
    
    // Case 2: stored 2-month ban in lateRestrictions.csv
    for (String line : FileUtil.readFile("lateRestrictions.csv")) {
        LocalDate until = LocalDate.parse(p[1].trim());
        if (!today.isAfter(until) && p[0].equalsIgnoreCase(studentId)) {
            return true; // ban is still active
        }
    }
    return false;
}
```

### How to explain in viva:
> "The restriction system has two components in `CartExpiryUtil`. First, `isLateRestricted()` checks two conditions: if the student currently has any book overdue by 30 or more days (checking `issueBooks.csv` live), or if they have a stored 2-month ban in `lateRestrictions.csv` that has not yet expired. Second, `saveLateRestriction()` is called by `AllIssuedBooksController` when a book is returned with a fee of 150 Tk or more — it writes `StudentID,RestrictedUntil` to `lateRestrictions.csv`, where RestrictedUntil is 2 months from today using `LocalDate.now().plusMonths(2)`."

---

## 3. Queue Promotion System — `AllIssuedBooksController.java`

### What it does:
When a book is returned, the system automatically promotes the oldest "Waiting" reservation for that book to "Ready" status, giving that student a 2-day pickup window.

### How I implemented it:

**Return flow calls `incrementBookInventory()` which calls `activateWaitingCartEntry()`:**
```java
private void incrementBookInventory() throws IOException {
    // Step 1: Increment remaining count in books.csv
    for (String line : books) {
        String[] parts = line.split(",", -1);
        if (parts[0].equalsIgnoreCase(selectedBook.getBookId())) {
            parts[6] = String.valueOf(Integer.parseInt(parts[6].trim()) + 1); // remaining++
            parts[7] = "Available";
        }
        updatedBooks.add(String.join(",", parts));
    }
    FileUtil.writeFile(BOOKS_DATA_FILE, updatedBooks, "ISBN,Title,Author,...");
    
    // Step 2: Promote the oldest Waiting cart entry for this book
    activateWaitingCartEntry(selectedBook.getBookId());
}
```

**`activateWaitingCartEntry()` — the queue promotion logic:**
```java
private void activateWaitingCartEntry(String bookIsbn) {
    List<String> lines = new ArrayList<>();
    boolean hasUpdated = false; // only promote ONE (the first/oldest) waiting entry
    
    try (BufferedReader br = new BufferedReader(new FileReader(cartFile))) {
        String line;
        boolean isFirstLine = true;
        while ((line = br.readLine()) != null) {
            if (isFirstLine) { lines.add(line); isFirstLine = false; continue; } // preserve header
            
            String[] columns = line.split(",", -1);
            // columns[3]=BookISBN, columns[7]=Status
            
            if (!hasUpdated 
             && columns[3].trim().equalsIgnoreCase(bookIsbn) 
             && columns[7].trim().equalsIgnoreCase("Waiting")) {
                
                columns[6] = LocalDate.now().plusDays(2).toString(); // set 2-day expiry
                columns[7] = "Ready";                                // promote to Ready
                hasUpdated = true;                                   // stop — only promote first match
            }
            lines.add(String.join(",", columns));
        }
    }
    
    if (hasUpdated) { /* write updated list back to addToCart.csv */ }
}
```

**Important:** The `!hasUpdated` condition ensures only the **first** (oldest) Waiting entry is promoted. This maintains queue order — first come, first served.

### How to explain in viva:
> "When `submitBook()` processes a return, it calls `incrementBookInventory()` which increments the remaining count and updates the status to 'Available' in `books.csv`. Then it calls `activateWaitingCartEntry()` which reads `addToCart.csv` line by line looking for a row where BookISBN matches and Status is 'Waiting'. The `!hasUpdated` flag ensures only the first such row is promoted — this maintains FIFO (first-in, first-out) queue order. The promoted row gets its ExpiryDate set to today + 2 days and Status changed from 'Waiting' to 'Ready'. The student whose status became 'Ready' now has 2 days to collect the book before their reservation expires."

---

## 4. Digital Clearance Module — `ClearanceController.java`

### What it does:
Displays all students' return history records from `clearance.csv` in a searchable table. Admins can filter by student name or book name.

### How I implemented it:

```java
private void loadClearanceData() {
    allRecords.clear();
    List<String> lines = FileUtil.readFile("clearance.csv");
    
    // clearance.csv columns: 0=StudentName, 1=BookName, 2=BorrowedDate, 3=DueDate,
    //                        4=ReturnDate, 5=IssuedTime, 6=ReturnTime, 7=LateFee
    for (String line : lines) {
        String[] p = line.split(",", -1);
        if (p.length >= 8) {
            allRecords.add(new ClearanceRecord(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        }
        // Backward compatibility: handle older 7-col or 6-col rows with N/A defaults
        else if (p.length >= 7) {
            allRecords.add(new ClearanceRecord(p[0], p[1], p[2], "N/A", p[3], p[4], p[5], p[6]));
        }
    }
}
```

**How records are written (in `AllIssuedBooksController.recordClearanceEntry()`):**
```java
String entry = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
    book.getStudentName(),
    getBookTitle(book.getBookId()),  // look up title from ISBN
    book.getIssuedDate(),            // original borrow date
    book.getReturnDate(),            // due date
    LocalDate.now(),                 // actual return date (today)
    "N/A",                           // issued time placeholder
    LocalTime.now(),                 // actual return time
    book.getLateFee()                // final fee at time of return
);
```

---

## 5. Announcement Service — `Service/AnnouncementService.java`

### What it does:
Full CRUD (Create, Read, Update, Delete) for library announcements, stored in `announcements.csv`. Timestamps are recorded for each post and edit.

### How I implemented it:

**CSV format:** `id,title,message,createdDateTime,updatedDateTime`

**Add:**
```java
public void addAnnouncement(Announcement a) {
    try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE_PATH, true))) { // append mode
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        w.write(getNextId() + "," + a.getTitle() + "," + a.getMessage() + "," + now + ",");
        // updatedDateTime left empty on first post
    }
}
```

**Update** (overwrites entire file with modified record):
```java
public void updateAnnouncement(Announcement updated) {
    String now = LocalDateTime.now().format(FORMATTER);
    for (Announcement a : all) {
        if (a.getId() == updated.getId()) {
            a.setTitle(updated.getTitle());
            a.setMessage(updated.getMessage());
            a.setUpdatedDateTime(now);   // stamp edit timestamp
        }
        // write all records back to file
    }
}
```

**Delete** (rewrites file skipping the deleted ID):
```java
public void deleteAnnouncement(int id) {
    for (Announcement a : all) {
        if (a.getId() != id) {  // write everything EXCEPT the deleted ID
            w.write(a.getId() + "," + a.getTitle() + "," + ...);
        }
    }
}
```

**Auto-incrementing ID:**
```java
private int getNextId() {
    int max = 0;
    for (Announcement a : getAllAnnouncements()) {
        if (a.getId() > max) max = a.getId();
    }
    return max + 1; // always 1 higher than the current maximum
}
```

### How to explain in viva:
> "The `AnnouncementService` handles the CRUD operations for announcements stored in `announcements.csv`. For Add, I use `FileWriter` in append mode (`new FileWriter(path, true)`) so I don't overwrite existing entries. For Update and Delete, I read all announcements into a list, modify or skip the target entry, then rewrite the entire file using a new `FileWriter`. I use `LocalDateTime.now().format(DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm'))` to create timestamps. The ID is auto-incremented by finding the current maximum ID and adding 1."

---

---

# Shared Utilities — Used by All Members

## `FileUtil.java` — CSV Read/Write Foundation

Used by **every single controller** in the project. Provides:

```java
// Read all non-header, non-empty lines from a CSV file
public static List<String> readFile(String filename) {
    // prepends "data/" to filename automatically
    // skips the header line (firstLine = true)
    // skips empty lines
    // returns list of data lines
}

// Write list of lines back to a CSV file (with header)
public static void writeFile(String filename, List<String> lines, String header) {
    // writes header first, then all lines
}

// Dashboard statistics helpers:
public static int getTotalBooks()         // sums Quantity column in books.csv
public static int getIssuedBooks()        // counts rows in issueBooks.csv
public static int getTotalStudents()      // counts rows in students.csv
public static Map<String,Integer> BookStatusCount() // computes Available/Issued totals

// Sync available counts from issueBooks.csv back to books.csv
public static void syncBooks()
```

## `CartExpiryUtil.java` — All Restriction Logic

Handles four types of restrictions:

| Method | Purpose |
|:---|:---|
| `purgeExpiredCartEntries()` | Removes expired Ready reservations; bans students who didn't collect |
| `getRestrictionExpiry()` | Checks if student has a per-book cart ban (3-day after missing collection) |
| `isLateRestricted()` | Checks if student has a 2-month ban (from 150+ Tk fee) or current 30+ day overdue |
| `saveLateRestriction()` | Writes a 2-month ban to `lateRestrictions.csv` |

## `LoadStage.java` — Screen Navigation

```java
// Used everywhere for screen transitions — reuses the same Window
new LoadStage("/mehrin/loginpage/Dashboard.fxml", sourceNode, true);
```

---

# Quick Viva Reference — Common Questions

| Question | Answer |
|:---|:---|
| **What design pattern is used?** | MVC (Model-View-Controller) + Singleton (SessionManager) + Strategy (AutoCompleteHelper functional interfaces) |
| **How is data stored?** | CSV files in the `data/` folder, read/written using Java's `BufferedReader` / `BufferedWriter` |
| **How does login work?** | `LoginController` reads `loginInfo.csv`, matches username/email + password, routes to admin or student dashboard |
| **How are late fees calculated?** | `ChronoUnit.DAYS.between(dueDate, today) * 5`; if >= 150 Tk then +300 Tk surcharge |
| **How does queue promotion work?** | On book return, `activateWaitingCartEntry()` finds the first row with Status='Waiting' for that ISBN and changes it to 'Ready' with today+2 day expiry |
| **How does the autocomplete work?** | `AutoCompleteHelper` attaches a `textProperty().addListener()` that calls a lambda to fetch suggestions, builds a `ContextMenu` with `CustomMenuItem` entries shown below the field |
| **How is session maintained?** | `SessionManager` singleton stores `loggedInStudentId` and `loggedInStudentName` in memory for the app's lifetime |
| **What is `CART-<n>` in issueBooks.csv?** | A placeholder row written when a student adds to cart, replaced with a real issue record when the librarian processes the checkout |
| **What happens if a student doesn't collect a reserved book?** | `CartExpiryUtil.purgeExpiredCartEntries()` deletes the expired Ready cart entry and writes a 3-day ban for that student+book to `cartRestrictions.csv` |
| **How to run the project?** | `mvn clean javafx:run` from the project root |
