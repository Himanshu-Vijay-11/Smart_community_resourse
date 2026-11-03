import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserDashboard extends JFrame {

    private JLabel availableResourcesVal;
    private JLabel myBookingsVal;
    private JLabel myPaymentsVal;
    private JLabel myFeedbackVal;

    public UserDashboard() {
        setTitle("User Dashboard - Smart Community Resource Rental & Booking System");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Color bgColor = new Color(248, 250, 252);
        Color headerBg = new Color(30, 41, 59);
        Color primaryBlue = new Color(37, 99, 235);
        Color cardBg = Color.WHITE;
        Color textDark = new Color(15, 23, 42);
        Color textMuted = new Color(100, 116, 139);

        getContentPane().setBackground(bgColor);

        // ================= HEADER PANEL =================
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(headerBg);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("Smart Community Portal");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        String displayUser = (Session.userName != null && !Session.userName.isEmpty()) ? Session.userName : "User";
        JLabel welcomeLabel = new JLabel("Welcome back, " + displayUser + " 👋");
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        welcomeLabel.setForeground(new Color(203, 213, 225));

        JPanel headerTextPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(titleLabel);
        headerTextPanel.add(welcomeLabel);

        JButton logoutBtn = createStyledButton("Logout", new Color(239, 68, 68), Color.WHITE);
        logoutBtn.setPreferredSize(new Dimension(100, 38));
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    UserDashboard.this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                Session.clear();
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
        mainContent.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ----- STATS CARDS PANEL -----
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(800, 100));

        availableResourcesVal = new JLabel("0", SwingConstants.CENTER);
        myBookingsVal = new JLabel("0", SwingConstants.CENTER);
        myPaymentsVal = new JLabel("0", SwingConstants.CENTER);
        myFeedbackVal = new JLabel("0", SwingConstants.CENTER);

        statsPanel.add(createStatCard("Available Items", availableResourcesVal, new Color(59, 130, 246)));
        statsPanel.add(createStatCard("My Bookings", myBookingsVal, new Color(16, 185, 129)));
        statsPanel.add(createStatCard("My Payments", myPaymentsVal, new Color(168, 85, 247)));
        statsPanel.add(createStatCard("My Feedbacks", myFeedbackVal, new Color(245, 158, 11)));

        mainContent.add(statsPanel);
        mainContent.add(Box.createRigidArea(new Dimension(0, 25)));

        // ----- QUICK ACTIONS TITLE -----
        JLabel actionsTitle = new JLabel("Quick Actions & Management");
        actionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        actionsTitle.setForeground(textDark);
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContent.add(actionsTitle);

        mainContent.add(Box.createRigidArea(new Dimension(0, 15)));

        // ----- BUTTON GRID PANEL -----
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setMaximumSize(new Dimension(800, 240));

        JButton resourcesBtn = createActionButton("Browse Resources", "Explore available community items", primaryBlue);
        JButton bookingsBtn = createActionButton("My Bookings", "Track your active & past bookings", new Color(16, 185, 129));
        JButton paymentBtn = createActionButton("Payments", "Pay rental fees & check receipts", new Color(168, 85, 247));
        JButton returnBtn = createActionButton("Return Resource", "Initiate item return process", new Color(14, 165, 233));
        JButton feedbackBtn = createActionButton("Feedback & Rating", "Share your rental experience", new Color(245, 158, 11));
        JButton refreshBtn = createActionButton("Refresh Data", "Sync metrics from database", new Color(100, 116, 139));

        resourcesBtn.addActionListener(e -> { new ResourceFrame(); dispose(); });
        bookingsBtn.addActionListener(e -> { new MyBookingsFrame(); dispose(); });
        paymentBtn.addActionListener(e -> { new PaymentFrame(); dispose(); });
        returnBtn.addActionListener(e -> { new ReturnFrame(); dispose(); });
        feedbackBtn.addActionListener(e -> { new FeedbackFrame(); dispose(); });
        refreshBtn.addActionListener(e -> fetchLiveStats());

        gridPanel.add(resourcesBtn);
        gridPanel.add(bookingsBtn);
        gridPanel.add(paymentBtn);
        gridPanel.add(returnBtn);
        gridPanel.add(feedbackBtn);
        gridPanel.add(refreshBtn);

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

    private JButton createActionButton(String text, String subtitle, Color color) {
        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(text);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(color);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subLabel.setForeground(new Color(148, 163, 184));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subLabel);

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
                // Available Resources count
                try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM resources");
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) availableResourcesVal.setText(String.valueOf(rs.getInt(1)));
                } catch (Exception ignored) {}

                // User Bookings count
                if (Session.userId > 0) {
                    try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM bookings WHERE user_id = ?")) {
                        ps.setInt(1, Session.userId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) myBookingsVal.setText(String.valueOf(rs.getInt(1)));
                        }
                    } catch (Exception ignored) {}

                    // User Payments count
                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT COUNT(*) FROM payments WHERE booking_id IN (SELECT booking_id FROM bookings WHERE user_id = ?)")) {
                        ps.setInt(1, Session.userId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) myPaymentsVal.setText(String.valueOf(rs.getInt(1)));
                        }
                    } catch (Exception ignored) {}

                    // User Feedback count
                    try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM feedback WHERE user_id = ?")) {
                        ps.setInt(1, Session.userId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) myFeedbackVal.setText(String.valueOf(rs.getInt(1)));
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.out.println("Live Stats Connection Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserDashboard());
    }
}
