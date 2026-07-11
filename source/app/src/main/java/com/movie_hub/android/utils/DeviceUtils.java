package com.movie_hub.android.utils;

import static android.content.Context.UI_MODE_SERVICE;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import timber.log.Timber;


public class DeviceUtils {

    private DeviceUtils() {
        //nothing
    }
    public static int convertStringToIn(String param){
        try {
            return Integer.parseInt(param);
        } catch (Exception e) {
            // TODO: handle exception
            return 0;
        }
    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
        if (activity.getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public static void hideSoftKeyboardEditText(Context context, EditText editText) {

        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    public static void openSoftKeyboard(Context context, EditText editText) {
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static boolean isTV(Context context){
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    public static boolean checkWifiEnable(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            return wifiManager.isWifiEnabled();

        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }

    public static void onOffWifi(Context context, boolean enable) {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(enable);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum DeviceTier {
        LOW_END,    // < 2GB RAM, CPU yếu
        MID_RANGE,  // 2-4GB RAM
        HIGH_END    // > 4GB RAM, flagship
    }

    public static DeviceTier getDeviceTier(Context context) {
        // 1. RAM
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memInfo);
        long totalRamMB = memInfo.totalMem / (1024 * 1024);

        // 2. CPU (số core + tốc độ)
        int cpuCores = Runtime.getRuntime().availableProcessors();
        double maxFreqGHz = getMaxCpuFreqGHz();

        //  dina
        if (totalRamMB < 2048 || cpuCores <= 4 || maxFreqGHz < 1.8) {
            return DeviceTier.LOW_END;
        } else if (totalRamMB <= 4096 || maxFreqGHz < 2.5) {
            return DeviceTier.MID_RANGE;
        } else {
            return DeviceTier.HIGH_END;
        }
    }

    private static double getMaxCpuFreqGHz() {
        try {
            java.util.Scanner scanner = new java.util.Scanner(new java.io.File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"));
            if (scanner.hasNextInt()) {
                int freqKHz = scanner.nextInt();
                return freqKHz / 1_000_000.0;
            }
        } catch (Exception ignored) {}
        return 1.8; // default
    }

    public static boolean isLowEndDevice(Context context) {
        return getDeviceTier(context) == DeviceTier.LOW_END;
    }

    public static boolean isQualcommOldChipset() {
        String hardware = Build.HARDWARE.toLowerCase();
        return hardware.contains("msm8916") || // Snapdragon 410
                hardware.contains("msm8909") || // Snapdragon 208
                hardware.contains("msm8917");   // Snapdragon 425
    }
}
