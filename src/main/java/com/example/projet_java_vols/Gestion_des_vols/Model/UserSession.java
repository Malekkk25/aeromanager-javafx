package com.example.projet_java_vols.Gestion_des_vols.Model;

public class UserSession {
    private static String roleActuel;
    private static String nomUtilisateur;

    public static void login(String role, String nom) {
        roleActuel = role;
        nomUtilisateur = nom;
    }

    public static String getRole() {
        return roleActuel;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(roleActuel) || "ADMINISTRATEUR".equalsIgnoreCase(roleActuel);
    }

    public static void logout() {
        roleActuel = null;
        nomUtilisateur = null;
    }
}
