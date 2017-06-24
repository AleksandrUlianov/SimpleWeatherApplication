package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.os.Environment;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.dbmanager.DBManager;
import edu.poleaxe.simpleweatherapplication.dbmanager.FileManager;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 18.06.2017.
 */
public class ForecastProcessor {

    private Activity parentActivity;

    public ForecastProcessor(Activity parentActivity) {
        this.parentActivity = parentActivity;

    }

    /**
     * retrieve Forecast instances for selected period
     * @param forecastPeriods
     * @return ArrayList of Forecast instances
     */
    public ArrayList<ForecastInstance> RetrieveWeather(ForecastPeriods forecastPeriods){

        ArrayList<ForecastInstance> forecastInstanceArrayList;

        //TODO check last update and retrieve weather from API
        XMLParser cachedWeatherXMLParser = new XMLParser(parentActivity);
        forecastInstanceArrayList = cachedWeatherXMLParser.RetrieveWeatherCached(forecastPeriods,);

        return forecastInstanceArrayList;
    }

    /**
     *
     * @param forecastPeriods
     * @param checkOnly
     * @param cityToCache
     * @return
     */
    private File CheckCachedData(ForecastPeriods forecastPeriods, boolean checkOnly, City cityToCache){
        //1. Check if cached file exists. return if File if exists : null. according to forecastPeriods, checkAndCreate mode
        //2. Create file if not exists
        //* fileName get from xml resources
        String weatherCachedFileName;
        switch (forecastPeriods){
            case DAYS5:
                weatherCachedFileName = parentActivity.getResources().getString(R.string.weatherDAYS5file);
                break;
            default:
                weatherCachedFileName = parentActivity.getResources().getString(R.string.weatherNOWfile);
        }
        String fullPathName = parentActivity.getCacheDir() + "//" + parentActivity.getResources().getString(R.string.cachedDataPath) +
                cityToCache.getLocationID();

        return (new FileManager()).CheckOrCreateFileByPath(fullPathName,weatherCachedFileName,parentActivity, checkOnly);
    }

    /**
     *
     * @param forecastPeriods
     */
    private void clearCachedData(ForecastPeriods forecastPeriods, City cityToCache){

    }

    /**
     *
     * @param forecastPeriods
     * @param stringToAdd
     * @return
     */
    public boolean appendCachedDataWithRecord(ForecastPeriods forecastPeriods, String stringToAdd, City cityToCache){
        File fileToProcess = CheckCachedData(forecastPeriods, true, cityToCache);

        return true;
    }

    public ArrayList<ForecastInstance> GetWeatherForecastForSelectedCity(ForecastPeriods forecastPeriods, City selectedCity){
        //+used
        ArrayList<ForecastInstance> forecastListToReturn = null;
        DBManager dbManager = new DBManager();

        long lastUpdateTime = dbManager.getLastUpdateTimeForCityForPeriod(forecastPeriods, selectedCity);
        //if no cached data then get update from serve
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > (1800000)){
            updateCache(forecastPeriods, selectedCity, dbManager);
        }

        forecastListToReturn = dbManager.getCachedForecast(selectedCity,forecastPeriods);
        return  forecastListToReturn;
    }

    private void updateCache(ForecastPeriods forecastPeriods, City selectedCity, DBManager dbManager) {
        dbManager.CleanUpCityCache(forecastPeriods, selectedCity);
        OpenWeatherMapAPI apiServis = new OpenWeatherMapAPI();
        apiServis.setCityToCheck(selectedCity);
        apiServis.setContext(parentActivity);
        apiServis.execute();
        //TODO how to check when AsyncTask finished its work?
        ArrayList<ForecastInstance> weatherDataFromXML = new XMLParser(parentActivity).RetrieveWeatherCached(forecastPeriods,selectedCity);
        dbManager.CacheWeatherForecast(selectedCity, forecastPeriods, weatherDataFromXML);


    }
}
