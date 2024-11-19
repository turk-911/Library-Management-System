import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PatronManagementPanel extends JPanel {
    private JTable patronTable;
    private DefaultTableModel tableModel;
    private JTextField txtPatronId, txtName;

    public PatronManagementPanel() {
        setSize(800, 600);
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(new String[] { "Patron ID", "Name", "Borrowed Books" }, 0);
        patronTable = new JTable(tableModel);
        add(new JScrollPane(patronTable), BorderLayout.CENTER);
        JPanel actionPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        actionPanel.add(new JLabel("Patron ID: "));
        txtPatronId = new JTextField();
        actionPanel.add(txtPatronId);
        actionPanel.add(new JLabel("Name: "));
        txtName = new JTextField();
        actionPanel.add(txtName);
        JButton btnAdd = new JButton("Add Patron");
        JButton btnEdit = new JButton("Edit Patron");
        JButton btnDelete = new JButton("Delete Patron");
        JButton btnLoad = new JButton("Load Patrons");
        actionPanel.add(btnAdd);
        actionPanel.add(btnEdit);
        actionPanel.add(btnDelete);
        actionPanel.add(btnLoad);
        add(actionPanel, BorderLayout.SOUTH);
        btnAdd.addActionListener(e -> addPatron());
        btnEdit.addActionListener(e -> editPatron());
        btnDelete.addActionListener(e -> deletePatron());
        btnLoad.addActionListener(e -> loadPatrons());
        loadPatrons();
    }

    private void loadPatrons() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password");
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM patrons");
                ResultSet rs = pstmt.executeQuery()) {
            tableModel.setRowCount(0);
            while(rs.next()) {
                String patronId = rs.getString("patron_id");
                String name = rs.getString("name");
                String borrowedBooks = rs.getString("borrowed_books");
                borrowedBooks = (borrowedBooks != null && !borrowedBooks.isEmpty()) ? borrowedBooks : "None";
                tableModel.addRow(new Object[]{patronId, name, borrowedBooks});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load patrons" + ex.getMessage());
        }
    }

    private void addPatron() {
        String patronId = txtPatronId.getText().trim();
        String name = txtName.getText();
        if(patronId.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all the details");
            return;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password"); PreparedStatement pstmt = conn.prepareStatement("INSERT INTO patrons (patron_id, name, borrowed_books) VALUES (?, ?, ?)")) {
            pstmt.setString(1, patronId);
            pstmt.setString(2, name);
            pstmt.setString(3, "");
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patron added");
            loadPatrons();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding patron" + ex.getMessage());
        }
    }
    private void editPatron() {
        int selectedRow = patronTable.getSelectedRow();
        if(selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select atleast one patron to edit");
            return;
        }
        String patronId = (String) tableModel.getValueAt(selectedRow, 0);
        String name = txtName.getText().trim();
        if(name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty");
            return;
        }
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password"); PreparedStatement pstmt = conn.prepareStatement("UPDATE patrons SET name = ? WHERE patron_id = ?")) {
            pstmt.setString(1, name);
            pstmt.setString(2, patronId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patron updated successfully");
            loadPatrons();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to edit patron");
        }
    }
    private void deletePatron() {
        int selectedRow = patronTable.getSelectedRow();
        if(selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a patron to delete");
            return;
        }
        String patronId = (String) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/library", "root", "password"); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM patrons WHERE patron_id = ?")) {
            pstmt.setString(1, patronId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Patron deleted successfully");
            loadPatrons();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to delete patron" + ex.getMessage());
        }
    }
}