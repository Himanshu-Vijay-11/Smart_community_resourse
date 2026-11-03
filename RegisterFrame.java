import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterFrame extends JFrame {

    JTextField nameField;
    JTextField emailField;
    JTextField phoneField;

    JPasswordField passwordField;
    JPasswordField confirmPasswordField;

    JButton registerButton;
    JButton backButton;

    public RegisterFrame() {

        setTitle("User Registration");
        setSize(500, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(125, 30, 300, 40);
        add(titleLabel);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setBounds(70, 100, 150, 30);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(220, 100, 200, 30);
        add(nameField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(70, 150, 150, 30);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(220, 150, 200, 30);
        add(emailField);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(70, 200, 150, 30);
        add(phoneLabel);

        phoneField = new JTextField();
        phoneField.setBounds(220, 200, 200, 30);
        add(phoneField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(70, 250, 150, 30);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(220, 250, 200, 30);
        add(passwordField);

        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setBounds(70, 300, 150, 30);
        add(confirmLabel);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(220, 300, 200, 30);
        add(confirmPasswordField);

        registerButton = new JButton("Register");
        registerButton.setBounds(150, 370, 200, 40);
        add(registerButton);

        backButton = new JButton("Back to Login");
        backButton.setBounds(150, 430, 200, 40);
        add(backButton);

        // REGISTER
        registerButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();

                String password =
                        new String(passwordField.getPassword());

                String confirmPassword =
                        new String(confirmPasswordField.getPassword());

                // EMPTY CHECK
                if (name.isEmpty() ||
                    email.isEmpty() ||
                    phone.isEmpty() ||
                    password.isEmpty() ||
                    confirmPassword.isEmpty()) {

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Please fill all fields!"
                    );
                    return;
                }

                // EMAIL CHECK
                if (!email.contains("@")) {

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Please enter a valid email!"
                    );
                    return;
                }

                // PHONE CHECK
                if (!phone.matches("\\d{10}")) {

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Phone number must be 10 digits!"
                    );
                    return;
                }

                // PASSWORD LENGTH
                if (password.length() < 6) {

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Password must be at least 6 characters!"
                    );
                    return;
                }

                // PASSWORD MATCH
                if (!password.equals(confirmPassword)) {

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Passwords do not match!"
                    );
                    return;
                }

                // DATABASE INSERT
                String sql = "INSERT INTO users " +
                        "(name, email, phone, password, role) " +
                        "VALUES (?, ?, ?, ?, 'USER')";

                try (
                    Connection con = DBConnection.getConnection();
                    PreparedStatement ps = con.prepareStatement(sql)
                ) {

                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, password);

                    int result = ps.executeUpdate();

                    if (result > 0) {

                        JOptionPane.showMessageDialog(
                                RegisterFrame.this,
                                "Registration Successful!"
                        );

                        new LoginFrame();
                        dispose();
                    }

                } catch (Exception ex) {

                    ex.printStackTrace();

                    JOptionPane.showMessageDialog(
                            RegisterFrame.this,
                            "Registration Failed!\n" +
                            "Email may already exist.",
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        // BACK
        backButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                new LoginFrame();
                dispose();
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {

        new RegisterFrame();
    }
}