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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 *  Class extends Fragment, is a list view with the list of the games
 */
public class MasterGames extends Fragment{


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
    private static final String STATE = "state";
    private static final String LIKES = "likes";

    static String image_key = "asasdas";
    public String qtype ,qname,qdesc,qcat,randomgame;



    public MasterGames(JSONArray json){
        this.json_arr = json;
        this.qtype   = "" ;
        this.qname  = "";
        this.qdesc  = "";
        this.qcat   = "";
        this.randomgame = "";
    }



    /* Create and inflate method*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.master_games, container, false);
        int rd = randInt(0,json_arr.length()-1);
        try {
            for(int i = 0; i < json_arr.length(); i++) {


                /* Get JSON data */
                final JSONObject c = json_arr.getJSONObject(i);
                String name = c.getString(TAG_NAME);
                String category = c.getString(TAG_CAT);
                String des = c.getString(TAG_DESC);
                String img = c.getString(TAG_IMG);
                String file = c.getString(TAG_FILE);
                String id = c.getString(KEY);
                String _likes = c.getString(LIKES);


                /* Validate is the game state is active */
                if (!c.getString(STATE).equals("0")) {
                    Log.i("log_tag", Integer.toString(rd));

                    /* Search a random game */
                    if (randomgame.equals("random") && rd == i) {
                        UniqueGame new_fragment =
                                new UniqueGame(name, category, des, file, img, id);
                        FragmentTransaction transaction =
                                getFragmentManager().beginTransaction();
                        transaction.replace(R.id.placeholder, new_fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        return view;
                    }

                    /* Options to advanced search - 'all' get all the games */
                    if ((!qname.equals("") && name.equals(qname)) ||
                            (!qcat.equals("") && name.equals(qcat)) ||
                            qtype.equals("all")) {



                        /* Hashing JSON data into a String */
                        HashMap<String, String> map = new HashMap<String, String>();
                        des = des.replace("_", " ");
                        category = category.replace("_", " ");
                        map.put(TAG_NAME, name);
                        map.put(TAG_CAT, category);
                        map.put(TAG_DESC, des);
                        map.put(TAG_FILE, img);
                        map.put(TAG_IMG, file);
                        map.put(KEY, id);
                        map.put(LIKES,_likes);
                        oslist.add(map);

                        listView = (ListView) view.findViewById(R.id.list);

                        /**Mapping json object, that is needed to use onclick event listener*/
                        ListAdapter adapter = new SimpleAdapter(getActivity(), oslist,
                                R.layout.game_detail,
                                new String[]{LIKES, TAG_NAME, TAG_CAT, TAG_DESC, TAG_IMG, TAG_FILE, KEY}, new int[]{
                                R.id.txt_likes, R.id.name, R.id.category, R.id.decription, R.id.txtgameid});


                        /* On click event listener*/
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {


                                Object object = listView.getItemAtPosition(position);
                                /* Parse string to vars */
                                String json_code = object.toString();
                                Log.i("log_tag", json_code);
                                json_code = json_code.replace("{", "");
                                json_code = json_code.replace("}", "");
                                String parts[] = json_code.split(",");
                                String[] cat = parts[1].split("=");
                                String[] _file = parts[2].split("=");
                                String[] _img = parts[3].split("=");
                                String[] desc = parts[4].split("=");
                                String[] name = parts[6].split("=");
                                String[] id_games = parts[0].split("=");

                                image_key = _img[1];

                                Log.i("log_tag", _file[1]);
                                Log.i("log_tag", _img[1]);
                                Log.i("log_tag", id_games[1]);

                                /* Create main frame of the game */
                                UniqueGame new_fragment =
                                        new UniqueGame(name[1], cat[1], desc[1], _file[1], _img[1], id_games[1]);


                                FragmentTransaction transaction =
                                        getFragmentManager().beginTransaction();
                                transaction.replace(R.id.placeholder, new_fragment);

                                /* Inflate unique game fragment on layout containter */
                                transaction.addToBackStack(null);
                                transaction.commit();

                            }
                        });

                        /* Inflate list fragment on layout containter */
                        listView.setAdapter(adapter);

                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    /**Set and get functions */
    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public void setQname(String qname) {
        this.qname = qname;
    }

    public void setQcat(String qcat) {
        this.qcat = qcat;
    }

    public void setQdesc(String qdesc) {
        this.qdesc = qdesc;
    }

    public void setRandomgame(String randomgame) {
        this.randomgame = randomgame;
    }

    /* Random generator to accelerometer event*/
    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }
}
