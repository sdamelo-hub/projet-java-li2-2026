package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import ProjetUniv_scheduler.Mesclasses.UnivSchedulerDAO;
import javax.swing.UIManager;

public class SimpleStudentGUI extends JFrame {
    
    private JTextField txtIdentifiant, txtNom, txtPrenom, txtEmail, txtMotDePasse, txtINE, txtNiveau, txtCycle;
    private JButton btnInsert;
    private JTextArea txtResult;
    private SessionFactory sessionFactory;
    
    public SimpleStudentGUI() {
        // Initialisation de Hibernate
        try {
            Configuration configuration = new Configuration().configure(new java.io.File("resources/hibernate.cfg.xml"));
            sessionFactory = configuration.buildSessionFactory();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur Hibernate: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        setTitle("🎓 Interface Simple - Test Étudiant");
        setSize(800, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        // Champs de texte
        txtIdentifiant = new JTextField(25);
        txtNom = new JTextField(25);
        txtPrenom = new JTextField(25);
        txtEmail = new JTextField(25);
        txtMotDePasse = new JTextField(25);
        txtINE = new JTextField(25);
        txtNiveau = new JTextField(25);
        txtCycle = new JTextField(25);
        
        // BOUTON TRÈS VISIBLE ET GRAND
        btnInsert = new JButton("🚀 CLIQUEZ ICI POUR INSÉRER L'ÉTUDIANT 🚀");
        btnInsert.setFont(new Font("Arial Black", Font.BOLD, 20));
        btnInsert.setBackground(Color.RED);
        btnInsert.setForeground(Color.WHITE);
        btnInsert.setPreferredSize(new Dimension(600, 80));
        btnInsert.setFocusPainted(false);
        btnInsert.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        
        // Zone de résultat
        txtResult = new JTextArea(15, 60);
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Courier New", Font.PLAIN, 12));
        txtResult.setBackground(Color.BLACK);
        txtResult.setForeground(Color.GREEN);
        
        // ACTION LISTENER TRÈS VISIBLE
        btnInsert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // FEEDBACK IMMÉDIAT
                txtResult.setText("🔥🔥🔥 BOUTON CLIQUÉ ! 🔥🔥🔥\n");
                txtResult.append("⏰ Heure: " + new java.util.Date() + "\n");
                txtResult.append("🎯 Le bouton fonctionne parfaitement !\n\n");
                
                // Message popup
                JOptionPane.showMessageDialog(SimpleStudentGUI.this, 
                    "🎉 LE BOUTON A ÉTÉ CLIQUÉ !\n\n" +
                    "Je vais maintenant insérer l'étudiant dans la base de données...", 
                    "✅ BOUTON FONCTIONNEL", JOptionPane.INFORMATION_MESSAGE);
                
                // Appel de la méthode d'insertion
                insertStudent();
            }
        });
    }
    
    private void layoutComponents() {
        // Panel formulaire avec fond coloré
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.LIGHT_GRAY);
        formPanel.setBorder(BorderFactory.createTitledBorder("📝 Formulaire d'inscription"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Ajout des labels et champs
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("🔑 Identifiant:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtIdentifiant, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("👤 Nom:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("👥 Prénom:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPrenom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("📧 Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("🔐 Mot de passe:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtMotDePasse, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("🆔 INE:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtINE, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("📚 Niveau:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNiveau, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("🎓 Cycle:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtCycle, gbc);
        
        // Panel pour le bouton (TRÈS VISIBLE)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.YELLOW);
        buttonPanel.setBorder(BorderFactory.createTitledBorder("⚡ ACTION PRINCIPALE"));
        buttonPanel.add(btnInsert);
        
        // Panel résultat
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("📊 Résultat de l'opération"));
        resultPanel.add(new JScrollPane(txtResult), BorderLayout.CENTER);
        
        // Assemblage final
        add(formPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);
    }
    
    private void insertStudent() {
        txtResult.append("\n🔍 DÉBUT DE L'INSERTION...\n");
        
        try {
            // Validation
            if (txtIdentifiant.getText().trim().isEmpty() ||
                txtNom.getText().trim().isEmpty() ||
                txtPrenom.getText().trim().isEmpty()) {
                
                txtResult.append("❌ Champs obligatoires manquants!\n");
                JOptionPane.showMessageDialog(this, 
                    "Les champs Identifiant, Nom et Prénom sont obligatoires!", 
                    "⚠️ Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            txtResult.append("✅ Validation réussie!\n");
            
            // Création de l'étudiant
            Etudiant etudiant = new Etudiant();
            etudiant.setIdentifiantConnexion(txtIdentifiant.getText().trim());
            etudiant.setNom(txtNom.getText().trim());
            etudiant.setPrenom(txtPrenom.getText().trim());
            etudiant.setEmail(txtEmail.getText().trim());
            etudiant.setMotDePasse(txtMotDePasse.getText().trim());
            etudiant.setIneEtudiant(txtINE.getText().trim());
            etudiant.setNiveauEtude(txtNiveau.getText().trim());
            etudiant.setCycle(txtCycle.getText().trim());
            
            txtResult.append("👤 Objet étudiant créé!\n");
            txtResult.append("   Nom: " + etudiant.getNom() + "\n");
            txtResult.append("   Prénom: " + etudiant.getPrenom() + "\n\n");
            
            // Connexion et sauvegarde
            txtResult.append("📊 Connexion à la base de données...\n");
            Session session = sessionFactory.openSession();
            Transaction transaction = null;
            
            try {
                transaction = session.beginTransaction();
                txtResult.append("💾 Sauvegarde en cours...\n");
                
                session.save(etudiant);
                txtResult.append("✅ session.save() exécuté!\n");
                
                transaction.commit();
                txtResult.append("🎉 transaction.commit() RÉUSSI!\n");
                
                // Succès total
                String message = "🎉 ÉTUDIANT INSÉRÉ AVEC SUCCÈS! 🎉\n\n" +
                    "📋 Données enregistrées:\n" +
                    "🔑 ID: " + etudiant.getIdentifiantConnexion() + "\n" +
                    "👤 Nom: " + etudiant.getNom() + "\n" +
                    "👥 Prénom: " + etudiant.getPrenom() + "\n" +
                    "📧 Email: " + etudiant.getEmail() + "\n" +
                    "🆔 INE: " + etudiant.getIneEtudiant() + "\n" +
                    "📚 Niveau: " + etudiant.getNiveauEtude() + "\n" +
                    "🎓 Cycle: " + etudiant.getCycle() + "\n\n" +
                    "💡 Vérifiez vos tables:\n" +
                    "   • SELECT * FROM utilisateurs;\n" +
                    "   • SELECT * FROM etudiants;";
                
                JOptionPane.showMessageDialog(this, message, 
                    "✅ SUCCÈS TOTAL", JOptionPane.INFORMATION_MESSAGE);
                
                txtResult.append("\n" + "=".repeat(60) + "\n");
                txtResult.append("🎉 OPÉRATION TERMINÉE AVEC SUCCÈS! 🎉\n");
                txtResult.append("=".repeat(60) + "\n");
                
                // Vider les champs
                txtIdentifiant.setText("");
                txtNom.setText("");
                txtPrenom.setText("");
                txtEmail.setText("");
                txtMotDePasse.setText("");
                txtINE.setText("");
                txtNiveau.setText("");
                txtCycle.setText("");
                txtIdentifiant.requestFocus();
                
            } catch (Exception e) {
                txtResult.append("❌ Erreur: " + e.getMessage() + "\n");
                if (transaction != null) {
                    transaction.rollback();
                    txtResult.append("🔄 Rollback effectué\n");
                }
                
                JOptionPane.showMessageDialog(this, 
                    "❌ Erreur lors de l'insertion: " + e.getMessage(), 
                    "💥 ERREUR", JOptionPane.ERROR_MESSAGE);
            } finally {
                session.close();
                txtResult.append("🔒 Session fermée\n");
            }
            
        } catch (Exception e) {
            txtResult.append("💥 Erreur générale: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, 
                "💥 Erreur générale: " + e.getMessage(), 
                "💥 ERREUR GRAVE", JOptionPane.ERROR_MESSAGE);
        }
        
        txtResult.append("\n🏁 FIN DU TRAITEMENT\n");
    }
    
    public static void main(String[] args) {
        // Lancement avec look and feel par défaut
        try {
            // UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeel());
        } catch (Exception e) {
            // Ignore
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SimpleStudentGUI().setVisible(true);
            }
        });
    }
}
