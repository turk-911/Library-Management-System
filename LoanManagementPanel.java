import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
public class LoanManagementPanel extends JPanel {
    private JTable loanTable;
    private JButton issueBookButton;
    private JButton returnBookButton;
    private DefaultTableModel tableModel;
    private Connection conn;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Dhriti@2604";
    public LoanManagementPanel() {
        setLayout(new BorderLayout());
        JLabel titleLabel = new JLabel("Loan Management");
        add(titleLabel, BorderLayout.NORTH);
        tableModel = new DefaultTableModel(new Object[] { "Book ID", "Patron ID", "Issue Date", "Return Date" }, 0);
        loanTable = new JTable(tableModel);
        add(new JScrollPane(loanTable), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        issueBookButton = new JButton("Issue Book");
        returnBookButton = new JButton("Return Book");
        buttonPanel.add(issueBookButton);
        buttonPanel.add(returnBookButton);
        add(buttonPanel, BorderLayout.SOUTH);
        loadLoanData();
        issueBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                issueBook();
            }
        });
        returnBookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnBook();
            }
        });
    }
    private void loadLoanData() {
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT * FROM loans";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0);
            while (rs.next()) {
                String bookId = rs.getString("book_id");
                String patronId = rs.getString("patron_id");
                String issueDate = rs.getString("issue_date");
                String returnDate = rs.getString("return_date");
                tableModel.addRow(new Object[] { bookId, patronId, issueDate, returnDate });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading loan data.", "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void issueBook() {
        JTextField bookIdField = new JTextField(15);
        JTextField patronIdField = new JTextField(15);
        JPanel panel = new JPanel();
        panel.add(new JLabel("Book ID:"));
        panel.add(bookIdField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Patron ID:"));
        panel.add(patronIdField);
        int option = JOptionPane.showConfirmDialog(this, panel, "Issue Book", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String bookId = bookIdField.getText();
            String patronId = patronIdField.getText();
            if (bookId.isEmpty() || patronId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both fields must be filled.", "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String issueDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                String query = "INSERT INTO loans (book_id, patron_id, issue_date) VALUES (?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, bookId);
                stmt.setString(2, patronId);
                stmt.setString(3, issueDate);
                stmt.executeUpdate();
                loadLoanData();
                JOptionPane.showMessageDialog(this, "Book issued successfully!", "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error issuing book.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void returnBook() {
        int selectedRow = loanTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan to return.", "Selection Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String bookId = (String) loanTable.getValueAt(selectedRow, 0);
        String patronId = (String) loanTable.getValueAt(selectedRow, 1);
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to return the book?", "Return Book",
                JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try {
                String returnDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                String query = "UPDATE loans SET return_date = ? WHERE book_id = ? AND patron_id = ? AND return_date IS NULL";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, returnDate);
                stmt.setString(2, bookId);
                stmt.setString(3, patronId);
                int rowsUpdated = stmt.executeUpdate();

                if (rowsUpdated > 0) {
                    loadLoanData();
                    JOptionPane.showMessageDialog(this, "Book returned successfully!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No loan found for the selected book and patron.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error returning book.", "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}