package ProjetUniv_scheduler.Mesclasses;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import ProjetUniv_scheduler.Mesclasses.Etudiant;

public class StudentTestGUI extends JFrame {
    
    private JTextField txtIdentifiant, txtNom, txtPrenom, txtEmail, txtMotDePasse, txtINE, txtNiveau, txtCycle;
    private JButton btnInsert, btnClear, btnShowStudents;
    private JTextArea txtResult;
    
    private SessionFactory sessionFactory;
    
    public StudentTestGUI() {
        // Initialisation de Hibernate
        try {
            Configuration configuration = new Configuration().configure(new java.io.File("resources/hibernate.cfg.xml"));
            sessionFactory = configuration.buildSessionFactory();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur d'initialisation Hibernate: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        setTitle("🎓 Test d'insertion d'étudiants - Base de données MySQL");
        setSize(700, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
        layoutComponents();
    }
    
    private void initComponents() {
        txtIdentifiant = new JTextField(20);
        txtNom = new JTextField(20);
        txtPrenom = new JTextField(20);
        txtEmail = new JTextField(20);
        txtMotDePasse = new JTextField(20);
        txtINE = new JTextField(20);
        txtNiveau = new JTextField(20);
        txtCycle = new JTextField(20);
        
        btnInsert = new JButton("🚀 Insérer l'étudiant dans la base");
        btnClear = new JButton("🗑️ Vider les champs");
        btnShowStudents = new JButton("📋 Afficher tous les étudiants");
        
        // Rendre les boutons plus visibles
        btnInsert.setBackground(new Color(76, 175, 80)); // Vert
        btnInsert.setForeground(Color.WHITE);
        btnInsert.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnClear.setBackground(new Color(255, 152, 0)); // Orange
        btnClear.setForeground(Color.WHITE);
        btnClear.setFont(new Font("Arial", Font.BOLD, 12));
        
        btnShowStudents.setBackground(new Color(33, 150, 243)); // Bleu
        btnShowStudents.setForeground(Color.WHITE);
        btnShowStudents.setFont(new Font("Arial", Font.BOLD, 12));
        
        txtResult = new JTextArea(10, 40);
        txtResult.setEditable(false);
        txtResult.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        // Actions
        btnInsert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("🔥 BOUTON CLIQUÉ !");
                txtResult.setText("🔥 Bouton cliqué ! Début du traitement...\n");
                
                // Test simple immédiat
                JOptionPane.showMessageDialog(StudentTestGUI.this, 
                    "Le bouton a bien été cliqué !\nJe vais maintenant essayer d'insérer l'étudiant.", 
                    "Test du bouton", JOptionPane.INFORMATION_MESSAGE);
                
                insertStudent();
            }
        });
        
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        
        btnShowStudents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAllStudents();
            }
        });
    }
    
    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Panel formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Labels et champs
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Identifiant connexion:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtIdentifiant, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nom:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Prénom:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPrenom, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Mot de passe:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtMotDePasse, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("INE étudiant:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtINE, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Niveau étude:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNiveau, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Cycle:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtCycle, gbc);
        
        // Panel boutons avec fond visible
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("⚡ Actions"));
        buttonPanel.add(btnInsert);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnShowStudents);
        
        // Panel résultat
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Résultat"));
        resultPanel.add(new JScrollPane(txtResult), BorderLayout.CENTER);
        
        // Assemblage
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(resultPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void insertStudent() {
        txtResult.append("🔍 Début de la méthode insertStudent()...\n");
        
        try {
            // Validation des champs
            txtResult.append("📝 Validation des champs...\n");
            if (txtIdentifiant.getText().trim().isEmpty() ||
                txtNom.getText().trim().isEmpty() ||
                txtPrenom.getText().trim().isEmpty()) {
                
                txtResult.append("❌ Champs obligatoires manquants!\n");
                JOptionPane.showMessageDialog(this, 
                    "Les champs Identifiant, Nom et Prénom sont obligatoires!", 
                    "Erreur de validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            txtResult.append("✅ Validation OK!\n");
            
            // Récupération des données
            String identifiant = txtIdentifiant.getText().trim();
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String motDePasse = txtMotDePasse.getText().trim();
            String ine = txtINE.getText().trim();
            String niveau = txtNiveau.getText().trim();
            String cycle = txtCycle.getText().trim();
            
            txtResult.append("👤 Création de l'objet étudiant...\n");
            txtResult.append("   ID: " + identifiant + "\n");
            txtResult.append("   Nom: " + nom + "\n");
            txtResult.append("   Prénom: " + prenom + "\n");
            
            // Création de l'étudiant
            Etudiant etudiant = new Etudiant();
            etudiant.setIdentifiantConnexion(identifiant);
            etudiant.setNom(nom);
            etudiant.setPrenom(prenom);
            etudiant.setEmail(email);
            etudiant.setMotDePasse(motDePasse);
            etudiant.setIneEtudiant(ine);
            etudiant.setNiveauEtude(niveau);
            etudiant.setCycle(cycle);
            
            txtResult.append("📊 Connexion à la base de données...\n");
            Session session = sessionFactory.openSession();
            Transaction transaction = null;
            
            try {
                transaction = session.beginTransaction();
                txtResult.append("💾 Sauvegarde dans la base...\n");
                
                session.save(etudiant);
                txtResult.append("✅ session.save() OK!\n");
                
                transaction.commit();
                txtResult.append("🎉 transaction.commit() OK!\n");
                
                // Succès!
                JOptionPane.showMessageDialog(this, 
                    "🎉 Étudiant inséré avec succès!\n\n" +
                    "Identifiant: " + etudiant.getIdentifiantConnexion() + "\n" +
                    "Nom: " + etudiant.getNom() + "\n" +
                    "Prénom: " + etudiant.getPrenom() + "\n" +
                    "Email: " + etudiant.getEmail() + "\n\n" +
                    "Vérifiez vos tables 'utilisateurs' et 'etudiants'!", 
                    "SUCCÈS", JOptionPane.INFORMATION_MESSAGE);
                
                txtResult.setText("🎉 SUCCÈS! Étudiant inséré!\n\n");
                txtResult.append("📋 Données enregistrées:\n");
                txtResult.append("   ID: " + etudiant.getIdentifiantConnexion() + "\n");
                txtResult.append("   Nom: " + etudiant.getNom() + "\n");
                txtResult.append("   Prénom: " + etudiant.getPrenom() + "\n");
                txtResult.append("   Email: " + etudiant.getEmail() + "\n");
                txtResult.append("   INE: " + etudiant.getIneEtudiant() + "\n");
                txtResult.append("   Niveau: " + etudiant.getNiveauEtude() + "\n");
                txtResult.append("   Cycle: " + etudiant.getCycle() + "\n\n");
                txtResult.append("💡 Vérifiez vos tables MySQL:\n");
                txtResult.append("   - SELECT * FROM utilisateurs;\n");
                txtResult.append("   - SELECT * FROM etudiants;\n");
                
                clearFields();
                
            } catch (Exception e) {
                txtResult.append("❌ Erreur pendant la transaction: " + e.getMessage() + "\n");
                if (transaction != null) {
                    transaction.rollback();
                    txtResult.append("🔄 Rollback effectué\n");
                }
                
                JOptionPane.showMessageDialog(this, 
                    "❌ Erreur lors de l'insertion: " + e.getMessage(), 
                    "ERREUR", JOptionPane.ERROR_MESSAGE);
            } finally {
                session.close();
                txtResult.append("🔒 Session fermée\n");
            }
            
        } catch (Exception e) {
            txtResult.append("❌ Erreur générale: " + e.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, 
                "❌ Erreur générale: " + e.getMessage(), 
                "ERREUR", JOptionPane.ERROR_MESSAGE);
        }
        
        txtResult.append("🏁 Fin du traitement\n");
    }
    
    private void showAllStudents() {
        try {
            Session session = sessionFactory.openSession();
            
            try {
                List<Etudiant> students = session.createQuery("from Etudiant", Etudiant.class).list();
                
                if (students.isEmpty()) {
                    txtResult.setText("Aucun étudiant trouvé dans la base de données.");
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("📋 Liste des étudiants (").append(students.size()).append(" trouvés):\n\n");
                    sb.append("ID\t\tNOM\t\tPRÉNOM\t\tEMAIL\n");
                    sb.append("=" .repeat(80)).append("\n");
                    
                    for (Etudiant etudiant : students) {
                        sb.append(etudiant.getIdentifiantConnexion()).append("\t");
                        sb.append(etudiant.getNom()).append("\t");
                        sb.append(etudiant.getPrenom()).append("\t");
                        sb.append(etudiant.getEmail()).append("\n");
                    }
                    
                    txtResult.setText(sb.toString());
                }
                
            } finally {
                session.close();
            }
            
        } catch (Exception e) {
            txtResult.setText("❌ Erreur lors de la récupération: " + e.getMessage());
        }
    }
    
    private void clearFields() {
        txtIdentifiant.setText("");
        txtNom.setText("");
        txtPrenom.setText("");
        txtEmail.setText("");
        txtMotDePasse.setText("");
        txtINE.setText("");
        txtNiveau.setText("");
        txtCycle.setText("");
        txtIdentifiant.requestFocus();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new StudentTestGUI().setVisible(true);
            }
        });
    }
}
