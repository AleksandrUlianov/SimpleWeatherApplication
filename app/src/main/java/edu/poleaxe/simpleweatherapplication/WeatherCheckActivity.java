package edu.poleaxe.simpleweatherapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.customenums.TemperatureDegrees;
import edu.poleaxe.simpleweatherapplication.customenums.UnitMeasurements;
import edu.poleaxe.simpleweatherapplication.dbmanager.DBManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogsTypesEnum;
import edu.poleaxe.simpleweatherapplication.support.internetconnection.InternetConnectionException;
import edu.poleaxe.simpleweatherapplication.support.internetconnection.InternetConnectionManager;
import edu.poleaxe.simpleweatherapplication.visualcomponents.WeatherEntryAdapter;
import edu.poleaxe.simpleweatherapplication.weatherapi.ForecastInstance;
import edu.poleaxe.simpleweatherapplication.weatherapi.ForecastProcessor;
import edu.poleaxe.simpleweatherapplication.weatherapi.OpenWeatherMapAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * main Activity yof an app. contains:
 * - search bar with autocomplete and search history
 * - settings button to define units for measurement, temperature degrees unit
 * - update button
 * - period to get weather forecast
 * - weather widgets
 */
public class WeatherCheckActivity extends AppCompatActivity {

    private DialogManager dialogManager = new DialogManager();
    private DBManager dbManager = new DBManager();
    private ForecastProcessor forecastProcessor;

    private ArrayList<ForecastInstance> forecastListToDisplay = new ArrayList<>();
    private WeatherEntryAdapter weatherEntryAdapter;

    private static OpenWeatherMapAPI openWeatherMapAPI = new OpenWeatherMapAPI();

    private static TemperatureDegrees   temperatureDegrees  = TemperatureDegrees.CELSIUS;
    private static UnitMeasurements     unitMeasurements    = UnitMeasurements.METRIC;
    private static ForecastPeriods      forecastPeriod      = ForecastPeriods.NOW;

    boolean openedFirstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_check);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UpdateWeather();
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        }
        );

        weatherEntryAdapter = new WeatherEntryAdapter(this, forecastListToDisplay);
        ListView forecastListView = (ListView) findViewById(R.id.lvForecastList);
        forecastListView.setAdapter(weatherEntryAdapter);

        RadioGroup rg = (RadioGroup) findViewById(R.id.rgForecastPeriod);
        rg.check(R.id.rbPeriodDays1);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (rb.isChecked()){
                    forecastPeriod = ForecastPeriods.valueOf(rb.getTag().toString());
                    ProcessCheckedRB();
                    UpdateWeather();
                }
            }
        });

    }

    @Override
    protected void onStart(){
        super.onStart();
        if (openedFirstTime) {
            SetUpApplicationConditions();
            openedFirstTime = false;
        }
    }

    private void ProcessCheckedRB() {

        Map settingsToChange = new HashMap<String, String>();
        settingsToChange.put("forecastPeriod",forecastPeriod.name());
        dbManager.updateAllSettings(settingsToChange);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather_check, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *
     * @return
     */
    boolean CheckConnections(){

        try {
            if ((new InternetConnectionManager()).IsConnectionEstablished(this)){
                dialogManager.DisplayDialog(DialogsTypesEnum.TOAST, "Network is active", this);
                return true;
            }
            else {
                dialogManager.DisplayDialog(DialogsTypesEnum.TOAST, "Network is INactive", this);
                return false;
            }
        } catch (InternetConnectionException e) {
            new DialogManager().DisplayDialog(DialogsTypesEnum.ALERT, e.getMessage(), this);
            return false;
        }
    }

    void SetUpApplicationConditions(){

        //Check availability of settings Db and apply settings if it is possible. Use default if not.
        forecastProcessor = new ForecastProcessor(this);
        boolean settingsDBAvailable;
            try {
                settingsDBAvailable = dbManager.PrepareAvailableSettingsDB(this);
            }
            catch (IllegalAccessError|NullPointerException e){
                dialogManager.DisplayDialog(DialogsTypesEnum.TOAST,"Unable to reach saved settigns. Default will be used", this);
                return;
            }

            dialogManager.DisplayDialog(DialogsTypesEnum.TOAST, settingsDBAvailable ? "Settings DB available" : "Settings DB not available", this);

            if (settingsDBAvailable){
                ApplySettingsFromDB();
                openWeatherMapAPI.setContext(this);
                openWeatherMapAPI.execute();
            }


    }

    private void ApplySettingsFromDB() {
        String parameterValue;
        parameterValue      = dbManager.getSettingValue("degreesType");
        temperatureDegrees  = parameterValue == null ? temperatureDegrees : TemperatureDegrees.valueOf(parameterValue);
        parameterValue      = dbManager.getSettingValue("unitsType");
        unitMeasurements    = parameterValue == null ? unitMeasurements : UnitMeasurements.valueOf(parameterValue);
        parameterValue      = dbManager.getSettingValue("period");
        forecastPeriod      = parameterValue == null ? forecastPeriod : ForecastPeriods.valueOf(parameterValue);

//        for (int i = 1; i < 15; i++){
//            String smthToAdd = String.valueOf(i);
//            forecastListToDisplay.add(new ForecastInstance(smthToAdd,smthToAdd,smthToAdd,smthToAdd,smthToAdd,smthToAdd,smthToAdd,smthToAdd));
//        }


    }

    private void UpdateWeather(){


//        ArrayList<ForecastInstance> tmpForecastList;
//        tmpForecastList = forecastProcessor.RetrieveWeather(forecastPeriod);
//        if (tmpForecastList != null) {
//            forecastListToDisplay.clear();
//            forecastListToDisplay.addAll(tmpForecastList);
//            weatherEntryAdapter.notifyDataSetChanged();
//        }
    }


    public void callBackToUI(String messageToDisplay){
        new DialogManager().DisplayDialog(DialogsTypesEnum.TOAST, messageToDisplay, this);
    }
}
