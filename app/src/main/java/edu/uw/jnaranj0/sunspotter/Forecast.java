package edu.uw.jnaranj0.sunspotter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Abstraction class to deal with extracting data from an unprocessed JSONObject
 */

public class Forecast {
    private JSONObject forecast;
    private Date date;
    private double temp;
    private String summary;
    public static final String DATE_FORMAT = "EEEE 'at' K a";

    public Forecast(JSONObject forecast) {
        this.forecast = forecast;
        try {
            long dt = forecast.getLong("dt");
            this.date = new Date(dt * 1000L);


            JSONObject main = forecast.getJSONObject("main");
            this.temp = main.getDouble("temp");

            JSONArray weather = forecast.getJSONArray("weather");
            JSONObject weather_obj = weather.getJSONObject(0);
            this.summary = weather_obj.getString("main");
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
    }

    public String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT-8"));
        String formattedDate = sdf.format(this.date);
        return formattedDate;
    }

    public double getTemp() {
        return this.temp;
    }

    public String getSummary() {
        return this.summary;
    }

    public boolean isSunny() {
        return this.getSummary().equals("Clear");
    }

    public String toString() {
        return this.getSummary() + " on " + this.getDate() + " with " + this.getTemp() + " F";
    }
}
