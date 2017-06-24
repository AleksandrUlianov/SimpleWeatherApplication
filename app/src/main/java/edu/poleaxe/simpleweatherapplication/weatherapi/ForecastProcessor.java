package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.os.Environment;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.dbmanager.FileManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogsTypesEnum;
import edu.poleaxe.simpleweatherapplication.visualcomponents.City;

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
        forecastInstanceArrayList = cachedWeatherXMLParser.RetrieveWeatherCached(forecastPeriods);

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

    public String CreateAPIRequest(ForecastPeriods forecastPeriods, String cityID){

        String apiRequest = "";

        switch (forecastPeriods){
            case DAYS5:
                apiRequest = parentActivity.getResources().getString(R.string.weatherForecastAPI);
                break;
            default:
                apiRequest = parentActivity.getResources().getString(R.string.weatherNowAPI);
        }

        apiRequest = apiRequest + "id=" + cityID + "&type=accurate&mode=xml" + "&APPID=" + parentActivity.getResources().getString(R.string.APIkey);
        //apiRequest = apiRequest + "id=498677&type=accurate&mode=xml" + "&APPID=" + parentActivity.getResources().getString(R.string.APIkey);

        return apiRequest;
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
}
