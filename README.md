# RUET Library Management System

A local, offline, high-performance Library Management System designed and implemented for CSE 2100: Software Development Project I at RUET.

The application is built using a JavaFX GUI frontend and uses a lightweight CSV-based File I/O database for offline data persistence. It supports advanced features such as real-time book availability search, personalized student dashboards, automatic queue promotion for waitlisted books, borrowing restrictions based on overdue books, and a digital clearance module.

---

## How to Run the Application Locally

### Prerequisites

- Java JDK 24 (configured in path)
- Apache Maven 3.x
- IntelliJ IDEA (Optional, recommended for FXML layout design)

### Run Command

Use the Maven wrapper to clean, compile, and run the JavaFX application:

```bash
mvn clean javafx:run
```

---

## Detailed System Features

### 1. Real-time Book Availability & Search

The system allows both admins and students to search books dynamically. When searching, the application scans the books records on the fly. Rather than displaying generic catalog descriptions, it displays real-time live copy counts (Quantity vs. Remaining copies) and dynamically adjusts the availability status (Available or Not Available) instantly as books are issued or returned.

### 2. Personalized Student Dashboards

When a student logs in, their profile is stored in a session context. The Student Dashboard dynamically loads relevant metrics tailored to that specific user:

- Their registered Name and Student ID.
- The count and list of books currently issued to them.
- Dynamic computation of any outstanding late fees.
- Targeted system announcements published by the library administration.

### 3. Add-to-Cart & Waitlist Queue Reservation

When a book's available quantity falls to 0, students can queue for it.

- **Waiting Queue**: The reservation request is written with a Waiting status.
- **Auto-Promotion**: The moment an admin processes a return of that book, the system identifies the oldest Waiting request in the queue and promotes it to Ready.
- **Collection Window**: A 2-day pickup deadline is calculated and assigned to the promoted student. If the book is not picked up within 2 days, the reservation expires.

### 4. Automated Fine Enforcement & Restrictions

A strict borrowing policy is enforced programmatically to ensure books are returned on time:

- **Late Fee Calculation**: Overdue items accumulate a fee of 5 Tk per day.
- **Excessive Overdue Penalty**: If a book is overdue by 30 days or more (accumulating a base fee of 150 Tk or higher), a penalty surcharge of 300 Tk is added, making the total fine 450 Tk.
- **Borrowing Ban (30+ Days Overdue)**: Any student with a book overdue by 30+ days is restricted from checkout actions.
- **Uncollected Reserve Ban**: If a student's reservation is promoted to Ready but they fail to collect it within 2 days, their cart entry is purged and they are restricted from reserving that specific book for 3 days.
- **Post-Return Borrowing Ban (150+ Tk Fee)**: Returning a book that accumulated a fee of 150 Tk or more triggers an automatic 2-month ban from checking out any books.

### 5. Digital Graduation Clearance

Graduating students can request clearance directly from their dashboard. The system evaluates:

- If the student has any checked-out books that have not been returned.
- If the student has any unpaid late fees.
  Clearance can only be requested if both conditions are clean (zero balance and zero checked-out books). Admins can then review and approve the digital clearance request, archiving the records.

### 6. PDF Catalog Links

For digitized publications or books with electronic versions, admins can attach a PDF link. Students can view the link and download or read the digital publication directly from their search window.

---

## System Documentation and Analyses

### 1. Feature Comparison Across Library Systems

The table below compares the features implemented in this project with existing university library systems.

