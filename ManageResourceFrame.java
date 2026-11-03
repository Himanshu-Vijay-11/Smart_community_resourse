import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageResourceFrame extends JFrame {

    JTextField nameField;
    JTextField quantityField;
    JTextField availableField;
    JTextField rentField;

    JComboBox<String> categoryBox;
    JComboBox<String> statusBox;

    JTable resourceTable;
    DefaultTableModel model;

    JButton addButton;
    JButton updateButton;
    JButton deleteButton;
    JButton clearButton;
    JButton backButton;

    public ManageResourceFrame() {

        setTitle("Resource Management - Admin");
        setSize(950, 700);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // TITLE
        JLabel titleLabel = new JLabel("Resource Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(330, 20, 350, 40);
        add(titleLabel);

        // RESOURCE NAME
        JLabel nameLabel = new JLabel("Resource Name:");
        nameLabel.setBounds(50, 90, 150, 30);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(200, 90, 220, 30);
        add(nameField);

        // CATEGORY
        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setBounds(500, 90, 120, 30);
        add(categoryLabel);

        categoryBox = new JComboBox<>(new String[]{"Tools", "Electronics", "Event", "Sports", "Medical", "Camping", "Educational"});
        categoryBox.setBounds(620, 90, 220, 30);
        add(categoryBox);

        // TOTAL QUANTITY
        JLabel quantityLabel = new JLabel("Total Quantity:");
        quantityLabel.setBounds(50, 140, 150, 30);
        add(quantityLabel);

        quantityField = new JTextField();
        quantityField.setBounds(200, 140, 220, 30);
        add(quantityField);

        // AVAILABLE QUANTITY
        JLabel availableLabel = new JLabel("Available Quantity:");
        availableLabel.setBounds(500, 140, 120, 30);
        add(availableLabel);

        availableField = new JTextField();
        availableField.setBounds(620, 140, 220, 30);
        add(availableField);

        // RENT
        JLabel rentLabel = new JLabel("Rent Per Day (₹):");
        rentLabel.setBounds(50, 190, 150, 30);
        add(rentLabel);

        rentField = new JTextField();
        rentField.setBounds(200, 190, 220, 30);
        add(rentField);

        // STATUS
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setBounds(500, 190, 120, 30);
        add(statusLabel);

        statusBox = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusBox.setBounds(620, 190, 220, 30);
        add(statusBox);

        // BUTTONS
        addButton = new JButton("ADD");
        addButton.setBounds(100, 250, 150, 40);
        add(addButton);

        updateButton = new JButton("UPDATE");
        updateButton.setBounds(280, 250, 150, 40);
        add(updateButton);

        deleteButton = new JButton("DELETE");
        deleteButton.setBounds(460, 250, 150, 40);
        add(deleteButton);

        clearButton = new JButton("CLEAR");
        clearButton.setBounds(640, 250, 150, 40);
        add(clearButton);

        // TABLE
        String[] columns = {"ID", "Resource Name", "Category", "Quantity", "Available", "Rent/Day", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resourceTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(resourceTable);
        scrollPane.setBounds(50, 320, 830, 230);
        add(scrollPane);

        // BACK BUTTON
        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(350, 580, 220, 40);
        add(backButton);

        // Load resources from database
        loadResourcesFromDatabase();

        // ADD BUTTON
        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String category = categoryBox.getSelectedItem().toString();
            String quantityText = quantityField.getText().trim();
            String availableText = availableField.getText().trim();
            String rentText = rentField.getText().trim();
            String status = statusBox.getSelectedItem().toString();

            if (name.isEmpty() || quantityText.isEmpty() || availableText.isEmpty() || rentText.isEmpty()) {
                JOptionPane.showMessageDialog(ManageResourceFrame.this, "Please fill all fields!");
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityText);
                int available = Integer.parseInt(availableText);
                double rent = Double.parseDouble(rentText);

                String sql = "INSERT INTO resources (name, category, quantity, available, rent_per_day, status) VALUES (?, ?, ?, ?, ?, ?)";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, category);
                    ps.setInt(3, quantity);
                    ps.setInt(4, available);
                    ps.setDouble(5, rent);
                    ps.setString(6, status);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(ManageResourceFrame.this, "Resource Added Successfully!");
                    clearFields();
                    loadResourcesFromDatabase();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ManageResourceFrame.this, "Error adding resource: " + ex.getMessage());
            }
        });

        // TABLE SELECT
        resourceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = resourceTable.getSelectedRow();
                if (selectedRow != -1) {
                    nameField.setText(model.getValueAt(selectedRow, 1).toString());
                    categoryBox.setSelectedItem(model.getValueAt(selectedRow, 2).toString());
                    quantityField.setText(model.getValueAt(selectedRow, 3).toString());
                    availableField.setText(model.getValueAt(selectedRow, 4).toString());
                    rentField.setText(model.getValueAt(selectedRow, 5).toString());
                    statusBox.setSelectedItem(model.getValueAt(selectedRow, 6).toString());
                }
            }
        });

        // UPDATE BUTTON
        updateButton.addActionListener(e -> {
            int selectedRow = resourceTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ManageResourceFrame.this, "Please select a resource first!");
                return;
            }

            int id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            try {
                String name = nameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                int available = Integer.parseInt(availableField.getText().trim());
                double rent = Double.parseDouble(rentField.getText().trim());
                String category = categoryBox.getSelectedItem().toString();
                String status = statusBox.getSelectedItem().toString();

                String sql = "UPDATE resources SET name = ?, category = ?, quantity = ?, available = ?, rent_per_day = ?, status = ? WHERE resource_id = ?";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, category);
                    ps.setInt(3, quantity);
                    ps.setInt(4, available);
                    ps.setDouble(5, rent);
                    ps.setString(6, status);
                    ps.setInt(7, id);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(ManageResourceFrame.this, "Resource Updated Successfully!");
                    clearFields();
                    loadResourcesFromDatabase();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ManageResourceFrame.this, "Error updating resource: " + ex.getMessage());
            }
        });

        // DELETE BUTTON
        deleteButton.addActionListener(e -> {
            int selectedRow = resourceTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ManageResourceFrame.this, "Please select a resource first!");
                return;
            }

            int id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int choice = JOptionPane.showConfirmDialog(
                    ManageResourceFrame.this,
                    "Are you sure you want to delete this resource?",
                    "Delete Resource",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM resources WHERE resource_id = ?";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(ManageResourceFrame.this, "Resource Deleted Successfully!");
                    clearFields();
                    loadResourcesFromDatabase();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ManageResourceFrame.this, "Error deleting resource: " + ex.getMessage());
                }
            }
        });

        clearButton.addActionListener(e -> clearFields());

        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadResourcesFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT resource_id, name, category, quantity, available, rent_per_day, status FROM resources";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("resource_id"),
                                rs.getString("name"),
                                rs.getString("category"),
                                rs.getInt("quantity"),
                                rs.getInt("available"),
                                rs.getDouble("rent_per_day"),
                                rs.getString("status")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Load Resources Error: " + ex.getMessage());
        }
    }

    public void clearFields() {
        nameField.setText("");
        quantityField.setText("");
        availableField.setText("");
        rentField.setText("");
        categoryBox.setSelectedIndex(0);
        statusBox.setSelectedIndex(0);
        resourceTable.clearSelection();
    }

    public static void main(String[] args) {
        new ManageResourceFrame();
    }
}
