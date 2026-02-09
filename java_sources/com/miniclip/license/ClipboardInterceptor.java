package com.miniclip.license;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

/**
 * Clipboard interceptor that automatically provides the fixed license
 * when the app tries to read from clipboard for login.
 */
public class ClipboardInterceptor {
    private static final String TAG = "ClipboardInterceptor";
    private static final String FIXED_LICENSE = "lasherinamk";
    private static ClipboardInterceptor instance;
    private Context context;
    
    private ClipboardInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized ClipboardInterceptor getInstance(Context context) {
        if (instance == null) {
            instance = new ClipboardInterceptor(context);
        }
        return instance;
    }
    
    /**
     * Sets the fixed license in the clipboard automatically.
     * This ensures that when the app reads from clipboard for login,
     * it will get the correct license value.
     */
    public void setFixedLicenseInClipboard() {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("license", FIXED_LICENSE);
            clipboard.setPrimaryClip(clip);
            Log.i(TAG, "Fixed license set in clipboard: " + FIXED_LICENSE);
        } catch (Exception e) {
            Log.e(TAG, "Failed to set license in clipboard", e);
        }
    }
    
    /**
     * Intercepts clipboard read and returns fixed license.
     * Call this method wherever the app reads from clipboard for login.
     * 
     * @return the fixed license value
     */
    public String getFixedLicense() {
        Log.i(TAG, "Clipboard read intercepted, returning fixed license: " + FIXED_LICENSE);
        // Also set it in actual clipboard for compatibility
        setFixedLicenseInClipboard();
        return FIXED_LICENSE;
    }
    
    /**
     * Checks if the clipboard contains a license-like string.
     * If yes, replaces it with the fixed license.
     */
    public void interceptAndReplace() {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                ClipData clipData = clipboard.getPrimaryClip();
                if (clipData != null && clipData.getItemCount() > 0) {
                    CharSequence text = clipData.getItemAt(0).getText();
                    if (text != null) {
                        String clipText = text.toString();
                        // If clipboard contains something that looks like a license/key
                        if (clipText.contains(":") || clipText.length() > 5) {
                            Log.i(TAG, "Detected potential license in clipboard, replacing with fixed license");
                            setFixedLicenseInClipboard();
                        }
                    }
                }
            } else {
                // No clipboard content, set the fixed license
                setFixedLicenseInClipboard();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during clipboard interception", e);
            // Fallback: just set the fixed license
            setFixedLicenseInClipboard();
        }
    }
}
