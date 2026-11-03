import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ResourceFrame extends JFrame {

    JTable resourceTable;
    JTextField searchField;

    JButton searchButton;
    JButton bookButton;
    JButton backButton;
    DefaultTableModel model;

    public ResourceFrame() {

        setTitle("Available Community Resources");
        setSize(850, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // TITLE
        JLabel titleLabel = new JLabel("Available Community Resources");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(230, 25, 400, 40);
        add(titleLabel);

        // SEARCH
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        searchLabel.setBounds(50, 90, 80, 30);
        add(searchLabel);

        searchField = new JTextField();
        searchField.setBounds(130, 90, 260, 30);
        add(searchField);

        searchButton = new JButton("Search");
        searchButton.setBounds(410, 90, 110, 30);
        add(searchButton);

        // TABLE
        String[] columns = {"ID", "Resource Name", "Category", "Available", "Rent/Day (₹)"};

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resourceTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(resourceTable);
        scrollPane.setBounds(50, 140, 730, 280);
        add(scrollPane);

        // BOOK BUTTON
        bookButton = new JButton("Book Selected Resource");
        bookButton.setBounds(250, 450, 220, 40);
        add(bookButton);

        // BACK BUTTON
        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(250, 500, 220, 40);
        add(backButton);

        // Load resources from MySQL
        loadResources();

        // SEARCH BUTTON
        searchButton.addActionListener(e -> {
            String searchText = searchField.getText().trim().toLowerCase();
            if (searchText.isEmpty()) {
                loadResources();
                return;
            }

            boolean found = false;
            for (int i = 0; i < resourceTable.getRowCount(); i++) {
                String resourceName = resourceTable.getValueAt(i, 1).toString().toLowerCase();
                if (resourceName.contains(searchText)) {
                    resourceTable.setRowSelectionInterval(i, i);
                    found = true;
                    break;
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(ResourceFrame.this, "Resource not found!");
            }
        });

        // BOOK BUTTON
        bookButton.addActionListener(e -> {
            int selectedRow = resourceTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(ResourceFrame.this, "Please select a resource to book!");
                return;
            }

            int resourceId = Integer.parseInt(resourceTable.getValueAt(selectedRow, 0).toString());
            String resourceName = resourceTable.getValueAt(selectedRow, 1).toString();
            int available = Integer.parseInt(resourceTable.getValueAt(selectedRow, 3).toString());
            double rent = Double.parseDouble(resourceTable.getValueAt(selectedRow, 4).toString());

            if (available <= 0) {
                JOptionPane.showMessageDialog(ResourceFrame.this, "This resource is currently out of stock!");
                return;
            }

            new BookingFrame(resourceId, resourceName, rent, available);
            dispose();
        });

        // BACK BUTTON
        backButton.addActionListener(e -> {
            new UserDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadResources() {
        model.setRowCount(0);
        String sql = "SELECT resource_id, name, category, available, rent_per_day FROM resources WHERE status = 'Active'";

        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("resource_id"),
                                rs.getString("name"),
                                rs.getString("category"),
                                rs.getInt("available"),
                                rs.getDouble("rent_per_day")
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new ResourceFrame();
    }
}