| Feature                         |     RUET      |     BUET      |     SUST      |      NSU      |     BRAC      | This Project  |
| :------------------------------ | :-----------: | :-----------: | :-----------: | :-----------: | :-----------: | :-----------: |
| **Real-time Book Availability** | Not Available |   Available   |    Partial    |   Available   |    Partial    | **Available** |
| **Student Personal Account**    | Not Available |    Partial    |    Partial    |    Partial    |    Partial    | **Available** |
| **Add-to-Cart / Reservation**   | Not Available | Not Available | Not Available | Not Available | Not Available | **Available** |
| **Auto Queue Promotion**        | Not Available | Not Available | Not Available | Not Available | Not Available | **Available** |
| **Automated Fine Enforcement**  | Not Available |    Partial    |    Partial    |    Partial    | Not Available | **Available** |
| **Digital Clearance Module**    | Not Available | Not Available | Not Available | Not Available | Not Available | **Available** |
| **PDF / Digital Book Links**    | Not Available | Not Available | Not Available | Not Available | Not Available | **Available** |
| **In-System Announcements**     | Not Available | Not Available | Not Available | Not Available | Not Available | **Available** |
| **Offline / No Server Needed**  | Not Available | Not Available | Not Available | Not Available | Not Available | **Available** |

---

### 2. Gap Analysis and Solutions

This table lists the common gaps found in traditional library systems and details the technical solutions implemented in this project.

| Identified Gap                                         | Root Cause                                                                            | Solution in This Project                                                                              |
| :----------------------------------------------------- | :------------------------------------------------------------------------------------ | :---------------------------------------------------------------------------------------------------- |
| **No student personal account or borrowing dashboard** | KOHA patron module not enabled for student-facing use at RUET                         | Per-student records with issued books, cart items, and restrictions tracked within the system         |
| **No real-time availability of specific book copies**  | OPAC shows catalogue records but not intuitive live copy counts                       | Books table shows Quantity, Remaining, and Status updated on every issue and return                   |
| **No cart or reservation for out-of-stock books**      | KOHA holds module not configured; no student-facing queue at any reviewed institution | Add-to-Cart with 'Waiting' queue; auto-promoted to 'Ready' when a copy is returned (CartService)      |
| **Fine/penalty system non-functional**                 | RUET library website shows 'feature not added yet' for Fine Policy                    | Overdue books trigger automatic borrowing restrictions; admin can manage and lift restrictions        |
| **No digital clearance process**                       | Clearance handled manually via paper at semester end across all reviewed institutions | Clearance module allows admins to approve/reject clearance requests digitally with records maintained |
| **No related book discovery for students**             | OPAC keyword search does not suggest related titles or show alternatives              | Simultaneous search by title, author, and ISBN surfaces related books in a single query               |

---

### 3. Technology Stack and Justification

| Technology         | Version / Tool | Justification                                                                                                                                                                                  |
| :----------------- | :------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Java**           | JDK 24         | Core implementation language. Learned in Years 1–2 at RUET (CSE curriculum). Statically typed, cross-platform, strong OOP support, and extensive standard library.                             |
| **JavaFX**         | 21.0.6 (LTS)   | Modern Java GUI framework providing rich controls (TableView, PieChart, ComboBox, etc.). Chosen for its seamless Java integration and FXML support. Self-learned by the team at project start. |
| **FXML**           | JavaFX 21      | Declarative XML-based UI layout markup. Separates UI design from application logic, enforcing the MVC pattern and enabling parallel development of UI and backend.                             |
| **CSS (JavaFX)**   | JavaFX CSS     | Used to style the admin and student interfaces (colors, fonts, hover effects, sidebar design). Keeps styling rules separated from controller logic.                                            |
| **Apache Maven**   | 3.x (mvnw)     | Build automation and dependency management tools. Manages JavaFX dependencies and enables consistent builds via mvn clean javafx:run across developer machines.                                |
| **CSV (File I/O)** | Java java.io   | Lightweight, human-readable data persistence. No database server setup required. Appropriate for the project scale; each entity has fromCSV() / toCSV() methods for serialization.             |
| **IntelliJ IDEA**  | Latest (2025)  | Primary IDE for the team. Provides JavaFX templates, Maven integration, FXML visual editor, and Git integration for streamlined development.                                                   |
| **Git & GitHub**   | github.com     | Version control and team collaboration. Hosted at github.com/mehrinnasa/LibraryManagementSystem.                                                                                               |
| **JUnit Jupiter**  | 5.12.1         | Unit testing framework included in pom.xml for testing service and utility classes independently of the JavaFX UI.                                                                             |

