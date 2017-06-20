package edu.poleaxe.simpleweatherapplication.dbmanager;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import edu.poleaxe.simpleweatherapplication.customenums.TemperatureDegrees;
import edu.poleaxe.simpleweatherapplication.customenums.UnitMeasurements;
import edu.poleaxe.simpleweatherapplication.support.LogManager;

import java.io.File;
import java.lang.reflect.Array;
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

    /**
     * method to retrieve a DB version from settings DB
     * @return int version of settings DB. could be 0 if DB is empty and needs to be created
     */
    private int DBVersion(){

        String checkDBConfig = "Select value from Config where key = 'dbVersion'";
        Cursor resultSet;
        try{
            resultSet = settingsDataBase.rawQueryWithFactory(null,checkDBConfig,null,null,null);
        }
        catch (SQLException e){
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
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
    private boolean PrepareSettingsDB(){
        settingsDataBase = SQLiteDatabase.openOrCreateDatabase(dbFullPath, null);
        return DBVersion() == 0 ? CreateEmptyDataBase() : true;
    }

    /**
     * creates a settings database according to the template with default values
     */
    private boolean CreateEmptyDataBase() {
        if (settingsDataBase == null){
            PrepareSettingsDB();
        }

        if (settingsDataBase == null){
            new LogManager().captureLog(parentActivity.getApplicationContext(), "Unable to connect to settings Database");
            return false;
        }

        settingsDataBase.beginTransaction();
        ArrayList<String> emptyDBCreationdump = getXMLAsArrayList();
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
    private ArrayList<String> getXMLAsArrayList() {

        ArrayList<String> listToReturn = new ArrayList<>();
        listToReturn.add("drop table if exists config;");
        listToReturn.add("create table config(key text not null unique, value text);");
        listToReturn.add("insert into config(key, value) values ('dbVersion',1);");
        listToReturn.add("drop table if exists settings;");
        listToReturn.add("create table settings(key text not null unique, value text);");
        listToReturn.add("insert into settings(key, value) values ('lastLocation', null);");
        listToReturn.add("insert into settings(key, value) values ('degreesType', 'CELSIUS');");
        listToReturn.add("insert into settings(key, value) values ('unitsType', 'METRIC');");
        listToReturn.add("insert into settings(key, value) values ('period', 'NOW');");
        listToReturn.add("drop table if exists PreviouslyBrowsedLocations;");
        listToReturn.add("create table PreviouslyBrowsedLocations(locationID text);");
        listToReturn.add("create table CityList(locationID text not null unique, locationname text not null, lat text, lon text, countrycode text);");
        return listToReturn;
    }

    /**
     * method to prepare available database with settings and search history
     * @param parentActivity Activity where from this method was initiated
     * @return boolean value is database available or not
     * @throws IllegalAccessError in case there are no permissions to work with storages
     * @throws NullPointerException in case it was impossible to instantiate or create a file for DB
     */
    public boolean PrepareAvailableSettingsDB(Activity parentActivity) throws IllegalAccessError, NullPointerException{
        this.parentActivity = parentActivity;

        fileManager.CheckOrCreateFileByPath(dbPath, dbFullName, this.parentActivity);

        return settingsDataBase == null ? PrepareSettingsDB() : true;
    }

    public boolean DBExists(){

        return (new File(dbPath)).exists();
    }

    public String getDBPath(){
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /**
     *
     * @param key String name of a attribute of settings to get from settings DB
     * @return String value sof requested attribute
     */
    public String getSettingValue(String key){
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
    public void updateAllSettings(Map<String, String> settingsToSet){

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
    public void UpdateSetting(String key, String value) {
       String statementToSet = "update settings set value = \"" + value + "\" where key = \"" + key + "\";";
       try {
           settingsDataBase.execSQL(statementToSet);
       }
       catch (SQLException e){
           if (parentActivity != null) {
               new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
           }
       }
    }



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


    public void UpdateCityListDB(ArrayList<String> citiesToAdd){

        for (String lineToExecute : citiesToAdd
                ) {
            String[] parsedLine = lineToExecute.split("\t");

            String queryToExecute = "insert into CityList(locationID, locationname, lat, lon, countrycode) " +
                    "values (\"" + parsedLine[0] + "\",\"" + parsedLine[1] + "\",\"" + parsedLine[2] + "\",\"" + parsedLine[3] + "\",\"" + parsedLine[4] + "\")";

            settingsDataBase.execSQL(queryToExecute);
        }


    }
}
