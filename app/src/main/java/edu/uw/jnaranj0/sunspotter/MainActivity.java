package edu.uw.jnaranj0.sunspotter;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
            builder.appendQueryParameter("q", "Seattle");
            Log.v(TAG, "URL: " + builder.toString());
            FetchWeather weather = new FetchWeather();
            weather.execute(builder.toString());

            LinearLayout parent = (LinearLayout) findViewById(R.id.parentLayout);
            View child = getLayoutInflater().inflate(R.layout.activity_main_middle, parent, true);
        }
    }

    public class FetchWeather extends AsyncTask<String, Void, String[]> {
        protected String[] doInBackground(String... params) {
            String api_url = "http://" + params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movies[] = null;

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
                String results = buffer.toString();
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

            return movies;
        }
        protected void onPostExecute(String[] result) {
            Log.v(TAG, "FINISHED EXECUTING ");
            for (int i=0; i < result.length; i++) {
                Log.v(TAG, result[i]);
            }
        }

    }
}
