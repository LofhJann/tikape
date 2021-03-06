package tikape.runko.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private String databaseAddress;

    public Database(String databaseAddress) throws ClassNotFoundException {
        this.databaseAddress = databaseAddress;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseAddress);
    }

    public void init() {
        List<String> lauseet = sqliteLauseet();

        // "try with resources" sulkee resurssin automaattisesti lopuksi
        try (Connection conn = getConnection()) {
            Statement st = conn.createStatement();

            // suoritetaan komennot
            for (String lause : lauseet) {
                System.out.println("Running command >> " + lause);
                st.executeUpdate(lause);
            }

        } catch (Throwable t) {
            // jos tietokantataulu on jo olemassa, ei komentoja suoriteta
            System.out.println("Error >> " + t.getMessage());
        }
    }

    private List<String> sqliteLauseet() {
        ArrayList<String> lista = new ArrayList<>();

        // tietokantataulujen luomiseen tarvittavat komennot suoritusjärjestyksessä
        lista.add("CREATE TABLE Viestiketju (\n"
                + "ID integer PRIMARY KEY AUTOINCREMENT,\n"
                + "otsikko varchar,\n"
                + "aihealue_id integer\n"
                + ");");

        lista.add("CREATE TABLE Viesti (\n"
                + "ID integer PRIMARY KEY AUTOINCREMENT,\n"
                + "teksti varchar,\n"
                + "aika DATETIME DEFAULT CURRENT_TIMESTAMP,\n"
                + "viestiketju_id integer,\n"
                + "kayttaja_id integer\n"
                + ");");
        
        // salasana cryptataan suolaa hyödyntäen ja tallennetaan cryptattuna tietokantaan
        // salasanan tarkistus toteutetaan hyödyntäen vertailua "Syötettysalasana"+salt = salasana tietokannasta
        // Jos merkkijonot täsmää voidaan olettaa käyttäjän syöttäneen sama salasana (koska hashayksen tulos muuttuu salasanan muuttuessa)
        // Esim. Salasana=qwerty123 ja suola(random generoitu)=a4d niin hash luodaan cryptaamalla merkkijono qwerty123a4d
        
        // Käyttäjän tietokannassa oleva sarake "login" tallentaa 128bit merkkijonon jota käytetään estämään kirjautumisen väärentäminen cookieta muokkaamalla
        lista.add("CREATE TABLE Kayttaja (\n"
                + "ID integer PRIMARY KEY AUTOINCREMENT,\n"
                + "nimimerkki varchar,\n"
                + "salt varchar,\n"
                + "hash varchar,\n"
                + "tyyppi integer,\n"
                + "login varchar\n"
                + ");");

        lista.add("CREATE TABLE Aihealue (\n"
                + "ID integer PRIMARY KEY AUTOINCREMENT,\n"
                + "otsikko varchar\n"
                + ");");
        
        return lista;
    }
}
