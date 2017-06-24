package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Environment;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.WeatherCheckActivity;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.dbmanager.DBManager;
import edu.poleaxe.simpleweatherapplication.support.LogManager;
import edu.poleaxe.simpleweatherapplication.visualcomponents.City;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 14.06.2017.
 */
public class OpenWeatherMapAPI extends AsyncTask<Void, Void, Void>{

    private WeatherCheckActivity parentActivity;
    private City cityToCheck;

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

    /**
     * Retrieves list of cities from the webserver
     * @return ArrayList of Strings (each string - one city with its attributes) to be added to DB
     * @throws IOException
     * @throws MalformedURLException
     */
    private ArrayList<String> getCityList() throws IOException {
        ArrayList<String> listOfCitiesToAddToDB = new ArrayList<>();
        String cityLinkURLString = parentActivity.getResources().getString(R.string.citi_list);
        URL cityListURL = new URL(cityLinkURLString);
        HttpURLConnection connection = (HttpURLConnection) cityListURL.openConnection();
        connection.connect();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new MalformedURLException("Server returned HTTP " + connection.getResponseCode()
                    + " " + connection.getResponseMessage());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(cityListURL.openStream()));
        String str;
        in.readLine();
        while ((str = in.readLine()) != null) {
            listOfCitiesToAddToDB.add(str);
        }
        in.close();
        connection.disconnect();

        return listOfCitiesToAddToDB;
    }

    /**
     *
     */
    public void UpdateCitiesDB(){
       //TODO check permissions and connection
        ArrayList<String> citiesToAdd = null;
        try {
            citiesToAdd = getCityList();
        } catch (IOException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
        }

        if (citiesToAdd == null){
            //TODO parentActivity.callBackToUI("An error occured while updating database of cities");
            return;
        }

        new DBManager().UpdateCityListDB(citiesToAdd);

    }


    /**
     * check if created connection to URL available
     * @param connectionToCheck
     * @return
     */
    public boolean ServerIsAvailable(HttpURLConnection connectionToCheck){
        try{
            connectionToCheck.setRequestMethod("HEAD");
            return (connectionToCheck.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (IOException e) {
            new LogManager().captureLog(parentActivity.getApplicationContext(), e.getMessage());
            return false;
        }
    }

    public void RetireveDataFromServer(HttpURLConnection connection, String stringURLToConnect){

        StringBuilder result = new StringBuilder();
        InputStream inputStream;
        BufferedReader bufferedReader;

        try {
            connection.connect();
            inputStream = new BufferedInputStream(connection.getInputStream());
            if (inputStream != null){
                //bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                bufferedReader = new BufferedReader(new InputStreamReader(new URL(stringURLToConnect).openStream()));

                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }

                inputStream.close();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/testApplication/", "testweather_" + cityToCheck.getLocationID() + ".xml");

        FileOutputStream fileOutput = null;
        fileOutput = new FileOutputStream(file);
        OutputStreamWriter outputStreamWriter=new OutputStreamWriter(fileOutput);
        outputStreamWriter.write(result.toString());
        outputStreamWriter.flush();
        fileOutput.getFD().sync();
        outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void RequestWeatherUpdate(ForecastPeriods forecastPeriods){
        String stringURLToConnect;
        if (cityToCheck == null){
            return;
        }

        stringURLToConnect = new ForecastProcessor(parentActivity).CreateAPIRequest(forecastPeriods, cityToCheck.getLocationID());

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

        RetireveDataFromServer(connection, stringURLToConnect);

        }


    @Override
    protected Void doInBackground(Void... voids) {
        //UpdateCitiesDB();
        RequestWeatherUpdate(ForecastPeriods.NOW);
        return null;
    }
}
