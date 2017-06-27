package edu.poleaxe.simpleweatherapplication.dbmanager;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import edu.poleaxe.simpleweatherapplication.WeatherCheckActivity;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.customenums.UnitMeasurements;
import edu.poleaxe.simpleweatherapplication.support.LogManager;
import edu.poleaxe.simpleweatherapplication.weatherapi.City;
import edu.poleaxe.simpleweatherapplication.weatherapi.ForecastInstance;

import java.util.ArrayList;
import java.util.Map;

/** class to work with Db, that consist settings of application, search history, etc.
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class DBManager {
    private static final String databaseName = "SIMPLE_WEATHER_APPLICATION";
    private static final String dbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testApplication/";
    private static final String dbFullName = databaseName + ".sqlite";
    private static final String dbFullPath = dbPath + dbFullName;
    private static SQLiteDatabase settingsDataBase;
    private Activity parentActivity;

    private FileManager fileManager = new FileManager();

    public DBManager(Activity parentActivity){
        this.parentActivity = parentActivity;
    }

    /**
     * method to retrieve a DB version from settings DB
     * @return int version of settings DB. could be 0 if DB is empty and needs to be created
     */
    private int DBVersion() throws SQLException{

        String checkDBExistance = "select count(*) " +
                "from sqlite_master " +
                "where type='table' and name='config';";

        Cursor resultSet = settingsDataBase.rawQueryWithFactory(null,checkDBExistance,null,null,null);

        if (resultSet == null){
            return 0;
        }

        resultSet.moveToFirst();
        String dbExists = resultSet.getString(0);
        resultSet.close();
        if (Integer.valueOf(dbExists) == 0){
            return 0;
        }

        String checkDBConfig = "select value from Config where key = 'dbVersion'";
        resultSet = settingsDataBase.rawQueryWithFactory(null,checkDBConfig,null,null,null);

        if (resultSet == null){
            return 0;
        }

        resultSet.moveToFirst();
        String dbVersionFromDB = resultSet.getString(0);
        resultSet.close();
        if (dbVersionFromDB.equals("")){
            return 0;
        }
        else {
            return Integer.valueOf(dbVersionFromDB);
        }
    }

    /**
     * opens database from existing file in storage.
     * in case DB not exists (file exists but empty) then creates empty DB according to the template
     */
    private boolean PrepareDefaultDB() throws Exception {
        settingsDataBase = SQLiteDatabase.openOrCreateDatabase(dbFullPath, null);
        if (settingsDataBase == null){
            throw new Exception("Unable to create database");
        }
        return DBVersion() == 0 ? CreateEmptyDataBase() : true;
    }

    /**
     * creates a settings database according to the template with default values
     */
    private boolean CreateEmptyDataBase() {

        settingsDataBase.beginTransaction();
        ArrayList<String> emptyDBCreationdump = getDefaultDataBaseDump();
        try {
            for (String dumpLine : emptyDBCreationdump
                    ) {
                settingsDataBase.execSQL(dumpLine);
            }
        }
        catch (SQLException e){
            settingsDataBase.endTransaction();
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return false;
        }

        try{
        settingsDataBase.setTransactionSuccessful();
        }
        catch (IllegalStateException e){
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return false;
        }
        settingsDataBase.endTransaction();

        return true;
    }

    /**
     * prepares a dump for sql db creation according to the template
     * @return ArrayList of sql statements
     */
    private ArrayList<String> getDefaultDataBaseDump() {

        ArrayList<String> listToReturn = new ArrayList<>();
        listToReturn.add("drop table if exists config;");
        listToReturn.add("create table config(key text not null unique, value text);");
        listToReturn.add("insert into config(key, value) values ('dbVersion',1);");
        listToReturn.add("drop table if exists settings;");
        listToReturn.add("create table settings(key text not null unique, value text);");
        listToReturn.add("insert into settings(key, value) values ('lastLocation', null);");
        listToReturn.add("insert into settings(key, value) values ('degreesType', 'CELSIUS');");
        listToReturn.add("insert into settings(key, value) values ('unitsType', 'METRIC');");
        listToReturn.add("insert into settings(key, value) values ('period', '" + ForecastPeriods.NOW.name().toLowerCase() + "');");
        listToReturn.add("drop table if exists PreviouslyBrowsedLocations;");
        listToReturn.add("create table PreviouslyBrowsedLocations(locationID text);");
        listToReturn.add("create table if not exists CityList(locationID text not null unique, locationname text not null, lat text, lon text, countrycode text);");
        listToReturn.add("drop table if exists cacheddata;");
        listToReturn.add("create table cacheddata(locationID text not null, forecastperiod text, unit text, updatetime text not null, forecastDateTime text, forecastPhenomena text," +
                "forecastPrecipitation text, forecastHumidity text, forecastUVIndex text, forecastTemperature text," +
                "forecastPressure text, forecastVisibility text);");
        return listToReturn;
    }

    /**
     * method to prepare available database with settings and search history
     * @return boolean value is database available or not
     * @throws IllegalAccessError in case there are no permissions to work with storages
     * @throws NullPointerException in case it was impossible to instantiate or create a file for DB
     */
    public boolean PrepareDB() throws IllegalAccessError, Exception {

        fileManager.setParentActivity((WeatherCheckActivity) parentActivity);
        fileManager.CheckOrCreateFileByPath(dbPath, dbFullName, false);

        return settingsDataBase == null ? PrepareDefaultDB() : true;
    }

    /**
     *
     * @param key String name of a attribute of settings to get from settings DB
     * @return String value sof requested attribute
     */
    public String getSettingValue(String key){
        //+used
        String getForecastPeriodQuery = "select value from settings where key = \"" + key + "\"";

        Cursor resultsSet = null;
        try {
            resultsSet = settingsDataBase.rawQuery(getForecastPeriodQuery, null);
        }
        catch (SQLException e){
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            if (resultsSet != null) {resultsSet.close();}
            return null;
        }

        if (resultsSet.getCount() == 0) {return null;}
        resultsSet.moveToFirst();

        String resultToReturn = resultsSet.getString(0);
        resultsSet.close();
        return resultToReturn;
    }

    /**
     * method to update all settings
     * @param settingsToSet Map pf keys and values of settings to update
     */
    public void updateAnySettings(Map<String, String> settingsToSet){
    //+used

        for (Map.Entry<String, String> entry : settingsToSet.entrySet())
        {
            UpdateSetting(entry.getKey(), entry.getValue());
        }
    }

    /**
     *
     * @param key String name of setting to update
     * @param value String value to set
     */
    private void UpdateSetting(String key, String value) {
       //+used
        String statementToSet = "update settings set value = \"" + value + "\" where key = \"" + key + "\";";
       try {
           settingsDataBase.execSQL(statementToSet);
       }
       catch (SQLException e){
           new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
       }
    }


    /**
     *
     * @param locationID of type String to store as last searched
     */
    public void addLocationToSearchHistory(String locationID){
        //TODO addLocationToSearchHistory
    }

    /**
     * method to retrieve searching history form data base
     * @return ArrayList with the list of locations that were observed
     */
    public ArrayList<String> getSearchHistory(){
        String queryToRetrieve = "select * from PreviouslyBrowsedLocations";

        Cursor resultsSet = null;
        try {
            resultsSet = settingsDataBase.rawQuery(queryToRetrieve, null);
        }
        catch (SQLException e){
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            if (resultsSet!= null){resultsSet.close();}
            return null;
        }

        if (resultsSet == null || resultsSet.getCount() == 0) {return null;}
        ArrayList<String> searchHistoryList = new ArrayList<>();
        resultsSet.moveToFirst();
        while (!resultsSet.isAfterLast()){

            searchHistoryList.add(resultsSet.getString(0));

            resultsSet.moveToNext();
        }

        resultsSet.close();

        return searchHistoryList;
    }

    /**
     *
     * @return boolean if database with the list of cities exists
     */
    public boolean checkExistanceOfCitiesBase(){
        Cursor resultSet;

        String stringToExecute = "select count(*) from CityList";
        try {
            resultSet = settingsDataBase.rawQuery(stringToExecute, null);
        }
        catch (SQLException e){
            new LogManager().captureLog(parentActivity.getApplicationContext(),e.getMessage());
            return false;
        }
        if (resultSet == null){
            return false;
        }

        resultSet.moveToFirst();
        if (Integer.valueOf(resultSet.getString(0)) > 0) {
            return true;
        }


        resultSet.close();
        return false;
    }


    /**
     *
     * @param citiesToAdd of type ArrayList. List of cities to be added to the database
     */
    public void UpdateCityListDB(ArrayList<String> citiesToAdd){

        settingsDataBase.execSQL("create table if not exists CityList(locationID text not null unique, locationname text not null, lat text, lon text, countrycode text);");
        settingsDataBase.execSQL("delete from CityList;");

        for (String lineToExecute : citiesToAdd
                ) {
            String[] parsedLine = lineToExecute.split("\t");

            String queryToExecute = "insert into CityList(locationID, locationname, lat, lon, countrycode) " +
                    "values (\"" + parsedLine[0] + "\",\"" + parsedLine[1] + "\",\"" + parsedLine[2] + "\",\"" + parsedLine[3] + "\",\"" + parsedLine[4] + "\")";

            settingsDataBase.execSQL(queryToExecute);
        }

    }

    /**
     *
     * @param resultsSet selection of cities that matches with entered data to be selected by end user
     * @return city that is in the current cursor position
     */
    public City cityToFromCursor(Cursor resultsSet){
        if (resultsSet == null) {
            return null;
        }

        return new City(resultsSet.getString(0), resultsSet.getString(1), resultsSet.getString(2), resultsSet.getString(3), resultsSet.getString(4));

    }

    /**
     *
     * @param resultSet selection of forecasts from dcached database
     * @return one instance of forecast to display
     */
    private ForecastInstance ForecastFromCursor(Cursor resultSet){
        if (resultSet == null) {
            return null;
        }

        return new ForecastInstance(resultSet.getString(0),
                resultSet.getString(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4),
                resultSet.getString(5),
                resultSet.getString(6),
                resultSet.getString(7));
    }

    /**
     *
     * @param enteredPartOfName String parameter that city name should match
     * @return cursor as a selection of cities that matches entered parameter
     */
    public Cursor getCursorOverSuggestedCities(String enteredPartOfName){
        Cursor resultsSet;
        try {
            String stringToExecute = "select \n" +
                    "locationID as _id,\n" +
                    "locationname,\n" +
                    "lat,\n" +
                    "lon,\n" +
                    "countrycode from CityList \n" +
                    "where locationname like \"" + enteredPartOfName + "%\" \n" +
                    "order by countrycode;";
            resultsSet = settingsDataBase.rawQuery(stringToExecute, null);
        }
        catch (SQLException e){
            new LogManager().captureLog(parentActivity, e.getMessage());
            return null;
        }
        return resultsSet;
    }

    /**
     *
     * @return a City of type City that had been seen during the last application session
     */
    public City getLastCity() {
        City lastCity;
        String stringToExecute = "select value from settings where key = \"lastLocation\"";
        Cursor resultSet;
        try {
            resultSet = settingsDataBase.rawQuery(stringToExecute, null);
        }
        catch (SQLException e){
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return null;
        }

        if (resultSet == null){
            return  null;
        }

        resultSet.moveToFirst();
        String lastCityID = resultSet.getString(0);
        //resultSet.close();

        if (lastCityID == null || lastCityID.equals("null")){return null;}

        stringToExecute = "select \n" +
                "locationID as _id,\n" +
                "locationname,\n" +
                "lat,\n" +
                "lon,\n" +
                "countrycode from CityList \n" +
                "where locationID = \"" + lastCityID + "\" \n" +
                "order by countrycode;";

        try {
            resultSet = settingsDataBase.rawQuery(stringToExecute, null);
        }
        catch (SQLException e){
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return null;
        }

        if (resultSet == null){
            return  null;
        }
        resultSet.moveToFirst();
        lastCity = cityToFromCursor(resultSet);
        resultSet.close();

        return lastCity;
    }

    public long getLastUpdateTimeForCityForPeriod(ForecastPeriods forecastPeriods, City selectedCity, UnitMeasurements unitMeasurements){
        long lastUpdateTime;

        String stringToExecute = "select updatetime " +
                "from cacheddata " +
                "where locationID = '" + selectedCity.getLocationID() + "' " +
                "and forecastperiod = '" + forecastPeriods.name().toLowerCase() + "'" +
                "and unit = '" + unitMeasurements.name().toLowerCase() + "'" +
                "order by updatetime desc";
        Cursor resultSet = settingsDataBase.rawQuery(stringToExecute,null);
        if (resultSet == null || resultSet.getCount() == 0) {
            return -1;
        }

        resultSet.moveToFirst();

        lastUpdateTime =Long.parseLong(resultSet.getString(0));
        resultSet.close();
        return lastUpdateTime;
    }

    /**
     *method to cache in database data obout forecast for selected city
      * @param selectedCity of type City
     * @param forecastPeriods enum ForecastPeriod
     * @param forecastInstanceListToCache list of instances to cache
     */
    public void CacheWeatherForecast(City selectedCity, ForecastPeriods forecastPeriods, ArrayList<ForecastInstance> forecastInstanceListToCache, UnitMeasurements unitMeasurements){
        if (forecastInstanceListToCache == null){ return;}

        for (ForecastInstance forecastInstance :
                forecastInstanceListToCache) {
            CacheOneForecastInstance(selectedCity, forecastInstance, forecastPeriods, unitMeasurements);
        }
    }

    /**
     *method to put one forecast instance in cache DB
     * @param cityToStore of type City
     * @param forecastInstance of type Forecast instance to be stored
     * @param forecastPeriods enum of forecast period to be stored
     */
    private void CacheOneForecastInstance(City cityToStore, ForecastInstance forecastInstance, ForecastPeriods forecastPeriods, UnitMeasurements unitMeasurements){
        String stringToExecute = "insert " +
                "into cacheddata(locationID" +
                ", forecastperiod" +
                ", unit" +
                ", updatetime" +
                ", forecastDateTime" +
                ", forecastPhenomena" +
                ", forecastPrecipitation" +
                ", forecastHumidity" +
                ", forecastUVIndex" +
                ", forecastTemperature" +
                ", forecastPressure" +
                ", forecastVisibility" +
                ") " +
                "values (" +
                "'" + cityToStore.getLocationID() + "'" +
                ",'" + forecastPeriods.name().toLowerCase() + "'" +
                ",'" + unitMeasurements.name().toLowerCase() + "'" +
                ",'" + String.valueOf(System.currentTimeMillis()) + "'" +
                ",'" + forecastInstance.getForecastDateTime() + "'" +
                ",'" + forecastInstance.getForecastPhenomena() + "'" +
                ",'" + forecastInstance.getForecastPrecipitation() + "'" +
                ",'" + forecastInstance.getForecastHumidity() + "'" +
                ",'" + forecastInstance.getForecastUVIndex() + "'" +
                ",'" + forecastInstance.getForecastTemperature() + "'" +
                ",'" + forecastInstance.getForecastPressure() + "'" +
                ",'" + forecastInstance.getForecastVisibility() + "'" +
                ")";

        settingsDataBase.execSQL(stringToExecute);

    }

    /**
     *method that cleans up cache for one selected cit for defined period of forecast
     * @param forecastPeriods enum of period to be cleaned
     * @param cityToClean of type City to be cleaned
     */
    public void CleanUpCityCache(ForecastPeriods forecastPeriods,City cityToClean, UnitMeasurements unitMeasurements){
        String stringToExecute = "delete " +
                "from cacheddata " +
                "where locationID = '" + cityToClean.getLocationID() + "' " +
                "and forecastperiod = '" + forecastPeriods.name().toLowerCase() + "' " +
                "and unit = '" + unitMeasurements.name().toLowerCase() + "' ";
        settingsDataBase.execSQL(stringToExecute);
    }

    /**
     *method to get cached data for
     * @param selectedCity of type City
     * @param forecastPeriods enum of available forecast periods
     * @return Array list of forecast instances to be displayed
     */
    public ArrayList<ForecastInstance> getCachedForecast(City selectedCity, ForecastPeriods forecastPeriods, UnitMeasurements unitMeasurements){
        ArrayList<ForecastInstance> forecastInstanceListToReturn = new ArrayList<>();
        String stringToExecute = "select " +
                " forecastDateTime" +
                ", forecastPhenomena" +
                ", forecastPrecipitation" +
                ", forecastHumidity" +
                ", forecastUVIndex" +
                ", forecastTemperature" +
                ", forecastPressure" +
                ", forecastVisibility " +
                "from cacheddata " +
                "where locationID = '" + selectedCity.getLocationID() + "' " +
                "and forecastperiod = '" + forecastPeriods.name().toLowerCase() + "' " +
                "and unit = '" + unitMeasurements.name().toLowerCase() + "' " +
                "order by forecastDateTime asc";
        Cursor resultSet = settingsDataBase.rawQuery(stringToExecute, null);
        if (resultSet == null) {
            return null;
        }

        resultSet.moveToFirst();
        while (!resultSet.isAfterLast()){
            forecastInstanceListToReturn.add(ForecastFromCursor(resultSet));
            resultSet.moveToNext();
        }

        resultSet.close();
        return forecastInstanceListToReturn;
    }

}
