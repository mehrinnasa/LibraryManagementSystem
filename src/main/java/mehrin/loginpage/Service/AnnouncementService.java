package mehrin.loginpage.Service;

import mehrin.loginpage.Model.Announcement;
import mehrin.loginpage.Util.FileUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Announcement Service Class
 * Manages all announcement-related operations using file storage
 */
public class AnnouncementService {

    private static final String ANNOUNCEMENTS_FILE = "announcements.csv";
    private static final String HEADER = "ID,Title,Message,Date";

    private List<Announcement> announcements;
    private int nextId;

    public AnnouncementService() {
        loadAnnouncements();
    }

    /**
     * Load all announcements from CSV file
     */
    private void loadAnnouncements() {
        announcements = new ArrayList<>();
        List<String> lines = FileUtil.readFile(ANNOUNCEMENTS_FILE);

        nextId = 1;
        for (String line : lines) {
            Announcement announcement = Announcement.fromCSV(line);
            if (announcement != null) {
                announcements.add(announcement);
                if (announcement.getId() >= nextId) {
                    nextId = announcement.getId() + 1;
                }
            }
        }
    }

    /**
     * Get all announcements
     */
    public List<Announcement> getAllAnnouncements() {
        return new ArrayList<>(announcements);
    }

    /**
     * Add a new announcement
     */
    public boolean addAnnouncement(Announcement announcement) {
        announcement.setId(nextId++);
        announcements.add(announcement);
        saveAnnouncements();
        return true;
    }

    /**
     * Delete an announcement by ID
     */
    public boolean deleteAnnouncement(int id) {
        announcements.removeIf(announcement -> announcement.getId() == id);
        saveAnnouncements();
        return true;
    }

    /**
     * Save all announcements to CSV file
     */
    private void saveAnnouncements() {
        List<String> lines = new ArrayList<>();
        for (Announcement announcement : announcements) {
            lines.add(announcement.toCSV());
        }
        FileUtil.writeFile(ANNOUNCEMENTS_FILE, lines, HEADER);
    }
}