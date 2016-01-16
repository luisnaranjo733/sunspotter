package edu.uw.jnaranj0.sunspotter;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Sunspotter";
    public static final String BASE_API_URL = "api.openweathermap.org/data/2.5/forecast";
    //public static final String API_KEY = "819cc2b23136d64ed52da7754c8f62b3";
    private EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
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
            String movies[] = null;
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
                movies = results.split("\n");
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

            Forecast firstSunnyForecast = null;
            try {
                JSONObject root = new JSONObject(json);
                JSONArray forecasts = root.getJSONArray("list");
                Log.v(TAG, "Succesfully retrieved " + forecasts.length() + " forecasts: ");

                for (int i=0; i < forecasts.length(); i++) {
                    Forecast forecast = new Forecast(forecasts.getJSONObject(i));
                    Log.v(TAG, "Parsed: " + forecast);
                    if (forecast.isSunny()) {
                        firstSunnyForecast = forecast;
                        Log.v(TAG, "Found a sunny day! Breaking out of loop");
                        break;
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

            //LinearLayout layout = (LinearLayout) findViewById(R.id.inflatedStub);
            TextView txtView1 = (TextView) findViewById(R.id.txtView1);
            TextView txtView2 = (TextView) findViewById(R.id.txtView2);
            ImageView imageView = (ImageView) findViewById(R.id.image);

            if (firstSunnyForecast != null) {
                Log.v(TAG, "Updating UI because it will be sunny");
                txtView1.setText("There will be sun!");
                Log.v(TAG, "Set 1");
                txtView2.setText("It will be sunny on " + firstSunnyForecast.getDate());
                Log.v(TAG, "Set 2");
                imageView.setImageResource(R.drawable.d01);
            } else {
                Log.v(TAG, "NOT SUNNY ANYTIME SOON FUCK");
                txtView2.setText("Looks like there will be no sun in 5 days.");
                txtView1.setText("It won't be sunny :(");
                imageView.setImageResource(R.drawable.n10);
            }
        }

    }
}
