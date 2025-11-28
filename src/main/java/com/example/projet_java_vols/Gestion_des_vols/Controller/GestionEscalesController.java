package com.example.projet_java_vols.Gestion_des_vols.Controller;

import com.example.projet_java_vols.ConnexionDB;
import com.example.projet_java_vols.Gestion_des_vols.Model.StatutVol;
import com.example.projet_java_vols.Gestion_des_vols.Model.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static com.example.projet_java_vols.Gestion_des_vols.Model.Escale.formaterDuree;

public class GestionEscalesController {
    @FXML private TextField txtIdEscale;
    @FXML private Spinner<Integer> spinOrdre;
    @FXML private ComboBox<Aeroport> cmbAeroportDepart;
    @FXML private ComboBox<Aeroport> cmbAeroportArrivee;
    @FXML private DatePicker dpDateDepart;
    @FXML private DatePicker dpDateArrivee;
    @FXML private TextField txtHeureDepart;
    @FXML private TextField txtHeureArrivee;
    @FXML private Label lblDuree;
    @FXML private Button btnAjouter;
    @FXML private Button btnModifier;
    @FXML private Button btnAnnuler;
    @FXML private Button btnSupprimer;
    @FXML private TextField txtRecherche;
    @FXML private Label lblNombreEscales;
    @FXML private VBox containerSaisie;
    @FXML private Button btnMenuEmployes;
    @FXML private TableView<Escale> tableEscales;
    @FXML private TableColumn<Escale,Integer> colId;
    @FXML private TableColumn<Escale, String> colNumVol;
    @FXML private TableColumn<Escale, Integer> colOrdre;
    @FXML private TableColumn<Escale, String> colAeroportDepart;
    @FXML private TableColumn<Escale, String> colAeroportArrivee;
    @FXML private TableColumn<Escale, Date> colDateDepart;
    @FXML private TableColumn<Escale, String> colHeureDepart;
    @FXML private TableColumn<Escale, Date> colDateArrivee;
    @FXML private TableColumn<Escale, String> colHeureArrivee;
    @FXML private TableColumn<Escale, Long> colDuree;
    @FXML private ComboBox<Vol> cmbVol;
    private ObservableList<Vol> listeVols = FXCollections.observableArrayList();

    private ObservableList<Escale> listeEscales= FXCollections.observableArrayList();
    private Escale escaleSelectionnee = null;

    private ObservableList<Aeroport> listeAeroports=FXCollections.observableArrayList();

    private final DateTimeFormatter HEURE_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Vol volAssocie;

