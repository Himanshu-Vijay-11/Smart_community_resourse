import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.*;

public class ReturnFrame extends JFrame {

    JTextField bookingIdField;
    JTextField dueDateField;
    JTextField returnDateField;
    JTextField finePerDayField;

    JButton calculateButton;
    JButton returnButton;
    JButton backButton;

    public ReturnFrame() {

        setTitle("Return Resource - Smart Community Resource Rental System");
        setSize(600, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Return Resource");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(190, 30, 250, 40);
        add(titleLabel);

        JLabel bookingIdLabel = new JLabel("Booking ID:");
        bookingIdLabel.setBounds(80, 110, 150, 30);
        add(bookingIdLabel);

        bookingIdField = new JTextField();
        bookingIdField.setBounds(250, 110, 220, 30);
        add(bookingIdField);

        JLabel dueDateLabel = new JLabel("Due Date (YYYY-MM-DD):");
        dueDateLabel.setBounds(80, 160, 170, 30);
        add(dueDateLabel);

        dueDateField = new JTextField(LocalDate.now().minusDays(1).toString());
        dueDateField.setBounds(250, 160, 220, 30);
        add(dueDateField);

        JLabel returnDateLabel = new JLabel("Return Date (YYYY-MM-DD):");
        returnDateLabel.setBounds(80, 210, 170, 30);
        add(returnDateLabel);

        returnDateField = new JTextField(LocalDate.now().toString());
        returnDateField.setBounds(250, 210, 220, 30);
        add(returnDateField);

        JLabel fineLabel = new JLabel("Fine Per Day (₹):");
        fineLabel.setBounds(80, 260, 150, 30);
        add(fineLabel);

        finePerDayField = new JTextField("50");
        finePerDayField.setBounds(250, 260, 220, 30);
        add(finePerDayField);

        calculateButton = new JButton("Calculate Fine");
        calculateButton.setBounds(170, 320, 250, 40);
        add(calculateButton);

        returnButton = new JButton("Confirm Return");
        returnButton.setBounds(170, 380, 250, 40);
        add(returnButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(170, 440, 250, 40);
        add(backButton);

        calculateButton.addActionListener(e -> {
            try {
                LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());
                LocalDate returnDate = LocalDate.parse(returnDateField.getText().trim());
                double finePerDay = Double.parseDouble(finePerDayField.getText().trim());

                long lateDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                if (lateDays < 0) lateDays = 0;
                double fine = lateDays * finePerDay;

                JOptionPane.showMessageDialog(ReturnFrame.this,
                        "Late Days: " + lateDays + "\nFine Per Day: ₹" + finePerDay + "\nTotal Fine: ₹" + fine,
                        "Fine Calculation", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ReturnFrame.this, "Please enter valid date and fine values!\nFormat: YYYY-MM-DD");
            }
        });

        returnButton.addActionListener(e -> {
            String bIdText = bookingIdField.getText().trim();
            if (bIdText.isEmpty()) {
                JOptionPane.showMessageDialog(ReturnFrame.this, "Please enter Booking ID!");
                return;
            }

            try {
                int bookingId = Integer.parseInt(bIdText);
                int userId = Session.userId > 0 ? Session.userId : 2;
                String rDate = returnDateField.getText().trim();

                String sqlReturn = "INSERT INTO returns (booking_id, user_id, resource_id, return_date, condition_status, status) VALUES (?, ?, 1, ?, 'Good', 'COMPLETED')";
                String sqlBooking = "UPDATE bookings SET status = 'RETURNED' WHERE booking_id = ?";

                try (Connection con = DBConnection.getConnection()) {
                    if (con != null) {
                        try (PreparedStatement ps1 = con.prepareStatement(sqlReturn);
                             PreparedStatement ps2 = con.prepareStatement(sqlBooking)) {

                            ps1.setInt(1, bookingId);
                            ps1.setInt(2, userId);
                            ps1.setString(3, rDate);
                            ps1.executeUpdate();

                            ps2.setInt(1, bookingId);
                            ps2.executeUpdate();

                            JOptionPane.showMessageDialog(ReturnFrame.this, "Resource Return Submitted Successfully!\nBooking ID: " + bookingId);
                            new UserDashboard();
                            dispose();
                        }
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ReturnFrame.this, "Error confirming return: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> {
            new UserDashboard();
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new ReturnFrame();
    }
}