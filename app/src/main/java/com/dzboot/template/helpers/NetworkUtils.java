package com.dzboot.template.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


@SuppressWarnings("unused")
public class NetworkUtils {

   /**
    * Returns MAC address of the given interface name.
    *
    * @param interfaceName eth0, wlan0 or null = use first interface
    * @return mac address or empty string
    */
   @NonNull
   public static String getMACAddress(String interfaceName) {
      try {
         List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
         for (NetworkInterface intf : interfaces) {
            if (interfaceName != null) {
               if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
            }
            byte[] mac = intf.getHardwareAddress();
            if (mac == null) return "";
            StringBuilder buf = new StringBuilder();
            for (byte aMac : mac) buf.append(String.format("%02X:", aMac));
            if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
            return buf.toString();
         }
      } catch (Exception ignore) {
      }
      return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
   }

   /**
    * Get IP address from first non-localhost interface
    *
    * @param useIPv4 true=return ipv4, false=return ipv6
    * @return address or empty string
    */
   public static String getIPAddress(boolean useIPv4) {
      try {
         List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
         for (NetworkInterface intf : interfaces) {
            List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
            for (InetAddress addr : addrs) {
               if (!addr.isLoopbackAddress()) {
                  String sAddr = addr.getHostAddress();
                  boolean isIPv4 = sAddr.indexOf(':') < 0;

                  if (useIPv4) {
                     if (isIPv4)
                        return sAddr;
                  } else {
                     if (!isIPv4) {
                        int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                        return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                     }
                  }
               }
            }
         }
      } catch (Exception ignore) {
      } // for now eat exceptions
      return "";
   }

   /**
    * Checks if connected to Internet
    *
    * @param c The context
    * @return true if connected to Internet, false if not
    */
   public static boolean isConnected(@NonNull Context c) {
      ConnectivityManager m = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
      return m != null
            && (m.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
            || m.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
   }

   /**
    * Checks if connected to Internet, and shows a dialog if not. Using the dialog, you can either
    * retry connecting, or stop which will make the activity finish
    *
    * @param activity      the activity
    * @param retryButton   retry button text as string resource
    * @param stopButton    stop button text as string resource
    * @param dialogTitle   dialog title text as string resource
    * @param dialogMessage dialog message text as string resource
    * @return true if connected to Internet, false if not
    */
   public static boolean isConnectedToInternet(@NonNull AppCompatActivity activity,
                                               @StringRes int retryButton,
                                               @StringRes int stopButton,
                                               @StringRes int dialogTitle,
                                               @StringRes int dialogMessage) {
      boolean result = false;
      ConnectivityManager m = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (m != null) {
         NetworkInfo activeNetworkInfo = m.getActiveNetworkInfo();
         result = activeNetworkInfo != null && activeNetworkInfo.isConnected();
      }

      if (result) {
         return true;
      }

      new AlertDialog.Builder(activity)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(retryButton, (dialog, which) -> activity.recreate())
            .setNegativeButton(stopButton, (dialog, which) -> activity.finish())
            .show();

      return false;
   }
}
