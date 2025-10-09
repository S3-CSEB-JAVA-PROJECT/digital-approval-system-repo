// Main.java
// Digital Approval System - full GUI & logic
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    static List<User> users;
    static List<Paper> papers;
    static User loggedUser;

    // departments with classes (professional names)
    static final Map<String, List<String>> deptClasses = new LinkedHashMap<>();
    static {
        deptClasses.put("CSE", Arrays.asList(
            "CSE A (1)","CSE B (1)","CSE C (1)","IT (1)","CSBS (1)","CS with AI (1)",
            "CSE A (2)","CSE B (2)","CSE C (2)","IT (2)",
            "CSE A (3)","CSE B (3)","IT (3)",
            "CSE A (4)","CSE B (4)","IT (4)"
        ));
        deptClasses.put("EEE", Arrays.asList("EEE Y1","EEE Y2","EEE Y3","EEE Y4"));
        deptClasses.put("ECE", Arrays.asList("ECE Y1","ECE Y2","ECE Y3","ECE Y4"));
        deptClasses.put("CIVIL", Arrays.asList("CIVIL Y1","CIVIL Y2","CIVIL Y3","CIVIL Y4"));
        deptClasses.put("MECH", Arrays.asList("MECH Y1","MECH Y2","MECH Y3","MECH Y4"));
    }

    public static void main(String[] args) {
        DataStore.ensureFiles();
        users = DataStore.loadUsers();
        papers = DataStore.loadPapers();
        autoRegisterFacultyAndAdmins();
        users = DataStore.loadUsers(); // reload after auto-reg
        SwingUtilities.invokeLater(() -> showLoginWindow());
    }

    // Normalize display names -> username (no spaces, no punctuation, lowercase)
    static String buildUsernameFromDisplay(String s) {
        if (s == null) return "";
        // remove titles (Prof, Dr, Mr, Mrs, Ms) case-insensitive
        String t = s.replaceAll("(?i)prof\\.?","").replaceAll("(?i)dr\\.?","").replaceAll("(?i)mr\\.?","").replaceAll("(?i)mrs\\.?","").replaceAll("(?i)ms\\.?","");
        // remove parentheses and punctuation, keep letters & digits
        t = t.replaceAll("[^A-Za-z0-9 ]", "");
        t = t.replaceAll("\\s+", " ").trim();
        // remove spaces and lowercase
        return t.replace(" ", "").toLowerCase();
    }

    // Auto-register principal, deans, hods, tutors with username no spaces and password "faculty"
    static void autoRegisterFacultyAndAdmins() {
        // Principal & Deans
        String principal = "Prof. (Dr.) Mohammad Sekoor T.";
        String ugDeanAcademic = "Prof. (Dr.) Praveenkumar K";
        String ugDeanStudent = "Prof. (Dr.) Vinodu George";

        Map<String,String> hods = new HashMap<>();
        hods.put("CSE","Dr. Manoj Kumar G");
        hods.put("MECH","Dr. Manoj Kumar. C. V");
        hods.put("ECE","Dr. Mary Reena K.E");
        hods.put("CIVIL","Dr. Anjali MS");
        hods.put("EEE","Prof. Jayakumar M.");

        Map<String,String> tutors = new HashMap<>();
        tutors.put("CSE A (1)","Prof. Binoy DM Panikar");
        tutors.put("CSE B (1)","Prof. Arathi S S");
        tutors.put("CSE C (1)","Prof. Indu K B");
        tutors.put("IT (1)","Prof. Geetha A V");
        tutors.put("CSBS (1)","Prof. Safarunisa K M");
        tutors.put("CS with AI (1)","Prof. Prathima A");
        tutors.put("EEE Y1","Prof. Seena K R");
        tutors.put("ECE Y1","Prof. Anusree L");
        tutors.put("CIVIL Y1","Prof. Merlin R");
        tutors.put("MECH Y1","Prof. Jowhar Mubarak");

        tutors.put("CSE A (2)","Prof. Krishnaprasad P K");
        tutors.put("CSE B (2)","Prof. Vengayil Nayana Murali");
        tutors.put("CSE C (2)","Prof. Baby Sunitha V P");
        tutors.put("IT (2)","Prof. Lijin Das S");
        tutors.put("EEE Y2","Prof. Mujeeb Rahuman");
        tutors.put("ECE Y2","Dr. Arathi T");
        tutors.put("CIVIL Y2","Prof. Sruthi M");
        tutors.put("MECH Y2","Prof. Vinod O. M");

        tutors.put("CSE A (3)","Dr. Sarith Divakar M");
        tutors.put("CSE B (3)","Dr. Jayalekshmi S");
        tutors.put("IT (3)","Prof. Nishy Reshmi S");
        tutors.put("EEE Y3","Prof. Arun S Mathew");
        tutors.put("ECE Y3","Dr. Baiju P.S.");
        tutors.put("CIVIL Y3","Dr. Anjali M S");
        tutors.put("MECH Y3","Prof. Swaraj Kumar B. & Prof. Sreejith M");

        tutors.put("CSE A (4)","Prof. Rasna P");
        tutors.put("CSE B (4)","Prof. Sajina K.");
        tutors.put("IT (4)","Prof. Fathimath Sameera M A");
        tutors.put("EEE Y4","Dr. Kannan M. & Dr. Aseem K.");
        tutors.put("ECE Y4","Dr. Anitha K");
        tutors.put("CIVIL Y4","Dr. Anjali & Dr. Arun N R");
        tutors.put("MECH Y4","Dr. Anil Kumar B.C.");

        // Register principal & deans
        registerAutoIfMissing(principal, "Principal", "", "");
        registerAutoIfMissing(ugDeanAcademic, "Dean", "", "");
        registerAutoIfMissing(ugDeanStudent, "Dean", "", "");

        // HODs
        for (Map.Entry<String,String> e : hods.entrySet()) {
            registerAutoIfMissing(e.getValue(), "HOD", e.getKey(), "");
        }

        // Tutors
        for (Map.Entry<String,String> e : tutors.entrySet()) {
            String cls = e.getKey();
            String name = e.getValue();
            String dept = guessDeptFromClass(cls);
            registerAutoIfMissing(name, "Tutor", dept, cls);
        }
    }

    static void registerAutoIfMissing(String fullName, String role, String dept, String cls) {
        String disp = fullName.replaceAll("\\s+", " ").trim();
        String uname = buildUsernameFromDisplay(disp);
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(uname));
        if (!exists) {
            User u = new User(disp, uname, "faculty".equals("faculty") ? "faculty" : "faculty", role, dept, cls);
            // note: password set as "faculty"
            u = new User(disp, uname, "faculty", role, dept, cls);
            DataStore.appendUser(u);
            users.add(u);
        }
    }

    static String guessDeptFromClass(String cls) {
        String s = cls.toUpperCase();
        if (s.contains("CSE") || s.contains("CS") || s.contains("IT")) return "CSE";
        if (s.startsWith("EEE")) return "EEE";
        if (s.startsWith("ECE")) return "ECE";
        if (s.startsWith("CIVIL") || s.startsWith("CE")) return "CIVIL";
        if (s.startsWith("MECH") || s.startsWith("ME")) return "MECH";
        return "";
    }

    // ------------ LOGIN WINDOW with autocomplete suggestions ------------
    static void showLoginWindow() {
        JFrame f = new JFrame("Digital Approval System - Login");
        f.setSize(520, 260);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(null);

        JLabel lTitle = new JLabel("Digital Approval System");
        lTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lTitle.setBounds(160, 8, 300, 30);

        JLabel lu = new JLabel("Username:");
        lu.setBounds(40, 60, 80, 25);
        JTextField tfUser = new JTextField();
        tfUser.setBounds(130, 60, 280, 25);

        // autocomplete popup
        JPopupMenu suggestionPopup = new JPopupMenu();
        suggestionPopup.setFocusable(false);
        JList<String> suggestionList = new JList<>();
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setFocusable(false);
        suggestionPopup.add(new JScrollPane(suggestionList));

        // populate suggestion list function
        Runnable updateSuggestions = () -> {
            String typed = tfUser.getText().trim().toLowerCase();
            List<String> suggestions;
            if (typed.isEmpty()) {
                // show top 10 display names
                suggestions = users.stream().map(User::getDisplayName).limit(10).collect(Collectors.toList());
            } else {
                suggestions = users.stream()
                        .map(User::getDisplayName)
                        .filter(d -> normalizeForMatch(d).contains(normalizeForMatch(typed)))
                        .limit(10).collect(Collectors.toList());
            }
            if (suggestions.isEmpty()) {
                suggestionPopup.setVisible(false);
            } else {
                suggestionList.setListData(suggestions.toArray(new String[0]));
                suggestionList.setVisibleRowCount(Math.min(8, suggestions.size()));
                try {
                    suggestionPopup.show(tfUser, 0, tfUser.getHeight());
                } catch (IllegalComponentStateException ex) {
                    // ignore if component not visible yet
                }
            }
        };

        // handle typing
        tfUser.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // ignore navigation keys
                if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    suggestionList.requestFocus();
                    suggestionList.setSelectedIndex(0);
                    return;
                }
                updateSuggestions.run();
            }
        });

        // when user selects from suggestion list, fill username field with corresponding username (no spaces)
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedDisplay = suggestionList.getSelectedValue();
                if (selectedDisplay != null) {
                    Optional<User> uOpt = users.stream().filter(u -> u.getDisplayName().equals(selectedDisplay)).findFirst();
                    if (uOpt.isPresent()) {
                        tfUser.setText(uOpt.get().getUsername()); // fill username (no spaces)
                    } else {
                        // fallback: fill with normalized display
                        tfUser.setText(buildUsernameFromDisplay(selectedDisplay));
                    }
                    suggestionPopup.setVisible(false);
                }
            }
        });

        // allow keyboard selection
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String selectedDisplay = suggestionList.getSelectedValue();
                    if (selectedDisplay != null) {
                        Optional<User> uOpt = users.stream().filter(u -> u.getDisplayName().equals(selectedDisplay)).findFirst();
                        if (uOpt.isPresent()) tfUser.setText(uOpt.get().getUsername());
                        else tfUser.setText(buildUsernameFromDisplay(selectedDisplay));
                        suggestionPopup.setVisible(false);
                    }
                }
            }
        });

        JLabel lp = new JLabel("Password:");
        lp.setBounds(40, 95, 80, 25);
        JPasswordField pf = new JPasswordField();
        pf.setBounds(130, 95, 280, 25);

        JButton bLogin = new JButton("Login");
        bLogin.setBounds(130, 135, 120, 30);
        JButton bStuReg = new JButton("Register (Student)");
        bStuReg.setBounds(260, 135, 150, 30);
        JButton bFacReg = new JButton("Register (Faculty)");
        bFacReg.setBounds(130, 175, 150, 30);
        JButton bExit = new JButton("Exit");
        bExit.setBounds(290, 175, 120, 30);

        f.add(lTitle);
        f.add(lu); f.add(tfUser);
        f.add(lp); f.add(pf);
        f.add(bLogin); f.add(bStuReg); f.add(bFacReg); f.add(bExit);

        // hide popup when focus lost
        tfUser.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> suggestionPopup.setVisible(false));
            }
        });

        bLogin.addActionListener(e -> {
            String uname = tfUser.getText().trim();
            String pass = new String(pf.getPassword()).trim();
            if (uname.isEmpty() || pass.isEmpty()) { JOptionPane.showMessageDialog(f, "Enter username and password."); return; }
            Optional<User> opt = users.stream().filter(u -> u.getUsername().equalsIgnoreCase(uname) && u.getPassword().equals(pass)).findFirst();
            if (!opt.isPresent()) {
                JOptionPane.showMessageDialog(f, "Invalid credentials. If you are a pre-registered faculty, use password 'faculty'.");
                return;
            }
            loggedUser = opt.get();
            f.dispose();
            showDashboard();
        });

        bStuReg.addActionListener(e -> {
            f.dispose();
            showStudentRegister();
        });

        bFacReg.addActionListener(e -> {
            f.dispose();
            showFacultyRegister();
        });

        bExit.addActionListener(e -> System.exit(0));

        f.setVisible(true);
    }

    // normalize string for matching: remove titles, non-alphanum, lowercase
    static String normalizeForMatch(String s) {
        if (s == null) return "";
        String t = s.replaceAll("(?i)prof\\.?","").replaceAll("(?i)dr\\.?","").replaceAll("(?i)mr\\.?","").replaceAll("(?i)mrs\\.?","").replaceAll("(?i)ms\\.?","");
        t = t.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return t;
    }

    // ---------------- Student Register (separate) ----------------
    static void showStudentRegister() {
        JFrame f = new JFrame("Student Registration");
        f.setSize(680, 420);
        f.setLocationRelativeTo(null);
        f.setLayout(null);

        JLabel la = new JLabel("Full Name:");
        la.setBounds(20,20,120,25);
        JTextField tfName = new JTextField();
        tfName.setBounds(150,20,480,25);

        JLabel lp = new JLabel("Password:");
        lp.setBounds(20,60,120,25);
        JPasswordField pf = new JPasswordField();
        pf.setBounds(150,60,200,25);

        JLabel ld = new JLabel("Department:");
        ld.setBounds(20,100,120,25);
        JComboBox<String> cbDept = new JComboBox<>(deptClasses.keySet().toArray(new String[0]));
        cbDept.setBounds(150,100,200,25);

        JLabel ly = new JLabel("Year:");
        ly.setBounds(380,100,50,25);
        JComboBox<String> cbYear = new JComboBox<>(new String[]{"1","2","3","4"});
        cbYear.setBounds(430,100,70,25);

        JLabel lc = new JLabel("Class:");
        lc.setBounds(20,140,120,25);
        JComboBox<String> cbClass = new JComboBox<>();
        cbClass.setBounds(150,140,300,25);

        cbDept.addActionListener(e -> updateClassOptions(cbDept, cbYear, cbClass));
        cbYear.addActionListener(e -> updateClassOptions(cbDept, cbYear, cbClass));
        updateClassOptions(cbDept, cbYear, cbClass);

        JButton bCreate = new JButton("Create Student");
        bCreate.setBounds(150, 200, 200, 35);
        JButton bBack = new JButton("Back");
        bBack.setBounds(370,200,120,35);

        f.add(la); f.add(tfName);
        f.add(lp); f.add(pf);
        f.add(ld); f.add(cbDept);
        f.add(ly); f.add(cbYear);
        f.add(lc); f.add(cbClass);
        f.add(bCreate); f.add(bBack);

        bCreate.addActionListener(e -> {
            String name = tfName.getText().trim();
            String pass = new String(pf.getPassword()).trim();
            String dept = (String) cbDept.getSelectedItem();
            String cls = (String) cbClass.getSelectedItem();
            if (name.isEmpty() || pass.isEmpty() || cls == null || cls.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please fill name, password and class.");
                return;
            }
            String username = buildUsernameFromDisplay(name);
            if (username.contains(" ")) {
                JOptionPane.showMessageDialog(f, "Username cannot contain spaces. Choose a different display name or contact admin.");
                return;
            }
            boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
            if (exists) {
                JOptionPane.showMessageDialog(f, "Username already exists. Try adding a middle initial.");
                return;
            }
            User su = new User(name, username, pass, "Student", dept, cls);
            DataStore.appendUser(su);
            users.add(su);
            JOptionPane.showMessageDialog(f, "Student account created. Your username is: " + username);
            f.dispose();
            showLoginWindow();
        });

        bBack.addActionListener(e -> { f.dispose(); showLoginWindow(); });

        f.setVisible(true);
    }

    static void updateClassOptions(JComboBox<String> cbDept, JComboBox<String> cbYear, JComboBox<String> cbClass) {
        cbClass.removeAllItems();
        String dept = (String) cbDept.getSelectedItem();
        String year = (String) cbYear.getSelectedItem();
        for (String cls : deptClasses.get(dept)) {
            if ((year.equals("1") && (cls.contains("(1)") || cls.contains("Y1"))) ||
                (year.equals("2") && (cls.contains("(2)") || cls.contains("Y2"))) ||
                (year.equals("3") && (cls.contains("(3)") || cls.contains("Y3"))) ||
                (year.equals("4") && (cls.contains("(4)") || cls.contains("Y4")))) {
                cbClass.addItem(cls);
            }
        }
        if (cbClass.getItemCount() == 0) for (String cls : deptClasses.get(dept)) cbClass.addItem(cls);
    }

    // ---------------- Faculty Register (separate) ----------------
    static void showFacultyRegister() {
        JFrame f = new JFrame("Faculty Registration");
        f.setSize(720, 480);
        f.setLocationRelativeTo(null);
        f.setLayout(null);

        JLabel la = new JLabel("Full Name:");
        la.setBounds(20,20,120,25);
        JTextField tfName = new JTextField();
        tfName.setBounds(150,20,520,25);

        JLabel lp = new JLabel("Password:");
        lp.setBounds(20,60,120,25);
        JPasswordField pf = new JPasswordField();
        pf.setBounds(150,60,200,25);

        JLabel ld = new JLabel("Department:");
        ld.setBounds(20,100,120,25);
        JComboBox<String> cbDept = new JComboBox<>(deptClasses.keySet().toArray(new String[0]));
        cbDept.setBounds(150,100,200,25);

        JLabel lcls = new JLabel("Class (optional):");
        lcls.setBounds(380,100,120,25);
        JComboBox<String> cbClass = new JComboBox<>();
        cbClass.setBounds(500,100,170,25);

        cbDept.addActionListener(e -> {
            cbClass.removeAllItems();
            String dep = (String) cbDept.getSelectedItem();
            for (String cls : deptClasses.get(dep)) cbClass.addItem(cls);
        });
        cbDept.setSelectedIndex(0);

        JLabel lsub = new JLabel("Subject:");
        lsub.setBounds(20,140,120,25);
        JTextField tfSub = new JTextField();
        tfSub.setBounds(150,140,300,25);

        JLabel lpref = new JLabel("Approver Preference:");
        lpref.setBounds(20,180,140,25);
        String[] prefs = {"HOD","Dean","Principal","Board of Governors","Governing Body","Executive Committee"};
        JComboBox<String> cbPref = new JComboBox<>(prefs);
        cbPref.setBounds(170,180,260,25);

        JButton bCreate = new JButton("Create Faculty");
        bCreate.setBounds(150, 230, 200, 35);
        JButton bBack = new JButton("Back");
        bBack.setBounds(370,230,120,35);

        f.add(la); f.add(tfName);
        f.add(lp); f.add(pf);
        f.add(ld); f.add(cbDept);
        f.add(lcls); f.add(cbClass);
        f.add(lsub); f.add(tfSub);
        f.add(lpref); f.add(cbPref);
        f.add(bCreate); f.add(bBack);

        bCreate.addActionListener(e -> {
            String name = tfName.getText().trim();
            String pass = new String(pf.getPassword()).trim();
            String dept = (String) cbDept.getSelectedItem();
            String cls = (String) cbClass.getSelectedItem();
            String subject = tfSub.getText().trim();
            String pref = (String) cbPref.getSelectedItem();
            if (name.isEmpty() || pass.isEmpty() || subject.isEmpty()) {
                JOptionPane.showMessageDialog(f, "Please fill required fields: name, password, subject.");
                return;
            }
            String username = buildUsernameFromDisplay(name);
            if (username.contains(" ")) { JOptionPane.showMessageDialog(f, "Username cannot contain spaces. Choose a different display name."); return; }
            boolean exists = users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
            if (exists) { JOptionPane.showMessageDialog(f, "Username exists. Use a different name."); return; }
            User fu = new User(name, username, pass, "Faculty", dept, cls == null ? "" : cls, subject, pref);
            DataStore.appendUser(fu);
            users.add(fu);
            JOptionPane.showMessageDialog(f, "Faculty account created. Your username: " + username);
            f.dispose();
            showLoginWindow();
        });

        bBack.addActionListener(e -> { f.dispose(); showLoginWindow(); });

        f.setVisible(true);
    }

    // ---------------- Dashboard (role-based) ----------------
    static void showDashboard() {
        JFrame f = new JFrame("Dashboard - Digital Approval System");
        f.setSize(1100, 650);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        JLabel lbl = new JLabel("Logged in: " + loggedUser.getDisplayName() + " [" + loggedUser.getUsername() + "] - " + loggedUser.getRole());
        lbl.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        JPanel right = new JPanel();
        JButton btnProfile = new JButton("Profile");
        JButton btnLogout = new JButton("Logout");
        right.add(btnProfile); right.add(btnLogout);
        top.add(lbl, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        btnProfile.addActionListener(e -> showProfileWindow());
        btnLogout.addActionListener(e -> { loggedUser = null; f.dispose(); users = DataStore.loadUsers(); papers = DataStore.loadPapers(); showLoginWindow(); });

        JPanel center = new JPanel(new BorderLayout());
        if (loggedUser.getRole().equalsIgnoreCase("Student")) center.add(studentPanel(), BorderLayout.CENTER);
        else if (loggedUser.getRole().equalsIgnoreCase("Faculty")) center.add(facultyPanel(), BorderLayout.CENTER);
        else center.add(approverPanel(), BorderLayout.CENTER); // Tutor/HOD/Dean/Principal

        f.add(top, BorderLayout.NORTH);
        f.add(center, BorderLayout.CENTER);
        f.setVisible(true);
    }

    // ---------------- Profile window ----------------
    static void showProfileWindow() {
        JFrame f = new JFrame("Profile");
        f.setSize(520,320);
        f.setLocationRelativeTo(null);
        f.setLayout(null);

        JLabel ln = new JLabel("Name: " + loggedUser.getDisplayName());
        ln.setBounds(20,20,460,25);
        JLabel lr = new JLabel("Role: " + loggedUser.getRole());
        lr.setBounds(20,50,460,25);
        JLabel ld = new JLabel("Dept/Class: " + loggedUser.getDepartment() + " / " + loggedUser.getClassName());
        ld.setBounds(20,80,460,25);

        JLabel lnp = new JLabel("New password:");
        lnp.setBounds(20,120,120,25);
        JPasswordField pfNew = new JPasswordField();
        pfNew.setBounds(150,120,200,25);
        JButton bUpdate = new JButton("Update");
        bUpdate.setBounds(370,120,100,25);

        bUpdate.addActionListener(e -> {
            String np = new String(pfNew.getPassword()).trim();
            if (np.isEmpty()) { JOptionPane.showMessageDialog(f, "Enter new password."); return; }
            loggedUser.setPassword(np);
            DataStore.saveAllUsers(users);
            JOptionPane.showMessageDialog(f, "Password updated.");
            f.dispose();
        });

        f.add(ln); f.add(lr); f.add(ld); f.add(lnp); f.add(pfNew); f.add(bUpdate);

        if (loggedUser.getRole().equalsIgnoreCase("Faculty")) {
            JLabel ls = new JLabel("Subject: ");
            ls.setBounds(20,160,80,25);
            JTextField tfSub = new JTextField(loggedUser.getSubject());
            tfSub.setBounds(100,160,200,25);

            JLabel lpref = new JLabel("Approver Pref:");
            lpref.setBounds(320,160,100,25);
            JComboBox<String> cbPref = new JComboBox<>(new String[]{"HOD","Dean","Principal","Board of Governors","Governing Body","Executive Committee"});
            cbPref.setBounds(430,160,200,25);
            if (loggedUser.getApproverPreference() != null && !loggedUser.getApproverPreference().isEmpty())
                cbPref.setSelectedItem(loggedUser.getApproverPreference());

            JButton bSave = new JButton("Save");
            bSave.setBounds(200,200,100,30);

            bSave.addActionListener(e -> {
                loggedUser.setApproverPreference((String)cbPref.getSelectedItem());
                DataStore.saveAllUsers(users);
                JOptionPane.showMessageDialog(f, "Profile saved.");
                f.dispose();
            });

            f.add(ls); f.add(tfSub); f.add(lpref); f.add(cbPref); f.add(bSave);
        }

        f.setVisible(true);
    }

    // ---------------- Student panel ----------------
    static JPanel studentPanel() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bSubmit = new JButton("Submit Paper");
        JButton bRefresh = new JButton("Refresh");
        top.add(bSubmit); top.add(bRefresh);

        String[] cols = {"ID","Title","Dept","Class","Status","History"};
        DefaultTableModel model = new DefaultTableModel(cols,0);
        JTable table = new JTable(model);
        loadStudentPapers(model);

        JScrollPane scroll = new JScrollPane(table);

        bSubmit.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(null, "Enter paper title:");
            if (title == null || title.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Title required."); return; }
            JTextArea ta = new JTextArea(10,40);
            int ok = JOptionPane.showConfirmDialog(null, new JScrollPane(ta), "Enter paper content:", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            String content = ta.getText().trim();
            if (content.isEmpty()) { JOptionPane.showMessageDialog(null, "Content required."); return; }

            String[] options = {"Tutor","HOD","Dean","Principal","Board of Governors","Governing Body","Executive Committee"};
            String nxt = (String) JOptionPane.showInputDialog(null, "Choose who should review next (default Tutor):", "Next Approver", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (nxt == null) nxt = "Tutor";

            String id = "P" + System.currentTimeMillis();
            Paper ppr = new Paper(id, title, content, loggedUser.getUsername(), loggedUser.getDisplayName(), loggedUser.getDepartment(), loggedUser.getClassName(), nxt);

            // determine approver display name
            String target = getApproverDisplayForPaper(ppr);
            ppr.addHistory("Sent to " + target + " for approval.");
            papers.add(ppr);
            DataStore.appendPaper(ppr);
            JOptionPane.showMessageDialog(null, "Sent to " + target + " for approval.");
            loadStudentPapers(model);
        });

        bRefresh.addActionListener(e -> loadStudentPapers(model));
        p.add(top, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    static void loadStudentPapers(DefaultTableModel model) {
        model.setRowCount(0);
        papers = DataStore.loadPapers();
        for (Paper p : papers) {
            if (p.getSubmittedByUsername().equalsIgnoreCase(loggedUser.getUsername())) {
                model.addRow(new Object[]{p.getId(), p.getTitle(), p.getDepartment(), p.getClassName(), p.getStatus(), String.join(" | ", p.getHistory())});
            }
        }
    }

    // ---------------- Faculty panel ----------------
    static JPanel facultyPanel() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bSubmit = new JButton("Submit Paper");
        JButton bRefresh = new JButton("Refresh");
        top.add(bSubmit); top.add(bRefresh);

        String[] cols = {"ID","Title","Dept","Class","SubmittedBy","Status"};
        DefaultTableModel model = new DefaultTableModel(cols,0);
        JTable table = new JTable(model);
        loadFacultyPapers(model);

        JScrollPane scroll = new JScrollPane(table);

        bSubmit.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(null, "Enter paper title:");
            if (title == null || title.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Title required."); return; }
            JTextArea ta = new JTextArea(10,40);
            int ok = JOptionPane.showConfirmDialog(null, new JScrollPane(ta), "Enter paper content:", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) return;
            String content = ta.getText().trim();
            if (content.isEmpty()) { JOptionPane.showMessageDialog(null, "Content required."); return; }

            String pref = loggedUser.getApproverPreference();
            String[] options = {"Tutor","HOD","Dean","Principal","Board of Governors","Governing Body","Executive Committee"};
            String nxt = (String) JOptionPane.showInputDialog(null, "Choose who should review next:", "Next Approver", JOptionPane.QUESTION_MESSAGE, null, options, pref == null || pref.isEmpty() ? options[0] : pref);
            if (nxt == null) nxt = pref == null || pref.isEmpty() ? "Tutor" : pref;

            String id = "P" + System.currentTimeMillis();
            Paper ppr = new Paper(id, title, content, loggedUser.getUsername(), loggedUser.getDisplayName(), loggedUser.getDepartment(), loggedUser.getClassName(), nxt);
            String target = getApproverDisplayForPaper(ppr);
            ppr.addHistory("Sent to " + target + " for approval.");
            papers.add(ppr);
            DataStore.appendPaper(ppr);
            JOptionPane.showMessageDialog(null, "Sent to " + target + " for approval.");
            loadFacultyPapers(model);
        });

        bRefresh.addActionListener(e -> loadFacultyPapers(model));
        p.add(top, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    static void loadFacultyPapers(DefaultTableModel model) {
        model.setRowCount(0);
        papers = DataStore.loadPapers();
        for (Paper p : papers) {
            if (p.getDepartment().equalsIgnoreCase(loggedUser.getDepartment()) || p.getSubmittedByUsername().equalsIgnoreCase(loggedUser.getUsername())) {
                model.addRow(new Object[]{p.getId(), p.getTitle(), p.getDepartment(), p.getClassName(), p.getSubmittedByDisplay(), p.getStatus()});
            }
        }
    }

    // ---------------- Approver panel (Tutor/HOD/Dean/Principal) ----------------
    static JPanel approverPanel() {
        JPanel p = new JPanel(new BorderLayout(6,6));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bRefresh = new JButton("Refresh");
        JButton bViewAll = new JButton("View Relevant");
        top.add(bRefresh); top.add(bViewAll);

        String[] cols = {"ID","Title","SubmittedBy","Dept","Class","Status"};
        DefaultTableModel model = new DefaultTableModel(cols,0);
        JTable table = new JTable(model);

        // custom renderer to highlight pending papers
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = table.getModel().getValueAt(row,5).toString().toLowerCase();
                if (status.contains("pending")) {
                    c.setBackground(new Color(255, 240, 200)); // light warm color
                } else {
                    c.setBackground(Color.WHITE);
                }
                if (isSelected) c.setBackground(new Color(200,220,255));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        loadRelevantPapersForApprover(model);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bApprove = new JButton("Approve");
        JButton bReject = new JButton("Reject");
        JButton bDetails = new JButton("Details");
        bottom.add(bApprove); bottom.add(bReject); bottom.add(bDetails);

        bRefresh.addActionListener(e -> loadRelevantPapersForApprover(model));
        bViewAll.addActionListener(e -> loadRelevantPapersForApprover(model));

        bDetails.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(null, "Select a paper."); return; }
            String id = (String) model.getValueAt(sel, 0);
            Paper ppr = findPaperById(id);
            if (ppr == null) { JOptionPane.showMessageDialog(null, "Paper not found."); return; }
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(ppr.getId()).append("\n");
            sb.append("Title: ").append(ppr.getTitle()).append("\n");
            sb.append("Submitted by: ").append(ppr.getSubmittedByDisplay()).append("\n");
            sb.append("Dept/Class: ").append(ppr.getDepartment()).append(" / ").append(ppr.getClassName()).append("\n");
            sb.append("Status: ").append(ppr.getStatus()).append("\n\n");
            sb.append("Content:\n").append(ppr.getContent()).append("\n\nHistory:\n");
            for (String h: ppr.getHistory()) sb.append("- ").append(h).append("\n");
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            JOptionPane.showMessageDialog(null, new JScrollPane(ta), "Paper Details", JOptionPane.INFORMATION_MESSAGE);
        });

        bApprove.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(null, "Select a paper."); return; }
            String id = (String) model.getValueAt(sel, 0);
            Paper ppr = findPaperById(id);
            if (ppr == null) { JOptionPane.showMessageDialog(null, "Paper not found."); return; }
            if (ppr.getStatus().startsWith("Rejected") || ppr.getStatus().equalsIgnoreCase("Approved (Final)")) {
                JOptionPane.showMessageDialog(null, "Paper already finalized."); return;
            }

            String role = loggedUser.getRole();
            // permission checks
            if (role.equalsIgnoreCase("Tutor")) {
                if (!ppr.getClassName().equalsIgnoreCase(loggedUser.getClassName())) { JOptionPane.showMessageDialog(null, "You can only approve papers of your class."); return; }
                if (!ppr.getNextStage().equalsIgnoreCase("Tutor") && !ppr.getStatus().toLowerCase().contains("tutor")) { JOptionPane.showMessageDialog(null, "Paper not at Tutor stage."); return; }
            } else if (role.equalsIgnoreCase("HOD")) {
                if (!ppr.getDepartment().equalsIgnoreCase(loggedUser.getDepartment())) { JOptionPane.showMessageDialog(null, "You can only approve papers in your department."); return; }
                if (!ppr.getNextStage().equalsIgnoreCase("HOD") && !ppr.getStatus().toLowerCase().contains("hod")) { JOptionPane.showMessageDialog(null, "Paper not at HOD stage."); return; }
            } else if (role.equalsIgnoreCase("Dean")) {
                if (!ppr.getNextStage().equalsIgnoreCase("Dean") && !ppr.getStatus().toLowerCase().contains("dean")) { JOptionPane.showMessageDialog(null, "Paper not at Dean stage."); return; }
            } else if (role.equalsIgnoreCase("Principal")) {
                if (!ppr.getNextStage().equalsIgnoreCase("Principal") && !ppr.getStatus().toLowerCase().contains("principal")) { JOptionPane.showMessageDialog(null, "Paper not at Principal stage."); return; }
            }

            ppr.approve(role, loggedUser.getDisplayName());
            DataStore.saveAllPapers(papers);
            JOptionPane.showMessageDialog(null, "Approved. Next: " + ppr.getNextStage());
            loadRelevantPapersForApprover(model);
        });

        bReject.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel == -1) { JOptionPane.showMessageDialog(null, "Select a paper."); return; }
            String id = (String) model.getValueAt(sel, 0);
            Paper ppr = findPaperById(id);
            if (ppr == null) { JOptionPane.showMessageDialog(null, "Paper not found."); return; }
            if (ppr.getStatus().startsWith("Rejected") || ppr.getStatus().equalsIgnoreCase("Approved (Final)")) {
                JOptionPane.showMessageDialog(null, "Paper already finalized."); return;
            }
            String reason = JOptionPane.showInputDialog(null, "Reason for rejection (optional):");
            ppr.reject(loggedUser.getRole(), loggedUser.getDisplayName(), reason);
            DataStore.saveAllPapers(papers);
            JOptionPane.showMessageDialog(null, "Paper rejected.");
            loadRelevantPapersForApprover(model);
        });

        p.add(top, BorderLayout.NORTH);
        p.add(scroll, BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);
        return p;
    }

    // determine approver display name for a paper's nextStage
    static String getApproverDisplayForPaper(Paper p) {
        String next = p.getNextStage();
        if (next.equalsIgnoreCase("Tutor")) {
            Optional<User> tut = users.stream().filter(u -> u.getRole().equalsIgnoreCase("Tutor") && u.getClassName().equalsIgnoreCase(p.getClassName())).findFirst();
            return tut.map(User::getDisplayName).orElse("Tutor");
        } else if (next.equalsIgnoreCase("HOD")) {
            Optional<User> hod = users.stream().filter(u -> u.getRole().equalsIgnoreCase("HOD") && u.getDepartment().equalsIgnoreCase(p.getDepartment())).findFirst();
            return hod.map(User::getDisplayName).orElse("HOD");
        } else if (next.equalsIgnoreCase("Dean")) {
            Optional<User> dean = users.stream().filter(u -> u.getRole().equalsIgnoreCase("Dean")).findFirst();
            return dean.map(User::getDisplayName).orElse("Dean");
        } else if (next.equalsIgnoreCase("Principal")) {
            Optional<User> pr = users.stream().filter(u -> u.getRole().equalsIgnoreCase("Principal")).findFirst();
            return pr.map(User::getDisplayName).orElse("Principal");
        } else {
            return next;
        }
    }

    static Paper findPaperById(String id) {
        papers = DataStore.loadPapers();
        for (Paper p : papers) if (p.getId().equals(id)) return p;
        return null;
    }

    static void loadRelevantPapersForApprover(DefaultTableModel model) {
        model.setRowCount(0);
        papers = DataStore.loadPapers();
        String role = loggedUser.getRole();
        for (Paper p : papers) {
            if (role.equalsIgnoreCase("Tutor")) {
                if (p.getClassName().equalsIgnoreCase(loggedUser.getClassName())) model.addRow(new Object[]{p.getId(), p.getTitle(), p.getSubmittedByDisplay(), p.getDepartment(), p.getClassName(), p.getStatus()});
            } else if (role.equalsIgnoreCase("HOD")) {
                if (p.getDepartment().equalsIgnoreCase(loggedUser.getDepartment())) model.addRow(new Object[]{p.getId(), p.getTitle(), p.getSubmittedByDisplay(), p.getDepartment(), p.getClassName(), p.getStatus()});
            } else if (role.equalsIgnoreCase("Dean") || role.equalsIgnoreCase("Principal")) {
                model.addRow(new Object[]{p.getId(), p.getTitle(), p.getSubmittedByDisplay(), p.getDepartment(), p.getClassName(), p.getStatus()});
            }
        }
    }
}
