package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.util.Xml;
import edu.poleaxe.simpleweatherapplication.customenums.ForecastPeriods;
import edu.poleaxe.simpleweatherapplication.dbmanager.FileManager;
import edu.poleaxe.simpleweatherapplication.support.LogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;
import edu.poleaxe.simpleweatherapplication.customenums.DialogsTypesEnum;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**class to process XML retrieved from weather forecast provider and create a set of forecast instances
 * Created by Aleksandr Ulianov (poleaxe) on 18.06.2017.
 */
public class XMLParser {

        private static final String ns = null;
        //private XmlResourceParser xmlResourceParser;
        private XmlPullParser xmlResourceParser = Xml.newPullParser();
        private Activity parentActivity;

    /**
     * constructor which gaines a parent activity to work with
     * @param parentActivity
     */
    public XMLParser(Activity parentActivity) throws IllegalArgumentException{
            if (parentActivity == null){
                throw new IllegalArgumentException("Parent activity for XML parcer MUST not be null");
            }
            this.parentActivity = parentActivity;
            Resources res = parentActivity.getResources();
            //xmlResourceParser = res.getXml(R.xml.current_weather);
    }

    /**
     * retrieves data about weather current or forecast according to incoming parameters
     * @param forecastPeriods period for which its necessary to retireve data
     * @return
     */
    public ArrayList<ForecastInstance> RetrieveWeatherCached(ForecastPeriods forecastPeriods, City selectedCity) {
        ArrayList<ForecastInstance> forecastInstancesToReturn = new ArrayList<>();

        String fileDir= Environment.getExternalStorageDirectory().getAbsolutePath() + "/testApplication/";
        String fullFileName = "testweather_" + selectedCity.getLocationID() + ".xml";

        Resources res = parentActivity.getResources();

        String toParse = new FileManager().PrepareXMLFileToParse(fileDir,fullFileName);

        if (toParse.trim().equals("")) {
            return forecastInstancesToReturn;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                ByteArrayInputStream testByteInputStream = new ByteArrayInputStream(toParse.getBytes(StandardCharsets.UTF_8));
                xmlResourceParser.setInput(testByteInputStream, "UTF-8");
            } catch (XmlPullParserException e) {
                new DialogManager().DisplayDialog(DialogsTypesEnum.ALERT,"XMLParser: " + e.getMessage(),parentActivity);
                return null;
            }
        }

        try {
            xmlResourceParser.next();
            xmlResourceParser.next();

            forecastInstancesToReturn.addAll(forecastPeriods == ForecastPeriods.NOW ? ProcessCurrentWeather(): ProcessForecastWeatherCached());

        } catch (XmlPullParserException | IOException e) {
            new LogManager().captureLog(parentActivity, e.getMessage());
            //new DialogManager().DisplayDialog(DialogsTypesEnum.ALERT,"Unfortunately an internal error happened! Forecast could not be shown",parentActivity);
            new DialogManager().DisplayDialog(DialogsTypesEnum.ALERT,"XMLParser: " + e.getMessage(),parentActivity);
            return null;
        }

