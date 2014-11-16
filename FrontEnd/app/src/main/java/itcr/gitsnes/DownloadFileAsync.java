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
import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Class to handle file downloading asynchronously
 */
class DownloadFileAsync  extends AsyncTask<String, String, String> {


    @Override
    /**
     * Download file on background
     * Params:
     *      aurl -> aurl[0] : file URL
     *      aurl -> aurl[1] : final name of file
     * */

    protected String doInBackground(String... aurl){
        Log.i("log_tag", "comenzando");
        int count;
        try {
            /*Get file URL*/
            URL url = new URL(aurl[0]);
            URLConnection conexion = url.openConnection();
            conexion.connect();

            /**
             * Read/write data from URL to a buffer of bytes
             */
            int lenghtOfFile = conexion.getContentLength();
            Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream("/sdcard/Download/"+ aurl[1] + ".smc");

            /**
             * Convert file into a byte buffer, first read data from stream
             * finally write data on outputstream to building file
             */
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress(""+(int)((total*100)/lenghtOfFile));
                output.write(data, 0, count);
            }


            Log.i("log_tag", "Descargado");
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {

            Log.i("log_tag", e.toString());
        }
        return null;
    }
}