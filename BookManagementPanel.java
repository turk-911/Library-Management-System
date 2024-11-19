import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BookManagementPanel extends JPanel {
    private JTable bookTable;
    private JTextField titleField, authorField, isbnField;
    private JButton addButton, editButton, deleteButton, checkAvailabilityButton;
    public BookManagementPanel() {
        setLayout(new BorderLayout());
        bookTable = new JTable();
        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Title: "));
        titleField = new JTextField();
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Author: "));
        authorField = new JTextField();
        inputPanel.add(authorField);
        inputPanel.add(new JLabel("ISBN: "));
        isbnField = new JTextField();
        inputPanel.add(isbnField);
        add(inputPanel, BorderLayout.NORTH);
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        checkAvailabilityButton = new JButton("Check Availability");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(checkAvailabilityButton);
        add(buttonPanel, BorderLayout.SOUTH);
        addButton.addActionListener(e -> addBook());
        editButton.addActionListener(e -> editBook());
        deleteButton.addActionListener(e -> deleteButton());
    }
    private void addBook() {
        String title = titleField.getText();
        String author = authorField.getText();
        String isbn = isbnField.getText();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password"); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO books (isbn, title, author) VALUES (?, ?, ?)")) {
            pstmt.setString(1, isbn);
            pstmt.setString(2, title);
            pstmt.setString(3, author);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book adding successfully");
            loadBooks();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding book. ");
        }
    }
    private void editBook() {
        int selectedRow = bookTable.getSelectedRow();
        if(selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit");
            return;
        }
        String isbn = bookTable.getValueAt(selectedRow, 2).toString();
        String title = titleField.getText();
        String author = authorField.getText();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password"); PreparedStatement pstmt = conn.prepareStatement("UPDATE books SET title = ?, author = ?, WHERE isbn = ?")) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, isbn);
            int rowsUpdated = pstmt.executeUpdate();
            if(rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Book details updated");
                loadBooks();
            } else JOptionPane.showMessageDialog(this, "Error updating Books");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
    private void deleteButton() {
        int selectedRow = bookTable.getSelectedRow();
        if(selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete");
            return;
        }
        String isbn = bookTable.getValueAt(selectedRow, 2).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this book?");
        if(confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password"); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM books WHERE isbn = ?")) {
            pstmt.setString(1, isbn);
            int rowsDeleted = pstmt.executeUpdate();
            if(rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Book deleted successfully");
                loadBooks();
            } else JOptionPane.showMessageDialog(this, "Error deleting book");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
    private void loadBooks() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password"); PreparedStatement pstmt = conn.prepareStatement("SELECT title, author, isbn, availability FROM books"); ResultSet rs = pstmt.executeQuery()) {
            String[] columnNames = {"Title", "Author", "ISBN", "Availability"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
            while(rs.next()) {
                String title = rs.getString("title");
                String author = rs.getString("author");
                String isbn = rs.getString("isbn");
                boolean availability = rs.getBoolean("availability");
                tableModel.addRow(new Object[]{title, author, isbn, availability ? "Available" : "Checked out"});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
}