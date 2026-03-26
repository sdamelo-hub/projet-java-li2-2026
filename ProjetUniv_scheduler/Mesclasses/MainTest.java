package ProjetUniv_scheduler.Mesclasses;

import javax.swing.SwingUtilities;

public class MainTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}