import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManagePaymentFrame extends JFrame {

    JTable paymentTable;
    DefaultTableModel model;

    JButton successButton;
    JButton pendingButton;
    JButton refundButton;
    JButton refreshButton;
    JButton backButton;

    public ManagePaymentFrame() {

        setTitle("Manage Payments - Admin");
        setSize(850, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Manage Payments");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(300, 25, 300, 40);
        add(titleLabel);

        String[] columns = {"Payment ID", "Booking ID", "User ID", "Amount (₹)", "Method", "Txn ID", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        paymentTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(paymentTable);
        scrollPane.setBounds(40, 90, 760, 270);
        add(scrollPane);

        successButton = new JButton("Mark Success");
        successButton.setBounds(50, 390, 140, 40);
        add(successButton);

        pendingButton = new JButton("Mark Pending");
        pendingButton.setBounds(200, 390, 140, 40);
        add(pendingButton);

        refundButton = new JButton("Mark Refund");
        refundButton.setBounds(350, 390, 140, 40);
        add(refundButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(500, 390, 120, 40);
        add(refreshButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(630, 390, 170, 40);
        add(backButton);

        // Load payments
        loadPaymentsFromDatabase();

        successButton.addActionListener(e -> updatePaymentStatus("COMPLETED"));
        pendingButton.addActionListener(e -> updatePaymentStatus("PENDING"));
        refundButton.addActionListener(e -> updatePaymentStatus("REFUNDED"));
        refreshButton.addActionListener(e -> loadPaymentsFromDatabase());

        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadPaymentsFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT payment_id, booking_id, user_id, amount, payment_method, transaction_id, status FROM payments ORDER BY payment_id DESC";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("payment_id"),
                                rs.getInt("booking_id"),
                                rs.getInt("user_id"),
                                rs.getDouble("amount"),
                                rs.getString("payment_method"),
                                rs.getString("transaction_id"),
                                rs.getString("status")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error loading payments: " + ex.getMessage());
        }
    }

    private void updatePaymentStatus(String newStatus) {
        int row = paymentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a payment record first!");
            return;
        }

        int payId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String sql = "UPDATE payments SET status = ? WHERE payment_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, payId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Payment status updated to: " + newStatus);
            loadPaymentsFromDatabase();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating payment status: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ManagePaymentFrame();
    }
}