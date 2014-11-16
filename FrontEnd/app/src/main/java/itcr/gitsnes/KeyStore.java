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

import java.security.PublicKey;


/* Contains all keys needed around the code*/
public class KeyStore {
    public static String URL = "https://s3-us-west-2.amazonaws.com/cde56c29-1f398-89ec-adb4-7bcyqo0pcqlg/"; //unique game
    public static  final String MY_ACCESS_KEY_ID= "AKIAINHV4T4GZJQN4WQA";
    public static final String MY_SECRET_KEY= "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";
    public static final String BUCKET_NAME= "aae55c27-ce98-44ec-adb4-7bc5830ec378";
    public static final String MAIN_PATH = "https://s3.amazonaws.com/aae55c27-ce98-44ec-adb4-7bc5830ec378/";
    public static final String BUCKET_IMG= "cde56c29-1f398-89ec-adb4-7bcyqo0pcqlg";
    public static final String API_KEY = "3d2a1046a17bb8d325403ae512e12f9467f159869817c834dac6aa7662235fb8";
    public static final String TAG = "log_tag";
    public String current_user;


    public void setCurrent_user(String current_user) {
        String []up = current_user.split("@");
        this.current_user = up[0];
    }

    public String getCurrent_user() {
        return current_user;
    }


    /* Generate MD5 hash value*/
    public static String getHash(String txt, String hashType) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance(hashType);
            byte[] array = md.digest(txt.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            //error action
        }
        return null;
    }

    /* Generate Hash of hash polinomial number [DOUBLE HASHING]*/
    public static String md5(){
        int x = 1 + (int)(Math.random() * ((500 - 1) + 1));
        int number = x*(x+1)*(4*x-1)/6;
        return getHash(getHash(Integer.toString(number), "MD5"), "MD5");
    }


}
