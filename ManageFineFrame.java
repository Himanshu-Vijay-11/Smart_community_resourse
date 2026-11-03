import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageFineFrame extends JFrame {

    JTable fineTable;
    DefaultTableModel model;

    JButton paidButton;
    JButton pendingButton;
    JButton refreshButton;
    JButton backButton;

    public ManageFineFrame() {

        setTitle("Manage Fine - Smart Community Resource Rental System");
        setSize(850, 550);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Manage Fines");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(320, 20, 250, 40);
        add(titleLabel);

        String[] columns = {"Fine ID", "Booking ID", "User Name", "Reason", "Fine Amount (₹)", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        fineTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(fineTable);
        scrollPane.setBounds(40, 90, 760, 250);
        add(scrollPane);

        paidButton = new JButton("Mark as Paid");
        paidButton.setBounds(80, 380, 160, 45);
        add(paidButton);

        pendingButton = new JButton("Mark as Pending");
        pendingButton.setBounds(260, 380, 160, 45);
        add(pendingButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(440, 380, 140, 45);
        add(refreshButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(600, 380, 180, 45);
        add(backButton);

        // Load fines
        loadFinesFromDatabase();

        paidButton.addActionListener(e -> updateFineStatus("PAID"));
        pendingButton.addActionListener(e -> updateFineStatus("PENDING"));
        refreshButton.addActionListener(e -> loadFinesFromDatabase());

        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadFinesFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT fine_id, booking_id, user_name, reason, fine_amount, status FROM fines ORDER BY fine_id DESC";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("fine_id"),
                                rs.getInt("booking_id"),
                                rs.getString("user_name"),
                                rs.getString("reason"),
                                rs.getDouble("fine_amount"),
                                rs.getString("status")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error loading fines: " + ex.getMessage());
        }
    }

    private void updateFineStatus(String newStatus) {
        int selectedRow = fineTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fine record first!");
            return;
        }

        int fineId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        String sql = "UPDATE fines SET status = ? WHERE fine_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, fineId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Fine status updated to: " + newStatus);
            loadFinesFromDatabase();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating fine status: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ManageFineFrame();
    }
}