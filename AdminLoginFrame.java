import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;

public class AdminLoginFrame extends JFrame {

    JTextField usernameField;
    JPasswordField passwordField;

    JButton loginButton;
    JButton backButton;

    public AdminLoginFrame() {

        setTitle("Admin Login - Smart Community Resource Rental System");
        setSize(500, 500);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Title
        JLabel titleLabel = new JLabel("Admin Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(170, 40, 200, 40);
        add(titleLabel);

        // Username / Email
        JLabel usernameLabel = new JLabel("Email / User:");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        usernameLabel.setBounds(80, 130, 120, 30);
        add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(200, 130, 200, 30);
        add(usernameField);

        // Password
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 15));
        passwordLabel.setBounds(80, 190, 120, 30);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(200, 190, 200, 30);
        add(passwordField);

        // Login Button
        loginButton = new JButton("Admin Login");
        loginButton.setBounds(150, 260, 200, 40);
        add(loginButton);

        // Back Button
        backButton = new JButton("Back to User Login");
        backButton.setBounds(150, 320, 200, 40);
        add(backButton);

        // Login Action
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputUser = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (inputUser.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            AdminLoginFrame.this,
                            "Please enter Admin Email/Username and Password!",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                // Fallback Admin
                if ((inputUser.equalsIgnoreCase("admin") || inputUser.equalsIgnoreCase("admin@gmail.com")) 
                        && password.equals("admin123")) {
                    Session.userId = 1;
                    Session.userName = "Admin User";
                    Session.userEmail = "admin@gmail.com";
                    JOptionPane.showMessageDialog(AdminLoginFrame.this, "Welcome Admin!\nLogin Successful.");
                    new AdminDashboard();
                    dispose();
                    return;
                }

                // Database Admin Query
                String sql = "SELECT user_id, name, email FROM users WHERE (email = ? OR name = ?) AND password = ? AND role = 'ADMIN'";
                try (Connection con = DBConnection.getConnection()) {
                    if (con != null) {
                        try (PreparedStatement ps = con.prepareStatement(sql)) {
                            ps.setString(1, inputUser);
                            ps.setString(2, inputUser);
                            ps.setString(3, password);

                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    Session.userId = rs.getInt("user_id");
                                    Session.userName = rs.getString("name");
                                    Session.userEmail = rs.getString("email");

                                    JOptionPane.showMessageDialog(AdminLoginFrame.this, "Welcome " + Session.userName + "!\nAdmin Login Successful.");
                                    new AdminDashboard();
                                    dispose();
                                    return;
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Admin Login DB Exception: " + ex.getMessage());
                }

                JOptionPane.showMessageDialog(
                        AdminLoginFrame.this,
                        "Invalid Admin Credentials!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        backButton.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new AdminLoginFrame();
    }
}