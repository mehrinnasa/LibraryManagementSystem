package mehrin.loginpage;

import mehrin.loginpage.Util.FileUtil;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * addToCart.csv:        Serial,StudentID,StudentName,BookISBN,BookName,RequestDate,ExpiryDate,Status
 * issueBooks.csv:       IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee
 * cartRestrictions.csv: StudentID,BookISBN,RestrictedUntil
 * lateRestrictions.csv: StudentID,RestrictedUntil
 *
 * FileUtil.readFile() auto-prepends "data/" — pass filename only.
 */
public class CartExpiryUtil {

    private CartExpiryUtil() {}

    // ─────────────────────────────────────────────────────────────
    //  PURGE EXPIRED CART ENTRIES
    // ─────────────────────────────────────────────────────────────
    public static void purgeExpiredCartEntries(String cartFilename, String issuedFilename) {
        createIfMissing("cartRestrictions.csv", "StudentID,BookISBN,RestrictedUntil");
        createIfMissing("lateRestrictions.csv",  "StudentID,RestrictedUntil");

        List<String> cartLines   = FileUtil.readFile(cartFilename);
        List<String> issuedLines = FileUtil.readFile(issuedFilename);
        if (cartLines.isEmpty()) return;

        List<String> validCart   = new ArrayList<>();
        List<String> validIssued = new ArrayList<>(issuedLines);
        LocalDate today = LocalDate.now();
        boolean cartChanged = false, issuedChanged = false;

        for (String line : cartLines) {
            String[] p = line.split(",", -1);
            if (p.length < 8) { validCart.add(line); continue; }

            String expiryStr = p[6].trim();
            String status    = p[7].trim();

            // Waiting entries have N/A expiry — never purge
            if (expiryStr.equalsIgnoreCase("N/A")) { validCart.add(line); continue; }

            LocalDate expiry;
            try { expiry = LocalDate.parse(expiryStr); }
            catch (Exception e) { validCart.add(line); continue; }

            if (!today.isAfter(expiry)) {
                validCart.add(line);
            } else {
                cartChanged = true;
                String cartKey = "CART-" + p[0].trim();
                if (validIssued.removeIf(l -> l.split(",", -1)[0].equalsIgnoreCase(cartKey)))
                    issuedChanged = true;

                // Ready expired = student didn't collect = restrict 3 days on that book
                if (status.equalsIgnoreCase("Ready")) {
                    saveCartRestriction(p[1].trim(), p[3].trim(), today.plusDays(3));
                }
            }
        }

        if (cartChanged)
            FileUtil.writeFile(cartFilename, validCart,
                    "Serial,StudentID,StudentName,BookISBN,BookName,RequestDate,ExpiryDate,Status");
        if (issuedChanged)
            FileUtil.writeFile(issuedFilename, validIssued,
                    "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
    }

    // ─────────────────────────────────────────────────────────────
    //  LATE RESTRICTION CHECK
    //  true if student has any book overdue >= 30 days
    //  OR is within a stored 2-month restriction window.
    // ─────────────────────────────────────────────────────────────
    public static boolean isLateRestricted(String studentId) {
        LocalDate today = LocalDate.now();

        // Case 1: currently overdue 30+ days
        for (String line : FileUtil.readFile("issueBooks.csv")) {
            String[] p = line.split(",", -1);
            if (p.length < 6) continue;
            if (p[0].trim().toUpperCase().startsWith("CART-")) continue;
            if (!p[2].trim().equalsIgnoreCase(studentId)) continue;
            String retDate = p[5].trim();
            if (retDate.equalsIgnoreCase("N/A")) continue;
            try {
                long daysLate = ChronoUnit.DAYS.between(LocalDate.parse(retDate), today);
                if (daysLate >= 30) return true;
            } catch (Exception ignored) {}
        }

        // Case 2: stored 2-month restriction after returning a 150+ Tk overdue book
        createIfMissing("lateRestrictions.csv", "StudentID,RestrictedUntil");
        List<String> lines   = FileUtil.readFile("lateRestrictions.csv");
        List<String> cleaned = new ArrayList<>();
        boolean restricted   = false;
        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length < 2) continue;
            try {
                LocalDate until = LocalDate.parse(p[1].trim());
                if (!today.isAfter(until)) {
                    cleaned.add(line);
                    if (p[0].trim().equalsIgnoreCase(studentId)) restricted = true;
                }
            } catch (Exception ignored) {}
        }
        FileUtil.writeFile("lateRestrictions.csv", cleaned, "StudentID,RestrictedUntil");
        return restricted;
    }

    // ─────────────────────────────────────────────────────────────
    //  CART RESTRICTION CHECK  (per student + book)
    // ─────────────────────────────────────────────────────────────
    public static LocalDate getRestrictionExpiry(String studentId, String bookIsbn) {
        createIfMissing("cartRestrictions.csv", "StudentID,BookISBN,RestrictedUntil");
        LocalDate today = LocalDate.now();
        List<String> lines   = FileUtil.readFile("cartRestrictions.csv");
        List<String> cleaned = new ArrayList<>();
        LocalDate found = null;
        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length < 3) continue;
            try {
                LocalDate expiry = LocalDate.parse(p[2].trim());
                if (!today.isAfter(expiry)) {
                    cleaned.add(line);
                    if (p[0].trim().equalsIgnoreCase(studentId)
                            && p[1].trim().equalsIgnoreCase(bookIsbn))
                        found = expiry;
                }
            } catch (Exception ignored) {}
        }
        FileUtil.writeFile("cartRestrictions.csv", cleaned, "StudentID,BookISBN,RestrictedUntil");
        return found;
    }

    // ─────────────────────────────────────────────────────────────
    //  SAVE 2-MONTH RESTRICTION  (called by AllIssuedBooksController on return)
    // ─────────────────────────────────────────────────────────────
    public static void saveLateRestriction(String studentId, LocalDate until) {
        createIfMissing("lateRestrictions.csv", "StudentID,RestrictedUntil");
        List<String> lines   = FileUtil.readFile("lateRestrictions.csv");
        List<String> updated = new ArrayList<>();
        boolean found = false;
        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length > 0 && p[0].trim().equalsIgnoreCase(studentId)) {
                updated.add(studentId + "," + until);
                found = true;
            } else { updated.add(line); }
        }
        if (!found) updated.add(studentId + "," + until);
        FileUtil.writeFile("lateRestrictions.csv", updated, "StudentID,RestrictedUntil");
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────
    private static void saveCartRestriction(String studentId, String bookIsbn, LocalDate until) {
        List<String> lines   = FileUtil.readFile("cartRestrictions.csv");
        List<String> updated = new ArrayList<>();
        boolean found = false;
        for (String line : lines) {
            String[] p = line.split(",", -1);
            if (p.length > 1 && p[0].trim().equalsIgnoreCase(studentId)
                    && p[1].trim().equalsIgnoreCase(bookIsbn)) {
                updated.add(studentId + "," + bookIsbn + "," + until);
                found = true;
            } else { updated.add(line); }
        }
        if (!found) updated.add(studentId + "," + bookIsbn + "," + until);
        FileUtil.writeFile("cartRestrictions.csv", updated, "StudentID,BookISBN,RestrictedUntil");
    }

    private static void createIfMissing(String filename, String header) {
        File f = new File("data/" + filename);
        if (f.exists()) return;
        try {
            f.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                bw.write(header); bw.newLine();
            }
        } catch (Exception ignored) {}
    }
}