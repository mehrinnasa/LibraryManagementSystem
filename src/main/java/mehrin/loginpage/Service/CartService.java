package mehrin.loginpage.Service;

import mehrin.loginpage.Util.FileUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    private static final String CART_CSV = "data/addToCart.csv";

    // 🔥 AUTO EXPIRY CLEANER
    public static void cleanExpiredCarts() {

        List<String> cartList = FileUtil.readFile(CART_CSV);
        List<String> updated = new ArrayList<>();

        LocalDate today = LocalDate.now();

        for (String line : cartList) {

            if (line.startsWith("SerialID")) {
                updated.add(line);
                continue;
            }

            String[] parts = line.split(",");

            LocalDate expiry = LocalDate.parse(parts[5]);

            if (today.isAfter(expiry) && parts[6].equals("Ready")) {
                continue; // remove expired
            }

            updated.add(line);
        }

        FileUtil.writeFile(CART_CSV, updated, null);

        promoteWaitingQueue();
    }

    // 🔥 WAITING → READY PROMOTION
    public static void promoteWaitingQueue() {

        List<String> cartList = FileUtil.readFile(CART_CSV);
        List<String> updated = new ArrayList<>();

        for (String line : cartList) {

            if (line.startsWith("SerialID")) {
                updated.add(line);
                continue;
            }

            String[] parts = line.split(",");

            if (parts[6].equals("Waiting")) {

                parts[6] = "Ready";
                parts[5] = LocalDate.now().plusDays(3).toString();

                line = String.join(",", parts);

                updated.add(line);
                break; // only first promote
            }

            updated.add(line);
        }

        FileUtil.writeFile(CART_CSV, updated, null);
    }
}