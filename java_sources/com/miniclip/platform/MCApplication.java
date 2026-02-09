package com.miniclip.platform;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.miniclip.framework.AbstractActivityListener;
import com.miniclip.framework.Miniclip;
import com.miniclip.framework.ThreadingContext;
import com.miniclip.license.LicenseManager;
import com.miniclip.license.ClipboardInterceptor;
import com.miniclip.utils.ErrorDialog;
import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
import java.util.regex.Pattern;

/* loaded from: /home/runner/work/rynel8bp/rynel8bp/classes.dex */
public class MCApplication extends AbstractActivityListener {
    private static Activity MCActivity = null;
    private static final String TAG = "MCApplication";
    private static String currentIntentDataString = null;
    private static boolean isInitialized = false;
    private static int numMemoryWarnings = 0;
    private static String permissionSettingsRedirectExplanation = null;
    private static RequestSelfPermissionCallback requestSelfPermissionResultCallback = null;
    private static long requestSelfPermissionResultCallbackNative = 0;
    private static boolean shouldEnableRequestPermissionRationale = true;

    public interface RequestSelfPermissionCallback {
        void response(boolean z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeLowMemory();

    /* JADX INFO: Access modifiers changed from: private */
    public static native void nativeNewIntentDataString(String str);

    private static native void nativeSetAPKPath(String str);

    private static native void nativeSetApplicationVersionNumber(String str);

    private static native void nativeSetCountryCode(String str);

    private static native void nativeSetLanguage(String str);

    /* JADX INFO: Access modifiers changed from: private */
    public static native void onRequestSelfPermissionResult(long j, boolean z);

    public static void init() {
        if (isInitialized) {
            return;
        }
        isInitialized = true;
        Activity activity = Miniclip.getActivity();
        MCActivity = activity;
        currentIntentDataString = activity.getIntent().getDataString();
        Miniclip.addListener(new MCApplication());
        
        // Initialize clipboard interceptor FIRST to catch any early clipboard operations
        ClipboardInterceptor clipboardInterceptor = ClipboardInterceptor.getInstance(activity);
        clipboardInterceptor.setFixedLicenseInClipboard();
        Log.i(TAG, "Clipboard interceptor initialized with fixed license");
        
        // Initialize and check license
        LicenseManager licenseManager = LicenseManager.getInstance(activity);
        boolean isLicensed = licenseManager.checkLicense();
        Log.i(TAG, "License check completed. Valid: " + isLicensed);
    }

    public static String getApplicationVersionNumber() {
        try {
            PackageInfo packageInfo = MCActivity.getPackageManager().getPackageInfo(MCActivity.getPackageName(), 0);
            if (packageInfo == null) {
                return "";
            }
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException unused) {
            return "";
        }
    }

    public static void firstRun() {
        nativeSetCountryCode(Locale.getDefault().getCountry());
        try {
            nativeSetAPKPath(MCActivity.getPackageManager().getApplicationInfo(MCActivity.getPackageName(), 0).sourceDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        nativeSetLanguage(Locale.getDefault().toLanguageTag());
        nativeSetApplicationVersionNumber(getApplicationVersionNumber());
    }

    public static String getApplicationName() {
        return MCActivity.getString(MCActivity.getApplicationInfo().labelRes);
    }

    public static String getApplicationPackageName() {
        return MCActivity.getPackageName();
    }

    public static void signalFatalError(int i) {
        ErrorDialog.signalFatalError(i);
    }

    public static int getFatalErrorCode() {
        return ErrorDialog.getFatalErrorCode();
    }

    public static boolean hasFatalErrorOccurred() {
        return ErrorDialog.hasFatalErrorOccurred();
    }

    public static int getCpuCoreCount() {
        try {
            return new File("/sys/devices/system/cpu/").listFiles(new FileFilter() { // from class: com.miniclip.platform.MCApplication.1CpuFilter
                @Override // java.io.FileFilter
                public boolean accept(File file) {
                    return Pattern.matches("cpu[0-9]", file.getName());
                }
            }).length;
        } catch (Exception unused) {
            return 1;
        }
    }

    public static int getApplicationVersionCode() {
        try {
            PackageInfo packageInfo = MCActivity.getPackageManager().getPackageInfo(MCActivity.getPackageName(), 0);
            if (packageInfo != null) {
                return packageInfo.versionCode;
            }
            return 0;
        } catch (PackageManager.NameNotFoundException unused) {
            return 0;
        }
    }

    public static String getStringValWithKeyFromMetadata(String str) {
        try {
            Bundle bundle = MCActivity.getPackageManager().getApplicationInfo(MCActivity.getPackageName(), 128).metaData;
            if (bundle != null) {
                return bundle.getString(str);
            }
            return null;
        } catch (PackageManager.NameNotFoundException unused) {
            return null;
        }
    }

    public static String getIntentDataString() {
        String str = currentIntentDataString;
        return str != null ? str : "";
    }

    public static boolean isLargeScreen() {
        int i = MCActivity.getResources().getConfiguration().screenLayout & 15;
        return i == 3 || i == 4;
    }

    public static float getScreenDensity() {
        return MCActivity.getResources().getDisplayMetrics().density;
    }

    public static float getScreenDPI() {
        return MCActivity.getResources().getDisplayMetrics().ydpi;
    }

    public static int getOrientation() {
        DisplayMetrics displayMetrics = MCActivity.getApplicationContext().getResources().getDisplayMetrics();
        if (displayMetrics.widthPixels == displayMetrics.heightPixels) {
            return 3;
        }
        return displayMetrics.widthPixels < displayMetrics.heightPixels ? 1 : 2;
    }

    private static int getCurrentOrientation() {
        int rotation = ((WindowManager) MCActivity.getSystemService("window")).getDefaultDisplay().getRotation();
        if (rotation == 0) {
            return 1;
        }
        if (rotation != 1) {
            if (rotation == 2) {
                return 9;
            }
            if (rotation == 3) {
                return 8;
            }
        }
        return 0;
    }

    public static void setAutoRotate(int i) {
        int requestedOrientation = MCActivity.getRequestedOrientation();
        boolean z = requestedOrientation == 1 || requestedOrientation == 9 || requestedOrientation == 7;
        if (i == 1) {
            if (z) {
                MCActivity.setRequestedOrientation(7);
                return;
            } else {
                MCActivity.setRequestedOrientation(6);
                return;
            }
        }
        int currentOrientation = getCurrentOrientation();
        boolean z2 = currentOrientation == 1 || currentOrientation == 9;
        boolean z3 = currentOrientation == 0 || currentOrientation == 8;
        if (!(z && z2) && (z || !z3)) {
            return;
        }
        MCActivity.setRequestedOrientation(currentOrientation);
    }

    public static boolean isDeviceOnline() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) MCActivity.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            return false;
        }
        return activeNetworkInfo.isConnectedOrConnecting();
    }

