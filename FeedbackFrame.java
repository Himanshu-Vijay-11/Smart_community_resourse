import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.*;

public class FeedbackFrame extends JFrame {

    JTextField resourceField;
    JComboBox<Integer> ratingBox;
    JTextArea commentArea;

    JButton submitButton;
    JButton backButton;

    public FeedbackFrame() {

        setTitle("Feedback & Rating - Smart Community Resource Rental System");
        setSize(600, 600);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel titleLabel = new JLabel("Feedback & Rating");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setBounds(170, 30, 300, 40);
        add(titleLabel);

        JLabel resourceLabel = new JLabel("Resource Name:");
        resourceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        resourceLabel.setBounds(70, 110, 150, 30);
        add(resourceLabel);

        resourceField = new JTextField();
        resourceField.setBounds(230, 110, 250, 30);
        add(resourceField);

        JLabel ratingLabel = new JLabel("Rating:");
        ratingLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        ratingLabel.setBounds(70, 160, 150, 30);
        add(ratingLabel);

        Integer[] ratings = {1, 2, 3, 4, 5};
        ratingBox = new JComboBox<>(ratings);
        ratingBox.setSelectedIndex(4);
        ratingBox.setBounds(230, 160, 250, 30);
        add(ratingBox);

        JLabel commentLabel = new JLabel("Comment:");
        commentLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        commentLabel.setBounds(70, 210, 150, 30);
        add(commentLabel);

        commentArea = new JTextArea();
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(commentArea);
        scrollPane.setBounds(230, 210, 250, 120);
        add(scrollPane);

        submitButton = new JButton("Submit Feedback");
        submitButton.setBounds(170, 370, 250, 45);
        add(submitButton);

        backButton = new JButton("Back to Dashboard");
        backButton.setBounds(170, 430, 250, 45);
        add(backButton);

        submitButton.addActionListener(e -> {
            String resource = resourceField.getText().trim();
            int rating = (Integer) ratingBox.getSelectedItem();
            String comment = commentArea.getText().trim();

            if (resource.isEmpty() || comment.isEmpty()) {
                JOptionPane.showMessageDialog(FeedbackFrame.this, "Please fill Resource Name and Comment!");
                return;
            }

            int userId = Session.userId > 0 ? Session.userId : 2;
            String userName = Session.userName != null ? Session.userName : "Rahul Sharma";

            String sql = "INSERT INTO feedback (user_id, user_name, resource_name, rating, comments) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ps.setString(2, userName);
                ps.setString(3, resource);
                ps.setInt(4, rating);
                ps.setString(5, comment);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(FeedbackFrame.this, "Feedback Submitted Successfully!\nThank you for your rating.");
                new UserDashboard();
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(FeedbackFrame.this, "Error submitting feedback: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> {
            new UserDashboard();
            dispose();
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new FeedbackFrame();
    }
}