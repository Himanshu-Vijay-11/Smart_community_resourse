import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {

    JTextField emailField;
    JPasswordField passwordField;

    JButton loginButton;
    JButton registerButton;
    JButton adminButton;

    public LoginFrame() {

        setTitle("Smart Community Resource Rental & Booking System");
        setSize(500, 550);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // =================================================
        // TITLE
        // =================================================

        JLabel titleLabel =
                new JLabel("User Login");

        titleLabel.setFont(
                new Font("Arial", Font.BOLD, 24)
        );

        titleLabel.setBounds(
                170, 40, 200, 40
        );

        add(titleLabel);

        // =================================================
        // EMAIL
        // =================================================

        JLabel emailLabel =
                new JLabel("Email:");

        emailLabel.setBounds(
                80, 120, 120, 30
        );

        add(emailLabel);

        emailField =
                new JTextField();

        emailField.setBounds(
                200, 120, 200, 30
        );

        add(emailField);

        // =================================================
        // PASSWORD
        // =================================================

        JLabel passwordLabel =
                new JLabel("Password:");

        passwordLabel.setBounds(
                80, 180, 120, 30
        );

        add(passwordLabel);

        passwordField =
                new JPasswordField();

        passwordField.setBounds(
                200, 180, 200, 30
        );

        add(passwordField);

        // =================================================
        // LOGIN BUTTON
        // =================================================

        loginButton =
                new JButton("Login");

        loginButton.setBounds(
                150, 250, 200, 40
        );

        add(loginButton);

        // =================================================
        // REGISTER BUTTON
        // =================================================

        registerButton =
                new JButton("Create New Account");

        registerButton.setBounds(
                150, 310, 200, 40
        );

        add(registerButton);

        // =================================================
        // ADMIN BUTTON
        // =================================================

        adminButton =
                new JButton("Admin Login");

        adminButton.setBounds(
                150, 370, 200, 40
        );

        add(adminButton);

        // =================================================
        // USER LOGIN
        // =================================================

        loginButton.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        String email =
                                emailField
                                        .getText()
                                        .trim();

                        String password =
                                new String(
                                        passwordField
                                                .getPassword()
                                );

                        // ---------------- VALIDATION ----------------

                        if (email.isEmpty()
                                || password.isEmpty()) {

                            JOptionPane.showMessageDialog(
                                    LoginFrame.this,
                                    "Please enter Email and Password!",
                                    "Warning",
                                    JOptionPane.WARNING_MESSAGE
                            );

                            return;
                        }

                        // =================================================
                        // DATABASE QUERY
                        // =================================================

                        String sql =
                                "SELECT user_id, name, email, role " +
                                "FROM users " +
                                "WHERE email = ? " +
                                "AND password = ? " +
                                "AND role = 'USER'";

                        try (
                                Connection con =
                                        DBConnection.getConnection();

                                PreparedStatement ps =
                                        con.prepareStatement(sql)
                        ) {

                            ps.setString(1, email);
                            ps.setString(2, password);

                            ResultSet rs =
                                    ps.executeQuery();

                            // =================================================
                            // LOGIN SUCCESS
                            // =================================================

                            if (rs.next()) {

                                // Get user information

                                int userId =
                                        rs.getInt("user_id");

                                String name =
                                        rs.getString("name");

                                String userEmail =
                                        rs.getString("email");

                                // =================================================
                                // SAVE USER IN SESSION
                                // =================================================

                                Session.userId =
                                        userId;

                                Session.userName =
                                        name;

                                Session.userEmail =
                                        userEmail;

                                // =================================================
                                // SUCCESS MESSAGE
                                // =================================================

                                JOptionPane.showMessageDialog(
                                        LoginFrame.this,

                                        "Welcome "
                                        + name
                                        + "!\n\n"
                                        + "Login Successful",

                                        "Login Successful",

                                        JOptionPane.INFORMATION_MESSAGE
                                );

                                // =================================================
                                // OPEN USER DASHBOARD
                                // =================================================

                                new UserDashboard();

                                dispose();

                            } else {

                                // =================================================
                                // LOGIN FAILED
                                // =================================================

                                JOptionPane.showMessageDialog(
                                        LoginFrame.this,

                                        "Invalid Email or Password!",

                                        "Login Failed",

                                        JOptionPane.ERROR_MESSAGE
                                );
                            }

                        } catch (Exception ex) {

                            ex.printStackTrace();

                            JOptionPane.showMessageDialog(
                                    LoginFrame.this,

                                    "Database Connection Error!\n"
                                    + ex.getMessage(),

                                    "Error",

                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
        );

        // =================================================
        // REGISTER
        // =================================================

        registerButton.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        new RegisterFrame();

                        dispose();
                    }
                }
        );

        // =================================================
        // ADMIN LOGIN
        // =================================================

        adminButton.addActionListener(
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        new AdminLoginFrame();

                        dispose();
                    }
                }
        );

        // =================================================
        // SHOW WINDOW
        // =================================================

        setVisible(true);
    }

    // =================================================
    // MAIN METHOD
    // =================================================

    public static void main(String[] args) {

        new LoginFrame();
    }
}