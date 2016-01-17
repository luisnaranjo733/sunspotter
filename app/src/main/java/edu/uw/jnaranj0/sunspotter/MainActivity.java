package edu.uw.jnaranj0.sunspotter;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Sunspotter";
    public static final String BASE_API_URL = "api.openweathermap.org/data/2.5/forecast";
    private EditText editText;
    private InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    public void onBtnClicked(View view) {
        if (view.getId() == R.id.button) {
            Editable zipCode = editText.getText();
            Log.v(TAG, "Button clicked! " +  zipCode);

            // fetch weather data
            Uri.Builder builder = new Uri.Builder();
            builder.path(this.BASE_API_URL);
            builder.appendQueryParameter("appid", BuildConfig.OPEN_WEATHER_MAP_API_KEY);
            builder.appendQueryParameter("zip", zipCode.toString());
            builder.appendQueryParameter("units", "imperial");
            Log.v(TAG, "URL: " + builder.toString());
            FetchWeather weather = new FetchWeather();
            weather.execute(builder.toString());

            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public class FetchWeather extends AsyncTask<String, Void, String> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        protected String doInBackground(String... params) {
            String api_url = "http://" + params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String results;

            try  {
                Log.v(TAG, "Entering try");
                URL url = new URL(api_url);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    return null;
                }
                results = buffer.toString();
                Log.v(TAG, "Exiting try");
            } catch(IOException io) {
                Log.v(TAG, "Caught: " + io);
                return null;
            } finally {
                Log.v(TAG, "Entering finally");
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {

                    }
                }

            }

            return results;
        }
        protected void onPostExecute(String json) {
            super.onPostExecute(json);
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            Log.v(TAG, "Retrieved json: " + json);
            ArrayList<Forecast> forecasts = new ArrayList<Forecast>();
            Forecast firstSunnyForecast = null;
            try {
                JSONObject root = new JSONObject(json);
                JSONArray forecast_list = root.getJSONArray("list");
                Log.v(TAG, "Succesfully retrieved " + forecast_list.length() + " forecasts: ");

                for (int i=0; i < forecast_list.length(); i++) {
                    Forecast forecast = new Forecast(forecast_list.getJSONObject(i));
                    forecasts.add(forecast);
                    //Log.v(TAG, "Parsed: " + forecast);
                    if (forecast.isSunny() && firstSunnyForecast == null) {
                        firstSunnyForecast = forecast;
                        Log.v(TAG, "Found a sunny day!");
                    }

                }
            } catch (JSONException exception) {
                exception.printStackTrace();
            }

            ViewStub stub = (ViewStub) findViewById(R.id.stub);
            if (stub != null) {
                Log.v(TAG, "Inflating stub");
                stub.inflate();
            }

            TextView txtView1 = (TextView) findViewById(R.id.txtView1);
            TextView txtView2 = (TextView) findViewById(R.id.secondTextView);
            ImageView image = (ImageView) findViewById(R.id.summaryImage);

            Log.v(TAG, "SETTING IMAGE");

            if (firstSunnyForecast != null) {
                image.setImageResource(R.drawable.happy);
                Log.v(TAG, "Updating UI because it will be sunny");
                txtView1.setText("There will be sun!");
                Log.v(TAG, "Set 1");
                txtView2.setText("It will be sunny on " + firstSunnyForecast.getDate());
                Log.v(TAG, "Set 2");
            } else {
                image.setImageResource(R.drawable.sad);
                Log.v(TAG, "not sunny anytime soon");
                txtView2.setText("Looks like there will be no sun in 5 days.");
                txtView1.setText("It won't be sunny :(");
            }


            //ArrayAdapter<Forecast> adapter =  new ArrayAdapter<Forecast>(MainActivity.this, R.layout.activity_main_list_item, forecasts);
            ForecastAdapter adapter = new ForecastAdapter(MainActivity.this, forecasts);
            AdapterView listView = (AdapterView) findViewById(R.id.listView);
            listView.setAdapter(adapter);
        }

    }

    public class ForecastAdapter extends ArrayAdapter<Forecast> {
        HashMap<String, Integer> icons;
        public ForecastAdapter(Context context, ArrayList<Forecast> forecast) {
            super(context, 0, forecast);

            icons = new HashMap<String, Integer>();
            icons.put("Rain", R.drawable.d09);
            icons.put("Clouds",  R.drawable.d03);
            icons.put("Clear", R.drawable.d01);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Forecast forecast = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_main_list_item, parent, false);
            }
            // Lookup view for data population
            ImageView listImage = (ImageView) convertView.findViewById(R.id.listImage);
            TextView forecastText = (TextView) convertView.findViewById(R.id.forecastText);
            TextView temperatureText = (TextView) convertView.findViewById(R.id.temperatureText);
            // Populate the data into the template view using the data object
            int icon = 0;
            icon = icons.get(forecast.getSummary());
            if (icon != 0) {
                listImage.setImageResource(icon);
            } else {
                //listImage.setImageResource(R.drawable.n01);
                listImage.setImageResource(R.drawable.shrug);
            }

            forecastText.setText(forecast.toString());
            temperatureText.setText("" + forecast.getTemp());
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
