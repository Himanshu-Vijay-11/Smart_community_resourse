import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageCategoryFrame extends JFrame {

    JTextField categoryField;
    JTable categoryTable;
    DefaultTableModel model;

    JButton addButton;
    JButton deleteButton;
    JButton backButton;

    public ManageCategoryFrame() {

        setTitle("Manage Categories - Admin");
        setSize(650, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Title
        JLabel titleLabel = new JLabel("Manage Categories");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(190, 30, 300, 40);
        add(titleLabel);

        // Category Name
        JLabel categoryLabel = new JLabel("Category Name:");
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        categoryLabel.setBounds(70, 110, 140, 30);
        add(categoryLabel);

        categoryField = new JTextField();
        categoryField.setBounds(210, 110, 220, 30);
        add(categoryField);

        // Add Button
        addButton = new JButton("Add Category");
        addButton.setBounds(120, 170, 180, 40);
        add(addButton);

        // Delete Button
        deleteButton = new JButton("Delete Category");
        deleteButton.setBounds(320, 170, 180, 40);
        add(deleteButton);

        // Table
        String[] columns = {"ID", "Category Name", "Description"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        categoryTable = new JTable(model);

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBounds(100, 240, 450, 220);
        add(scrollPane);

        // Back Button
        backButton = new JButton("Back to Admin Dashboard");
        backButton.setBounds(180, 490, 250, 40);
        add(backButton);

        // Load Categories from Database
        loadCategoriesFromDatabase();

        // ADD CATEGORY
        addButton.addActionListener(e -> {
            String category = categoryField.getText().trim();
            if (category.isEmpty()) {
                JOptionPane.showMessageDialog(ManageCategoryFrame.this, "Please enter category name!");
                return;
            }

            String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, category);
                ps.setString(2, category + " category items");
                ps.executeUpdate();

                JOptionPane.showMessageDialog(ManageCategoryFrame.this, "Category Added Successfully!");
                categoryField.setText("");
                loadCategoriesFromDatabase();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ManageCategoryFrame.this, "Error adding category: " + ex.getMessage());
            }
        });

        // DELETE CATEGORY
        deleteButton.addActionListener(e -> {
            int selectedRow = categoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ManageCategoryFrame.this, "Please select a category first!");
                return;
            }

            int catId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int choice = JOptionPane.showConfirmDialog(
                    ManageCategoryFrame.this,
                    "Are you sure you want to delete this category?",
                    "Delete Category",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM categories WHERE category_id = ?";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, catId);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(ManageCategoryFrame.this, "Category Deleted Successfully!");
                    loadCategoriesFromDatabase();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageCategoryFrame.this, "Error deleting category: " + ex.getMessage());
                }
            }
        });

        // BACK
        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadCategoriesFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT category_id, name, description FROM categories";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("category_id"),
                                rs.getString("name"),
                                rs.getString("description")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Load Categories Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ManageCategoryFrame();
    }
}