        return forecastInstancesToReturn;
    }

    /**
     * retrieves data about current weather from xml file in resources (probably cached data)
     * @return ArrayList of Forecast instances about current weather
     * @throws IOException
     * @throws XmlPullParserException
     */
    private ArrayList<ForecastInstance> ProcessCurrentWeather() throws IOException, XmlPullParserException {

        ArrayList<ForecastInstance> forecastInstancesToReturn = new ArrayList<>();
        //xmlResourceParser.require(XmlPullParser.START_TAG,ns,"current");

        String forecastDateTime = "N/A";
        String forecastPhenomena = "N/A";
        String forecastPrecipitation = "N/A";
        String forecastHumidity = "N/A";
        String forecastUVIndex = "N/A";
        String forecastTemperature = "N/A";
        String forecastPressure = "N/A";
        String forecastVisibility = "N/A";


        boolean repeatGet = true;
        while (repeatGet){
            int eventType = xmlResourceParser.nextTag();
            String eventName = xmlResourceParser.getName();

            if (eventType == XmlPullParser.END_TAG & eventName.equals("current")){
                repeatGet = false;
            }else {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (eventName) {
                        case "lastupdate":
                            forecastDateTime = processDateTime();
                            break;
                        case "clouds":
                            forecastPhenomena = "Clouds: " + processPhenomena();
                            break;
                        case "precipitation":
                            forecastPrecipitation = "Precipitation: " + processPrecipitation();
                            break;
                        case "humidity":
                            forecastHumidity = "Humidity: " + processHumidity();
                            break;
                        case "temperature":
                            forecastTemperature = processTemperature();
                            break;
                        case "pressure":
                            forecastPressure = processPressure();
                            break;
                        case "visibility":
                            forecastVisibility = processVisibility();
                        default:
                            skip();
                    }
                }
            }
        }
        forecastInstancesToReturn.add(new ForecastInstance(forecastDateTime, forecastPhenomena, forecastPrecipitation,
                forecastHumidity, forecastUVIndex, forecastTemperature, forecastPressure, forecastVisibility));

        return forecastInstancesToReturn;
    }

    private String processVisibility() {
        return "" + xmlResourceParser.getAttributeValue(ns, "value");
    }

    private String processPressure() {
        return "" + xmlResourceParser.getAttributeValue(ns, "value") + " " + xmlResourceParser.getAttributeValue(ns, "unit");
    }

    private String processPrecipitation() {
        return "" + xmlResourceParser.getAttributeValue(ns, "mode");
    }

    private String processPhenomena() {
        return "" + xmlResourceParser.getAttributeValue(ns, "name");
    }

    private String processDateTime() {
        return "" + xmlResourceParser.getAttributeValue(ns, "value");
    }

    private String processHumidity() {
        return "" + xmlResourceParser.getAttributeValue(ns, "value") + " " + xmlResourceParser.getAttributeValue(ns, "unit");
    }

    private String processTemperature() {
        String valueToReturn = "";
        String value = xmlResourceParser.getAttributeValue(ns, "value");
        String unit = xmlResourceParser.getAttributeValue(ns, "unit");
        valueToReturn = "" + value + " " + (unit.equals("fahrenheit") ? "F" : "C");

        return valueToReturn;
    }

    private void processCity(){


    }

    private void skip() throws XmlPullParserException, IOException {
        if (xmlResourceParser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (xmlResourceParser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }


    public ArrayList<ForecastInstance> ProcessForecastWeatherCached() throws IOException, XmlPullParserException {
        ArrayList<ForecastInstance> forecastInstancesToReturn = new ArrayList<>();

        xmlResourceParser.require(XmlPullParser.START_TAG,ns,"location");

        boolean repeatGet = true;

        while (repeatGet){
            int eventType = xmlResourceParser.nextTag();
            String eventName = xmlResourceParser.getName();
            if (eventType == XmlPullParser.END_TAG & eventName.equals("weatherdata")){
                repeatGet = false;
                continue;
            }

            if (eventType == XmlPullParser.START_TAG){
                switch (eventName){
                    case "forecast" :

                        break;
                    case "time" :
                        forecastInstancesToReturn.add(RertrieveOneForecastInstance());
                        break;
                    default:
                        skip();
                }
            }
        }

        return forecastInstancesToReturn;
    }

    private ForecastInstance RertrieveOneForecastInstance() throws IOException, XmlPullParserException {
        //xmlResourceParser.next();
        xmlResourceParser.require(XmlPullParser.START_TAG,ns,"time");

        String forecastDateTime = "N/A";
        String forecastPhenomena = "N/A";
        String forecastPrecipitation = "N/A";
        String forecastHumidity = "N/A";
        String forecastUVIndex = "N/A";
        String forecastTemperature = "N/A";
        String forecastPressure = "N/A";
        String forecastVisibility = "N/A";

        forecastDateTime = processDateTimeForecast();


        boolean repeatGet = true;

        while (repeatGet){
            int eventType = xmlResourceParser.nextTag();
            String eventName = xmlResourceParser.getName();

            if (eventType == XmlPullParser.END_TAG & eventName.equals("time")){
                repeatGet = false;
            }else {
                if (eventType == XmlPullParser.START_TAG){
                    switch (eventName) {

                        case "symbol":
                            forecastPhenomena = "Clouds: " + processPhenomenaForecast();
                            break;
                        case "precipitation":
                            forecastPrecipitation = "Precipitation: " + processPrecipitationForecast();
                            break;
                        case "humidity":
                            forecastHumidity = "Humidity: " + processHumidity();
                            break;
                        case "temperature":
                            forecastTemperature = processTemperature();
                            break;
                        case "pressure":
                            forecastPressure = processPressure();
                            break;
                    }
                }
            }

        }

        return new ForecastInstance(forecastDateTime, forecastPhenomena, forecastPrecipitation,
                forecastHumidity, forecastUVIndex, forecastTemperature, forecastPressure, forecastVisibility);
    }

    private String processPrecipitationForecast() {
        return "" + xmlResourceParser.getAttributeValue(ns, "value");
    }

    private String processPhenomenaForecast() {
        return "" + xmlResourceParser.getAttributeValue(ns, "name");
    }

    private String processDateTimeForecast() {
        return "From: " + xmlResourceParser.getAttributeValue(ns, "from") + " to: " + xmlResourceParser.getAttributeValue(ns, "to");
    }
}
