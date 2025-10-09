// User.java
// Represents a user with both displayName and username (username contains no spaces).
public class User {
    private String displayName;   // e.g., "Binoy DM Panikar"
    private String username;      // e.g., "binoydmpanikar" (no spaces)
    private String password;
    private String role;          // Student, Tutor, HOD, Dean, Principal, Faculty
    private String department;    // CSE, EEE, ECE, CIVIL, MECH
    private String className;     // "CSE A (1)" or empty
    private String subject;       // for faculty (optional)
    private String approverPreference; // faculty default approver

    public User(String displayName, String username, String password, String role, String department, String className) {
        this(displayName, username, password, role, department, className, "", "");
    }

    public User(String displayName, String username, String password, String role, String department, String className, String subject, String approverPreference) {
        this.displayName = displayName == null ? "" : displayName;
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.role = role == null ? "" : role;
        this.department = department == null ? "" : department;
        this.className = className == null ? "" : className;
        this.subject = subject == null ? "" : subject;
        this.approverPreference = approverPreference == null ? "" : approverPreference;
    }

    // getters
    public String getDisplayName() { return displayName; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getDepartment() { return department; }
    public String getClassName() { return className; }
    public String getSubject() { return subject; }
    public String getApproverPreference() { return approverPreference; }

    // setters
    public void setPassword(String p) { this.password = p; }
    public void setApproverPreference(String pref) { this.approverPreference = pref; }

    public boolean checkPassword(String p) { return password.equals(p); }

    // CSV representation: displayName,username,password,role,department,class,subject,approverPref
    public String toCsv() {
        return esc(displayName) + "," + esc(username) + "," + esc(password) + "," + esc(role) + "," + esc(department) + "," + esc(className) + "," + esc(subject) + "," + esc(approverPreference);
    }

    public static User fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 8) return null;
        return new User(unesc(parts[0]), unesc(parts[1]), unesc(parts[2]), unesc(parts[3]), unesc(parts[4]), unesc(parts[5]), unesc(parts[6]), unesc(parts[7]));
    }

    private static String esc(String s) { return s == null ? "" : s.replace(",", " "); }
    private static String unesc(String s) { return s == null ? "" : s; }

    @Override
    public String toString() {
        return displayName + " (" + username + " - " + role + (department.isEmpty() ? "" : " - " + department) + (className.isEmpty() ? "" : " - " + className) + ")";
    }
}
