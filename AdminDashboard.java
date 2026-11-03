import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AdminDashboard extends JFrame {

    private JLabel totalUsersVal;
    private JLabel totalResourcesVal;
    private JLabel totalBookingsVal;
    private JLabel totalRevenueVal;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Smart Community Resource Rental System");
        setSize(950, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color bgColor = new Color(248, 250, 252);
        Color headerBg = new Color(15, 23, 42);
        Color primaryBlue = new Color(37, 99, 235);
        Color textDark = new Color(15, 23, 42);

        getContentPane().setBackground(bgColor);

        // ================= HEADER PANEL =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(headerBg);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("Community Admin Control Center");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("System Management & Analytics Portal");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(new Color(148, 163, 184));

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(titleLabel);
        headerTextPanel.add(welcomeLabel);

        JButton logoutBtn = createStyledButton("Logout", new Color(239, 68, 68), Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(100, 38));
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    AdminDashboard.this,
                    "Are you sure you want to logout from Admin panel?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                new LoginFrame();
                dispose();
            }
        });

        headerPanel.add(headerTextPanel, BorderLayout.WEST);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ================= MAIN CONTENT PANEL =================
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(20, 30, 20, 30));

        // ----- STATS CARDS PANEL -----
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(890, 95));

        totalUsersVal = new JLabel("0", SwingConstants.CENTER);
        totalResourcesVal = new JLabel("0", SwingConstants.CENTER);
        totalBookingsVal = new JLabel("0", SwingConstants.CENTER);
        totalRevenueVal = new JLabel("₹0", SwingConstants.CENTER);

        statsPanel.add(createStatCard("Total Users", totalUsersVal, new Color(59, 130, 246)));
        statsPanel.add(createStatCard("Total Resources", totalResourcesVal, new Color(16, 185, 129)));
        statsPanel.add(createStatCard("Total Bookings", totalBookingsVal, new Color(168, 85, 247)));
        statsPanel.add(createStatCard("Total Revenue", totalRevenueVal, new Color(245, 158, 11)));

        mainContent.add(statsPanel);
        mainContent.add(Box.createRigidArea(new Dimension(0, 25)));

        // ----- MANAGEMENT ACTIONS TITLE -----
        JLabel actionsTitle = new JLabel("System Modules & Operations");
        actionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        actionsTitle.setForeground(textDark);
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(actionsTitle);

        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));

        // ----- BUTTON GRID PANEL -----
        JPanel gridPanel = new JPanel(new GridLayout(3, 3, 18, 18));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(890, 330));

        JButton usersBtn = createAdminCard("Manage Users", "View, edit & approve members", new Color(37, 99, 235));
        JButton categoryBtn = createAdminCard("Manage Categories", "Add or edit resource types", new Color(16, 185, 129));
        JButton resourceBtn = createAdminCard("Manage Resources", "Control inventory & rates", new Color(168, 85, 247));
        JButton bookingBtn = createAdminCard("Manage Bookings", "Approve or cancel rentals", new Color(14, 165, 233));
        JButton paymentBtn = createAdminCard("Manage Payments", "Audit transactions & receipts", new Color(245, 158, 11));
        JButton returnBtn = createAdminCard("Manage Returns", "Process returned items", new Color(236, 72, 153));
        JButton fineBtn = createAdminCard("Manage Fines", "Calculate & collect late fees", new Color(239, 68, 68));
        JButton feedbackBtn = createAdminCard("View Feedback", "Read user ratings & reviews", new Color(99, 102, 241));
        JButton reportsBtn = createAdminCard("Reports & Analytics", "View detailed system stats", new Color(15, 23, 42));

        usersBtn.addActionListener(e -> { new ManageUserFrame(); dispose(); });
        categoryBtn.addActionListener(e -> { new ManageCategoryFrame(); dispose(); });
        resourceBtn.addActionListener(e -> { new ManageResourceFrame(); dispose(); });
        bookingBtn.addActionListener(e -> { new ManageBookingFrame(); dispose(); });
        paymentBtn.addActionListener(e -> { new ManagePaymentFrame(); dispose(); });
        returnBtn.addActionListener(e -> { new ManageReturnFrame(); dispose(); });
        fineBtn.addActionListener(e -> { new ManageFineFrame(); dispose(); });
        feedbackBtn.addActionListener(e -> { new ViewFeedbackFrame(); dispose(); });
        reportsBtn.addActionListener(e -> { new ReportsFrame(); dispose(); });

        gridPanel.add(usersBtn);
        gridPanel.add(categoryBtn);
        gridPanel.add(resourceBtn);
        gridPanel.add(bookingBtn);
        gridPanel.add(paymentBtn);
        gridPanel.add(returnBtn);
        gridPanel.add(fineBtn);
        gridPanel.add(feedbackBtn);
        gridPanel.add(reportsBtn);

        mainContent.add(gridPanel);

        add(mainContent, BorderLayout.CENTER);

        // Fetch metrics from live database
        fetchLiveStats();

        setVisible(true);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 10, 12, 10)
        ));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JButton createAdminCard(String title, String description, Color accentColor) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(accentColor);

        JLabel descLbl = new JLabel(description);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        descLbl.setForeground(new Color(148, 163, 184));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        textPanel.setOpaque(false);
        textPanel.add(titleLbl);
        textPanel.add(descLbl);

        btn.add(textPanel, BorderLayout.CENTER);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(241, 245, 249));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return btn;
    }

    private void fetchLiveStats() {
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                // Total Users
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM users");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalUsersVal.setText(String.valueOf(rs.getInt(1)));
                } catch (Exception ignored) {}

                // Total Resources
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM resources");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalResourcesVal.setText(String.valueOf(rs.getInt(1)));
                } catch (Exception ignored) {}

                // Total Bookings
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bookings");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalBookingsVal.setText(String.valueOf(rs.getInt(1)));
                } catch (Exception ignored) {}

                // Total Revenue
                try (PreparedStatement ps = con.prepareStatement("SELECT IFNULL(SUM(amount), 0) FROM payments");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) totalRevenueVal.setText("₹" + String.format("%.0f", rs.getDouble(1)));
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            System.out.println("Live Stats Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard());
    }
}