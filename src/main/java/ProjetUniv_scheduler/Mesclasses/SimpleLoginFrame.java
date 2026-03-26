package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import static ProjetUniv_scheduler.Mesclasses.UnivSchedulerDAO.*;

/**
 * Écran de connexion simple.
 */
public class SimpleLoginFrame extends JFrame {

    private JTextField fieldId;
    private JPasswordField fieldMdp;
    private JLabel errorLabel;

    public SimpleLoginFrame() {
        setTitle("UNIV-SCHEDULER — Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.add(new JLabel("Identifiant:"));
        fieldId = new JTextField();
        panel.add(fieldId);
        
        panel.add(new JLabel("Mot de passe:"));
        fieldMdp = new JPasswordField();
        panel.add(fieldMdp);
        
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        
        JButton loginBtn = new JButton("Se connecter");
        loginBtn.addActionListener(e -> tenterConnexion());
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(errorLabel, BorderLayout.NORTH);
        southPanel.add(loginBtn, BorderLayout.CENTER);
        
        add(panel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void tenterConnexion() {
        String id  = fieldId.getText().trim();
        String mdp = new String(fieldMdp.getPassword()).trim();

        if (id.isEmpty() || mdp.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        // Vérification en base via Hibernate
        Utilisateur user = UnivSchedulerDAO.authentifier(id, mdp);

        if (user != null) {
            errorLabel.setText(" ");
            dispose();
            JOptionPane.showMessageDialog(this, 
                "Connexion réussie !\nBienvenue " + user.getPrenom() + " " + user.getNom(), 
                "Succès", JOptionPane.INFORMATION_MESSAGE);
        } else {
            errorLabel.setText("Identifiant ou mot de passe incorrect.");
            fieldMdp.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleLoginFrame().setVisible(true));
    }
}
