import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ViewFeedbackFrame extends JFrame {

    JTable feedbackTable;
    DefaultTableModel model;

    JButton deleteButton;
    JButton refreshButton;
    JButton backButton;

    public ViewFeedbackFrame() {

        setTitle("View Feedback - Smart Community Resource Rental System");
        setSize(900, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("User Feedback & Rating");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(280, 20, 350, 40);
        add(titleLabel);

        String[] columns = {"ID", "User", "Resource", "Rating", "Comment", "Date"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        feedbackTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(feedbackTable);
        scrollPane.setBounds(40, 90, 800, 280);
        add(scrollPane);

        deleteButton = new JButton("Delete Feedback");
        deleteButton.setBounds(180, 410, 180, 45);
        add(deleteButton);

        refreshButton = new JButton("Refresh");
        refreshButton.setBounds(380, 410, 140, 45);
        add(refreshButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(540, 410, 190, 45);
        add(backButton);

        // Load Feedback
        loadFeedbackFromDatabase();

        deleteButton.addActionListener(e -> {
            int selectedRow = feedbackTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select feedback to delete!");
                return;
            }

            int id = Integer.parseInt(model.getValueAt(selectedRow, 0).toString());
            int choice = JOptionPane.showConfirmDialog(this, "Delete feedback ID " + id + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM feedback WHERE feedback_id = ?";
                try (Connection con = DBConnection.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Feedback Deleted Successfully!");
                    loadFeedbackFromDatabase();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error deleting feedback: " + ex.getMessage());
                }
            }
        });

        refreshButton.addActionListener(e -> loadFeedbackFromDatabase());

        backButton.addActionListener(e -> {
            new AdminDashboard();
            dispose();
        });

        setVisible(true);
    }

    private void loadFeedbackFromDatabase() {
        model.setRowCount(0);
        String sql = "SELECT feedback_id, user_name, resource_name, rating, comments, created_at FROM feedback ORDER BY feedback_id DESC";
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getInt("feedback_id"),
                                rs.getString("user_name"),
                                rs.getString("resource_name"),
                                rs.getInt("rating") + " ★",
                                rs.getString("comments"),
                                rs.getString("created_at")
                        });
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error loading feedback: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new ViewFeedbackFrame();
    }
}