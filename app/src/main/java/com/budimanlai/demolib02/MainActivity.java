/*
 * Created by Budiman Lai (budiman.lai@gmail.com) on 4/25/20 10:43 PM
 *  Web: http://budimanlai.com, https://github.com/budimanlai
 *  Copyright (c) 2020 . All rights reserved.
 *  Last modified 4/25/20 10:40 PM
 */

package com.budimanlai.demolib02;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.budimanlai.securepreferences.SecurePreferences;

import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String TAG = "TAG_LIB";

        try {
            // example save preference
            SharedPreferences securePreferences = new SecurePreferences(this);

            // or
            // SharedPreferences securePreferences = new SecurePreferences(this, "password");
            // or
            // SharedPreferences securePreferences = new SecurePreferences(this, "password", "pref_custome_name");
            // or
            // SharedPreferences securePreferences = new SecurePreferences(this, "password", "salt", "pref_custome_name");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", 123);
            jsonObject.put("email", "budiman.lai@gmail.com");
            jsonObject.put("fullname", "Budiman Lai");

            // write to pref
            securePreferences.edit().putString("profile", jsonObject.toString()).apply();
            securePreferences.edit().putString("token", "1234567890").apply();
            securePreferences.edit().putBoolean("key3", true).apply();
            securePreferences.edit().putFloat("key4", 0.123456f).apply();
            securePreferences.edit().putInt("key5", 123456).apply();
            securePreferences.edit().putLong("key6", 123456).apply();

            // write string set
            Set<String> sets = new HashSet<>();
            sets.add("String set 1");
            sets.add("String set 2");
            sets.add("String set 3");

            securePreferences.edit().putStringSet("keySet1", sets).apply();

            // example read preference
            // get all
            Map<String, ?> map = securePreferences.getAll();
            Log.i(TAG, "map: " + map);
            Log.i(TAG, "map[profile]: " + map.get(SecurePreferences.keyName("profile")));

            // read one by one
            String key1 = securePreferences.getString("profile", null);
            String key2 = securePreferences.getString("token", null);
            boolean key3 = securePreferences.getBoolean("key3", false);
            float key4 = securePreferences.getFloat("key4", 0);
            int key5 = securePreferences.getInt("key5", 0);
            long key6 = securePreferences.getLong("key6", 0);

            Log.i(TAG, "profile: " + key1);
            Log.i(TAG, "token: " + key2);
            Log.i(TAG, "boolean: " + key3);
            Log.i(TAG, "float: " + key4);
            Log.i(TAG, "int: " + key5);
            Log.i(TAG, "long: " + key6);

            // read string set
            Set<String> newSet = new HashSet<>(securePreferences.getStringSet("keySet1", new HashSet<String>()));
            Log.i(TAG, "newSet: " + newSet);

            // add a new string
            newSet.add("String set 4");
            securePreferences.edit().putStringSet("keySet1", newSet).apply();

            Set<String> newSet2 = new HashSet<>(securePreferences.getStringSet("keySet1", new HashSet<String>()));
            Log.i(TAG, "newSet2: " + newSet2);

        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }
}
