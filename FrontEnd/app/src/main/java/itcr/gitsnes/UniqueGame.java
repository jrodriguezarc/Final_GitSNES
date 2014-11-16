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
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *  Class extends Fragment, is a main frame to see the game info
 */
public class UniqueGame extends Fragment {

    String name,category,desc,url_file,url_img,gameid;


    public UniqueGame(String name, String category, String desc,String file, String img, String id){
        this.name = name;
        this.category = category;
        this.desc=desc;
        this.url_file = file;
        this.url_img =img;
        this.gameid = id;
    }


    /* Create and inflate method*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* Async mode */
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /* Set UI values to */
        final View view = inflater.inflate(R.layout.unique_game, container, false);
        TextView nameTextView = (TextView) view.findViewById(R.id.Uni_name);
        nameTextView.setText(this.name);
        TextView typeView = (TextView) view.findViewById(R.id.Uni_category);
        typeView.setText(this.category);
        TextView descView = (TextView) view.findViewById(R.id.Uni_decription);
        descView.setText(this.desc);
        TextView file = (TextView) view.findViewById(R.id.fileurl);
        file.setText(this.url_file);
        TextView idgame = (TextView) view.findViewById(R.id.txtgameid);
        idgame.setText(this.gameid);
        final TextView image = (TextView) view.findViewById(R.id.imgurl);
        image.setText(this.url_img);

        /* Thread  to download file */
        new Thread(new Runnable() {
        @Override
        public void run() {
           getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView myFirstImage = (ImageView) view.findViewById(R.id.Uni_img_game);
                    myFirstImage.setTag(new KeyStore().URL + image.getText().toString());
                    new DownloadImagesTask().execute(myFirstImage);
                }
            });
        }}).start();


        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("log_tag", "keyCode: " + keyCode);
                if( keyCode == KeyEvent.KEYCODE_BACK ) {
                    Log.i("log_tag", "onKey Back listener is working!!!");
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return true;
                } else {
                    return false;
                }
            }
        });

        /* Inflate addgame fragment on layout containter */
        return view;
    }





}
