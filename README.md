# Secure-preferences

This is Android Shared preference wrapper that encrypts the values of Shared Preferences using AES 256 GCM. Each key is stored as a one way SHA 256 hash. Both keys and values are base64 encoded before storing into prefs xml file. By default the generated key is stored in the backing preferences file and so can be read and extracted by root user.

![](https://budimanlai.com/i/secure_pref_02.jpg)

# Usage

**Step 1.** Add the JitPack repository to your build file Add it in your root build.gradle at the end of repositories:

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

**Step 2.** Add the dependency

    dependencies {
       implementation 'com.github.budimanlai:android-secure-preferences:1.0'
    }

# Examples

import the library
```java
import com.budimanlai.securepreferences.SecurePreferences;
```

```java
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
```
