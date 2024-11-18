import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

public class LibraryApp extends JFrame {
    public LibraryApp() {
        setTitle("Library Management System");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Books", new BookManagementPanel());
        add(tabbedPane);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LibraryApp().setVisible(true);
        });
    }
}