/*
 * Created by Budiman Lai (budiman.lai@gmail.com) on 4/25/20 10:43 PM
 *  Web: http://budimanlai.com, https://github.com/budimanlai
 *  Copyright (c) 2020 . All rights reserved.
 *  Last modified 4/25/20 10:40 PM
 */

package com.budimanlai.demolib02;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.budimanlai.securepreferences.SecurePreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String TAG = "TAG";
    private SecurePreferences securePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btnWrite = findViewById(R.id.btnWrite);
        btnWrite.setOnClickListener(mWriteListener);

        final Button btnRead = findViewById(R.id.btnRead);
        btnRead.setOnClickListener(mReadListener);

        this.securePreferences = new SecurePreferences(this, "password", "pref_custome_name");
        this.securePreferences.setDebugable(true);
        this.securePreferences.setPrefix("ganteng");
    }

    private String randomString(int length) {
        String ALLOWED_CHARACTERS ="123455678890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-";

        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(length);
        for(int i=0;i<length;++i) sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    private View.OnClickListener mWriteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                // example save json string to preferences
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user_id", 123);
                jsonObject.put("email", "budiman.lai@gmail.com");
                jsonObject.put("fullname", "Budiman Lai");

                securePreferences.edit().putString("profile", jsonObject.toString()).apply();

                // or
                securePreferences.setJSONObject("profile_object", jsonObject);

                // you also can save JSONArray too
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
                jsonArray.put(jsonObject);
                jsonArray.put(jsonObject);
                jsonArray.put(jsonObject);
                securePreferences.setJSONArray("profile_array", jsonArray);

                // example to save data to preferences
                securePreferences.edit().putString("date", String.valueOf(new Date())).apply();
                securePreferences.edit().putString("token", randomString(16)).apply();
                securePreferences.edit().putBoolean("key3", true).apply();
                securePreferences.edit().putFloat("key4", 0.123456f).apply();
                securePreferences.edit().putInt("key5", 123456).apply();
                securePreferences.edit().putLong("key6", 123456).apply();

                // example string set
                Set<String> sets = new HashSet<>();
                sets.add("String set 1");
                sets.add("String set 2");
                sets.add("String set 3");

                securePreferences.edit().putStringSet("keySet1", sets).apply();
            } catch (Exception e) {
                Log.e(TAG, "error");
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    };

    private View.OnClickListener mReadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // example read preferences
            Log.d(TAG, "JSON String[profile]: " + securePreferences.getString("profile", null));
            Log.d(TAG, "String[date]: " + securePreferences.getString("date", null));
            Log.d(TAG, "String[token]: " + securePreferences.getString("token", null));
            Log.d(TAG, "Boolean[key3]: " + securePreferences.getBoolean("key3", false));
            Log.d(TAG, "Float[key4]: " + securePreferences.getFloat("key4", 0));
            Log.d(TAG, "Int[key5]: " + securePreferences.getInt("key5", 0));
            Log.d(TAG, "Long[key6]: " + securePreferences.getLong("key6", 0));

            // example read all field
            Map<String, ?> map = securePreferences.getAll();
            Log.i(TAG, "map: " + map);
            Log.i(TAG, "map[profile]: " + map.get(securePreferences.keyName("profile")));

            // read string set
            Set<String> newSet = new HashSet<>(Objects.requireNonNull(securePreferences.getStringSet("keySet1", new HashSet<String>())));
            Log.i(TAG, "Set<String>: " + newSet);

            try {
                // read JSONObject
                JSONObject profile_object = securePreferences.getJSONObject("profile_object", null);
                if (profile_object != null) {
                    Log.d(TAG, "JSONObject[profile_object]: " + profile_object.toString());
                } else {
                    Log.d(TAG, "JSONObject[profile_object]: null");
                }

                // read JSONArray
                JSONArray profile_array = securePreferences.getJSONArray("profile_array", null);
                if (profile_object != null) {
                    Log.d(TAG, "JSONObject[profile_array]: " + profile_array.toString());
                } else {
                    Log.d(TAG, "JSONObject[profile_array]: null");
                }
            } catch (JSONException e) {
                Log.e(TAG, "error");
                Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }
    };
}
