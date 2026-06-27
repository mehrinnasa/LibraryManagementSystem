package mehrin.loginpage.Util;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtil {

    private static final String DATA = "data/";

    public static void initializeDataFiles() {
        File file = new File(DATA);
        if (!file.exists()) {
            file.mkdirs();
            System.out.println("✓ Created data directory");
        }

    }

// ================== ISSUED BOOK COUNT (FROM issueBooks.csv) ==================
public static int getIssuedBooks() {
    List<String> lines = readFile("issueBooks.csv");
    return lines.size(); // header already skipped
}
//Read data on csv
    //static tai object lagbe na direct FileUtil.readFile("students.csv") emne call korte parbo
    public static List<String> readFile(String filename) {
        List<String> lines=new ArrayList<>();
        String filepath=DATA+filename;//dhoro filename book.csv to eitar file path 'data/book.csv'
        //line pora ses hole close kore dibe
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) //BufferedReader pura line by line eksate pore
        {
            String line;
            boolean firstLine=true;
            while((line=br.readLine())!=null) {
                if(firstLine){
                    firstLine=false; //header skip korbo
                    continue;
                }
                if (!line.trim().isEmpty()){//faka line na hole list a add kora
                    lines.add(line);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            return lines;
        }
        catch(IOException e)
        {
            e.printStackTrace();//error er bistarito console a print kore
        }
        return lines;
    }

    /**
     * Write lines to a CSV file
     */
    public static void writeFile(String filename, List<String> lines, String header) {
        String filepath = DATA + filename;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filepath))) {
            bw.write(header);
            bw.newLine();

            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }

           // System.out.println("✓ Data saved to: " + filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static int getTotalBooks() {
        List<String> lines = readFile("books.csv");
        int total = 0;

        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;//empty line skip korbe

            String[] parts = line.split(",", -1); // -1 to include empty strings & Split CSV line by comma

            if (parts[0].equalsIgnoreCase("ISBN")) continue;//header k skip korbe
                //equalsIgnoreCase case sensitivity ignore kore
            try {
                int quantity = Integer.parseInt(parts[5].trim());//string to int
                total += quantity;
            } catch (NumberFormatException e) {//sting k int banate gele je exception chokhe pore
                // skip if Quantity is empty or invalid
            }
        }

        return total;
    }


    public static int getTotalStudents() {
        List<String> lines = readFile("students.csv");
        return lines.size();
    }
    public static Map<String, Integer> BookStatusCount() {
        Map<String, Integer> map = new HashMap<>();
        map.put("Available", 0);
        map.put("Issued", 0);

        List<String> lines = readFile("books.csv");
        for (String line : lines) {
            try {
                String[] parts = line.split(",");
                int quantity = Integer.parseInt(parts[5].trim()); // Quantity
                int remaining = Integer.parseInt(parts[6].trim()); // Remaining

                int issued = quantity - remaining;

                map.put("Available", map.get("Available") + remaining);
                map.put("Issued", map.get("Issued") + issued);
            } catch (Exception e) {
                // skip invalid line
            }
        }

        return map;
    }

        // ================== SYNC BOOKS WITH ISSUED BOOKS ==================
        public static void syncBooks() {

            List<String> issuedLines = readFile("issueBooks.csv");
            Map<String, Integer> issuedCountMap = new HashMap<>();

            for (String line : issuedLines) {
                String[] parts = line.split(",");
                String bookId = parts[1];

                issuedCountMap.put(
                        bookId,
                        issuedCountMap.getOrDefault(bookId, 0) + 1
                );
            }

            List<String> bookLines = readFile("books.csv");
            List<String> updatedBooks = new ArrayList<>();

            for (String line : bookLines) {
                String[] parts = line.split(",");

                String bookId = parts[0];
                int quantity = Integer.parseInt(parts[5]);
                int issued = issuedCountMap.getOrDefault(bookId, 0);
                int remaining = quantity - issued;

                parts[6] = String.valueOf(remaining);              // Remaining
                parts[7] = remaining > 0 ? "Available" : "Not Available"; // Availability

                updatedBooks.add(String.join(",", parts));
            }

            writeFile(
                    "books.csv",
                    updatedBooks,
                    "ISBN,Title,Author,Publisher,Edition,Quantity,Remaining,Availability,PDF"
            );
        }

    }
