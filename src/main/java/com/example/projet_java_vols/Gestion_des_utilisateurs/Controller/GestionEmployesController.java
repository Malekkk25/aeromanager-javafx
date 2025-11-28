package com.example.projetjava;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GestionEmployesController {

    // Formulaire d'ajout
    @FXML private ComboBox<String> cmbTypeEmploye;
    @FXML private TextField txtIdEmploye;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelephone;
    @FXML private PasswordField txtMotDePasse;

    // Champs Agent Vol
    @FXML private VBox vboxAgentVol;
    @FXML private TextField txtCodeCompagnie;
    @FXML private TextField txtNomCompagnie;
    @FXML private TextField txtPaysOrigine;
    @FXML private TextField txtFlotte;

    // Champs Agent Enregistrement
    @FXML private VBox vboxAgentEnreg;
    @FXML private TextField txtBureau;
    @FXML private TextField txtComptoir;

    // Actions
    @FXML private TextField txtIdEmployeAction;

    // Tableau
    @FXML private TableView<Employe> tableEmployes;
    @FXML private TableColumn<Employe, Integer> colIdEmploye;
    @FXML private TableColumn<Employe, String> colNom;
    @FXML private TableColumn<Employe, String> colPrenom;
    @FXML private TableColumn<Employe, String> colEmail;
    @FXML private TableColumn<Employe, String> colTelephone;
    @FXML private TableColumn<Employe, String> colType;
    @FXML private TableColumn<Employe, String> colActif;

    // Labels
    @FXML private Label lblAdminInfo;
    @FXML private Label lblTotalEmployes;
    @FXML private Label lblEmployesActifs;
    @FXML private Label lblAgentsVol;
    @FXML private Label lblAgentsEnreg;

    // Administrateur
    private Administrateur administrateur;

    // Liste observable pour le tableau
    private ObservableList<Employe> employesObservableList;

    /**
     * Initialisation du contrôleur
     */
    @FXML
    public void initialize() {
        System.out.println("🔧 Début de l'initialisation du contrôleur Employés...");

        // Initialiser le ComboBox des types
        ObservableList<String> types = FXCollections.observableArrayList(
                "Agent de Vol",
                "Agent d'Enregistrement"
        );

        if (cmbTypeEmploye != null) {
            cmbTypeEmploye.setItems(types);
            System.out.println("✅ cmbTypeEmploye initialisé avec " + types.size() + " éléments");

            // Listener pour afficher/masquer les champs spécifiques
            cmbTypeEmploye.setOnAction(event -> {
                String type = cmbTypeEmploye.getValue();
                System.out.println("📋 Type sélectionné: " + type);

                if (type != null) {
                    if (type.equals("Agent de Vol")) {
                        vboxAgentVol.setVisible(true);
                        vboxAgentVol.setManaged(true);
                        vboxAgentEnreg.setVisible(false);
                        vboxAgentEnreg.setManaged(false);
                        System.out.println("✅ Champs Agent de Vol affichés");
                    } else if (type.equals("Agent d'Enregistrement")) {
                        vboxAgentEnreg.setVisible(true);
                        vboxAgentEnreg.setManaged(true);
                        vboxAgentVol.setVisible(false);
                        vboxAgentVol.setManaged(false);
                        System.out.println("✅ Champs Agent d'Enregistrement affichés");
                    }
                }
            });
        } else {
            System.out.println("❌ cmbTypeEmploye est NULL!");
        }

        // Configurer les colonnes du tableau
        if (colIdEmploye != null) {
            colIdEmploye.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIdEmploye()).asObject());
        }
        if (colNom != null) {
            colNom.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getNom()));
        }
        if (colPrenom != null) {
            colPrenom.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getPrenom()));
        }
        if (colEmail != null) {
            colEmail.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getEmail()));
        }
        if (colTelephone != null) {
            colTelephone.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getTelephone()));
        }
        if (colType != null) {
            colType.setCellValueFactory(cellData -> {
                Employe emp = cellData.getValue();
                String type = emp instanceof AgentVol ? "Agent de Vol" : "Agent d'Enregistrement";
                return new SimpleStringProperty(type);
            });
        }
        if (colActif != null) {
            colActif.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().isActif() ? "Oui" : "Non"));
        }

        System.out.println("✅ Colonnes du tableau configurées");

        // Initialiser la liste observable
        employesObservableList = FXCollections.observableArrayList();
        if (tableEmployes != null) {
            tableEmployes.setItems(employesObservableList);
            System.out.println("✅ TableView initialisée");
        }

        System.out.println("✅ Initialisation du contrôleur terminée!");
    }

    /**
     * Définir l'administrateur
     */
    public void setAdministrateur(Administrateur admin) {
        System.out.println("🔧 setAdministrateur appelé avec: " + admin.getNom());
        this.administrateur = admin;

        if (lblAdminInfo != null) {
            lblAdminInfo.setText("Administrateur: " + admin.getNom() + " " + admin.getPrenom());
        }

        rafraichir();
    }

    /**
     * Ajouter un employé
     */
    @FXML
    private void ajouterEmploye() {
        try {
            // Validation des champs communs
            if (cmbTypeEmploye.getValue() == null || txtIdEmploye.getText().isEmpty() ||
                    txtNom.getText().isEmpty() || txtPrenom.getText().isEmpty() ||
                    txtEmail.getText().isEmpty() || txtTelephone.getText().isEmpty() ||
                    txtMotDePasse.getText().isEmpty()) {
                afficherErreur("Tous les champs sont obligatoires!");
                return;
            }

            // Récupérer les données communes
            int idEmploye = Integer.parseInt(txtIdEmploye.getText().trim());
            String nom = txtNom.getText().trim();
            String prenom = txtPrenom.getText().trim();
            String email = txtEmail.getText().trim();
            String telephone = txtTelephone.getText().trim();
            String motDePasse = txtMotDePasse.getText().trim();

            Employe nouvelEmploye;

            // Créer l'employé selon le type
            if (cmbTypeEmploye.getValue().equals("Agent de Vol")) {
                // Validation champs Agent Vol
                if (txtCodeCompagnie.getText().isEmpty() || txtNomCompagnie.getText().isEmpty() ||
                        txtPaysOrigine.getText().isEmpty() || txtFlotte.getText().isEmpty()) {
                    afficherErreur("Tous les champs Agent de Vol sont obligatoires!");
                    return;
                }

                String codeCompagnie = txtCodeCompagnie.getText().trim();
                String nomCompagnie = txtNomCompagnie.getText().trim();
                String paysOrigine = txtPaysOrigine.getText().trim();
                int flotte = Integer.parseInt(txtFlotte.getText().trim());

                nouvelEmploye = new AgentVol(idEmploye, nom, prenom, email, telephone,
                        motDePasse, codeCompagnie, nomCompagnie, paysOrigine, flotte);

                System.out.println("➕ Ajout Agent de Vol: " + nom + " " + prenom);

            } else { // Agent d'Enregistrement
                // Validation champs Agent Enregistrement
                if (txtBureau.getText().isEmpty() || txtComptoir.getText().isEmpty()) {
                    afficherErreur("Tous les champs Agent d'Enregistrement sont obligatoires!");
                    return;
                }

                String bureau = txtBureau.getText().trim();
                int comptoir = Integer.parseInt(txtComptoir.getText().trim());

                nouvelEmploye = new AgentEnregistrement(idEmploye, nom, prenom, email,
                        telephone, motDePasse, bureau, comptoir);

                System.out.println("➕ Ajout Agent d'Enregistrement: " + nom + " " + prenom);
            }

            // Ajouter via l'administrateur
            administrateur.ajouterEmploye(nouvelEmploye);

            // Rafraîchir l'affichage
            rafraichir();

            // Effacer le formulaire
            effacerFormulaire();

            // Message de succès
            afficherSucces("Employé ajouté avec succès!");

        } catch (NumberFormatException e) {
            afficherErreur("Les champs numériques doivent contenir des nombres valides!");
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Activer un employé
     */
    @FXML
    private void activerEmploye() {
        try {
            if (txtIdEmployeAction.getText().isEmpty()) {
                afficherErreur("Veuillez saisir l'ID de l'employé!");
                return;
            }

            int idEmploye = Integer.parseInt(txtIdEmployeAction.getText().trim());
            administrateur.activerEmploye(idEmploye);
            rafraichir();
            txtIdEmployeAction.clear();
            afficherSucces("Employé activé avec succès!");

        } catch (NumberFormatException e) {
            afficherErreur("L'ID doit être un nombre!");
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    /**
     * Désactiver un employé
     */
    @FXML
    private void desactiverEmploye() {
        try {
            if (txtIdEmployeAction.getText().isEmpty()) {
                afficherErreur("Veuillez saisir l'ID de l'employé!");
                return;
            }

            int idEmploye = Integer.parseInt(txtIdEmployeAction.getText().trim());
            administrateur.desactiverEmploye(idEmploye);
            rafraichir();
            txtIdEmployeAction.clear();
            afficherSucces("Employé désactivé avec succès!");

        } catch (NumberFormatException e) {
            afficherErreur("L'ID doit être un nombre!");
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    /**
     * Supprimer un employé
     */
    @FXML
    private void supprimerEmploye() {
        try {
            if (txtIdEmployeAction.getText().isEmpty()) {
                afficherErreur("Veuillez saisir l'ID de l'employé!");
                return;
            }

            int idEmploye = Integer.parseInt(txtIdEmployeAction.getText().trim());

            // Confirmation
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer l'employé");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cet employé?");

            if (confirmation.showAndWait().get() == ButtonType.OK) {
                administrateur.supprimerEmploye(idEmploye);
                rafraichir();
                txtIdEmployeAction.clear();
                afficherSucces("Employé supprimé avec succès!");
            }

        } catch (NumberFormatException e) {
            afficherErreur("L'ID doit être un nombre!");
        } catch (Exception e) {
            afficherErreur("Erreur: " + e.getMessage());
        }
    }

    /**
     * Effacer le formulaire
     */
    @FXML
    private void effacerFormulaire() {
        cmbTypeEmploye.setValue(null);
        txtIdEmploye.clear();
        txtNom.clear();
        txtPrenom.clear();
        txtEmail.clear();
        txtTelephone.clear();
        txtMotDePasse.clear();

        // Agent Vol
        txtCodeCompagnie.clear();
        txtNomCompagnie.clear();
        txtPaysOrigine.clear();
        txtFlotte.clear();

        // Agent Enregistrement
        txtBureau.clear();
        txtComptoir.clear();

        // Masquer les sections spécifiques
        vboxAgentVol.setVisible(false);
        vboxAgentVol.setManaged(false);
        vboxAgentEnreg.setVisible(false);
        vboxAgentEnreg.setManaged(false);
    }

    /**
     * Rafraîchir l'affichage
     */
    @FXML
    private void rafraichir() {
        if (administrateur == null) {
            System.out.println("⚠️ Impossible de rafraîchir: administrateur est null");
            return;
        }

        System.out.println("🔄 Rafraîchissement de l'affichage...");

        // Mettre à jour le tableau
        employesObservableList.clear();
        employesObservableList.addAll(administrateur.getEmployes().values());

        System.out.println("📊 Employés affichés: " + employesObservableList.size());

        // Compter les types
        long nbAgentsVol = administrateur.getEmployes().values().stream()
                .filter(e -> e instanceof AgentVol)
                .count();
        long nbAgentsEnreg = administrateur.getEmployes().values().stream()
                .filter(e -> e instanceof AgentEnregistrement)
                .count();

        // Mettre à jour les statistiques
        if (lblTotalEmployes != null) {
            lblTotalEmployes.setText("Total employés: " + administrateur.getEmployes().size());
        }
        if (lblEmployesActifs != null) {
            lblEmployesActifs.setText("Employés actifs: " + administrateur.compterEmployesActifs());
        }
        if (lblAgentsVol != null) {
            lblAgentsVol.setText("Agents de vol: " + nbAgentsVol);
        }
        if (lblAgentsEnreg != null) {
            lblAgentsEnreg.setText("Agents enregistrement: " + nbAgentsEnreg);
        }

        System.out.println("✅ Rafraîchissement terminé");
    }

    /**
     * Quitter
     */
    @FXML
    private void quitter() {
        Stage stage = (Stage) tableEmployes.getScene().getWindow();
        stage.close();
    }

    /**
     * Afficher un message d'erreur
     */
    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher un message de succès
     */
    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}