package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.os.Environment;
import edu.poleaxe.simpleweatherapplication.WeatherCheckActivity;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.customenums.TemperatureDegrees;
import edu.poleaxe.simpleweatherapplication.customenums.UnitMeasurements;
import edu.poleaxe.simpleweatherapplication.dbmanager.DBManager;
import edu.poleaxe.simpleweatherapplication.dbmanager.FileManager;

import java.util.ArrayList;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 18.06.2017.
 */
public class ForecastProcessor implements CallBackInstance {

    private WeatherCheckActivity parentActivity;
    private DBManager dbManager;

    public ForecastProcessor(Activity parentActivity) {
        this.parentActivity = (WeatherCheckActivity) parentActivity;
        dbManager = new DBManager(parentActivity);

    }

    /**
     *
     * @param forecastPeriods
     * @param selectedCity
     * @return
     */
    public void GetWeatherForecastForSelectedCity(ForecastPeriods forecastPeriods, City selectedCity, UnitMeasurements unitMeasurements){
        //+used

        long lastUpdateTime = dbManager.getLastUpdateTimeForCityForPeriod(forecastPeriods, selectedCity, unitMeasurements);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > (1800000)){
            updateCache(forecastPeriods,selectedCity, unitMeasurements);
        }
        else{
            parentActivity.UpdateWeather(dbManager.getCachedForecast(selectedCity,forecastPeriods, unitMeasurements));
        }

       // UpdateWeaterOnUIForSelectedCity(forecastPeriods, selectedCity);
    }

    /**
     *
     * @param forecastPeriods
     * @param selectedCity
     */
    private void updateCache(ForecastPeriods forecastPeriods, City selectedCity, UnitMeasurements unitMeasurements) {


        dbManager.CleanUpCityCache(forecastPeriods, selectedCity, unitMeasurements);

        OpenWeatherMapAPI apiServis = new OpenWeatherMapAPI();
        apiServis.setContext(parentActivity);
        apiServis.setCityToCheck(selectedCity);
        apiServis.setPeriodToCheck(forecastPeriods);
        apiServis.setUnitMeasurements(unitMeasurements);
        apiServis.setCallBackClass(this);
        apiServis.execute();

    }

    /**
     *
     * @param forecastPeriods
     * @param selectedCity
     */
    @Override
    public void UpdateWeaterOnUIForSelectedCity(ForecastPeriods forecastPeriods, City selectedCity, UnitMeasurements unitMeasurements){
        String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testApplication/";
        String fullFileName = "testweather_" + selectedCity.getLocationID() + ".xml";

        ArrayList<ForecastInstance> weatherDataFromXML = new XMLParser(parentActivity).RetrieveWeatherCached(forecastPeriods,selectedCity);
        dbManager.CacheWeatherForecast(selectedCity, forecastPeriods, weatherDataFromXML, unitMeasurements);
        new FileManager().RemoveTMPFile(fileDir, fullFileName);

        parentActivity.UpdateWeather(dbManager.getCachedForecast(selectedCity,forecastPeriods, unitMeasurements));
    }
}
