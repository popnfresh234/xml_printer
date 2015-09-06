package alexander.dmtaiwan.com.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String APIUrl = "http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=ddb80380-f1b3-4f8e-8016-7ed9cba571d5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyAsyncTask asyncTask = new MyAsyncTask();
        asyncTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            URL url;
            HttpURLConnection urlConnection = null;
            String responseString = null;
            try {
                url = new URL(APIUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    responseString = readStream(urlConnection.getInputStream());
                    parseJson(responseString);
                } else {
                    Log.i("Async Error", String.valueOf(responseCode));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }
    }

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }
    private void parseJson(String jsonData) {
        ArrayList<String> stationNameList = new ArrayList<String>();
        try {
            JSONObject result = new JSONObject(jsonData);
            JSONObject result1 = result.getJSONObject("result");
            JSONArray resultsArray = result1.getJSONArray("results");
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject stationObject = resultsArray.getJSONObject(i);
                String name = stationObject.getString("sna");
                stationNameList.add(name);
            }
            Log.i("list of stationNames", stationNameList.toString());

//            File file = getFileStreamPath("test.txt");
//
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//
//            FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);
//
//            for (String string: stationNameList){
//                writer.write(string.getBytes());
//                writer.flush();
//            }

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
            for (String string : stationNameList) {
                writer.write(string);
                writer.newLine();
            }
            writer.close();

        } catch (JSONException e) {
            Log.i("ERROR", "ERROR");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("ERROR", e.toString());
            e.printStackTrace();
        }
    }

}
