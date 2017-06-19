package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogsTypesEnum;

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
        XMLParser cachedWeatherXMLPatser = new XMLParser(parentActivity);
        forecastInstanceArrayList = cachedWeatherXMLPatser.RetrieveWeatherCached(forecastPeriods);

        return forecastInstanceArrayList;
    }

}
