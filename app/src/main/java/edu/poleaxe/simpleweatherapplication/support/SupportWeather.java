package edu.poleaxe.simpleweatherapplication.support;

import java.sql.Time;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 22.06.2017.
 */
public class SupportWeather {

    public String getCurrentTime(){
        return String.valueOf(System.currentTimeMillis());
    }
}
