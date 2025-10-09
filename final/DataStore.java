// DataStore.java
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    private static final String PAPERS_FILE = DATA_DIR + File.separator + "papers.txt";

    public static void ensureFiles() {
        try {
            File d = new File(DATA_DIR);
            if (!d.exists()) d.mkdir();
            File u = new File(USERS_FILE);
            if (!u.exists()) u.createNewFile();
            File p = new File(PAPERS_FILE);
            if (!p.exists()) p.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<User> loadUsers() {
        ensureFiles();
        List<User> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                User u = User.fromCsv(line);
                if (u != null) list.add(u);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static synchronized void appendUser(User u) {
        ensureFiles();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            bw.write(u.toCsv());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveAllUsers(List<User> users) {
        ensureFiles();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE, false))) {
            for (User u : users) {
                bw.write(u.toCsv());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Paper> loadPapers() {
        ensureFiles();
        List<Paper> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PAPERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                Paper p = Paper.fromCsvLine(line);
                if (p != null) list.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static synchronized void appendPaper(Paper p) {
        ensureFiles();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PAPERS_FILE, true))) {
            bw.write(p.toCsvLine());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveAllPapers(List<Paper> papers) {
        ensureFiles();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PAPERS_FILE, false))) {
            for (Paper p : papers) {
                bw.write(p.toCsvLine());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
