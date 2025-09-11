import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    static User[] users = {
        new User("student1", "1234", "Student"),
        new User("tutor1", "1234", "Tutor"),
        new User("hod1", "1234", "HOD"),
        new User("principal1", "1234", "Principal")
    };

    static Document doc = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> loginScreen());
    }

    // --- LOGIN SCREEN ---
    static void loginScreen() {
        JFrame f = new JFrame("Login");
        f.setSize(300, 150);
        f.setLayout(new GridLayout(3, 2));

        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        f.add(new JLabel("Username:")); f.add(userField);
        f.add(new JLabel("Password:")); f.add(passField);
        f.add(new JLabel("")); f.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String u = userField.getText();
            String p = new String(passField.getPassword());
            for (User usr : users) {
                if (usr.login(u, p)) {
                    f.dispose();
                    dashboard(usr);
                    return;
                }
            }
            JOptionPane.showMessageDialog(f, "Invalid login!");
        });

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    // --- DASHBOARD ---
    static void dashboard(User user) {
        JFrame f = new JFrame("Dashboard - " + user.role);
        f.setSize(400, 200);
        f.setLayout(new FlowLayout());

        if (user.role.equals("Student")) {
            JButton submitBtn = new JButton("Submit Document");
            JButton statusBtn = new JButton("Check Status");

            submitBtn.addActionListener(e -> {
                String t = JOptionPane.showInputDialog("Enter title:");
                String c = JOptionPane.showInputDialog("Enter content:");
                doc = new Document(t, c);
                JOptionPane.showMessageDialog(f, "Submitted for Tutor review!");
            });

            statusBtn.addActionListener(e -> {
                if (doc == null) JOptionPane.showMessageDialog(f, "No document.");
                else JOptionPane.showMessageDialog(f, "Status: " + doc.status);
            });

            f.add(submitBtn);
            f.add(statusBtn);

        } else { // Tutor / HOD / Principal
            JButton approveBtn = new JButton("Approve");
            JButton rejectBtn = new JButton("Reject");

            approveBtn.addActionListener(e -> {
                if (doc == null) JOptionPane.showMessageDialog(f, "No document.");
                else {
                    if (user.role.equals("Tutor")) doc.status = "Approved by Tutor (Pending HOD)";
                    else if (user.role.equals("HOD")) doc.status = "Approved by HOD (Pending Principal)";
                    else if (user.role.equals("Principal")) doc.status = "Approved by Principal (Final)";
                    JOptionPane.showMessageDialog(f, "Approved by " + user.role);
                }
            });

            rejectBtn.addActionListener(e -> {
                if (doc == null) JOptionPane.showMessageDialog(f, "No document.");
                else {
                    doc.status = "Rejected by " + user.role;
                    JOptionPane.showMessageDialog(f, "Rejected by " + user.role);
                }
            });

            f.add(approveBtn);
            f.add(rejectBtn);
        }

        f.setVisible(true);
    }
}
