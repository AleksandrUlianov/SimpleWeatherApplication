package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.WeatherCheckActivity;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.dbmanager.FileManager;
import edu.poleaxe.simpleweatherapplication.support.LogManager;

import java.io.*;
import java.net.*;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class OpenWeatherMapAPI extends AsyncTask<Void, Void, Void>{

    private WeatherCheckActivity parentActivity;
    private CallBackInstance callBackClass;
    private City cityToCheck;
    private ForecastPeriods forecastPeriodToCheck;

    /**
     *
     * @param callBackClass
     */
    public void setCallBackClass(CallBackInstance callBackClass) {
        this.callBackClass = callBackClass;
    }

    /**
     *
     * @param parentActivity
     */
    public void setContext(Activity parentActivity){
        this.parentActivity = (WeatherCheckActivity) parentActivity;

    }

    public void setCityToCheck(City cityToCheck){
        this.cityToCheck = cityToCheck;
    }

    public void setPeriodToCheck(ForecastPeriods forecastPeriod){
        this.forecastPeriodToCheck = forecastPeriod;
    }

    /**
     * check if created connection to URL available
     * @param connectionToCheck
     * @return
     */
    private boolean ServerIsAvailable(HttpURLConnection connectionToCheck){
        try{
            connectionToCheck.setRequestMethod("HEAD");
            return (connectionToCheck.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return false;
        }
    }

    private StringBuilder RetireveDataFromServer(HttpURLConnection connection, String stringURLToConnect) throws IOException {

        StringBuilder result = new StringBuilder();
        BufferedReader bufferedReader;

        try {
            connection.connect();
            bufferedReader = new BufferedReader(new InputStreamReader(new URL(stringURLToConnect).openStream()));

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().equals("")){
                    continue;
                }
                result.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     *
     * @param forecastPeriods
     * @param cityID
     * @return
     */
    private String CreateAPIRequest(ForecastPeriods forecastPeriods, String cityID){

        String apiRequest;

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
     * @throws IOException
     */
    private void RequestWeatherUpdate(ForecastPeriods forecastPeriods) throws IOException {
        String stringURLToConnect;
        if (cityToCheck == null){
            return;
        }

        stringURLToConnect = CreateAPIRequest(forecastPeriods, cityToCheck.getLocationID());

        if (stringURLToConnect.trim().equals("")){
            new LogManager().captureLog(parentActivity.getApplicationContext(), "Empty connection. Nowhere to retrieve from");
            return;
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(stringURLToConnect).openConnection();
        } catch (IOException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return;
        }

        if (connection == null){
            new LogManager().captureLog(parentActivity.getApplicationContext(), "Empty connection. Nowhere to retrieve from");
            return;}

        if (!ServerIsAvailable(connection)){
            new LogManager().captureLog(parentActivity.getApplicationContext(), "Server is not responding. Cannot update forecast");
            return;}

        StringBuilder weatherRequestResult = RetireveDataFromServer(connection, stringURLToConnect);
        String fileDirToStore = Environment.getExternalStorageDirectory().getAbsolutePath() + "/testApplication/";
        String fullFileNameToSave = "testweather_" + cityToCheck.getLocationID() + ".xml";
        FileManager fileManager = new FileManager();
        fileManager.setParentActivity(parentActivity);
        fileManager.SaveStringBuilderToFileByPath(fileDirToStore,fullFileNameToSave,weatherRequestResult);

    }


    @Override
    protected Void doInBackground(Void... voids) {
        try {
            RequestWeatherUpdate(forecastPeriodToCheck);
        } catch (IOException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callBackClass.UpdateWeaterOnUIForSelectedCity(forecastPeriodToCheck, cityToCheck);
    }

}
