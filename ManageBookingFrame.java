import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageBookingFrame extends JFrame {

    JTable bookingTable;
    DefaultTableModel model;

    JButton approveButton;
    JButton rejectButton;
    JButton refreshButton;
    JButton backButton;

    public ManageBookingFrame() {

        setTitle("Manage Bookings - Admin");
        setSize(900, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Manage Bookings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(330, 25, 300, 40);
        add(titleLabel);

        String[] columns = {"Booking ID", "User Name", "Resource", "Start Date", "End Date", "Total Price (₹)", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        bookingTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBounds(40, 90, 800, 270);
        add(scrollPane);

        approveButton = new JButton("Approve Booking");
        approveButton.setBounds(100, 390, 180, 45);
        add(approveButton);

        rejectButton = new JButton("Reject Booking");
        rejectButton.setBounds(300, 390, 180, 45);
        add(rejectButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(500, 390, 140, 45);
        add(refreshButton);

        backButton = new JButton("Back to Admin Dashboard");
        backButton.setBounds(280, 460, 260, 45);
        add(backButton);

        // Load Bookings
        loadBookingsFromDatabase();

        approveButton.addActionListener(e -> updateBookingStatus("APPROVED"));
        rejectButton.addActionListener(e -> updateBookingStatus("REJECTED"));
        refreshButton.addActionListener(e -> loadBookingsFromDatabase());

        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadBookingsFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT booking_id, user_name, resource_name, start_date, end_date, total_price, status FROM bookings ORDER BY booking_id DESC";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("booking_id"),
                                rs.getString("user_name"),
                                rs.getString("resource_name"),
                                rs.getString("start_date"),
                                rs.getString("end_date"),
                                rs.getDouble("total_price"),
                                rs.getString("status")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error loading bookings: " + ex.getMessage());
        }
    }

    private void updateBookingStatus(String newStatus) {
        int row = bookingTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking first!");
            return;
        }

        int bookingId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String sql = "UPDATE bookings SET status = ? WHERE booking_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, bookingId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Booking status updated to: " + newStatus);
            loadBookingsFromDatabase();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating status: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ManageBookingFrame();
    }
}