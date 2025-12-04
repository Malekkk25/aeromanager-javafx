package com.example.projet_java_vols.Gestion_des_utilisateurs.Controller;

import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.ClasseVol;
import com.example.projet_java_vols.Gestion_des_utilisateurs.Model.Reservation;
import com.example.projet_java_vols.Gestion_des_vols.Model.StatutVol;
import com.example.projet_java_vols.Gestion_des_vols.Model.*;
import com.example.projet_java_vols.ConnexionDB;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class GestionReservationsController {

    @FXML private ComboBox<String> cmbVol;
    @FXML private TextField txtPasseport;
    @FXML private ComboBox<ClasseVol> cmbClasse;
    @FXML private Label lblPrixTotal;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;
    @FXML private Button btnSupprimer;
    @FXML private TextField txtRecherche;
    @FXML private Label lblNombreReservations;
    @FXML private VBox containerSaisie;
    @FXML private Button btnMenuEmployes;
    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Integer> colIdReservation;
    @FXML private TableColumn<Reservation, String> colNumVol;
    @FXML private TableColumn<Reservation, Integer> colPasseport;
    @FXML private TableColumn<Reservation, ClasseVol> colClasse;
    @FXML private TableColumn<Reservation, Double> colPrixTotal;

    private ObservableList<Reservation> listeReservations = FXCollections.observableArrayList();
    private Reservation reservationSelectionnee = null;
    private ObservableList<Vol> listeVols = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbClasse.setItems(FXCollections.observableArrayList(ClasseVol.values()));
        chargerVolsDepuisBD();

        colIdReservation.setCellValueFactory(new PropertyValueFactory<>("idReservation"));
        colNumVol.setCellValueFactory(new PropertyValueFactory<>("numVol"));
        colPasseport.setCellValueFactory(new PropertyValueFactory<>("passeport_pasasager"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colPrixTotal.setCellValueFactory(new PropertyValueFactory<>("prixTotal"));

        chargerReservationsDepuisBD();
        tableReservations.setItems(listeReservations);

        tableReservations.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            reservationSelectionnee = newVal;
            btnModifier.setDisable(newVal == null);
            btnSupprimer.setDisable(newVal == null);
            if (newVal != null) remplirChamps(newVal);
            else effacerChamps();
        });

        cmbVol.valueProperty().addListener((obs, oldVal, newVal) -> calculerPrix());
        cmbClasse.valueProperty().addListener((obs, oldVal, newVal) -> calculerPrix());
        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerReservations(newVal));

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        updateCompteurReservations();
        gererDroitsAcces();
    }
    private void gererDroitsAcces() {
        String role = UserSession.getRole();
        if (role == null) role = "INVITE";

        boolean isAdmin = role.equalsIgnoreCase("ADMIN") || role.equalsIgnoreCase("ADMINISTRATEUR");
        if (btnMenuEmployes != null) {
            btnMenuEmployes.setVisible(isAdmin);
            btnMenuEmployes.setManaged(isAdmin);
        }

        boolean isAgentEnreg = role.equalsIgnoreCase("AGENT_ENREG");
        boolean aDroitModification = isAdmin || isAgentEnreg;

        if (containerSaisie != null) {
            containerSaisie.setVisible(aDroitModification);
            containerSaisie.setManaged(aDroitModification);
        }

        if (!aDroitModification) {
            btnAjouter.setVisible(false);
            btnModifier.setVisible(false);
            btnSupprimer.setVisible(false);
            btnAnnuler.setVisible(false);
        }
    }
    private void chargerVolsDepuisBD() {
        listeVols.clear();
        ObservableList<String> numsVols = FXCollections.observableArrayList();
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vol");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("type");
                Vol v;
                if ("International".equalsIgnoreCase(type)) {
                    v = new VolInternational(
                            rs.getString("numVol"),
                            rs.getInt("nbPlaces"),
                            rs.getDouble("prixBase"),
                            rs.getDouble("prixVol"),
                            StatutVol.valueOf(rs.getString("statut")),
                            rs.getString("paysDestination"),
                            new ArrayList<>(),
                            new HashMap<>(),
                            rs.getDate("dateArrivee"),
                            rs.getDate("dateDepart"),
                            rs.getTime("heureArrivee").toLocalTime(),
                            chargerAeroport(rs.getString("aeroportArrivee")),
                            rs.getTime("heureDepart").toLocalTime(),
                            chargerAeroport(rs.getString("aeroportDepart")),
                            rs.getString("numeroAutorisation"),
                            rs.getBoolean("exigenceVisa")
                    );
                } else {
                    v = new VolNational(
                            rs.getString("numVol"),
                            rs.getInt("nbPlaces"),
                            rs.getDouble("prixBase"),
                            rs.getDouble("prixVol"),
                            StatutVol.valueOf(rs.getString("statut")),
                            rs.getString("paysDestination"),
                            new ArrayList<>(),
                            new HashMap<>(),
                            rs.getDate("dateArrivee"),
                            rs.getDate("dateDepart"),
                            rs.getTime("heureArrivee").toLocalTime(),
                            chargerAeroport(rs.getString("aeroportArrivee")),
                            rs.getTime("heureDepart").toLocalTime(),
                            chargerAeroport(rs.getString("aeroportDepart")),
                            rs.getString("numeroAutorisation"),
                            rs.getString("terminal")
                    );
                }

                listeVols.add(v);
                numsVols.add(v.getNumVol());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        cmbVol.setItems(numsVols);
    }

    private Aeroport chargerAeroport(String idAeroport) {
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM aeroport WHERE idAeroport = ?")) {
            stmt.setString(1, idAeroport);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom");
                    String ville = rs.getString("ville");
                    String pays = rs.getString("pays");
                    return new Aeroport(idAeroport, nom, ville, pays);
                }
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        return null;
    }

    private void chargerReservationsDepuisBD() {
        listeReservations.clear();
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reservation");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                listeReservations.add(new Reservation(
                        rs.getInt("idReservation"),
                        rs.getDouble("prixTotal"),
                        ClasseVol.valueOf(rs.getString("classeVol")),
                        rs.getInt("passeport_pasasager"),
                        rs.getString("numVol")
                ));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        tableReservations.setItems(listeReservations);
        updateCompteurReservations();
    }

    private void calculerPrix() {
        String volNum = cmbVol.getValue();
        ClasseVol classe = cmbClasse.getValue();
        Vol volObj = getVolByNum(volNum);
        if (volNum == null || classe == null || volObj == null) { lblPrixTotal.setText("-- €"); return; }
        double base = volObj.getPrixVol();
        double prix = switch (classe) {
            case ECONOMIQUE -> base;
            case BUSINESS -> base * 1.5;
            case PREMIERE -> base * 2;
        };
        lblPrixTotal.setText(prix + " €");
    }

    private Vol getVolByNum(String numVol) {
        for (Vol v : listeVols) {
            if (v.getNumVol().equals(numVol)) return v;
        }
        return null;
    }

    private void remplirChamps(Reservation r) {
        txtPasseport.setText(String.valueOf(r.getPasseport_pasasager()));
        cmbClasse.setValue(r.getClasse());
        cmbVol.setValue(String.valueOf(r.getNumVol()));
        lblPrixTotal.setText(r.getPrixTotal() + " €");
    }

    private void effacerChamps() {
        txtPasseport.clear();
        cmbClasse.getSelectionModel().clearSelection();
        cmbVol.getSelectionModel().clearSelection();
        lblPrixTotal.setText("-- €");
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    @FXML
    private void ajouterReservation() {
        if (!validerChamps()) return;

        String volNum = cmbVol.getValue();
        Vol volObj = getVolByNum(volNum);
        if (volObj == null) return;

        double prix = 0;
        try {
            prix = Double.parseDouble(lblPrixTotal.getText().replace(" €", ""));
        } catch (NumberFormatException e) {
            lblPrixTotal.setText("-- €");
            return;
        }

        String sqlInsert = "INSERT INTO reservation (numVol, passeport_pasasager, prixTotal, classeVol, idEmploye) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {

            stmt.setString(1, volNum);
            stmt.setInt(2, Integer.parseInt(txtPasseport.getText()));
            stmt.setDouble(3, prix);
            stmt.setString(4, cmbClasse.getValue().name());

            int idEmploye = UserSession.getUserId();
            if (idEmploye == 0) {
                afficherErreur("Aucun employé connecté : impossible d’enregistrer l’ID employé.");
                return;
            }
            stmt.setInt(5, idEmploye);

            stmt.executeUpdate();
            afficherSucces("Réservation ajoutée avec succès !");

            try (PreparedStatement psUpdate = conn.prepareStatement(
                    "UPDATE vol SET nbPlaces = nbPlaces - 1 WHERE numVol = ? AND nbPlaces > 0")) {
                psUpdate.setString(1, volNum);
                psUpdate.executeUpdate();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            afficherErreur("Erreur lors de l'enregistrement : " + ex.getMessage());
        }

        chargerReservationsDepuisBD();
        effacerChamps();
    }





    @FXML
    private void modifierReservation() {
        if (reservationSelectionnee == null || !validerChamps()) return;
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE reservation SET numVol=?, passeport_pasasager=?, prixTotal=?, classeVol=? WHERE idReservation=?")) {
            stmt.setString(1, cmbVol.getValue());
            stmt.setInt(2, Integer.parseInt(txtPasseport.getText()));
            stmt.setDouble(3, Double.parseDouble(lblPrixTotal.getText().replace(" €", "")));
            stmt.setString(4, cmbClasse.getValue().name());
            stmt.setInt(5, reservationSelectionnee.getIdReservation());
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
        chargerReservationsDepuisBD();
        effacerChamps();
        tableReservations.getSelectionModel().clearSelection();
    }

    @FXML
    private void supprimerReservation() {
        if (reservationSelectionnee == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Suppression");
        alert.setContentText("Confirmer la suppression ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = ConnexionDB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM reservation WHERE idReservation=?")) {
                stmt.setInt(1, reservationSelectionnee.getIdReservation());
                stmt.executeUpdate();
            } catch (Exception ex) { ex.printStackTrace(); }
            chargerReservationsDepuisBD();
            effacerChamps();
            tableReservations.getSelectionModel().clearSelection();
            reservationSelectionnee = null;
            if (reservationSelectionnee != null) {
                try (Connection conn = ConnexionDB.getConnection();
                     PreparedStatement psUpdate = conn.prepareStatement(
                             "UPDATE vol SET nbPlaces = nbPlaces + 1 WHERE numVol = ?")) {
                    psUpdate.setString(1, reservationSelectionnee.getNumVol());
                    psUpdate.executeUpdate();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
            afficherSucces("Réservation supprimée avec succès !");

        }
    }
    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void annulerAction() {
        effacerChamps();
        tableReservations.getSelectionModel().clearSelection();
        reservationSelectionnee = null;
    }

    private boolean validerChamps() {
        if (cmbVol.getValue() == null) {
            afficherErreur("Veuillez sélectionner un numéro de vol.");
            cmbVol.requestFocus();
            return false;
        }

        if (cmbClasse.getValue() == null) {
            afficherErreur("Veuillez sélectionner la classe.");
            cmbClasse.requestFocus();
            return false;
        }

        String passeportTxt = txtPasseport.getText();
        if (passeportTxt == null || passeportTxt.trim().isEmpty()) {
            afficherErreur("Le numéro de passeport est obligatoire.");
            txtPasseport.requestFocus();
            return false;
        }

        String numVol = cmbVol.getValue();
        for (Reservation r : listeReservations) {
            if (r.getNumVol().equals(numVol) && String.valueOf(r.getPasseport_pasasager()).equals(passeportTxt.trim())
            ) {
                if (reservationSelectionnee == null || r.getIdReservation() != reservationSelectionnee.getIdReservation()) {
                    afficherErreur("Ce passeport a déjà une réservation pour ce vol.");
                    return false;
                }
            }

            String volNum = cmbVol.getValue();
            Vol volObj = getVolByNum(volNum);
            if (volObj != null) {
                int nbReservations = 0;
                try (Connection conn = ConnexionDB.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM reservation WHERE numVol = ?")) {
                    stmt.setString(1, volNum);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) nbReservations = rs.getInt(1);
                } catch (Exception ex) { ex.printStackTrace(); }
                int placesRestantes = volObj.getNbPlacesDisponibles() - nbReservations;
                if (placesRestantes <= 0) {
                    afficherErreur("Ce vol est complet : aucune place disponible.");
                    cmbVol.requestFocus();
                    return false;
                }
            }


        }

        String prixTxt = lblPrixTotal.getText().replace(" €", "").replace("--", "0").trim();
        try {
            double prix = Double.parseDouble(prixTxt);
            if (prix <= 0) {
                afficherErreur("Le prix total doit être supérieur à 0.");
                return false;
            }
        } catch (NumberFormatException e) {
            afficherErreur("Le prix total doit être un nombre valide.");
            return false;
        }

        return true;
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void updateCompteurReservations() {
        lblNombreReservations.setText("Total: " + listeReservations.size() + " réservation(s)");
    }
    private void filtrerReservations(String filtre) {
        if (filtre == null || filtre.isEmpty()) {
            tableReservations.setItems(listeReservations);
        } else {
            ObservableList<Reservation> filtrées = FXCollections.observableArrayList();
            for (Reservation r : listeReservations) {
                if (
                        String.valueOf(r.getIdReservation()).contains(filtre) ||
                                String.valueOf(r.getPasseport_pasasager()).contains(filtre) ||
                                (r.getClasse() != null && r.getClasse().name().toLowerCase().contains(filtre.toLowerCase())) ||
                                (r.getNumVol() != null && r.getNumVol().toLowerCase().contains(filtre.toLowerCase()))
                ) {
                    filtrées.add(r);
                }
            }
            tableReservations.setItems(filtrées);
        }
        updateCompteurReservations();
    }



    @FXML
    private void allerAeroports() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Aeroport.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Aéroports");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void allerEscales() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Escales.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Escales");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void allerVols() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Vols.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Vols");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void allerReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Reservations.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnAjouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des Réservations");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void allerEmploye() {  try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_employes.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Gestion des Employés");
    } catch (Exception e) {
        e.printStackTrace();
    }

    }


    public void allerStats(ActionEvent actionEvent) {  try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Gestion_Statistiques.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Statistiques");
    } catch (Exception e) {
        e.printStackTrace();
    }

    }



    @FXML
    private void handleMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #1e40af; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-padding: 8 16; -fx-background-radius: 8; " +
                "-fx-border-color: transparent; -fx-cursor: hand;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #475569; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-padding: 8 16; -fx-background-radius: 8; " +
                "-fx-border-color: transparent; -fx-cursor: hand;");
    }

    @FXML
    private void imprimerTicket() {
        if (reservationSelectionnee == null) {
            afficherErreur("Veuillez sélectionner une réservation à imprimer.");
            return;
        }

        Stage ticketStage = new Stage();
        ticketStage.setTitle("Billet d'Avion - Vol " + reservationSelectionnee.getNumVol());

        VBox ticketContent = creerBilletAvion(reservationSelectionnee);

        Scene scene = new Scene(ticketContent, 800, 400);
        ticketStage.setScene(scene);
        ticketStage.initModality(Modality.APPLICATION_MODAL);
        ticketStage.show();
    }

    private VBox creerBilletAvion(Reservation reservation) {
        VBox ticket = new VBox(0);
        ticket.setStyle("-fx-background-color: white;");

        Vol vol = getVolByNumero(reservation.getNumVol());

        String villeDepart = vol != null && vol.getAeroportDepart() != null ? vol.getAeroportDepart().getVille() : "N/A";
        String codeDepart = vol != null && vol.getAeroportDepart() != null ? vol.getAeroportDepart().getIdAeroport() : "XXX";
        String villeArrivee = vol != null && vol.getAeroportArrivee() != null ? vol.getAeroportArrivee().getVille() : "N/A";
        String codeArrivee = vol != null && vol.getAeroportArrivee() != null ? vol.getAeroportArrivee().getIdAeroport() : "XXX";
        String dateVol = vol != null && vol.getDateDepart() != null ?
                new java.text.SimpleDateFormat("dd MMM yyyy").format(vol.getDateDepart()).toUpperCase() : "N/A";
        String heureDepart = vol != null && vol.getHeureDepart() != null ?
                vol.getHeureDepart().format(DateTimeFormatter.ofPattern("HH:mm")) : "N/A";

        HBox billetComplet = new HBox(0);

        VBox sectionPrincipale = new VBox(0);
        sectionPrincipale.setPrefWidth(600);
        sectionPrincipale.setPrefHeight(600);
        sectionPrincipale.setStyle("-fx-background-color: linear-gradient(to bottom, #0ea5e9, #0284c7); " +
                "-fx-padding: 0;");

        HBox entete = new HBox(15);
        entete.setStyle("-fx-padding: 20 30; -fx-background-color: rgba(255,255,255,0.1);");
        entete.setAlignment(Pos.CENTER_LEFT);

        Label logoAvion = new Label("✈");
        logoAvion.setStyle("-fx-font-size: 36px; -fx-text-fill: white;");

        VBox logoText = new VBox(2);
        Label compagnie = new Label("AEROMANAGER");
        compagnie.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Arial Black';");
        Label airlines = new Label("AIRLINES");
        airlines.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-letter-spacing: 3px;");
        logoText.getChildren().addAll(compagnie, airlines);

        entete.getChildren().addAll(logoAvion, logoText);

        HBox boardingPassBar = new HBox();
        boardingPassBar.setStyle("-fx-background-color: white; -fx-padding: 8 30;");
        Label boardingPass = new Label("BOARDING PASS");
        boardingPass.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0284c7; -fx-letter-spacing: 2px;");
        boardingPassBar.getChildren().add(boardingPass);

        VBox infoPrincipale = new VBox(25);
        infoPrincipale.setStyle("-fx-padding: 25 30;");

        GridPane ligne1 = new GridPane();
        ligne1.setHgap(50);
        ligne1.setVgap(8);

        creerChampBillet(ligne1, "PASSENGER NAME", "PASSAGER " + reservation.getPasseport_pasasager(), 0, 0);
        creerChampBillet(ligne1, "DATE", dateVol, 1, 0);

        HBox routeBox = new HBox(20);
        routeBox.setAlignment(Pos.CENTER);
        routeBox.setStyle("-fx-padding: 20 0;");

        VBox departBox = new VBox(5);
        departBox.setAlignment(Pos.CENTER);
        Label lblFrom = new Label("FROM");
        lblFrom.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
        Label codeDepLabel = new Label(codeDepart);
        codeDepLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Arial Black';");
        Label villeDepLabel = new Label(villeDepart);
        villeDepLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        departBox.getChildren().addAll(lblFrom, codeDepLabel, villeDepLabel);

        Label flecheAvion = new Label("✈");
        flecheAvion.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-rotate: 45;");

        VBox arriveeBox = new VBox(5);
        arriveeBox.setAlignment(Pos.CENTER);
        Label lblTo = new Label("TO");
        lblTo.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-font-weight: bold;");
        Label codeArrLabel = new Label(codeArrivee);
        codeArrLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-family: 'Arial Black';");
        Label villeArrLabel = new Label(villeArrivee);
        villeArrLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        arriveeBox.getChildren().addAll(lblTo, codeArrLabel, villeArrLabel);

        routeBox.getChildren().addAll(departBox, flecheAvion, arriveeBox);

        GridPane ligne3 = new GridPane();
        ligne3.setHgap(40);
        ligne3.setVgap(12);
        ligne3.setStyle("-fx-padding: 15 0;");

        creerChampBillet(ligne3, "FLIGHT", reservation.getNumVol(), 0, 0);
        creerChampBillet(ligne3, "GATE", "A" + (Math.abs(reservation.getIdReservation() % 20) + 1), 1, 0);
        creerChampBillet(ligne3, "SEAT", (Math.abs(reservation.getIdReservation() % 30) + 1) + "A", 2, 0);
        creerChampBillet(ligne3, "BOARDING TIME", heureDepart, 0, 1);
        creerChampBillet(ligne3, "CLASS", reservation.getClasse().toString(), 1, 1);

        ColumnConstraints col1 = new ColumnConstraints(150);
        ColumnConstraints col2 = new ColumnConstraints(150);
        ColumnConstraints col3 = new ColumnConstraints(150);
        ligne3.getColumnConstraints().addAll(col1, col2, col3);

        infoPrincipale.getChildren().addAll(ligne1, routeBox, ligne3);

        sectionPrincipale.getChildren().addAll(entete, boardingPassBar, infoPrincipale);

        VBox sectionTalon = new VBox(0);
        sectionTalon.setPrefWidth(200);
        sectionTalon.setStyle("-fx-background-color: linear-gradient(to bottom, #0ea5e9, #0284c7); " +
                "-fx-border-color: white; -fx-border-width: 0 0 0 3; -fx-border-style: dashed;");

        VBox talonContent = new VBox(20);
        talonContent.setStyle("-fx-padding: 30 20;");
        talonContent.setAlignment(Pos.CENTER);

        Label miniLogo = new Label("✈");
        miniLogo.setStyle("-fx-font-size: 28px; -fx-text-fill: white;");

        VBox barcodeVertical = new VBox(2);
        barcodeVertical.setAlignment(Pos.CENTER);
        for (int i = 0; i < 25; i++) {
            Region barre = new Region();
            barre.setPrefHeight(i % 3 == 0 ? 4 : 2);
            barre.setPrefWidth(60);
            barre.setStyle("-fx-background-color: white;");
            barcodeVertical.getChildren().add(barre);
        }

        VBox infosTalon = new VBox(15);
        infosTalon.setAlignment(Pos.CENTER);

        creerChampTalon(infosTalon, "FROM", codeDepart);
        creerChampTalon(infosTalon, "TO", codeArrivee);
        creerChampTalon(infosTalon, "FLIGHT", reservation.getNumVol());
        creerChampTalon(infosTalon, "SEAT", (Math.abs(reservation.getIdReservation() % 30) + 1) + "A");
        creerChampTalon(infosTalon, "DATE", dateVol);

        talonContent.getChildren().addAll(miniLogo, barcodeVertical, infosTalon);
        sectionTalon.getChildren().add(talonContent);

        billetComplet.getChildren().addAll(sectionPrincipale, sectionTalon);

        VBox barcodeSection = new VBox(8);
        barcodeSection.setStyle("-fx-padding: 20 30; -fx-background-color: white;");
        barcodeSection.setAlignment(Pos.CENTER);

        HBox barcode = new HBox(2);
        barcode.setAlignment(Pos.CENTER);
        for (int i = 0; i < 60; i++) {
            Region barre = new Region();
            barre.setPrefWidth(i % 3 == 0 ? 3 : 2);
            barre.setPrefHeight(50);
            barre.setStyle("-fx-background-color: black;");
            barcode.getChildren().add(barre);
        }

        Label codeText = new Label("* " + reservation.getIdReservation() + " " + reservation.getNumVol() + " " + reservation.getPasseport_pasasager() + " *");
        codeText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748b;");

        barcodeSection.getChildren().addAll(barcode, codeText);

        HBox boutons = new HBox(15);
        boutons.setStyle("-fx-padding: 20 30; -fx-background-color: white;");
        boutons.setAlignment(Pos.CENTER);

        Button btnImprimer = creerBouton("🖨 Imprimer", "#0284c7");
        btnImprimer.setOnAction(e -> {
            VBox billetSeul = new VBox(billetComplet, barcodeSection);
            imprimerTicketNode(billetSeul);
            ((Stage) btnImprimer.getScene().getWindow()).close();
        });

        Button btnPDF = creerBouton("💾 Sauvegarder PDF", "#059669");
        btnPDF.setOnAction(e -> sauvegarderTicketPDF(ticket, reservation));

        Button btnFermer = creerBouton("❌ Fermer", "#6b7280");
        btnFermer.setOnAction(e -> ((Stage) btnFermer.getScene().getWindow()).close());

        boutons.getChildren().addAll(btnImprimer, btnPDF, btnFermer);

        ticket.getChildren().addAll(billetComplet, barcodeSection, boutons);

        return ticket;
    }

    private void creerChampBillet(GridPane grid, String label, String valeur, int col, int row) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 9px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold; -fx-letter-spacing: 1px;");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        box.getChildren().addAll(lbl, val);
        grid.add(box, col, row);
    }

    private void creerChampTalon(VBox parent, String label, String valeur) {
        VBox box = new VBox(3);
        box.setAlignment(Pos.CENTER);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 8px; -fx-text-fill: rgba(255,255,255,0.7); -fx-font-weight: bold;");
        Label val = new Label(valeur);
        val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        box.getChildren().addAll(lbl, val);
        parent.getChildren().add(box);
    }

    private Button creerBouton(String texte, String couleur) {
        Button btn = new Button(texte);
        btn.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    private Vol getVolByNumero(String numVol) {
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM vol WHERE numVol = ?")) {
            stmt.setString(1, numVol);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("type");
                    Vol v;

                    Aeroport aeroDep = chargerAeroport(rs.getString("aeroportDepart"));
                    Aeroport aeroArr = chargerAeroport(rs.getString("aeroportArrivee"));

                    if ("International".equalsIgnoreCase(type)) {
                        v = new VolInternational(
                                rs.getString("numVol"),
                                rs.getInt("nbPlaces"),
                                rs.getDouble("prixBase"),
                                rs.getDouble("prixVol"),
                                StatutVol.valueOf(rs.getString("statut")),
                                rs.getString("paysDestination"),
                                new ArrayList<>(),
                                new HashMap<>(),
                                rs.getDate("dateArrivee"),
                                rs.getDate("dateDepart"),
                                rs.getTime("heureArrivee").toLocalTime(),
                                chargerAeroport(rs.getString("aeroportArrivee")),
                                rs.getTime("heureDepart").toLocalTime(),
                                chargerAeroport(rs.getString("aeroportDepart")),
                                rs.getString("numeroAutorisation"),
                                rs.getBoolean("exigenceVisa")
                        );
                    } else {
                        v = new VolNational(
                                rs.getString("numVol"),
                                rs.getInt("nbPlaces"),
                                rs.getDouble("prixBase"),
                                rs.getDouble("prixVol"),
                                StatutVol.valueOf(rs.getString("statut")),
                                rs.getString("paysDestination"),
                                new ArrayList<>(),
                                new HashMap<>(),
                                rs.getDate("dateArrivee"),
                                rs.getDate("dateDepart"),
                                rs.getTime("heureArrivee").toLocalTime(),
                                chargerAeroport(rs.getString("aeroportArrivee")),
                                rs.getTime("heureDepart").toLocalTime(),
                                chargerAeroport(rs.getString("aeroportDepart")),
                                rs.getString("numeroAutorisation"),
                                rs.getString("terminal")
                        );
                    }

                    v.setPrixBase(rs.getDouble("prixVol"));
                    return v;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Aeroport getAeroportById(String id) {
        return chargerAeroport(id);
    }

    private void imprimerTicketNode(VBox ticket) {
        javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(ticket.getScene().getWindow())) {
            boolean success = job.printPage(ticket);
            if (success) {
                job.endJob();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Impression");
                alert.setHeaderText(null);
                alert.setContentText("Billet imprimé avec succès !");
                alert.showAndWait();
            }
        }
    }

    private void sauvegarderTicketPDF(VBox ticket, Reservation reservation) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Sauvegarde PDF");
        info.setHeaderText("Fonctionnalité à venir");
        info.setContentText("La sauvegarde PDF nécessite l'ajout d'une bibliothèque externe (iText/PDFBox).");
        info.showAndWait();
    }
    @FXML
    private void quitter() {
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.close();
    }
    @FXML
    private void deconnexion() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projet_java_vols/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnAjouter.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion - AeroManager");

            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur : Impossible de charger la vue Login.fxml");
        }
    }

}