---

## Project Structure and Architecture

The project enforces the Model-View-Controller (MVC) architectural design pattern, separating UI definition files (FXML) from data logic (Models) and UI interactions (Controllers).

```
LibraryManagementSystem/
├── src/main/java/mehrin/loginpage/
│   ├── Run/
│   │   └── Launcher.java                   # Entry-point bootstrap helper
│   ├── Model/
│   │   ├── Book.java                       # Book data model with availability tracking
│   │   ├── Student.java                    # Student data model (records status & fines)
│   │   ├── IssuedBook.java                 # Check-out record (dates, fees)
│   │   ├── ClearanceRecord.java            # Graduation clearance applications
│   │   ├── Announcement.java               # Global notification model
│   │   └── LoginInfo.java                  # User authentication schema
│   ├── Service/
│   │   ├── BookService.java                # Book CRUD helper
│   │   ├── StudentService.java             # Student CRUD helper
│   │   ├── CartService.java                # Waitlist logic & queue promotions
│   │   └── AnnouncementService.java        # Publishing & caching announcements
│   ├── Util/
│   │   ├── FileUtil.java                   # Low-level CSV read/write and setup helper
│   │   └── AutoCompleteHelper.java         # Text-field autocomplete UI helper
│   ├── LoginApplication.java               # Core application runner
│   ├── SessionManager.java                 # Session state holding logged-in users
│   ├── CartExpiryUtil.java                 # Expiry checker (overdues, bans, cart purges)
│   ├── LoadStage.java                      # FXML transitions utility
│   ├── MyAlert.java                        # Custom JavaFX Dialog Boxes
│   └── (Controllers)                       # UI screen action managers (Admin & Student)
└── src/main/resources/mehrin/loginpage/
    ├── loginStyle.css                      # Styling rules for Login
    ├── mainStyle.css                       # Application layout & UI themes
    └── (FXML Files)                        # UI definitions
```

---

## Team Structure, Responsibilities and Code Logic

The development team divided the modules logically to ensure frontend interfaces, data handlers, and restriction rules integrate smoothly.

| Member                    | Roll No. | Responsibilities                                                                                                                                                                                                                                                                                   |
| :------------------------ | :------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Meherin Nasa Mom**      | 2303008  | **Team Lead & Admin UI**. Designed and implemented the Admin-side interface: Admin Dashboard, Books management screen (CRUD + PDF link + search), Students registry, All Issued Books monitor, Export module, and the core Authentication/Login system. Coordinated GitHub and module integration. |
| **Maisum Maliha**         | 2303007  | **Student UI**. Developed all student-facing screens: Student Dashboard, Student Books Browser (real-time availability view), Student Add-to-Cart module, Student All Issued Books, Student Announcements view, and Student Clearance request module.                                              |
| **Sawal Islam**           | 2303005  | **Student & Book Data Layer**. Implemented the IssueBookController check-out and return workflows, the StudentBooksController, StudentsController registry, and contributed to CartExpiryUtil. Developed Book and Student model CSV serializers.                                                   |
| **Md Mominul Islam Moon** | 2303001  | **Clearance & Restriction Module**. Designed and implemented the ClearanceController, AllIssuedBooksController (overdue detection, return logic, and penalty enforcement), the ClearanceRecord data model, the Announcement service, and final integration.                                        |

---

### Code Analysis: Detailed Implementation Logic

#### Meherin Nasa Mom (Roll 2303008)

- **Authentication Logic (`LoginController`, `SessionManager`)**:
  Processes authentication checks against `loginInfo.csv`. Upon a successful match of username/email and password, the details are recorded in the `SessionManager` singleton object. This binds the active Student ID and Name, making student credentials globally available to customize user views across different stages. Includes UI password visibility toggle actions.
