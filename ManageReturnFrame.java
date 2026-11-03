import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ManageReturnFrame extends JFrame {

    JTable returnTable;
    DefaultTableModel model;

    JButton calculateButton;
    JButton confirmButton;
    JButton refreshButton;
    JButton backButton;

    JTextField bookingIdField;
    JTextField resourceField;
    JTextField dueDateField;
    JTextField returnDateField;
    JTextField finePerDayField;
    JTextField fineAmountField;

    public ManageReturnFrame() {

        setTitle("Manage Returns - Smart Community Resource Rental System");
        setSize(900, 650);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Manage Returns");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(330, 20, 250, 40);
        add(titleLabel);

        String[] columns = {"Return ID", "Booking ID", "User ID", "Return Date", "Condition", "Fine Amount (₹)"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        returnTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(returnTable);
        scrollPane.setBounds(40, 80, 800, 180);
        add(scrollPane);

        JLabel bookingLabel = new JLabel("Booking ID:");
        bookingLabel.setBounds(80, 290, 120, 30);
        add(bookingLabel);

        bookingIdField = new JTextField();
        bookingIdField.setBounds(200, 290, 200, 30);
        add(bookingIdField);

        JLabel resourceLabel = new JLabel("Resource:");
        resourceLabel.setBounds(470, 290, 100, 30);
        add(resourceLabel);

        resourceField = new JTextField();
        resourceField.setBounds(570, 290, 200, 30);
        add(resourceField);

        JLabel dueLabel = new JLabel("Due Date:");
        dueLabel.setBounds(80, 340, 120, 30);
        add(dueLabel);

        dueDateField = new JTextField("2026-09-15");
        dueDateField.setBounds(200, 340, 200, 30);
        add(dueDateField);

        JLabel returnLabel = new JLabel("Return Date:");
        returnLabel.setBounds(470, 340, 100, 30);
        add(returnLabel);

        returnDateField = new JTextField("2026-09-16");
        returnDateField.setBounds(570, 340, 200, 30);
        add(returnDateField);

        JLabel finePerDayLabel = new JLabel("Fine Per Day:");
        finePerDayLabel.setBounds(80, 390, 120, 30);
        add(finePerDayLabel);

        finePerDayField = new JTextField("50");
        finePerDayField.setBounds(200, 390, 200, 30);
        add(finePerDayField);

        JLabel fineAmountLabel = new JLabel("Fine Amount:");
        fineAmountLabel.setBounds(470, 390, 100, 30);
        add(fineAmountLabel);

        fineAmountField = new JTextField("0");
        fineAmountField.setBounds(570, 390, 200, 30);
        add(fineAmountField);

        calculateButton = new JButton("Calculate Fine");
        calculateButton.setBounds(80, 460, 160, 40);
        add(calculateButton);

        confirmButton = new JButton("Confirm Return");
        confirmButton.setBounds(260, 460, 160, 40);
        add(confirmButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(440, 460, 140, 40);
        add(refreshButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(600, 460, 190, 40);
        add(backButton);

        // Load returns
        loadReturnsFromDatabase();

        calculateButton.addActionListener(e -> {
            try {
                LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());
                LocalDate returnDate = LocalDate.parse(returnDateField.getText().trim());
                double finePerDay = Double.parseDouble(finePerDayField.getText().trim());

                long lateDays = ChronoUnit.DAYS.between(dueDate, returnDate);
                if (lateDays < 0) lateDays = 0;
                double fine = lateDays * finePerDay;
                fineAmountField.setText(String.valueOf(fine));

                JOptionPane.showMessageDialog(ManageReturnFrame.this, "Late Days: " + lateDays + "\nFine Amount: ₹" + fine);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ManageReturnFrame.this, "Invalid Date format! Use YYYY-MM-DD");
            }
        });

        confirmButton.addActionListener(e -> {
            String bIdText = bookingIdField.getText().trim();
            String fineText = fineAmountField.getText().trim();

            if (bIdText.isEmpty()) {
                JOptionPane.showMessageDialog(ManageReturnFrame.this, "Please enter Booking ID!");
                return;
            }

            try {
                int bookingId = Integer.parseInt(bIdText);
                double fine = Double.parseDouble(fineText);

                if (fine > 0) {
                    String sqlFine = "INSERT INTO fines (booking_id, user_id, user_name, fine_amount, reason, status) VALUES (?, 2, 'User', ?, 'Late Return', 'PENDING')";
                    try (Connection con = DBConnection.getConnection();
                         PreparedStatement ps = con.prepareStatement(sqlFine)) {
                        ps.setInt(1, bookingId);
                        ps.setDouble(2, fine);
                        ps.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(ManageReturnFrame.this, "Return Processed Successfully!");
                loadReturnsFromDatabase();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ManageReturnFrame.this, "Error processing return: " + ex.getMessage());
            }
        });

        refreshButton.addActionListener(e -> loadReturnsFromDatabase());

        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadReturnsFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT return_id, booking_id, user_id, return_date, condition_status, fine_amount FROM returns ORDER BY return_id DESC";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("return_id"),
                                rs.getInt("booking_id"),
                                rs.getInt("user_id"),
                                rs.getString("return_date"),
                                rs.getString("condition_status"),
                                rs.getDouble("fine_amount")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error loading returns: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ManageReturnFrame();
    }
}