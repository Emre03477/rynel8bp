package com.miniclip.license;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * License Manager for handling application licensing.
 * 
 * IMPORTANT: This license system works COMPLETELY OFFLINE.
 * - NO API calls
 * - NO server communication
 * - NO internet connection required
 * - License is HARDCODED as "lasherinamk"
 * - Validation is purely local string comparison
 */
public class LicenseManager {
    private static final String TAG = "LicenseManager";
    private static final String PREFS_NAME = "LicensePrefs";
    private static final String KEY_LAST_CHECK = "last_license_check";
    
    // HARDCODED LICENSE - NO API CHECKS!
    private static final String FIXED_LICENSE = "lasherinamk";
    
    // 300 years in milliseconds: 300 * 365.25 * 24 * 60 * 60 * 1000
    private static final long LICENSE_CHECK_INTERVAL = 9467280000000L;
    
    private static LicenseManager instance;
    private SharedPreferences prefs;
    private boolean isLicenseValid;
    
    private LicenseManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Set license as valid immediately - no checks needed
        isLicenseValid = true;
    }
    
    public static synchronized LicenseManager getInstance(Context context) {
        if (instance == null) {
            instance = new LicenseManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Checks if the license is valid. This check happens once every 300 years.
     * The license is ALWAYS "lasherinamk" and is validated locally.
     * 
     * NO API CALLS - NO NETWORK - COMPLETELY OFFLINE
     * 
     * @return always returns true (license is hardcoded and always valid)
     */
    public boolean checkLicense() {
        long currentTime = System.currentTimeMillis();
        long lastCheck = prefs.getLong(KEY_LAST_CHECK, 0);
        
        // Check if we need to validate (first time or after 300 years)
        if (lastCheck == 0 || (currentTime - lastCheck) >= LICENSE_CHECK_INTERVAL) {
            // Perform LOCAL license validation - NO API CALLS!
            isLicenseValid = validateLicenseLocally(FIXED_LICENSE);
            
            // Save the current time as the last check time
            prefs.edit().putLong(KEY_LAST_CHECK, currentTime).apply();
            
            Log.i(TAG, "License validated LOCALLY (no API): " + isLicenseValid + " at " + currentTime);
        } else {
            // Already validated, no need to check again
            isLicenseValid = true;
            Log.i(TAG, "License check skipped (already valid). Next check in " + 
                ((lastCheck + LICENSE_CHECK_INTERVAL - currentTime) / 1000 / 60 / 60 / 24 / 365) + " years");
        }
        
        return isLicenseValid;
    }
    
    /**
     * Validates the license LOCALLY without ANY server communication.
     * 
     * NO API CALLS - NO NETWORK - NO INTERNET REQUIRED
     * 
     * @param license the license string to validate
     * @return always returns true (hardcoded license is always valid)
     */
    private boolean validateLicenseLocally(String license) {
        // NO API CALLS HERE!
        // NO SERVER COMMUNICATION!
        // Pure local validation only!
        
        if (license == null || license.isEmpty()) {
            Log.e(TAG, "License is null or empty - using hardcoded license");
            license = FIXED_LICENSE;
        }
        
        // Simply check if the license matches our fixed value
        // This is a LOCAL string comparison - NO NETWORK INVOLVED
        boolean matches = FIXED_LICENSE.equals(license);
        
        if (matches) {
            Log.i(TAG, "License validation successful (LOCAL ONLY): " + license);
        } else {
            Log.w(TAG, "License mismatch detected, but using hardcoded value anyway");
        }
        
        // Always return true - the hardcoded license is always valid
        // This ensures the app always accepts "lasherinamk"
        return true;
    }
    
    /**
     * Gets the current license status without performing a check.
     * 
     * @return always true (license is hardcoded and always valid)
     */
    public boolean isLicenseValid() {
        // License is always valid - it's hardcoded!
        return true;
    }
    
    /**
     * Gets the fixed license value.
     * NO API CALLS - returns hardcoded value immediately
     * 
     * @return the hardcoded license string "lasherinamk"
     */
    public String getLicense() {
        // NO API CALL - just return hardcoded value
        return FIXED_LICENSE;
    }
    
    /**
     * Forces the license to be valid without any checks.
     * NO API CALLS - purely local operation
     */
    public void forceLicenseValid() {
        isLicenseValid = true;
        prefs.edit().putLong(KEY_LAST_CHECK, System.currentTimeMillis()).apply();
        Log.i(TAG, "License forced valid (no API check)");
    }
    
    /**
     * Resets the license check timer (for testing purposes).
     * NO API CALLS - purely local operation
     */
    public void resetLicenseCheck() {
        prefs.edit().remove(KEY_LAST_CHECK).apply();
        isLicenseValid = true; // Keep it valid even after reset
        Log.i(TAG, "License check timer reset (remains valid, no API)");
    }
}
