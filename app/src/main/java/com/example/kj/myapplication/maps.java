package com.example.kj.myapplication;

public class maps {

    private double latitude;
    private double longitude;
    private String location;
    public maps(){

    }

    public maps(String location,double latitude, double longitude) {
        this.latitude = latitude;
        this.location=location;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocation() {
        return location;
    }
}
