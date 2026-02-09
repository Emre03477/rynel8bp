package com.miniclip.license;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Simple License Dialog - Shows EditText for direct license entry
 * 
 * COMPLETELY OFFLINE - NO API CALLS
 * Only accepts: "lasherinamk"
 */
public class SimpleLicenseDialog {
    private static final String TAG = "SimpleLicenseDialog";
    private static final String PREFS_NAME = "SimpleLicensePrefs";
    private static final String KEY_LICENSE_VALIDATED = "license_validated";
    private static final String CORRECT_LICENSE = "lasherinamk";
    
    /**
     * Shows a license input dialog.
     * User can type the license directly - NO CLIPBOARD NEEDED
     * 
     * @param activity The activity context
     * @param callback Called when license is validated
     */
    public static void showLicenseDialog(final Activity activity, final LicenseCallback callback) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Check if already validated
        if (prefs.getBoolean(KEY_LICENSE_VALIDATED, false)) {
            Log.i(TAG, "License already validated");
            if (callback != null) {
                callback.onLicenseValidated(true);
            }
            return;
        }
        
        // Show dialog for license entry
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Enter License");
                builder.setMessage("Please enter your license key:");
                builder.setCancelable(false);
                
                // Create EditText for input
                final EditText input = new EditText(activity);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Enter license here");
                builder.setView(input);
                
                // OK button
                builder.setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        String enteredLicense = input.getText().toString().trim();
                        
                        // LOCAL VALIDATION - NO API CALL
                        if (validateLicense(enteredLicense, activity)) {
                            Log.i(TAG, "License validated successfully: " + enteredLicense);
                            Toast.makeText(activity, "License valid!", Toast.LENGTH_SHORT).show();
                            
                            // Save validation
                            SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            prefs.edit().putBoolean(KEY_LICENSE_VALIDATED, true).apply();
                            
                            if (callback != null) {
                                callback.onLicenseValidated(true);
                            }
                        } else {
                            Log.w(TAG, "Invalid license entered: " + enteredLicense);
                            Toast.makeText(activity, "Invalid license! Please try again.", Toast.LENGTH_LONG).show();
                            
                            // Show dialog again
                            showLicenseDialog(activity, callback);
                        }
                    }
                });
                
                // Show hint button
                builder.setNeutralButton("Hint", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        Toast.makeText(activity, "License: lasherinamk", Toast.LENGTH_LONG).show();
                        showLicenseDialog(activity, callback);
                    }
                });
                
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    
    /**
     * Validates license LOCALLY - NO API CALLS
     * 
     * @param license The license string to validate
     * @param context Application context
     * @return true if license is "lasherinamk"
     */
    private static boolean validateLicense(String license, Context context) {
        // NO API CALLS!
        // NO NETWORK!
        // Pure local string comparison
        
        if (license == null || license.isEmpty()) {
            Log.e(TAG, "License is null or empty");
            return false;
        }
        
        boolean isValid = CORRECT_LICENSE.equals(license);
        
        if (isValid) {
            Log.i(TAG, "License match: " + license);
        } else {
            Log.w(TAG, "License mismatch. Expected: " + CORRECT_LICENSE + ", Got: " + license);
        }
        
        return isValid;
    }
    
    /**
     * Check if license is already validated
     */
    public static boolean isLicenseValid(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_LICENSE_VALIDATED, false);
    }
    
    /**
     * Reset license validation (for testing)
     */
    public static void resetLicense(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        Log.i(TAG, "License reset");
    }
    
    /**
     * Callback interface for license validation
     */
    public interface LicenseCallback {
        void onLicenseValidated(boolean valid);
    }
}
