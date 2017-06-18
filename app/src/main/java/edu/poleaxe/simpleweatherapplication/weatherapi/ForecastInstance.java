package edu.poleaxe.simpleweatherapplication.weatherapi;

/**
 * Created by Aleksandr Ulianov (poleaxe) on 16.06.2017.
 */
public class ForecastInstance {

    private String forecastDateTime = "N/A";
    private String forecastPhenomena = "N/A";
    private String forecastPrecipitation = "N/A";
    private String forecastHumidity = "N/A";
    private String forecastUVIndex = "N/A";
    private String forecastTemperature = "N/A";
    private String forecastPressure = "N/A";
    private String forecastVisibility = "N/A";

    /**
     *
     * @param forecastDateTime
     * @param forecastPhenomena
     * @param forecastPrecipitation
     * @param forecastHumidity
     * @param forecastUVIndex
     * @param forecastTemperature
     * @param forecastPressure
     * @param forecastVisibility
     */
    public ForecastInstance(String forecastDateTime, String forecastPhenomena, String forecastPrecipitation,
                            String forecastHumidity, String forecastUVIndex, String forecastTemperature, String forecastPressure,
                            String forecastVisibility){
        this.forecastDateTime       = forecastDateTime == null ? "N/A" : forecastDateTime;
        this.forecastPhenomena      = forecastPhenomena == null ? "N/A" : forecastPhenomena;
        this.forecastPrecipitation  = forecastPrecipitation == null ? "N/A" : forecastPrecipitation;
        this.forecastHumidity       = forecastHumidity == null ? "N/A" : forecastHumidity;
        this.forecastUVIndex        = forecastUVIndex == null ? "N/A" : forecastUVIndex;
        this.forecastTemperature    = forecastTemperature == null ? "N/A" : forecastTemperature;
        this.forecastPressure       = forecastPressure == null ? "N/A" : forecastPressure;
        this.forecastVisibility     = forecastVisibility == null ? "N/A" : forecastVisibility;

    }


    public String getForecastDateTime() {
        return forecastDateTime;
    }

    public String getForecastPhenomena() {
        return forecastPhenomena;
    }

    public String getForecastPrecipitation() {
        return forecastPrecipitation;
    }

    public String getForecastHumidity() {
        return forecastHumidity;
    }

    public String getForecastUVIndex() {
        return forecastUVIndex;
    }

    public String getForecastTemperature() {
        return forecastTemperature;
    }

    public String getForecastPressure() {
        return forecastPressure;
    }

    public String getForecastVisibility() {
        return forecastVisibility;
    }
}
