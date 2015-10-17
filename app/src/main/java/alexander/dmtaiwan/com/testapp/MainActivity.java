package alexander.dmtaiwan.com.testapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {
    private static final String APIUrl = "https://ybapp01.youbike.com.tw/json/gwjs.json";

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
                OkHttpClient client = getUnsafeOkHttpClient();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = client.newCall(request).execute();
                int responseCode = response.code();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    parseJson(response.body().string());
                    return null;
                } else {
                    switch (responseCode) {
                        case HttpURLConnection.HTTP_NOT_FOUND:

                            return null;
                        default:
                            return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            ArrayList<Integer> intArray = new ArrayList<Integer>();
            try {
                JSONObject result = new JSONObject(jsonData);
                JSONArray resultsArray = result.getJSONArray("retVal");
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject stationObject = resultsArray.getJSONObject(i);
                    String name = stationObject.getString("sna");
                    int j = stationObject.getInt("iid");
                    stationNameList.add(name);
                    intArray.add(j);
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
                File f = new File(Environment.getExternalStorageDirectory(), "test.txt");

                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                for (int i = 0; i < stationNameList.size(); i++) {
                    String string = stationNameList.get(i);
                    String stationNumber = String.valueOf(intArray.get(i));
                    String first = "<string name=\"";
                    String second = "station" + stationNumber + "\"";
                    String third = " translatable=\"false\">";
                    String fourth = "</string>";
                    String appended = first + second  + third + string + fourth;
                    writer.write(appended);
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

        public OkHttpClient getUnsafeOkHttpClient() {
            try {
                // Create a trust manager that does not validate certificate chains
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                        }
                };

                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                OkHttpClient okHttpClient = new OkHttpClient();
                okHttpClient.setSslSocketFactory(sslSocketFactory);
                okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                return okHttpClient;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
