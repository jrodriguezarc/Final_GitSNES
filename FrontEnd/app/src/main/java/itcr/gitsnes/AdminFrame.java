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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.amazonaws.services.ec2.model.Tag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *  Class extends Fragment, is a list view when only the administrator can use
 */
public class AdminFrame extends Fragment {
    JSONArray json_arr;
    ArrayList<HashMap<String, String>> oslist = new ArrayList<HashMap<String, String>>();
    ListView listView;

    /* Keys of the JSON object  */
    private static final String TAG_NAME = "name";
    private static final String TAG_DESC = "category";
    private static final String TAG_CAT = "description";
    private static final String TAG_FILE = "file_url";
    private static final String TAG_IMG = "image_url";
    private static final String KEY = "id";
    static String image_key = "";
    public String qtype ,qname,qdesc,qcat,randomgame;


    public AdminFrame(JSONArray json){
        this.json_arr = json;
        this.qtype   = "all" ;
        this.qname  = "";
        this.qdesc  = "";
        this.qcat   = "";
        this.randomgame = "";
    }



    /* Create and inflate method*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.master_games, container, false);

        try {
            for(int i = 0; i < json_arr.length(); i++){
                final JSONObject c = json_arr.getJSONObject(i);

                /* Get JSON data */
                String id = c.getString(KEY);
                String name = c.getString(TAG_NAME);
                String category = c.getString(TAG_CAT);
                String des = c.getString(TAG_DESC);
                String img = c.getString(TAG_IMG);
                String file = c.getString(TAG_FILE);


                if((!qname.equals("") && name.equals(qname)) ||
                        (!qcat.equals("") && name.equals(qcat)) ||
                        qtype.equals("all")) {

                    HashMap<String, String> map = new HashMap<String, String>();
                    des      = des.replace("_", " ");
                    category = category.replace("_", " ");
                    map.put(TAG_NAME, name);
                    map.put(TAG_CAT, category);
                    map.put(TAG_DESC, des);
                    map.put(TAG_FILE, img);
                    map.put(TAG_IMG, file);
                    map.put(KEY, id);
                    oslist.add(map);

                    listView = (ListView) view.findViewById(R.id.list);

                    ListAdapter adapter = new SimpleAdapter(getActivity(), oslist,
                            R.layout.admin_detail,
                            new String[]{TAG_NAME, KEY}, new int[]{
                            R.id.ad_name, R.id.txt_idgame});



                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {


                            Object object = listView.getItemAtPosition(position);

                            String json_code=  object.toString();
                            Log.i("log_tag",json_code);


                            json_code = json_code.replace("{","");
                            json_code = json_code.replace("}","");

                            final String parts[] = json_code.split(",");
                            final String []cat = parts[1].split("=");
                            final String []_file = parts[2].split("=");
                            final String []_img = parts[3].split("=");
                            final String []desc = parts[4].split("=");
                            final String []name = parts[5].split("=");
                            final String []id_games = parts[0].split("=");

                            image_key = _img[1];

                         /*Toast.makeText(getActivity(), object.toString()
                                        + "", Toast. LENGTH_LONG).show();*/

                            Log.i("log_tag",_file[1]);
                            Log.i("log_tag",_img[1]);
                            Log.i("log_tag",id_games[1]);
                            final String metadata = "Information about: " + name[1] +
                                    "\n\nCategory: " + cat[1] +
                                    "\nDescription: " + desc[1] +
                                    "\nBucket link: " + _file[1];



                            /** Alert dialog with the admin functions,
                             *  that is inflate when listview item is clicked
                             *  */
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(metadata+"\n\nLet's go...we are now the admin of GitSNES !!")
                                    .setCancelable(false)
                                    .setNeutralButton("Accept game ", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            /* Async function */
                                            StrictMode.ThreadPolicy policy = new StrictMode.
                                            ThreadPolicy.Builder().permitAll().build();
                                            StrictMode.setThreadPolicy(policy);

                                            /*Call POST method to changed stated*/

                                            new BackendHandler().approve_Game(id_games[1]);
                                            Toast.makeText(getActivity(),"The game was approved", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setPositiveButton("Delete game ", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            /* Async function */
                                            StrictMode.ThreadPolicy policy = new StrictMode.
                                            ThreadPolicy.Builder().permitAll().build();
                                            StrictMode.setThreadPolicy(policy);

                                            /*Call DELETE method to delete a selected item (Game)*/
                                            new BackendHandler().delete_Game(id_games[1]);
                                            Toast.makeText(getActivity(),"The game was deleted", Toast.LENGTH_LONG).show();


                                        }
                                    })
                                    .setNegativeButton("Scan file", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            /* Async function */
                                            StrictMode.ThreadPolicy policy = new StrictMode.
                                            ThreadPolicy.Builder().permitAll().build();
                                            StrictMode.setThreadPolicy(policy);

                                            /** Call VirusScanner class and first generate
                                             * request to get report from url */
                                            new VirusScanner().Scan(KeyStore.MAIN_PATH + _file[1]);

                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            try {

                                                Thread.sleep(1000); /* Wait for report*/
                                                /* Get URL report if that was success*/
                                                builder.setMessage(new VirusScanner().getUrlReport(KeyStore.MAIN_PATH + _file[1] )).setTitle("Scan virus report");
                                                Log.i(new KeyStore().TAG, KeyStore.MAIN_PATH + _file[1]);
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }


                                            AlertDialog dialog2 = builder.create();
                                            dialog2.show();
                                        }
                                    });
                            AlertDialog alert = builder.create(); /* Create dialog to handle options */
                            alert.show();

                        }
                    });


                    listView.setAdapter(adapter);

                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }
}
