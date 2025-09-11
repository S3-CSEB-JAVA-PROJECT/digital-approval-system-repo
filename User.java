// File: User.java
public class User {
    String username;
    String password;
    String role; // "Student", "Tutor", "HOD", "Principal"

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public boolean login(String u, String p) {
        return username.equals(u) && password.equals(p);
    }
}
