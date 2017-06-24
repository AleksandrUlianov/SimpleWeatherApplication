package edu.poleaxe.simpleweatherapplication.weatherapi;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 20.06.2017.
 */
public class City {
    private String locationID           = "";
    private String locationName         = "";
    private String locationLat          = "";
    private String locationLon          = "";
    private String locationCountryCode  = "";

    public City(String locationID, String locationName, String locationLat, String locationLon, String locationCountryCode){
        this.locationID             = locationID;
        this.locationName           = locationName;
        this.locationLat            = locationLat;
        this.locationLon            = locationLon;
        this.locationCountryCode    = locationCountryCode;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLocationCountryCode() {
        return locationCountryCode;
    }

    public String getLocationID() {
        return locationID;
    }
}