    @FXML
    public void initialize(){

        spinOrdre.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 3, 1));
        spinOrdre.setEditable(true);

        chargerAeroportsDepuisBD();
        cmbAeroportDepart.setItems(listeAeroports);
        cmbAeroportArrivee.setItems(listeAeroports);


        colId.setCellValueFactory(new PropertyValueFactory<Escale ,Integer>("idEscale"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<Escale,Integer>("ordre"));
        colNumVol.setCellValueFactory(new PropertyValueFactory<Escale,String>("numVol"));
        colAeroportDepart.setCellValueFactory(new PropertyValueFactory<Escale,String>("aeroportDepart"));
        colAeroportArrivee.setCellValueFactory(new PropertyValueFactory<Escale,String>("aeroportArrivee"));
        colDateDepart.setCellValueFactory(new PropertyValueFactory<Escale,Date>("dateDepart"));
        colDateArrivee.setCellValueFactory(new PropertyValueFactory<Escale,Date>("dateArrivee"));
        colHeureDepart.setCellValueFactory(new PropertyValueFactory<>("heureDepart"));
        colHeureArrivee.setCellValueFactory(new PropertyValueFactory<>("heureArrivee"));
        chargerVolsDepuisBD();
        cmbVol.setItems(listeVols);
        chargerEscalesDepuisBD();
        tableEscales.setItems(listeEscales);
        tableEscales.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            escaleSelectionnee = newSelection;
            btnModifier.setDisable(newSelection == null);
            btnSupprimer.setDisable(newSelection == null);
            if (newSelection != null) {
                remplirChamps(newSelection);
            } else {
                effacerChamps();
            }
        });

        txtHeureDepart.textProperty().addListener((obs, oldVal, newVal) -> calculerDuree());
        txtHeureArrivee.textProperty().addListener((obs, oldVal, newVal) -> calculerDuree());

        txtRecherche.textProperty().addListener((obs, oldVal, newVal) -> filtrerEscales(newVal));

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        updateCompteurEscales();
        gererDroitsAcces();

    }
    private void gererDroitsAcces() {
        String role = UserSession.getRole();
        if (role == null) role = "AGENT_ENREG";

        if (role.equals("AGENT_ENREG")) {
            if (containerSaisie != null) {
                containerSaisie.setVisible(false);
                containerSaisie.setManaged(false);
            }
            btnAjouter.setVisible(false);
            btnAnnuler.setVisible(false);
            btnModifier.setVisible(false);
            btnSupprimer.setVisible(false);

            if (btnMenuEmployes != null) {
                btnMenuEmployes.setVisible(false);
                btnMenuEmployes.setManaged(false);
            }
        }
        else if (role.equals("AGENT_VOL")) {
            if (btnMenuEmployes != null) {
                btnMenuEmployes.setVisible(false);
                btnMenuEmployes.setManaged(false);
            }
        }
    }

    private boolean estModeLectureSeule() {
        String role = UserSession.getRole();
        return role == null || role.equals("AGENT_ENREG");
    }
    private void chargerAeroportsDepuisBD() {
        listeAeroports.clear();
        try (Connection conn = ConnexionDB.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM aeroport")) {
            while (rs.next()) {
                String code = rs.getString("idAeroport");
                String nom = rs.getString("nom");
                String ville = rs.getString("ville");
                String pays = rs.getString("pays");
                listeAeroports.add(new Aeroport(code, nom, ville, pays));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }
    private void chargerVolsDepuisBD() {
        listeVols.clear();
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
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        cmbVol.setItems(listeVols);
    }
    private void chargerEscalesDepuisBD() {
        listeEscales.clear();
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM escale");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Escale e = new Escale(
                        rs.getInt("idEscale"),
                        rs.getInt("ordre"),
                        chargerAeroport(rs.getString("idAeroportArrivee")),
                        chargerAeroport(rs.getString("idAeroportDepart")),
                        rs.getDate("dateArrivee"),
                        rs.getDate("dateDepart"),
                        rs.getTime("heureArrivee").toLocalTime(),
                        rs.getTime("heureDepart").toLocalTime(),
                        rs.getString("numVol")
                );
                listeEscales.add(e);
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        tableEscales.setItems(listeEscales);
        updateCompteurEscales();
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


    private void remplirChamps(Escale e) {
        txtIdEscale.setText(String.valueOf(e.getIdEscale()));
        spinOrdre.getValueFactory().setValue(e.getOrdre());
        cmbAeroportDepart.setValue(e.getAeroportDepart());
        cmbAeroportArrivee.setValue(e.getAeroportArrivee());
        dpDateDepart.setValue(convertToLocalDate(e.getDateDepart()));
        dpDateArrivee.setValue(convertToLocalDate(e.getDateArrivee()));
        txtHeureDepart.setText(formatHeure(e.getHeureDepart()));
        txtHeureArrivee.setText(formatHeure(e.getHeureArrivee()));
        lblDuree.setText(e.calculerDureeMinutes() + " min");
    }


    @FXML
    public void ajouterEscale() {
        if (!validerChamps()) return;
        Escale e = construireEscale(false);
        Vol vol = cmbVol.getValue();
        if (vol == null) {
            showAlert("Sélection du vol", "Veuillez sélectionner un numéro de vol.");
            return;
        }
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO escale (ordre, idAeroportDepart, idAeroportArrivee, dateDepart, dateArrivee, heureDepart, heureArrivee, numVol) "
                             + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            stmt.setInt(1, e.getOrdre());
            stmt.setString(2, e.getAeroportDepart().getIdAeroport());
            stmt.setString(3, e.getAeroportArrivee().getIdAeroport());
            stmt.setDate(4, new java.sql.Date(e.getDateDepart().getTime()));
            stmt.setDate(5, new java.sql.Date(e.getDateArrivee().getTime()));
            stmt.setTime(6, java.sql.Time.valueOf(e.getHeureDepart()));
            stmt.setTime(7, java.sql.Time.valueOf(e.getHeureArrivee()));
            stmt.setString(8, vol.getNumVol());
            stmt.executeUpdate();
            afficherSucces("Escale ajoutée avec succès !");

        } catch (Exception ex) { ex.printStackTrace(); }
        chargerEscalesDepuisBD();
        effacerChamps();
    }



    @FXML
    private void modifierEscale() {
        if (escaleSelectionnee == null || !validerChamps()) return;
        Escale maj = construireEscale(true);
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE escale SET ordre=?, idAeroportDepart=?, idAeroportArrivee=?, dateDepart=?, dateArrivee=?, heureDepart=?, heureArrivee=? WHERE idEscale=?")) {
            stmt.setInt(1, maj.getOrdre());
            stmt.setString(2, maj.getAeroportDepart().getIdAeroport());
            stmt.setString(3, maj.getAeroportArrivee().getIdAeroport());
            stmt.setDate(4, new java.sql.Date(maj.getDateDepart().getTime()));
            stmt.setDate(5, new java.sql.Date(maj.getDateArrivee().getTime()));
            stmt.setTime(6, java.sql.Time.valueOf(maj.getHeureDepart()));
            stmt.setTime(7, java.sql.Time.valueOf(maj.getHeureArrivee()));
            stmt.setInt(8, escaleSelectionnee.getIdEscale());
            stmt.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
        chargerEscalesDepuisBD();
        effacerChamps();
        tableEscales.getSelectionModel().clearSelection();
    }



    @FXML
    private void supprimerEscale() {
        if (escaleSelectionnee == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Suppression");
        alert.setContentText("Confirmer la suppression ?");
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try (Connection conn = ConnexionDB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM escale WHERE idEscale=?")) {
                stmt.setInt(1, escaleSelectionnee.getIdEscale());
                stmt.executeUpdate();
            } catch (Exception ex) { ex.printStackTrace(); }
            chargerEscalesDepuisBD();
            effacerChamps();
            tableEscales.getSelectionModel().clearSelection();
            escaleSelectionnee = null;
        }
    }


    @FXML
    private void annulerAction() {
        effacerChamps();
        tableEscales.getSelectionModel().clearSelection();
        escaleSelectionnee = null;
    }

    private void filtrerEscales(String filtre) {
        if (filtre == null || filtre.isEmpty()) {
            tableEscales.setItems(listeEscales);
        } else {
            String recherche = filtre.toLowerCase();
            ObservableList<Escale> filtrees = FXCollections.observableArrayList();
            for (Escale e : listeEscales) {
                if (
                        (e.getNumVol() != null && e.getNumVol().toLowerCase().contains(recherche)) ||
                                (e.getAeroportDepart() != null && e.getAeroportDepart().getNom().toLowerCase().contains(recherche)) ||
                                (e.getAeroportArrivee() != null && e.getAeroportArrivee().getNom().toLowerCase().contains(recherche)) ||
                                String.valueOf(e.getOrdre()).contains(recherche) ||
                                String.valueOf(e.getIdEscale()).contains(recherche) ||
                                (e.getDateDepart() != null && formatDate(e.getDateDepart()).contains(recherche)) ||
                                (e.getDateArrivee() != null && formatDate(e.getDateArrivee()).contains(recherche))
                ) {
                    filtrees.add(e);
                }
            }
            tableEscales.setItems(filtrees);
        }
        updateCompteurEscales();
    }



    private void effacerChamps() {
        txtIdEscale.clear();
        spinOrdre.getValueFactory().setValue(1);
        cmbVol.getSelectionModel().clearSelection();
        cmbAeroportDepart.getSelectionModel().clearSelection();
        cmbAeroportArrivee.getSelectionModel().clearSelection();
        dpDateDepart.setValue(null);
        dpDateArrivee.setValue(null);
        txtHeureDepart.clear();
        txtHeureArrivee.clear();
        lblDuree.setText("-- min");
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }

    private void afficherErreur(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private boolean validerChamps() {
        Vol vol = cmbVol.getValue();
        if (vol == null) {
            afficherErreur("Veuillez sélectionner un vol.");
            cmbVol.requestFocus();
            return false;
        }

        int ordre = spinOrdre.getValue();
        if (ordre <= 0) {
            afficherErreur("L'ordre de l'escale doit être un entier positif.");
            return false;
        }
        for (Escale esc : listeEscales) {
            if (esc.getNumVol().equals(vol.getNumVol()) && esc.getOrdre() == ordre) {
                if (escaleSelectionnee == null || esc.getIdEscale() != escaleSelectionnee.getIdEscale()) {
                    afficherErreur("Ce vol contient déjà une escale d'ordre " + ordre + ".");
                    return false;
                }
            }
        }

        Aeroport dep = cmbAeroportDepart.getValue();
        Aeroport arr = cmbAeroportArrivee.getValue();
        if (dep == null || arr == null) {
            afficherErreur("Veuillez sélectionner les aéroports de départ et d'arrivée.");
            return false;
        }
        if (dep.getIdAeroport().equals(arr.getIdAeroport())) {
            afficherErreur("Départ et arrivée doivent être différents.");
            return false;
        }

        LocalDate dateDep = dpDateDepart.getValue();
        LocalDate dateArr = dpDateArrivee.getValue();
        if (dateDep == null || dateArr == null) {
            afficherErreur("Veuillez saisir les deux dates.");
            return false;
        }
        if (!dateDep.isBefore(dateArr)) {
            afficherErreur("La date de départ doit être strictement avant la date d'arrivée.");
            return false;
        }


        try {
            LocalTime heureDep = LocalTime.parse(txtHeureDepart.getText().trim(), HEURE_FORMATTER);
            LocalTime heureArr = LocalTime.parse(txtHeureArrivee.getText().trim(), HEURE_FORMATTER);
            if (dateDep.equals(dateArr) && !heureDep.isBefore(heureArr)) {
                afficherErreur("L'heure de départ doit précéder celle d'arrivée (le même jour).");
                return false;
            }
        } catch (Exception e) {
            afficherErreur("Format d'heure invalide. Utilisez le format HH:mm.");
            return false;
        }

        return true;
    }

    private void afficherSucces(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private Escale construireEscale(boolean garderId) {
        int id = garderId && txtIdEscale != null && !txtIdEscale.getText().isEmpty()
                ? Integer.parseInt(txtIdEscale.getText()) : 0;
        int ordre = spinOrdre.getValue();
        Aeroport dep = cmbAeroportDepart.getValue();
        Aeroport arr = cmbAeroportArrivee.getValue();
        Date dDep = convertToDate(dpDateDepart.getValue());
        Date dArr = convertToDate(dpDateArrivee.getValue());
        LocalTime hDep = parseHeure(txtHeureDepart.getText());
        LocalTime hArr = parseHeure(txtHeureArrivee.getText());
        String numVol = (cmbVol != null && cmbVol.getValue() != null) ? cmbVol.getValue().getNumVol() : "";

        return new Escale(id, ordre, arr, dep, dArr, dDep, hArr, hDep, numVol);
    }


    private void calculerDuree() {
        LocalDate dateDep = dpDateDepart.getValue();
        LocalDate dateArr = dpDateArrivee.getValue();
        LocalTime hDep = parseHeure(txtHeureDepart.getText());
        LocalTime hArr = parseHeure(txtHeureArrivee.getText());
        String duree = formaterDuree(dateDep, hDep, dateArr, hArr);
        lblDuree.setText(duree);
    }


    private int genererIdTemporaire() {
        return listeEscales.stream().mapToInt(Escale::getIdEscale).max().orElse(0) + 1;
    }

    private String formatHeure(LocalTime time) {
        return time == null ? "" : time.format(HEURE_FORMATTER);
    }


    private String formatDate(Date date) {
        if (date == null) return "";
        return convertToLocalDate(date).format(DATE_FORMATTER);
    }

    private LocalTime parseHeure(String text) {
        try { return LocalTime.parse(text, HEURE_FORMATTER); }
        catch (Exception e) { return null; }
    }

    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) return null;
        return java.sql.Date.valueOf(localDate);
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime()).toLocalDate();
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(titre);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void updateCompteurEscales() {
        int nb = 0;
        try (Connection conn = ConnexionDB.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM escale")) {
            if (rs.next()) nb = rs.getInt(1);
        } catch (Exception ex) { ex.printStackTrace(); }
        lblNombreEscales.setText("Total: " + nb + " escale(s)");
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
    @FXML
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

}
