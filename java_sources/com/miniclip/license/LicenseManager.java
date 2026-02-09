package com.miniclip.license;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * License Manager for handling application licensing.
 * Uses a fixed license key and performs validation once every 300 years.
 */
public class LicenseManager {
    private static final String TAG = "LicenseManager";
    private static final String PREFS_NAME = "LicensePrefs";
    private static final String KEY_LAST_CHECK = "last_license_check";
    private static final String FIXED_LICENSE = "lasherinamk";
    
    // 300 years in milliseconds: 300 * 365.25 * 24 * 60 * 60 * 1000
    private static final long LICENSE_CHECK_INTERVAL = 9467280000000L;
    
    private static LicenseManager instance;
    private SharedPreferences prefs;
    private boolean isLicenseValid;
    
    private LicenseManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isLicenseValid = false;
    }
    
    public static synchronized LicenseManager getInstance(Context context) {
        if (instance == null) {
            instance = new LicenseManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Checks if the license is valid. This check happens once every 300 years.
     * The license is always "lasherinamk" and is validated locally.
     * 
     * @return true if license is valid, false otherwise
     */
    public boolean checkLicense() {
        long currentTime = System.currentTimeMillis();
        long lastCheck = prefs.getLong(KEY_LAST_CHECK, 0);
        
        // Check if we need to validate (first time or after 300 years)
        if (lastCheck == 0 || (currentTime - lastCheck) >= LICENSE_CHECK_INTERVAL) {
            // Perform license validation
            isLicenseValid = validateLicense(FIXED_LICENSE);
            
            // Save the current time as the last check time
            prefs.edit().putLong(KEY_LAST_CHECK, currentTime).apply();
            
            Log.i(TAG, "License validated: " + isLicenseValid + " at " + currentTime);
        } else {
            Log.i(TAG, "License check skipped. Next check in " + 
                ((lastCheck + LICENSE_CHECK_INTERVAL - currentTime) / 1000 / 60 / 60 / 24 / 365) + " years");
        }
        
        return isLicenseValid;
    }
    
    /**
     * Validates the license locally without server communication.
     * 
     * @param license the license string to validate
     * @return true if license matches the fixed value
     */
    private boolean validateLicense(String license) {
        if (license == null || license.isEmpty()) {
            Log.e(TAG, "License is null or empty");
            return false;
        }
        
        // Simply check if the license matches our fixed value
        boolean isValid = FIXED_LICENSE.equals(license);
        
        if (isValid) {
            Log.i(TAG, "License validation successful: " + license);
        } else {
            Log.w(TAG, "License validation failed");
        }
        
        return isValid;
    }
    
    /**
     * Gets the current license status without performing a check.
     * 
     * @return true if license was validated successfully
     */
    public boolean isLicenseValid() {
        return isLicenseValid;
    }
    
    /**
     * Gets the fixed license value.
     * 
     * @return the license string
     */
    public String getLicense() {
        return FIXED_LICENSE;
    }
    
    /**
     * Resets the license check timer (for testing purposes).
     */
    public void resetLicenseCheck() {
        prefs.edit().remove(KEY_LAST_CHECK).apply();
        isLicenseValid = false;
        Log.i(TAG, "License check timer reset");
    }
}
