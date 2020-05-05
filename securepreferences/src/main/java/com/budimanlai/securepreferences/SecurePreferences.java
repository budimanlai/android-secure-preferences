/*
 * Created by Budiman Lai (budiman.lai@gmail.com) on 4/26/20 6:53 AM
 *  Web: http://budimanlai.com, https://github.com/budimanlai
 *  Copyright (c) 2020 . All rights reserved.
 *  Last modified 4/26/20 6:53 AM
 */

package com.budimanlai.securepreferences;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurePreferences implements SharedPreferences {

    private final static int flags = Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;

    // the backing pref file
    private SharedPreferences mSharedPreferences;

    private String mSharePrefrencesFilename;

    private static String prefix;

    // TAG for logging
    private String TAG = SecurePreferences.class.getName();

    // The context object
    private Context mContext;

    private byte[] mAssociatedData;

    private SecretKey secretKey;

    private static boolean mIsLoggingEnabled;

    private final SecureRandom secureRandom = new SecureRandom();
    private final int GCM_IV_LENGTH = 12;
    private final Charset charSet = StandardCharsets.UTF_8;
    private final String algo = "AES/GCM/NoPadding";

    public SecurePreferences(Context context) {
        init(context, null, null, null);
    }

    public SecurePreferences(Context context, String password) {
        init(context, password, null, null);
    }

    public SecurePreferences(Context context, String password, String sharedPrefFilename) {
        init(context, password, null, sharedPrefFilename);
    }

    public SecurePreferences(Context context, String password, String salt, String sharedPrefFilename) {
        init(context, password, salt, sharedPrefFilename);
    }

    private void init(Context context, String password, String salt, String sharedPrefFilename) {
        mContext = context;

        if (mSharedPreferences == null) {
            this.mSharedPreferences = getSharedPreferenceFile(sharedPrefFilename);
        }

        prefix = getDefaultSalt(mContext);

        // Salt for enc and dec
        if (TextUtils.isEmpty(salt)) {
            mAssociatedData = prefix.getBytes(charSet);
        } else {
            mAssociatedData = salt.getBytes(charSet);
        }

        // The password for enc and decc
        byte[] mPassword;
        if (TextUtils.isEmpty(password)) {
            // generate default password
            mPassword = sha256(mContext.getPackageName() + "." + new String(mAssociatedData, charSet));
        } else {
            mPassword = sha256(password + "." + new String(mAssociatedData, charSet));
        }

        secretKey = new SecretKeySpec(mPassword, "AES");
    }

    /**
     * generate sha256 hash
     *
     * @param name string will be hash
     * @return hash
     */
    private static byte[] sha256(String name) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
            assert digest != null;
            digest.reset();
            return digest.digest(name.getBytes());
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * This method is here for backwards compatibility reasons. Recommend supplying your own Salt
     *
     * @param context
     * @return Consistent between app restarts, device restarts, factory resets,
     * however cannot be guaranteed on OS updates.
     */
    @SuppressLint("MissingPermission")
    private static String getDefaultSalt(Context context) {

        //Android Q removes all access to Serial, fallback to Settings.Secure.ANDROID_ID
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            return getSecureDeviceId(context);
        } else {
            return getDeviceSerialNumber(context);
        }
    }

    @SuppressLint("HardwareIds")
    private static String getSecureDeviceId(Context context) {
        return Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
    }

    /**
     * Gets the hardware serial number of this device. This only for backwards compatibility
     *
     * @return serial number or Settings.Secure.ANDROID_ID if not available.
     */
    @SuppressLint("MissingPermission")
    private static String getDeviceSerialNumber(Context context) {
        try {
            String deviceSerial = "";
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                deviceSerial = Build.getSerial();
            } else {
                deviceSerial = Build.SERIAL;
            }

            if (TextUtils.isEmpty(deviceSerial)) {
                return getSecureDeviceId(context);
            } else {
                return deviceSerial;
            }
        } catch (Exception ignored) {
            // Fall back to Android_ID
            return getSecureDeviceId(context);
        }
    }

    /**
     * if a prefFilename is not defined the getDefaultSharedPreferences is used.
     *
     * @return SharedPreferences
     */
    private SharedPreferences getSharedPreferenceFile(String prefFilename) {
        //name of the currently loaded sharedPrefFile, can be null if default
        mSharePrefrencesFilename = prefFilename;

        if (TextUtils.isEmpty(prefFilename)) {
            return PreferenceManager.getDefaultSharedPreferences(mContext);
        } else {
            return mContext.getSharedPreferences(prefFilename, Context.MODE_PRIVATE);
        }
    }

    /**
     * Generate encrypted key name
     *
     * @param key preference key name
     * @return String
     */
    public String keyName(String key) {
        return Base64.encodeToString(sha256(key + "." + prefix), SecurePreferences.flags);
    }

    /**
     * Encrypt the data and return as Base64 String
     *
     * @param content value want to encrypt
     * @return String
     */
    private String encrypt(String content) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH]; //NEVER REUSE THIS IV WITH SAME KEY
            secureRandom.nextBytes(iv);

            final Cipher cipher = Cipher.getInstance(algo);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv); //128 bit auth tag length
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            if (mAssociatedData != null) { cipher.updateAAD(mAssociatedData); }
            byte[] cipherText = cipher.doFinal(content.getBytes(charSet));

            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);

            return Base64.encodeToString(byteBuffer.array(), SecurePreferences.flags);
        } catch (Exception e) {
            return null;
        }
    }

    private String decrypt(String content) {
        try {
            byte[] cipherMessage = Base64.decode(content, SecurePreferences.flags);

            final Cipher cipher = Cipher.getInstance(algo);
            //use first 12 bytes for iv
            AlgorithmParameterSpec gcmIv = new GCMParameterSpec(128, cipherMessage, 0, GCM_IV_LENGTH);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmIv);

            if (mAssociatedData != null) { cipher.updateAAD(mAssociatedData); }

            //use everything from 12 bytes on as ciphertext
            byte[] dec = cipher.doFinal(cipherMessage, GCM_IV_LENGTH, cipherMessage.length - GCM_IV_LENGTH);

            return new String(dec, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private void loge(String msg) {
        if (mIsLoggingEnabled) { Log.e(TAG, msg); }
    }

    @Override
    public Map<String, ?> getAll() {
        final Map<String, ?> encryptedMap = mSharedPreferences.getAll();
        final Map<String, Object> decryptedMap = new HashMap<>(encryptedMap.size());

        for (Map.Entry<String, ?> entry: encryptedMap.entrySet()) {
            Object cipherText = entry.getValue();

            // Check if the data stored is a StringSet
            if (cipherText == null) { continue; }

            try {
                Set<String> stringSet = getDecryptedStringSet(cipherText);

                if (stringSet != null) {
                    decryptedMap.put(entry.getKey(), stringSet);
                } else {
                    decryptedMap.put(entry.getKey(), decrypt(cipherText.toString()));
                }
            } catch (Exception e) {
                loge(e.getMessage());
                // Ignore issues that unencrypted values and use instead raw cipher text string
                decryptedMap.put(entry.getKey(), cipherText.toString());
            }
        }

        return decryptedMap;
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String defaultValue) {
        final String encryptedValue = mSharedPreferences.getString(keyName(s), null);

        String decryptedValue = decrypt(encryptedValue);
        if (encryptedValue != null && decryptedValue != null) {
            return decryptedValue;
        } else {
            return defaultValue;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getStringSet(String key, Set<String> defaultValues) {
        final Set<String> encryptedSet = mSharedPreferences.getStringSet(keyName(key), null);

        if (encryptedSet == null) { return defaultValues; }

        final Set<String> decryptedSet = new HashSet<>(encryptedSet.size());

        for (String encryptedValue : encryptedSet) {
            decryptedSet.add(decrypt(encryptedValue));
        }
        return decryptedSet;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        final String encryptedValue = mSharedPreferences.getString(keyName(key), null);

        if (encryptedValue == null) { return defaultValue; }
        try {
            String s = decrypt(encryptedValue);
            if (s == null) { return defaultValue; }

            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    public double getDouble(String key, int defaultValue) {
        final String encryptedValue = mSharedPreferences.getString(keyName(key), null);

        if (encryptedValue == null) { return defaultValue; }
        try {
            String s = decrypt(encryptedValue);
            if (s == null) { return defaultValue; }

            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        final String encryptedValue = mSharedPreferences.getString(keyName(key), null);

        if (encryptedValue == null) { return defaultValue; }

        try {
            String s = decrypt(encryptedValue);
            if (s == null) { return defaultValue; }

            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        final String encryptedValue = mSharedPreferences.getString(keyName(key), null);
        if (encryptedValue == null) { return defaultValue; }

        try {
            String s = decrypt(encryptedValue);
            if (s == null) { return defaultValue; }

            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        final String encryptedValue = mSharedPreferences.getString(keyName(key), null);
        if (encryptedValue == null) { return defaultValue; }
        try {
            String s = decrypt(encryptedValue);
            if (s == null) { return defaultValue; }

            return Boolean.parseBoolean(s);
        } catch (NumberFormatException e) {
            throw new ClassCastException(e.getMessage());
        }
    }

    @Override
    public boolean contains(String key) {
        return mSharedPreferences.contains(keyName(key));
    }

    @Override
    public Editor edit() {
        return new Editor();
    }

    /**
     * Wrapper for Android's {@link android.content.SharedPreferences.Editor}.
     * <p>
     * Used for modifying values in a {@link SecurePreferences} object. All
     * changes you make in an editor are batched, and not copied back to the
     * original {@link SecurePreferences} until you call {@link #commit()} or
     * {@link #apply()}.
     */
    public final class Editor implements SharedPreferences.Editor {
        private SharedPreferences.Editor mEditor;

        /**
         * Constructor.
         */
        private Editor() {
            mEditor = mSharedPreferences.edit();
        }

        @Override
        public SharedPreferences.Editor putString(String key, String value) {
            mEditor.putString(keyName(key),
                    encrypt(value));
            return this;
        }

        /**
         * This is useful for storing values that have be encrypted by something
         * else or for testing
         *
         * @param key   - encrypted as usual
         * @param value will not be encrypted
         * @return
         */
        public SharedPreferences.Editor putUnencryptedString(String key,
                                                             String value) {
            mEditor.putString(keyName(key), value);
            return this;
        }

        @Override
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public SharedPreferences.Editor putStringSet(String key,
                                                     Set<String> values) {
            final Set<String> encryptedValues = new HashSet<String>(
                    values.size());
            for (String value : values) {
                encryptedValues.add(encrypt(value));
            }
            mEditor.putStringSet(keyName(key),
                    encryptedValues);
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            mEditor.putString(keyName(key),
                    encrypt(Integer.toString(value)));
            return this;
        }

        public SharedPreferences.Editor putDouble(String key, double value) {
            mEditor.putString(keyName(key),
                    encrypt(Double.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            mEditor.putString(keyName(key),
                    encrypt(Long.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            mEditor.putString(keyName(key),
                    encrypt(Float.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            mEditor.putString(keyName(key),
                    encrypt(Boolean.toString(value)));
            return this;
        }

        @Override
        public SharedPreferences.Editor remove(String key) {
            mEditor.remove(keyName(key));
            return this;
        }

        @Override
        public SharedPreferences.Editor clear() {
            mEditor.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return mEditor.commit();
        }

        @Override
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        public void apply() {
            mEditor.apply();
        }
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Method to get the decrypted string set from a cipher text
     * @param cipherText The cipher text from which the string set needs to be retrieved
     * @return null if the cipherText is not a valid StringSet, or any of the values in the set are not strings.
     * Else, it will return the StringSet with the decrypted values.
     */
    private Set<String> getDecryptedStringSet(Object cipherText) throws Exception {
        if (cipherText == null) { return null; }

        boolean isSet = cipherText instanceof Set<?>;

        if (!isSet) { return null; }

        Set<?> encryptedSet = (Set<?>) cipherText;
        Set<String> decryptedSet = new HashSet<>();

        for (Object object : encryptedSet) {
            if (object instanceof String) {
                decryptedSet.add(decrypt((String) object));
            } else {
                return null;
            }
        }

        return decryptedSet;
    }

    public static boolean isLoggingEnabled() {
        return mIsLoggingEnabled;
    }

    public static void setLoggingEnabled(boolean loggingEnabled) {
        mIsLoggingEnabled = loggingEnabled;
    }

    public void changePassword(String password) { changePassword(password, null);}

    public void changePassword(String password, String salt) {
        final Map<String, ?> oldPref = getAll();
        final Map<String, Object> decryptedMap = new HashMap<>(oldPref.size());

        // clear old pref data
        edit().clear().apply();

        secretKey = null;

        init(mContext, password, salt, mSharePrefrencesFilename);
        SharedPreferences sharedPreferences = getSharedPreferenceFile(mSharePrefrencesFilename);

        // re-write encrypted values
        for (Map.Entry<String, ?> entry: oldPref.entrySet()) {
            Object cipherText = entry.getValue();

            if (cipherText == null) { continue; }

            try {
                if (cipherText instanceof HashSet) {
                    Set<String> stringSet = (Set<String>) cipherText;

                    final Set<String> encryptedValues = new HashSet<String>(stringSet.size());
                    for (String value : stringSet) {
                        encryptedValues.add(encrypt(value));
                    }
                    sharedPreferences.edit().putStringSet(entry.getKey(), encryptedValues).apply();

                } else {
                    sharedPreferences.edit().putString(entry.getKey(), encrypt(cipherText.toString())).apply();
                }
            } catch (Exception e) {
                loge(e.getMessage());
                sharedPreferences.edit().putString(entry.getKey(), cipherText.toString()).apply();
            }
        }

    }
}
