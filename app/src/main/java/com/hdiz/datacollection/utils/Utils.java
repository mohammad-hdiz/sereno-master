package com.hdiz.datacollection.utils;

import android.hardware.usb.UsbDevice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Utils {
    private static Boolean isFlirServiceEnabled = false;

    public static UsbDevice getFlirDevice(HashMap<String, UsbDevice> deviceList) {
        for (UsbDevice usbDevice : deviceList.values()) {
            if (usbDevice.getProductName().startsWith("FLIR")) {
                return usbDevice;
            }
        }
        return null;
    }


    public static Boolean isFlirServiceEnabled() {
        return isFlirServiceEnabled;
    }

    public static void setIsFlirServiceEnabled(Boolean isFlirServiceEnabled) {
        Utils.isFlirServiceEnabled = isFlirServiceEnabled;
    }
}
