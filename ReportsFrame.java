import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class ReportsFrame extends JFrame {

    private JTable reportTable;
    private DefaultTableModel model;

    private JButton bookingReportButton;
    private JButton paymentReportButton;
    private JButton resourceReportButton;
    private JButton refreshButton;
    private JButton backButton;

    public ReportsFrame() {
        setTitle("System Reports & Analytics - Smart Community Resource Rental System");
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color bgColor = new Color(248, 250, 252);
        Color headerBg = new Color(15, 23, 42);
        Color textDark = new Color(15, 23, 42);

        getContentPane().setBackground(bgColor);

        // ================= HEADER =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(headerBg);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("System Analytics & Reports");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Real-time database metrics & audit summaries");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(new Color(148, 163, 184));

        JPanel headerText = new JPanel(new GridLayout(2, 1, 0, 4));
        headerText.setOpaque(false);
        headerText.add(titleLabel);
        headerText.add(subLabel);

        headerPanel.add(headerText, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // ================= CENTER CONTENT =================
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        String[] columns = {"Metric / Report Category", "Live Total / Count", "Status / Remarks"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reportTable = new JTable(model);
        reportTable.setRowHeight(32);
        reportTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        reportTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        reportTable.getTableHeader().setBackground(new Color(241, 245, 249));
        reportTable.getTableHeader().setForeground(textDark);

        // Center align value column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        reportTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // ================= BOTTOM BUTTON BAR =================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setOpaque(false);

        bookingReportButton = createStyledButton("Booking Summary", new Color(37, 99, 235), Color.WHITE);
        paymentReportButton = createStyledButton("Revenue Summary", new Color(16, 185, 129), Color.WHITE);
        resourceReportButton = createStyledButton("Inventory Summary", new Color(168, 85, 247), Color.WHITE);
        refreshButton = createStyledButton("Refresh Live Data", new Color(100, 116, 139), Color.WHITE);
        backButton = createStyledButton("Back to Dashboard", new Color(239, 68, 68), Color.WHITE);

        bookingReportButton.addActionListener(e -> showBookingSummary());
        paymentReportButton.addActionListener(e -> showPaymentSummary());
        resourceReportButton.addActionListener(e -> showResourceSummary());
        refreshButton.addActionListener(e -> loadReportData());
        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        buttonPanel.add(bookingReportButton);
        buttonPanel.add(paymentReportButton);
        buttonPanel.add(resourceReportButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        centerPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Load live DB stats into table
        loadReportData();

        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return btn;
    }

    private void loadReportData() {
        model.setRowCount(0);

        int totalUsers = getCount("SELECT COUNT(*) FROM users");
        int totalResources = getCount("SELECT COUNT(*) FROM resources");
        int totalBookings = getCount("SELECT COUNT(*) FROM bookings");
        int totalPayments = getCount("SELECT COUNT(*) FROM payments");
        double totalRevenue = getDouble("SELECT IFNULL(SUM(amount), 0) FROM payments");
        int totalReturns = getCount("SELECT COUNT(*) FROM returns");
        int pendingFines = getCount("SELECT COUNT(*) FROM fines WHERE status = 'PENDING'");

        model.addRow(new Object[]{"Total Registered Users", totalUsers, "Active System Members"});
        model.addRow(new Object[]{"Total Resources Inventory", totalResources, "Community Assets"});
        model.addRow(new Object[]{"Total Rental Bookings", totalBookings, "Reservations Logged"});
        model.addRow(new Object[]{"Successful Payments", totalPayments, "Transactions Processed"});
        model.addRow(new Object[]{"Total System Revenue", "₹" + String.format("%.2f", totalRevenue), "Collected Fees"});
        model.addRow(new Object[]{"Total Resource Returns", totalReturns, "Returned to Stock"});
        model.addRow(new Object[]{"Pending Fines", pendingFines, pendingFines > 0 ? "Action Required" : "All Clear"});
    }

    private int getCount(String query) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            System.out.println("Query Error: " + e.getMessage());
        }
        return 0;
    }

    private double getDouble(String query) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {
            System.out.println("Query Error: " + e.getMessage());
        }
        return 0.0;
    }

    private void showBookingSummary() {
        int total = getCount("SELECT COUNT(*) FROM bookings");
        int approved = getCount("SELECT COUNT(*) FROM bookings WHERE status = 'APPROVED'");
        int pending = getCount("SELECT COUNT(*) FROM bookings WHERE status = 'PENDING'");
        int rejected = getCount("SELECT COUNT(*) FROM bookings WHERE status = 'REJECTED'");

        JOptionPane.showMessageDialog(this,
                "📊 Booking Summary Report\n\n" +
                "• Total Reservations: " + total + "\n" +
                "• Approved: " + approved + "\n" +
                "• Pending Review: " + pending + "\n" +
                "• Rejected: " + rejected,
                "Booking Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showPaymentSummary() {
        int total = getCount("SELECT COUNT(*) FROM payments");
        double revenue = getDouble("SELECT IFNULL(SUM(amount), 0) FROM payments");

        JOptionPane.showMessageDialog(this,
                "💰 Revenue & Payment Report\n\n" +
                "• Total Payment Records: " + total + "\n" +
                "• Total Collected Revenue: ₹" + String.format("%.2f", revenue),
                "Payment Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showResourceSummary() {
        int total = getCount("SELECT COUNT(*) FROM resources");
        int categories = getCount("SELECT COUNT(*) FROM categories");

        JOptionPane.showMessageDialog(this,
                "📦 Inventory Summary Report\n\n" +
                "• Total Resource Items: " + total + "\n" +
                "• Active Categories: " + categories,
                "Inventory Statistics", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReportsFrame());
    }
}