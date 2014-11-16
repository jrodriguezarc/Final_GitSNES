/*
 * Copyright 2014 GitSNES Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package itcr.gitsnes;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Class to handle REST-Client, that's a simple http client where GET-PUT-DELETE
 * data from https://gitsnes.appspot.com
 */
public class BackendHandler {


    /**
     GET a game metadata
     Method: GET
     Request Parameters:
     Returns String with JSON build
     */

    public String readJSON() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://gitsnes.appspot.com/games/"+ new KeyStore().md5());
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);

                }
            } else {
                Log.e("log_tag", "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            Log.e("log_tag", e.toString());
        } catch (IOException e) {
            Log.e("log_tag", e.toString());
        }
        return builder.toString();
    }


    /**
     *  Method POST user
     *  Params:
     *      arg0: email
     *      arg1: name
     */
    public void sendUser(String arg0, String arg1) {


        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://gitsnes.appspot.com/users");
        String sender = "https://gitsnes.appspot.com/createuser/" + arg0 + "/" + arg1 + "/" + new KeyStore().md5();
        Log.i("log_tag", sender);
        try {
            HttpPost request = new HttpPost(sender);
            request.addHeader("content-type", "application/json");
            client.execute(request);
            Log.i("log_tag", sender);
        } catch (Exception ex) {
            Log.i("log_tag", ex.toString());
        }
    }



    /**
     *  Method POST data from web
     *  Params:
     *      arg0: name of uploader  arg1:name of game
     *      arg2: category of game  arg3:description
     *      arg4: bucket file key   arg5: bucket image key
     */
    public void sendJSON(String arg0, String arg1,
                         String arg2, String arg3,
                         String arg4, String arg5) {


        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://gitsnes.appspot.com/game");
        arg0 = arg0.replace(" ", "_"); //uploader
        arg1 = arg1.replace(" ", "_"); //name
        arg2 = arg2.replace(" ", "_"); //category
        arg3 = arg3.replace(" ", "_"); //description


        String sender = "https://gitsnes.appspot.com/game/" + arg0 + "/"
                + arg1 + "/" + arg2 + "/" + arg3 + "/" + arg4 + "/" + arg5 + "/" + new KeyStore().md5();


        try {
            HttpPost request = new HttpPost(sender);

            request.addHeader("content-type", "application/json");
            client.execute(request);
            Log.i("log_tag", sender);
        } catch (Exception ex) {
            Log.i("log_tag", ex.toString());
        }


    }


    /**
     Like a game
     Method: POST
     Request Parameters: Game ID
     Returns void [true if game was updated]
     */
    public void liked_Game(String game_key){
        URL url = null;
        try {
            url = new URL("https://gitsnes.appspot.com/likegame/"+game_key);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            out.write("Resource content");
            out.close();
            httpCon.getInputStream();
            Log.i("log_tag", out.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     Delete a specific game
     Method: DELETE
     Request Parameters: GAME ID
     Returns void [true if game was delete]
     */
    public void delete_Game(String game_key){
        URL url = null;
        try {
            url = new URL("https://gitsnes.appspot.com/deletegame/"+game_key + "/" + new KeyStore().md5());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            int responseCode = connection.getResponseCode();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     GET a moreliked games
     Method: GET
     Request Parameters:
     Returns void [true if game state was changed]
     */
    public String getFavorites() {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://gitsnes.appspot.com/moreliked" + new KeyStore().md5());
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);

                }
            } else {
                Log.e("log_tag", "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            Log.e("log_tag", e.toString());
        } catch (IOException e) {
            Log.e("log_tag", e.toString());
        }
        return builder.toString();
    }


    /**
     Change a game state
     Method: POST
     Request Parameters: GAME KEY
     Returns void [true if game state was changed]
     */

    public void approve_Game(String game_key){
        URL url = null;
        try {
            url = new URL("https://gitsnes.appspot.com/changestate/"+game_key);
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            httpCon.setDoOutput(true);
            httpCon.setRequestMethod("PUT");
            OutputStreamWriter out = new OutputStreamWriter(
                    httpCon.getOutputStream());
            out.write("Resource content");
            out.close();
            httpCon.getInputStream();
            Log.i("log_tag", out.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String get_Recommended(String userID) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet("http://gitsnes.appspot.com/recomendedliker/" + new KeyStore().md5());
        try {
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);

                }
            } else {
                Log.e("log_tag", "Failed to download file");
            }
        } catch (ClientProtocolException e) {
            Log.e("log_tag", e.toString());
        } catch (IOException e) {
            Log.e("log_tag", e.toString());
        }
        return builder.toString();
    }

}
