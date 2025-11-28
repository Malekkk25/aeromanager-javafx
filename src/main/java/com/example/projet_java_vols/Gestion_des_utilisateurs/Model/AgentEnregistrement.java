package com.example.projetjava;

import java.util.ArrayList;

// Final car c'est une feuille de la sealed class
public final class AgentEnregistrement extends Employe {
    private String bureauEnregistrement;
    private int comptoir;

    public AgentEnregistrement(int idEmploye, String nom, String prenom, String email,
                               String telephone, String motDePasse,
                               String bureauEnregistrement, int comptoir) {
        super(idEmploye, nom, prenom, email, telephone, motDePasse);
        this.bureauEnregistrement = bureauEnregistrement;
        this.comptoir = comptoir;
    }

    public String getBureauEnregistrement() {
        return bureauEnregistrement;
    }

    public void setBureauEnregistrement(String bureauEnregistrement) {
        this.bureauEnregistrement = bureauEnregistrement;
    }

    public int getComptoir() {
        return comptoir;
    }

    public void setComptoir(int comptoir) {
        this.comptoir = comptoir;
    }

    public void reserverVol(VolSimple vol, int numeroPasseport, int numeroPassager) throws Exception {
        if (vol.nbPlaces() <= 0) {
            throw new Exception("Aucune place disponible sur le vol " + vol.numeroVol());
        }

        System.out.println("Réservation effectuée par " + getNom() +
                " pour le vol " + vol.numeroVol() +
                " - Passeport: " + numeroPasseport +
                ", Passager: " + numeroPassager);
    }

    public void annulerReservation(int idReservation) throws Exception {
        System.out.println("Réservation #" + idReservation + " annulée par " + getNom());
    }

    @Override
    public void afficherRole() {
        System.out.println("Rôle: Agent d'Enregistrement - Bureau: " + bureauEnregistrement + ", Comptoir: " + comptoir);
    }

    @Override
    public String toString() {
        return "AgentEnregistrement{" +
                "idEmploye=" + getIdEmploye() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", bureau='" + bureauEnregistrement + '\'' +
                ", comptoir=" + comptoir +
                '}';
    }
}