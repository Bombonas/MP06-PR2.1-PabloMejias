package cat.iesesteveterradas;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Main {
    static Connection conn = null;
    
    public static void main(String[] args) {
        
        try {
            String url = "jdbc:sqlite:" + obtenirPathFitxer();
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("BBDD driver: " + meta.getDriverName());
            }
            System.out.println("BBDD SQLite connectada");
        } catch (SQLException e) { e.printStackTrace(); }

        // Executar consultes …
        initDB();
        String faction = "";
        Scanner scanner = new Scanner(System.in);
        while (faction == "") {
            System.out.println("Choose a faction:\n1. Higashikata Family\n2. Rokakaka Smugglers\n3. Rock Humans");
            System.out.print("Faction number: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    faction = "Higashikata";
                    break;
                case 2:
                    faction = "Rokakaka";
                    break;
                case 3:
                    faction = "Rock Humans";
                    break;
                default:
                    System.out.println("Invalid number");
            }
        }
        scanner.close();
        charactersList(conn, faction);
        bestAttacker(conn, faction);
        bestDefender(conn, faction);
        
        try {
            if (conn != null) {
                conn.close();
                System.out.println("DDBB SQLite desconnectada");
            }
        } catch (SQLException ex) {         
            System.out.println(ex.getMessage()); 
        }

    }

    public static Path obtenirPathFitxer() {
        return Paths.get(System.getProperty("user.dir"), "data", "database.db");
    }

    public static void initDB(){
        Operations.queryUpdate(conn, "DROP TABLE IF EXISTS factions;");
        Operations.queryUpdate(conn, "DROP TABLE IF EXISTS characters;");


        Operations.queryUpdate(conn, "CREATE TABLE IF NOT EXISTS factions ("
                                    + "	id integer PRIMARY KEY AUTOINCREMENT,"
                                    + "	name TEXT CHECK(LENGTH(name) <= 15),"
                                    + " summary TEXT CHECK(LENGTH(summary) <= 500));");

        Operations.queryUpdate(conn, "CREATE TABLE IF NOT EXISTS characters ("
                                    + "	id integer PRIMARY KEY AUTOINCREMENT,"
                                    + "	name TEXT CHECK(LENGTH(name) <= 15),"
                                    + " atack REAL,"
                                    + " defence REAL,"
                                    + " idFaction integer,"
                                    + " FOREIGN KEY (idFaction) REFERENCES factions(id));");
                                    
        Operations.queryUpdate(conn, "INSERT INTO factions (name, summary) VALUES"
                                    + "('Higashikata', 'La Familia Higashikata es una influyente familia que vive en Morioh y juega un papel importante en JoJolion.'),"
                                    + "('Rokakaka', 'Los Rokakaka Smugglers son contrabandistas que trafican con las frutas Rokakaka, un elemento clave en JoJolion.'),"
                                    + "('Rock Humans', 'Los Rock Humans son una raza de seres humanos con habilidades sobrenaturales y son antagonistas en JoJolion.');");

        Operations.queryUpdate(conn, "INSERT INTO characters (name, atack, defence, idFaction) VALUES"
                                    + "('Josuke H.', 8.5, 9.0, 1),"
                                    + "('Yasuho', 7.0, 7.5, 1),"
                                    + "('Jobin', 9.0, 8.0, 1),"
                                    + "('Aisho', 8.0, 7.5, 2),"
                                    + "('Mitsuko', 7.5, 7.0, 2),"
                                    + "('Tamaki Damo', 9.5, 8.5, 2),"
                                    + "('Doremifasolati', 8.0, 7.0, 3),"
                                    + "('Satoru Akefu', 7.5, 7.5, 3),"
                                    + "('Tooru', 8.5, 8.0, 3);");
    }

    public static void charactersList(Connection conn, String faction){
        String sql = "SELECT id, name FROM characters WHERE idFaction = (SELECT id FROM factions WHERE name =\'"+ faction +"\')";
        ResultSet resultSet = Operations.querySelect(conn, sql);

        try {
            System.out.println("=========================");
            System.out.println(faction + " characters:");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                System.out.println( id + ", " + name);
            }
            System.out.println("=========================");
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bestAttacker(Connection conn, String faction){
        String sql = "SELECT id, name, atack FROM characters WHERE idFaction = (SELECT id FROM factions WHERE name = \'" + faction + "\') ORDER BY atack DESC LIMIT 1";
        ResultSet resultSet = Operations.querySelect(conn, sql);

        try {
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double attack = resultSet.getDouble("atack");
                System.out.println("=========================");
                System.out.println("Best " + faction + " attacker:");
                System.out.println(id + ", " + name + ", Atack: " + attack);
            } else {
                System.out.println("No characters found");
            }
            System.out.println("=========================");
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void bestDefender(Connection conn, String faction){
        String sql = "SELECT id, name, defence FROM characters WHERE idFaction = (SELECT id FROM factions WHERE name = \'" + faction + "\') ORDER BY defence DESC LIMIT 1";
        ResultSet resultSet = Operations.querySelect(conn, sql);

        try {
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                double attack = resultSet.getDouble("defence");
                System.out.println("=========================");
                System.out.println("Best " + faction + " defender:");
                System.out.println(id + ", " + name + ", Defence: " + attack);
            } else {
                System.out.println("No characters found");
            }
            System.out.println("=========================");
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