    public static boolean isDeviceRooted() {
        String[] strArr = {"/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"};
        for (int i = 0; i < 8; i++) {
            if (new File(strArr[i] + "su").exists()) {
                return true;
            }
        }
        return false;
    }

    public static void disableRequestPermissionRationale() {
        shouldEnableRequestPermissionRationale = false;
    }

    public static void requestSelfPermission(String str, String str2, String str3, RequestSelfPermissionCallback requestSelfPermissionCallback) {
        requestSelfPermission(str, str2, str3, 0L, requestSelfPermissionCallback);
    }

    public static void requestSelfPermission(String str, String str2, String str3, long j) {
        requestSelfPermission(str, str2, str3, j, null);
    }

    public static void requestSelfPermission(final String str, final String str2, String str3, long j, RequestSelfPermissionCallback requestSelfPermissionCallback) {
        if (requestSelfPermissionResultCallback != null || requestSelfPermissionResultCallbackNative != 0) {
            Log.i(TAG, "request aborted: we have one request already in progress");
            return;
        }
        permissionSettingsRedirectExplanation = str3;
        requestSelfPermissionResultCallback = requestSelfPermissionCallback;
        requestSelfPermissionResultCallbackNative = j;
        if (MCActivity.checkSelfPermission(str) != 0) {
            if (MCActivity.shouldShowRequestPermissionRationale(str)) {
                if (shouldEnableRequestPermissionRationale) {
                    Log.i(TAG, "show why need the permission: " + str);
                    MCActivity.runOnUiThread(new Runnable() { // from class: com.miniclip.platform.MCApplication.1
                        @Override // java.lang.Runnable
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MCApplication.MCActivity);
                            builder.setMessage(str2);
                            builder.setCancelable(false);
                            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.miniclip.platform.MCApplication.1.1
                                @Override // android.content.DialogInterface.OnClickListener
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Log.i(MCApplication.TAG, "ask for permission: " + str);
                                    MCApplication.MCActivity.requestPermissions(new String[]{str}, 0);
                                    dialogInterface.cancel();
                                }
                            });
                            builder.create().show();
                        }
                    });
                    return;
                }
                return;
            }
            Log.i(TAG, "ask for permission: " + str);
            MCActivity.requestPermissions(new String[]{str}, 0);
            return;
        }
        Log.i(TAG, "permission granted: already granted");
        callOnRequestSelfPermissionResult(true);
    }

    private static void callOnRequestSelfPermissionResult(final boolean z) {
        permissionSettingsRedirectExplanation = null;
        RequestSelfPermissionCallback requestSelfPermissionCallback = requestSelfPermissionResultCallback;
        if (requestSelfPermissionCallback != null) {
            requestSelfPermissionResultCallback = null;
            requestSelfPermissionCallback.response(z);
        }
        final long j = requestSelfPermissionResultCallbackNative;
        if (j != 0) {
            requestSelfPermissionResultCallbackNative = 0L;
            Miniclip.queueEvent(ThreadingContext.Main, new Runnable() { // from class: com.miniclip.platform.MCApplication.2
                @Override // java.lang.Runnable
                public void run() {
                    MCApplication.onRequestSelfPermissionResult(j, z);
                }
            });
        }
    }

    public static boolean checkSelfPermission(String str) {
        return MCActivity.checkSelfPermission(str) == 0;
    }

    private static boolean hasVibrator() {
        Vibrator vibrator = (Vibrator) MCActivity.getSystemService("vibrator");
        if (vibrator != null) {
            return vibrator.hasVibrator();
        }
        return false;
    }

    public void onNewIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String trim = Uri.decode(data.toString()).trim();
            currentIntentDataString = trim;
            if (trim != null) {
                Miniclip.queueEvent(ThreadingContext.Main, new Runnable() { // from class: com.miniclip.platform.MCApplication.3
                    @Override // java.lang.Runnable
                    public void run() {
                        MCApplication.nativeNewIntentDataString(MCApplication.currentIntentDataString);
                    }
                });
            }
        }
    }

    public void onLowMemory() {
        Log.i("MEMORY WARNING", "LOW MEMORY");
        int i = numMemoryWarnings + 1;
        numMemoryWarnings = i;
        if (i >= 3) {
            numMemoryWarnings = 0;
            Miniclip.queueEvent(ThreadingContext.Main, new Runnable() { // from class: com.miniclip.platform.MCApplication.4
                @Override // java.lang.Runnable
                public void run() {
                    MCApplication.nativeLowMemory();
                }
            });
        }
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        String str;
        if (requestSelfPermissionResultCallback == null && requestSelfPermissionResultCallbackNative == 0) {
            Log.e(TAG, "error: callback not defined");
            return;
        }
        if (iArr.length > 0 && iArr[0] == 0) {
            Log.i(TAG, "permission granted");
            callOnRequestSelfPermissionResult(true);
            return;
        }
        Log.i(TAG, "permission not granted");
        callOnRequestSelfPermissionResult(false);
        if (shouldEnableRequestPermissionRationale) {
            if ((strArr.length > 0 && MCActivity.shouldShowRequestPermissionRationale(strArr[0])) || (str = permissionSettingsRedirectExplanation) == null || str.isEmpty()) {
                return;
            }
            MCActivity.runOnUiThread(new Runnable() { // from class: com.miniclip.platform.MCApplication.5
                @Override // java.lang.Runnable
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MCApplication.MCActivity);
                    builder.setMessage(MCApplication.permissionSettingsRedirectExplanation);
                    builder.setCancelable(true);
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.miniclip.platform.MCApplication.5.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.addFlags(268435456);
                            intent.setData(Uri.fromParts("package", MCApplication.MCActivity.getPackageName(), null));
                            MCApplication.MCActivity.startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.miniclip.platform.MCApplication.5.2
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i2) {
                        }
                    });
                    builder.create().show();
                }
            });
        }
    }
}
