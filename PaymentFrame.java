import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.*;

public class PaymentFrame extends JFrame {

    JTextField bookingIdField;
    JTextField amountField;
    JComboBox<String> paymentMethodBox;

    JButton paymentButton;
    JButton backButton;

    public PaymentFrame() {

        setTitle("Payment - Smart Community Resource Rental System");
        setSize(550, 500);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Payment");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(220, 40, 150, 40);
        add(titleLabel);

        JLabel bookingIdLabel = new JLabel("Booking ID:");
        bookingIdLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        bookingIdLabel.setBounds(80, 120, 150, 30);
        add(bookingIdLabel);

        bookingIdField = new JTextField();
        bookingIdField.setBounds(230, 120, 200, 30);
        add(bookingIdField);

        JLabel amountLabel = new JLabel("Amount (₹):");
        amountLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        amountLabel.setBounds(80, 170, 150, 30);
        add(amountLabel);

        amountField = new JTextField();
        amountField.setBounds(230, 170, 200, 30);
        add(amountField);

        JLabel methodLabel = new JLabel("Payment Method:");
        methodLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        methodLabel.setBounds(80, 220, 150, 30);
        add(methodLabel);

        paymentMethodBox = new JComboBox<>(new String[]{"UPI", "Debit Card", "Credit Card", "Cash"});
        paymentMethodBox.setBounds(230, 220, 200, 30);
        add(paymentMethodBox);

        paymentButton = new JButton("Make Payment");
        paymentButton.setBounds(150, 290, 250, 45);
        add(paymentButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(150, 350, 250, 45);
        add(backButton);

        paymentButton.addActionListener(e -> {
            String bookingIdText = bookingIdField.getText().trim();
            String amountText = amountField.getText().trim();
            String method = paymentMethodBox.getSelectedItem().toString();

            if (bookingIdText.isEmpty() || amountText.isEmpty()) {
                JOptionPane.showMessageDialog(PaymentFrame.this, "Please fill all fields!");
                return;
            }

            try {
                int bookingId = Integer.parseInt(bookingIdText);
                double amount = Double.parseDouble(amountText);
                String txnId = "TXN" + System.currentTimeMillis();
                int userId = Session.userId > 0 ? Session.userId : 2;

                String sql = "INSERT INTO payments (booking_id, user_id, amount, payment_method, transaction_id, status) VALUES (?, ?, ?, ?, ?, 'COMPLETED')";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, bookingId);
                    ps.setInt(2, userId);
                    ps.setDouble(3, amount);
                    ps.setString(4, method);
                    ps.setString(5, txnId);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(PaymentFrame.this, "Payment Successful!\nTransaction ID: " + txnId + "\nAmount Paid: ₹" + amount);
                    new UserDashboard();
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(PaymentFrame.this, "Payment Error: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> {
            new UserDashboard();
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new PaymentFrame();
    }
}