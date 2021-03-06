package edu.poleaxe.simpleweatherapplication.weatherapi;

import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.customenums.UnitMeasurements;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 24.06.2017.
 */
public interface CallBackInstance {
    void UpdateWeaterOnUIForSelectedCity(ForecastPeriods forecastPeriods, City selectedCity, UnitMeasurements unitMeasurements);
}
