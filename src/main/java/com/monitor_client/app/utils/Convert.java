package com.monitor_client.app.utils;

import java.util.ArrayList;

public class Convert {

  public static byte[] intToByteArray(int value) {
    byte[] byteArray = new byte[4];
    byteArray[0] = (byte) (value >> 24);
    byteArray[1] = (byte) (value >> 16);
    byteArray[2] = (byte) (value >> 8);
    byteArray[3] = (byte) (value);
    return byteArray;
  }

  public static int byteArrayToInt(byte bytes[]) {
    return ((((int) bytes[0] & 0xff) << 24) | 
            (((int) bytes[1] & 0xff) << 16) | 
            (((int) bytes[2] & 0xff) << 8)  | 
            (((int) bytes[3] & 0xff)));
  }

  public static String[] getStringArray(ArrayList<String> arr) {
    // declaration and initialise String Array
    String str[] = new String[arr.size()];
    // ArrayList to Array Conversion
    for (int j = 0; j < arr.size(); j++) {
      // Assign each value to String array
      str[j] = arr.get(j);
    }

    return str;
  }
}