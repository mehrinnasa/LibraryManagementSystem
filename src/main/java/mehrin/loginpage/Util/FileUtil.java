package mehrin.loginpage.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * File Utility Class
 * Handles all CSV file reading and writing operations
 * Creates data directory and sample files automatically
 */
public class FileUtil {

    private static final String DATA_DIR = "data/";

    /**
     * Initialize data directory and create sample files if they don't exist
     */
    public static void initializeDataFiles() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
            System.out.println("✓ Created data directory");
        }

        // Create sample files if they don't exist
        createSampleBooksFile();
        createSampleStudentsFile();
        createSampleAnnouncementsFile();
    }

    /**
     * Read all lines from a CSV file
     */
    public static List<String> readFile(String filename) {
        List<String> lines = new ArrayList<>();
        String filepath = DATA_DIR + filename;

        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + filepath);
            return lines;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }

    /**
     * Write lines to a CSV file
     */
    public static void writeFile(String filename, List<String> lines, String header) {
        String filepath = DATA_DIR + filename;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))) {
            // Write header
            bw.write(header);
            bw.newLine();

            // Write data lines
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }

            System.out.println("✓ Data saved to: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create sample books.csv file with 15 books
     */
    private static void createSampleBooksFile() {
        File file = new File(DATA_DIR + "books.csv");
        if (file.exists()) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Section,Availability");
            bw.newLine();

            // Sample book data - exactly as shown in your screenshot
            String[][] books = {
                    {"9780071161619", "Data Structures with C", "John R. Hubbard P.", "McGrawHill", "2", "1", "1", "Long Loan Book", "Available"},
                    {"9780071362078", "COMPUTER ARCHI...", "NICHOLAS CARTER...", "McGrawHill", "2", "1", "1", "Long Loan Book", "Available"},
                    {"9781259255663", "Using Information T...", "Williams Sawyer", "McGraw Hill", "11", "6", "6", "Long Loan Book", "Available"},
                    {"9781259029936", "Object Oriented Pr...", "E Balagurusamy", "Mc Graw Hill Educa...", "6", "1", "1", "Long Loan Book", "Available"},
                    {"9780340815809", "Browse's Introducti...", "Norman L. Browse ...", "BookPower", "4", "1", "1", "Short Loan Book", "Available"},
                    {"9780446577069", "Battlefield of the Mi...", "JOYCE MEYER", "Faith Words", "1", "1", "1", "Long Loan Book", "Available"},
                    {"9780071364355", "OPERATING SYSTE...", "J. ARCHER HARRIS", "McGraw-Hill", "1", "1", "1", "Short Loan Book", "Available"},
                    {"9780007580422", "12 YEARS A SLAVE", "Solomon Northup", "CENTRAL BOOKSH...", "5", "1", "1", "Long Loan Book", "Available"},
                    {"9780443064081", "ESSENTIAL SURGERY", "H. George Burkitt C...", "CHURCHILL LIVING...", "3", "1", "1", "Long Loan Book", "Available"},
                    {"9781111530488", "Discovering Compu...", "Gary B. Shelly Mist...", "COURSE TECHNOL...", "0", "10", "10", "Long Loan Book", "Available"},
                    {"9780495244493", "MATLAB Programmi...", "Stephen J. Chapman", "CENGAGE Learning", "4", "9", "9", "Short Loan Book", "Available"},
                    {"9780307887894", "THE LEAN STARTUP", "Eric Ries", "CROWN BUSINESS", "1", "1", "1", "Short Loan Book", "Available"},
                    {"9780273787105", "Systems Analysis an...", "Kenneth E. Kendall ...", "PEARSON", "9", "1", "1", "Long Loan Book", "Available"},
                    {"9780131854758", "Corporate Computer...", "Raymond R. Panko", "PEARSON", "2", "1", "1", "Short Loan Book", "Available"},
                    {"9788131792544", "Data Structures Usi...", "A. K. Sharma", "PearsonIn", "2", "1", "1", "Long Loan Book", "Available"}
            };

            for (String[] book : books) {
                bw.write(String.join(",", book));
                bw.newLine();
            }

            System.out.println("✓ Created books.csv with 15 sample books");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create sample students.csv file with 17 students
     */
    private static void createSampleStudentsFile() {
        File file = new File(DATA_DIR + "students.csv");
        if (file.exists()) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("StudentID,Name,Phone,Email");
            bw.newLine();

            // Sample student data - exactly as shown in your screenshot
            String[][] students = {
                    {"Bsckt/1/15", "Aaron Kika", "+265994744444", "info@deepenglish.com"},
                    {"Bsckt/10/15", "Mrs Kadzakumanja", "+265884444444", "meriynkapakasa@gmail.com"},
                    {"Bsckt/11/15", "Fernando Maganga", "+265889023891", "fernandomaganga@gmail.com"},
                    {"Bsckt/12/15", "Vin Chimizimu", "+265888737987", "vinsntchimizimu@gmail.com"},
                    {"Bsckt/13/15", "Ernest Kim", "+265995020284", "ernestkim87@gmail.com"},
                    {"Bsckt/14/15", "Dennis Phakula", "+265999217765", "Dennisphakula@gmail.com"},
                    {"Bsckt/15/15", "Bright Issah", "+265884234639", "brightis@gmail.com"},
                    {"Bsckt/16/15", "Felix Kamwana", "+265998844444", "kamwanafelix@gmail.com"},
                    {"Bsckt/17/15", "Bright Phiri", "+265993555222", "mangodeveloper214@gmail.com"},
                    {"Bsckt/2/15", "Stone White", "+265994646463", "support@stoneriverlearning.com"},
                    {"Bsckt/3/15", "Sinch kim", "+265993322222", "dev@sinch.com"},
                    {"Bsckt/4/15", "Emm Banda", "+265993305832", "emmanuelmnjowe@gmail.com"},
                    {"Bsckt/5/15", "Angel Pedrego", "+265994328888", "angel@mytrum.com"},
                    {"Bsckt/6/15", "Clara Ntokotha", "+265888026265", "Clarantokotha@gmail.com"},
                    {"Bsckt/7/15", "Jane Ndundu", "+265992389008", "noreply@paylab.com"},
                    {"Bsckt/8/15", "Ashan kANTENGULE", "+265994043591", "kantenguleshan@gmail.com"},
                    {"Bsckt/9/15", "Dickson Mwase", "+265881004588", "mwasedickson200@gmail.com"}
            };

            for (String[] student : students) {
                bw.write(String.join(",", student));
                bw.newLine();
            }

            System.out.println("✓ Created students.csv with 17 sample students");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create sample announcements.csv file
     */
    private static void createSampleAnnouncementsFile() {
        File file = new File(DATA_DIR + "announcements.csv");
        if (file.exists()) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("ID,Title,Message,Date");
            bw.newLine();
            bw.write("1,Welcome,Welcome to RUET Library Management System!,2026-01-31 10:00:00");
            bw.newLine();

            System.out.println("✓ Created announcements.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}