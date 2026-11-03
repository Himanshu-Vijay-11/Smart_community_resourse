import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class BookingFrame extends JFrame {

    JLabel resourceLabel;
    JLabel rentLabel;
    JLabel totalLabel;

    JTextField quantityField;
    JTextField startDateField;
    JTextField endDateField;

    JButton calculateButton;
    JButton confirmButton;
    JButton backButton;

    String resourceName;
    double rentPerDay;
    int resourceId;
    int availableQuantity;

    int rentalDays = 1;
    double totalAmount = 0;

    public BookingFrame(int resourceId, String resourceName, double rentPerDay, int availableQuantity) {

        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.rentPerDay = rentPerDay;
        this.availableQuantity = availableQuantity;

        setTitle("Resource Booking - Smart Community");
        setSize(600, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Resource Booking");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBounds(190, 30, 250, 40);
        add(titleLabel);

        JLabel resourceText = new JLabel("Resource:");
        resourceText.setBounds(80, 110, 120, 30);
        add(resourceText);

        resourceLabel = new JLabel(resourceName);
        resourceLabel.setBounds(220, 110, 250, 30);
        add(resourceLabel);

        JLabel rentText = new JLabel("Rent Per Day:");
        rentText.setBounds(80, 160, 120, 30);
        add(rentText);

        rentLabel = new JLabel("₹" + rentPerDay);
        rentLabel.setBounds(220, 160, 250, 30);
        add(rentLabel);

        JLabel availableText = new JLabel("Available:");
        availableText.setBounds(80, 190, 120, 30);
        add(availableText);

        JLabel availableLabel = new JLabel(String.valueOf(availableQuantity));
        availableLabel.setBounds(220, 190, 250, 30);
        add(availableLabel);

        JLabel quantityLabel = new JLabel("Quantity:");
        quantityLabel.setBounds(80, 230, 120, 30);
        add(quantityLabel);

        quantityField = new JTextField("1");
        quantityField.setBounds(220, 230, 200, 30);
        add(quantityField);

        JLabel startLabel = new JLabel("Start Date:");
        startLabel.setBounds(80, 280, 120, 30);
        add(startLabel);

        startDateField = new JTextField("2026-09-20");
        startDateField.setBounds(220, 280, 200, 30);
        add(startDateField);

        JLabel endLabel = new JLabel("End Date:");
        endLabel.setBounds(80, 330, 120, 30);
        add(endLabel);

        endDateField = new JTextField("2026-09-22");
        endDateField.setBounds(220, 330, 200, 30);
        add(endDateField);

        calculateButton = new JButton("Calculate Amount");
        calculateButton.setBounds(180, 380, 220, 40);
        add(calculateButton);

        totalLabel = new JLabel("Total Amount: ₹0");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setBounds(190, 430, 250, 30);
        add(totalLabel);

        confirmButton = new JButton("Confirm Booking");
        confirmButton.setBounds(180, 470, 220, 40);
        add(confirmButton);

        backButton = new JButton("Back to Resources");
        backButton.setBounds(180, 520, 220, 35);
        add(backButton);

        calculateButton.addActionListener(e -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity <= 0 || quantity > availableQuantity) {
                    JOptionPane.showMessageDialog(BookingFrame.this, "Invalid quantity! Max available: " + availableQuantity);
                    return;
                }

                String daysStr = JOptionPane.showInputDialog(BookingFrame.this, "Enter number of rental days:", "2");
                if (daysStr != null) {
                    rentalDays = Integer.parseInt(daysStr.trim());
                    if (rentalDays <= 0) rentalDays = 1;
                    totalAmount = quantity * rentPerDay * rentalDays;
                    totalLabel.setText("Total Amount: ₹" + totalAmount);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(BookingFrame.this, "Please enter valid numeric values!");
            }
        });

        confirmButton.addActionListener(e -> {
            if (Session.userId == 0) {
                Session.userId = 2; // Default fallback user for testing
                Session.userName = "Rahul Sharma";
            }

            String startDate = startDateField.getText().trim();
            String endDate = endDateField.getText().trim();

            if (startDate.isEmpty() || endDate.isEmpty()) {
                JOptionPane.showMessageDialog(BookingFrame.this, "Please enter start and end dates!");
                return;
            }

            int quantity = 1;
            try {
                quantity = Integer.parseInt(quantityField.getText().trim());
            } catch (Exception ignored) {}

            if (totalAmount <= 0) {
                totalAmount = rentPerDay * quantity;
            }

            String insertSql = "INSERT INTO bookings (user_id, user_name, resource_id, resource_name, start_date, end_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";
            String updateSql = "UPDATE resources SET available = available - ? WHERE resource_id = ? AND available >= ?";

            try (Connection con = DBConnection.getConnection()) {
                if (con != null) {
                    con.setAutoCommit(false);
                    try (PreparedStatement psInsert = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                         PreparedStatement psUpdate = con.prepareStatement(updateSql)) {

                        psInsert.setInt(1, Session.userId);
                        psInsert.setString(2, Session.userName != null ? Session.userName : "User");
                        psInsert.setInt(3, resourceId);
                        psInsert.setString(4, resourceName);
                        psInsert.setString(5, startDate);
                        psInsert.setString(6, endDate);
                        psInsert.setDouble(7, totalAmount);
                        psInsert.executeUpdate();

                        psUpdate.setInt(1, quantity);
                        psUpdate.setInt(2, resourceId);
                        psUpdate.setInt(3, quantity);
                        psUpdate.executeUpdate();

                        con.commit();

                        JOptionPane.showMessageDialog(BookingFrame.this, "Booking Confirmed Successfully!\nStatus: PENDING ADMIN APPROVAL\nTotal Price: ₹" + totalAmount);
                        new MyBookingsFrame();
                        dispose();
                    } catch (Exception ex) {
                        con.rollback();
                        JOptionPane.showMessageDialog(BookingFrame.this, "Booking Error: " + ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                System.out.println("DB Booking Connection Error: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> {
            new ResourceFrame();
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new BookingFrame(1, "Cricket Kit", 200, 5);
    }
}