package edu.poleaxe.simpleweatherapplication.weatherapi;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import edu.poleaxe.simpleweatherapplication.R;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogManager;
import edu.poleaxe.simpleweatherapplication.support.customdialogmanager.DialogsTypesEnum;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

/**class to process XML retrieved from weather forecast provider and create a set of forecast instances
 * Created by Aleksandr Ulianov (poleaxe) on 18.06.2017.
 */
public class XMLParcer {

        private static final String ns = null;
        private XmlResourceParser xmlResourceParser;
        private Activity parentActivity;

    /**
     * constructor which gaines a parent activity to work with
     * @param parentActivity
     */
    public XMLParcer(Activity parentActivity) throws IllegalArgumentException{
            if (parentActivity == null){
                throw new IllegalArgumentException("Parent activity for XML parcer MUST not be null");
            }
            this.parentActivity = parentActivity;
            Resources res = parentActivity.getResources();
            xmlResourceParser = res.getXml(R.xml.current_weather_example);
        }

    public ArrayList<ForecastInstance> RetrieveForecastEntries() {
        ArrayList<ForecastInstance> forecastInstancesToReturn = new ArrayList<>();

        try {
            //xmlResourceParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            //xmlResourceParser.nextTag();
            xmlResourceParser.next();
            xmlResourceParser.next();
            forecastInstancesToReturn.clear();
            forecastInstancesToReturn.addAll(ProcessMainTag("current"));

        } catch (XmlPullParserException | IOException e) {
            new DialogManager().DisplayDialog(DialogsTypesEnum.ALERT,e.getMessage(),parentActivity);
            return null;
        }

        return forecastInstancesToReturn;
    }

    private ArrayList<ForecastInstance> ProcessMainTag(String tagToStartFrom) throws IOException, XmlPullParserException {

        ArrayList<ForecastInstance> forecastInstancesToReturn = new ArrayList<>();
        xmlResourceParser.require(XmlPullParser.START_TAG,ns,tagToStartFrom);

        String forecastDateTime = "N/A";
        String forecastPhenomena = "N/A";
        String forecastPrecipitation = "N/A";
        String forecastHumidity = "N/A";
        String forecastUVIndex = "N/A";
        String forecastTemperature = "N/A";
        String forecastPressure = "N/A";
        String forecastVisibility = "N/A";

        while (xmlResourceParser.nextTag() != XmlPullParser.END_TAG){
            if (xmlResourceParser.getEventType() != XmlPullParser.START_TAG){
                continue;
            }
            {
                String tagName = xmlResourceParser.getName();
                switch (tagName){
                    case "lastupdate" :
                        forecastDateTime = processDateTime();
                        break;
                    case  "clouds" :
                        forecastPhenomena = "Clouds: " + processPhenomena();
                        break;
                    case "precipitation" :
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
                    case "visibility" :
                        forecastVisibility = processVisibility();
                    default:

                }
                skip();
            }

           // new DialogManager().DisplayDialog(DialogsTypesEnum.TOAST,xmlResourceParser.getName(),parentActivity);

        }
        forecastInstancesToReturn.add(new ForecastInstance(forecastDateTime, forecastPhenomena, forecastPrecipitation,
                forecastHumidity, forecastUVIndex, forecastTemperature, forecastPressure, forecastVisibility));

        return forecastInstancesToReturn;
    }

    private String processVisibility() {
        String valueToReturn = "";

        valueToReturn = "" + xmlResourceParser.getAttributeValue(ns, "value");

        return valueToReturn;
    }

    private String processPressure() {
        String valueToReturn = "";

        valueToReturn = "" + xmlResourceParser.getAttributeValue(ns, "value") + " " + xmlResourceParser.getAttributeValue(ns, "unit");

        return valueToReturn;
    }

    private String processPrecipitation() {
        String valueToReturn = "";

        valueToReturn = "" + xmlResourceParser.getAttributeValue(ns, "mode");

        return valueToReturn;
    }

    private String processPhenomena() {
        String valueToReturn = "";

        valueToReturn = "" + xmlResourceParser.getAttributeValue(ns, "name");

        return valueToReturn;
    }

    private String processDateTime() {
        String valueToReturn = "";

        valueToReturn = "" + xmlResourceParser.getAttributeValue(ns, "value");

        return valueToReturn;
    }

    private String processHumidity() {
        String valueToReturn = "";

        valueToReturn = "" + xmlResourceParser.getAttributeValue(ns, "value") + " " + xmlResourceParser.getAttributeValue(ns, "unit");

        return valueToReturn;

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
}
