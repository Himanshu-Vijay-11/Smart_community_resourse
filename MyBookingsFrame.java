import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class MyBookingsFrame extends JFrame {

    JTable table;
    DefaultTableModel model;

    public MyBookingsFrame() {

        setTitle("My Bookings - Smart Community");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("My Bookings");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(title, BorderLayout.NORTH);

        String[] columns = {"Booking ID", "Resource Name", "Start Date", "End Date", "Total Price (₹)", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        JButton cancelButton = new JButton("Cancel Booking");
        JButton backButton = new JButton("Back to Dashboard");

        buttonPanel.add(refreshButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(backButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        add(panel);

        // Load bookings from DB
        loadBookings();

        refreshButton.addActionListener(e -> loadBookings());
        cancelButton.addActionListener(e -> cancelBooking());
        backButton.addActionListener(e -> {
            new UserDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadBookings() {
        model.setRowCount(0);
        int uid = Session.userId > 0 ? Session.userId : 2; // Default fallback for testing

        String sql = "SELECT booking_id, resource_name, start_date, end_date, total_price, status FROM bookings WHERE user_id = ? ORDER BY booking_id DESC";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, uid);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            model.addRow(new Object[]{
                                    rs.getInt("booking_id"),
                                    rs.getString("resource_name"),
                                    rs.getString("start_date"),
                                    rs.getString("end_date"),
                                    rs.getDouble("total_price"),
                                    rs.getString("status")
                            });
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
    }

    private void cancelBooking() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel.");
            return;
        }

        int bookingId = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
        String status = model.getValueAt(selectedRow, 5).toString();

        if (status.equalsIgnoreCase("CANCELLED") || status.equalsIgnoreCase("COMPLETED")) {
            JOptionPane.showMessageDialog(this, "This booking cannot be cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Cancel Booking ID " + bookingId + "?", "Confirm Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "UPDATE bookings SET status = 'CANCELLED' WHERE booking_id = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Booking Cancelled Successfully!");
                loadBookings();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        new MyBookingsFrame();
    }
}