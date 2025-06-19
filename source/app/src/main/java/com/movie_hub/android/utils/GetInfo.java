package com.movie_hub.android.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;

import org.hashids.Hashids;

import timber.log.Timber;

public class GetInfo {

    private GetInfo() {
        //private constructor
    }

    private static int valDefaults = 0xffffff;

    public static String getDeviceID(Context context) {
        try {
            String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            if (deviceId == null) {
                deviceId = valDefaults + "";
            }
            return deviceId;
        } catch (Exception e) {
            return valDefaults + "";
        }

    }

    public static String getBluetoothMacAddress() {
        try {
            BluetoothAdapter mBluetoothadapter = null; // Local Bluetooth adapter
            mBluetoothadapter = BluetoothAdapter.getDefaultAdapter();
            String mSzbtmac = mBluetoothadapter.getAddress();
            if (mSzbtmac == null) {
                return valDefaults + "";
            }
            return mSzbtmac;
        } catch (Exception e) {
            return valDefaults + "";
        }

    }

    public static String getWifiMacAdress(Context context) {
        try {
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String mSzWLANMAC = wm.getConnectionInfo().getMacAddress();
            if (mSzWLANMAC == null) {
                mSzWLANMAC = valDefaults + "";
            }
            Timber.d("====================> Mac: %s", mSzWLANMAC);
            return mSzWLANMAC;
        } catch (Exception e) {
            return valDefaults + "";
        }

    }


//    public static String getAll(Context c) {
//        String hash = getDeviceID(c) + getBluetoothMacAddress() + getWifiMacAdress(c) + getBluetoothMacAddress() + getDeviceID(c);
//        /**String  wifi = getWifiMacAdress(c).replace(":", "");**/
//        String rs = convertToA(hash);
//        StringBuilder kq = new StringBuilder(rs);
//        Hashids hashids = new Hashids(getWifiMacAdress(c), 19);
//        String id = hashids.encode(Integer.parseInt(kq.toString()));
//        return id.toUpperCase();
//    }

    public static String getAll(Context c) {
        String hash = getDeviceID(c) + getBluetoothMacAddress() + getWifiMacAdress(c) + getBluetoothMacAddress() + getDeviceID(c);
        String wifi = getWifiMacAdress(c).replace(":", "");
        String rs = convertToA(hash);
        String kq = rs;
        Hashids hashids = new Hashids(getWifiMacAdress(c), 10);
        String id = hashids.encode(Integer.parseInt(kq));

        return id.toUpperCase();
    }


    private static String convertToA(String src) {
        double result = 0;
        double x = 0;
        double y = 0;
        for (int i = 0; i < src.length(); i++) {
            double numberI = (int) src.charAt(i);
            y += 1.577;
            x = x + (numberI * y * 2.25);
            result += x;
        }

        return (int) result + "";
    }

}
