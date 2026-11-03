import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageUserFrame extends JFrame {

    JTable userTable;
    DefaultTableModel model;

    JButton addButton;
    JButton deleteButton;
    JButton refreshButton;
    JButton backButton;

    public ManageUserFrame() {

        setTitle("Manage Users - Smart Community Resource Rental System");
        setSize(850, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Manage Community Users");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBounds(270, 20, 350, 40);
        add(titleLabel);

        String[] columns = {"User ID", "Name", "Email", "Phone", "Role"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(model);
        userTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBounds(50, 80, 740, 320);
        add(scrollPane);

        addButton = new JButton("Add New User");
        addButton.setBounds(80, 440, 160, 40);
        add(addButton);

        deleteButton = new JButton("Delete User");
        deleteButton.setBounds(260, 440, 160, 40);
        add(deleteButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(440, 440, 140, 40);
        add(refreshButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(600, 440, 190, 40);
        add(backButton);

        // Load users from DB
        loadUsersFromDatabase();

        // ADD USER
        addButton.addActionListener(e -> {
            JTextField nameField = new JTextField();
            JTextField emailField = new JTextField();
            JTextField phoneField = new JTextField();
            JPasswordField passField = new JPasswordField();
            JComboBox<String> roleBox = new JComboBox<>(new String[]{"USER", "ADMIN"});

            Object[] fields = {
                    "Full Name:", nameField,
                    "Email:", emailField,
                    "Phone:", phoneField,
                    "Password:", passField,
                    "Role:", roleBox
            };

            int result = JOptionPane.showConfirmDialog(
                    ManageUserFrame.this,
                    fields,
                    "Add New User",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String password = new String(passField.getPassword());
                String role = roleBox.getSelectedItem().toString();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(ManageUserFrame.this, "Please fill required fields (Name, Email, Password)!");
                    return;
                }

                String sql = "INSERT INTO users (name, email, phone, password, role) VALUES (?, ?, ?, ?, ?)";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, password);
                    ps.setString(5, role);

                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(ManageUserFrame.this, "User Added Successfully!");
                    loadUsersFromDatabase();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageUserFrame.this, "Error adding user: " + ex.getMessage());
                }
            }
        });

        // DELETE USER
        deleteButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ManageUserFrame.this, "Please select a user to delete!");
                return;
            }

            int userId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int choice = JOptionPane.showConfirmDialog(
                    ManageUserFrame.this,
                    "Are you sure you want to delete user ID " + userId + "?",
                    "Delete User",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM users WHERE user_id = ?";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                    JOptionPane.showMessageDialog(ManageUserFrame.this, "User Deleted Successfully!");
                    loadUsersFromDatabase();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageUserFrame.this, "Error deleting user: " + ex.getMessage());
                }
            }
        });

        refreshButton.addActionListener(e -> loadUsersFromDatabase());

        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadUsersFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT user_id, name, email, phone, role FROM users";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("user_id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("phone"),
                                rs.getString("role")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Load Users Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ManageUserFrame();
    }
}