package mehrin.loginpage.Service;

import mehrin.loginpage.Model.Announcement;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementService {

    private static final String FILE_PATH = "data/announcements.csv";

    // CSV columns: id, title, message, createdDateTime, updatedDateTime
    private static final String HEADER = "id,title,message,createdDateTime,updatedDateTime";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ─────────────────────────────────────────────────────────────
    //  ENSURE FILE EXISTS
    // ─────────────────────────────────────────────────────────────
    private void ensureFileExists() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
                    w.write(HEADER);
                    w.newLine();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  GET ALL
    // ─────────────────────────────────────────────────────────────
    public List<Announcement> getAllAnnouncements() {
        ensureFileExists();
        List<Announcement> list = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new FileReader(FILE_PATH))) {
            r.readLine(); // skip header
            String line;
            while ((line = r.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Split into max 5 parts (message may contain commas)
                String[] p = line.split(",", 5);

                if (p.length >= 4) {
                    String updated = (p.length == 5) ? p[4].trim() : "";
                    list.add(new Announcement(
                            Integer.parseInt(p[0].trim()),
                            p[1].trim(),
                            p[2].trim(),
                            p[3].trim(),
                            updated));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return list;
    }

    // ─────────────────────────────────────────────────────────────
    //  ADD
    // ─────────────────────────────────────────────────────────────
    public void addAnnouncement(Announcement a) {
        ensureFileExists();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            String now = LocalDateTime.now().format(FORMATTER);
            // updatedDateTime = empty on first post
            w.write(getNextId() + "," + a.getTitle() + "," + a.getMessage()
                    + "," + now + ",");
            w.newLine();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────────
    public void updateAnnouncement(Announcement updated) {
        ensureFileExists();
        List<Announcement> all = getAllAnnouncements();
        String now = LocalDateTime.now().format(FORMATTER);

        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE_PATH))) {
            w.write(HEADER); w.newLine();
            for (Announcement a : all) {
                if (a.getId() == updated.getId()) {
                    a.setTitle(updated.getTitle());
                    a.setMessage(updated.getMessage());
                    a.setUpdatedDateTime(now);   // stamp update time
                }
                w.write(a.getId() + "," + a.getTitle() + "," + a.getMessage()
                        + "," + a.getCreatedDateTime() + "," + a.getUpdatedDateTime());
                w.newLine();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────
    public void deleteAnnouncement(int id) {
        ensureFileExists();
        List<Announcement> all = getAllAnnouncements();

        try (BufferedWriter w = new BufferedWriter(new FileWriter(FILE_PATH))) {
            w.write(HEADER); w.newLine();
            for (Announcement a : all) {
                if (a.getId() != id) {
                    w.write(a.getId() + "," + a.getTitle() + "," + a.getMessage()
                            + "," + a.getCreatedDateTime() + "," + a.getUpdatedDateTime());
                    w.newLine();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─────────────────────────────────────────────────────────────
    //  NEXT ID
    // ─────────────────────────────────────────────────────────────
    private int getNextId() {
        int max = 0;
        for (Announcement a : getAllAnnouncements()) {
            if (a.getId() > max) max = a.getId();
        }
        return max + 1;
    }
}