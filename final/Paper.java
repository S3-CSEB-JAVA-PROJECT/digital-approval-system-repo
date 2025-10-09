// Paper.java
import java.util.ArrayList;
import java.util.List;

public class Paper {
    private String id;
    private String title;
    private String content;
    private String submittedByUsername; // username of submitter
    private String submittedByDisplay;  // display name (for convenience)
    private String department;
    private String className;
    private String status; // e.g., "Pending (Tutor)", "Pending (HOD)", "Approved (Final)", "Rejected by HOD"
    private String nextStage; // "Tutor", "HOD", "Dean", "Principal", "Board..."
    private List<String> history;

    public Paper(String id, String title, String content, String submittedByUsername, String submittedByDisplay, String department, String className, String nextStage) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.submittedByUsername = submittedByUsername;
        this.submittedByDisplay = submittedByDisplay;
        this.department = department == null ? "" : department;
        this.className = className == null ? "" : className;
        this.history = new ArrayList<>();
        this.nextStage = (nextStage == null || nextStage.isEmpty()) ? "Tutor" : nextStage;
        setInitialStatus();
        addHistory("Submitted by " + this.submittedByDisplay + " (next: " + this.nextStage + ")");
    }

    private void setInitialStatus() {
        this.status = "Pending (" + nextStage + ")";
    }

    // getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSubmittedByUsername() { return submittedByUsername; }
    public String getSubmittedByDisplay() { return submittedByDisplay; }
    public String getDepartment() { return department; }
    public String getClassName() { return className; }
    public String getStatus() { return status; }
    public String getNextStage() { return nextStage; }
    public List<String> getHistory() { return history; }

    public void addHistory(String s) { history.add(s); }

    // approve by approverRole with approverDisplayName
    public void approve(String approverRole, String approverDisplay) {
        if (status.startsWith("Rejected") || status.equals("Approved (Final)")) return;
        if (approverRole.equalsIgnoreCase("Tutor")) {
            nextStage = "HOD";
            status = "Pending (HOD)";
            addHistory("Approved by Tutor: " + approverDisplay);
        } else if (approverRole.equalsIgnoreCase("HOD")) {
            nextStage = "Dean";
            status = "Pending (Dean)";
            addHistory("Approved by HOD: " + approverDisplay);
        } else if (approverRole.equalsIgnoreCase("Dean")) {
            nextStage = "Principal";
            status = "Pending (Principal)";
            addHistory("Approved by Dean: " + approverDisplay);
        } else if (approverRole.equalsIgnoreCase("Principal")) {
            nextStage = "Final";
            status = "Approved (Final)";
            addHistory("Approved by Principal: " + approverDisplay);
        } else {
            // custom boards etc
            nextStage = approverRole;
            status = "Pending (" + approverRole + ")";
            addHistory("Approved by " + approverRole + ": " + approverDisplay);
        }
    }

    public void reject(String approverRole, String approverDisplay, String reason) {
        status = "Rejected by " + approverRole;
        addHistory("Rejected by " + approverDisplay + (reason == null || reason.trim().isEmpty() ? "" : " (Reason: " + reason + ")"));
    }

    // CSV: id,title,content,submitUser,submitDisplay,dept,class,status,nextStage,history|...
    public String toCsvLine() {
        String safeTitle = title.replace(",", " ");
        String safeContent = content.replace(",", " ").replace("\n", " ");
        String hist = String.join(" | ", history).replace(",", " ");
        return esc(id)+","+esc(safeTitle)+","+esc(safeContent)+","+esc(submittedByUsername)+","+esc(submittedByDisplay)+","+esc(department)+","+esc(className)+","+esc(status)+","+esc(nextStage)+","+esc(hist);
    }

    public static Paper fromCsvLine(String line) {
        String[] parts = line.split(",", 10);
        if (parts.length < 9) return null;
        String id = unesc(parts[0]);
        String title = unesc(parts[1]);
        String content = unesc(parts[2]);
        String subUser = unesc(parts[3]);
        String subDisp = unesc(parts[4]);
        String dept = unesc(parts[5]);
        String cls = unesc(parts[6]);
        String status = unesc(parts[7]);
        String next = unesc(parts[8]);
        List<String> hist = new ArrayList<>();
        if (parts.length == 10 && parts[9] != null && !parts[9].trim().isEmpty()) {
            String[] items = parts[9].split("\\|");
            for (String it : items) hist.add(it.trim());
        }
        Paper p = new Paper(id, title, content, subUser, subDisp, dept, cls, next);
        p.status = status;
        p.history = hist;
        p.nextStage = next;
        return p;
    }

    private static String esc(String s) { return s == null ? "" : s.replace(",", " "); }
    private static String unesc(String s) { return s == null ? "" : s; }
}