- **Password Change Algorithm (`ResetPassController`)**:
  Verifies that the username/email exists, validates that the current password matches, and ensures the new password satisfies length parameters (>= 3 characters). It writes updates to `loginInfo.csv` by capturing the password change date using `LocalDate.now()` (written into column 5) while preserving original registration dates (column 6).
- **Admin Dashboard Statistics (`DashboardController`)**:
  Populates status indicators on the admin home page. Reads the CSV database via `FileUtil` and populates the total book count, checked-out books, and registered students. Features a JavaFX `PieChart` visualizer comparing Available vs. Issued copies by calculating remaining vs. total quantity.

#### Maisum Maliha (Roll 2303007)

- **Student Reservation Limits (`StudentAddToCartController`)**:
  Enforces check-out safety parameters on the student side. Before allowing a student to add a book to their cart, the code checks `CartExpiryUtil.getRestrictionExpiry` to block inputs if the student has a pending ban from failing to collect a past reservation.
- **Reservation Queue Boundaries**:
  Implements validation rules in `StudentAddToCartController` that scan `addToCart.csv`. It blocks a reservation if the student already has the book in their cart, or if another student is currently holding the book in `Ready` status within their 2-day collection window.
- **Dashboard Information Filtration (`StudentDashboardController`, `StudentClearanceController`)**:
  Loads student data by querying `clearance.csv` and filtering records to match the active student name from `SessionManager.getInstance().getLoggedInStudentName()`. This allows graduation clearance records to render list data specific to that student.

#### Sawal Islam (Roll 2303005)

- **Queue-Based Book Checkout (`IssueBookController`)**:
  Designed the checkout pipeline where a librarian processes a check-out using the queue serial number (CART-xx). The code extracts student and book details from `addToCart.csv` and updates `books.csv` by decrementing inventory (`remaining--`) and updates `issueBooks.csv` by replacing the placeholder `CART-<serial>` row with active check-out parameters (IssueDate, DueDate).
- **Data Models and CSV File Serialization (`Book.java`, `Student.java`)**:
  Implemented model schemas with `.fromCSV()` and `.toCSV()` factories. Uses string split tokens to parse line data into properties (ISBN, title, author, publisher, quantity, remaining, and PDF links) and reconstructs comma-delimited strings to save files.
- **Dynamic Auto-Completion UI (`AutoCompleteHelper`)**:
  Wrote search autocomplete helpers for text fields. Employs text-change listeners and custom filtration pipelines that parse books and students on matching substring patterns in real-time, displaying auto-complete dropdown selections.

#### Md Mominul Islam Moon (Roll 2303001)

- **Dynamic Late Fee Calculation (`AllIssuedBooksController`)**:
  Calculates active late fees using `ChronoUnit.DAYS.between(dueDate, today) * 5` (5 Tk/day). Implements a penalty surcharge algorithm: if the base fee is >= 150 Tk (equivalent to 30 days overdue), a surcharge of 300 Tk is added, making the total fee 450 Tk.
- **Borrowing Restriction Enforcement**:
  Integrates checking pipelines. If a returned book has an accumulated fee >= 150 Tk, the system calls `CartExpiryUtil.saveLateRestriction` to write a 2-month borrow ban into `lateRestrictions.csv` to restrict the student's borrowing permissions.
- **Queue Promotion System (`AllIssuedBooksController`)**:
  Programmed the inventory adjustment during returns. Upon confirming a return, the controller increments inventory in `books.csv` and triggers `activateWaitingCartEntry`, which scans `addToCart.csv` for the next student in the queue with status `Waiting` and promotes them to `Ready` with a 2-day pickup deadline.
- **Digital Clearance Module & Notice Boards (`ClearanceController`, `AnnouncementService`)**:
  Coordinates the admin digital clearance panel, loading files into table views with multi-column filtering. Wrote the global `AnnouncementService` to handle CRUD operations on notice files, supporting announcement publishing, editing, and timestamping.
