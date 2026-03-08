package mehrin.loginpage;

import mehrin.loginpage.Util.FileUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Removes cart requests whose ExpiryDate has passed.
 * Also removes the matching pending "CART-{id}" row from issueBooks.csv.
 *
 * addToCart.csv columns (0-based):
 *   0=Serial, 1=StudentID, 2=StudentName, 3=BookISBN, 4=BookName,
 *   5=RequestDate, 6=ExpiryDate, 7=Status
 *
 * FileUtil.readFile() auto-prepends "data/" — so pass filename only.
 */
public class CartExpiryUtil {

    private CartExpiryUtil() {}

    public static void purgeExpiredCartEntries(String cartFilename, String issuedFilename) {

        List<String> cartLines   = FileUtil.readFile(cartFilename);
        List<String> issuedLines = FileUtil.readFile(issuedFilename);

        // If cart is empty nothing to purge
        if (cartLines.isEmpty()) return;

        List<String> validCart   = new ArrayList<>();
        List<String> validIssued = new ArrayList<>(issuedLines);

        LocalDate today        = LocalDate.now();
        boolean   cartChanged  = false;
        boolean   issuedChanged = false;

        for (String line : cartLines) {
            String[] p = line.split(",", -1);

            // Need at least 7 columns (Serial through Status)
            if (p.length < 7) {
                validCart.add(line);
                continue;
            }

            try {
                LocalDate expiry = LocalDate.parse(p[6].trim()); // col 6 = ExpiryDate

                if (!today.isAfter(expiry)) {
                    validCart.add(line); // not expired, keep
                } else {
                    // Expired – remove matching CART-{serial} from issueBooks.csv
                    cartChanged = true;
                    String cartKey = "CART-" + p[0].trim();
                    boolean removed = validIssued.removeIf(issuedLine -> {
                        String[] ip = issuedLine.split(",", -1);
                        return ip.length > 0 && ip[0].equalsIgnoreCase(cartKey);
                    });
                    if (removed) issuedChanged = true;
                }
            } catch (Exception e) {
                validCart.add(line); // unparseable date, keep
            }
        }

        if (cartChanged) {
            FileUtil.writeFile(cartFilename, validCart,
                    "Serial,StudentID,StudentName,BookISBN,BookName,RequestDate,ExpiryDate,Status");
        }
        if (issuedChanged) {
            FileUtil.writeFile(issuedFilename, validIssued,
                    "IssuedID,BookID,StudentID,StudentName,IssuedDate,ReturnDate,LateFee");
        }
    }
}