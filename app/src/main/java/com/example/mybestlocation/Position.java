package com.example.mybestlocation;

public class Position {
    public int id;
    public String pseudo;
    public String longitude;
    public String latitude;
    public String numero;

    public Position( int id,String pseudo, String longitude, String latitude, String numero) {
        this.pseudo = pseudo;
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
    }
    public Position(String pseudo, String longitude, String latitude, String numero) {
        this.pseudo = pseudo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", pseudo='" + pseudo + '\'' +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }
}
