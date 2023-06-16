package com.dzboot.template.helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.dzboot.template.R;


public class NotifUtils {

   public static final String CHANNEL_ID = "channel";

   public static void createNotificationChannel(@NonNull Context context) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         CharSequence name = context.getString(R.string.channel_name);
         String description = context.getString(R.string.channel_description);
         int importance = NotificationManager.IMPORTANCE_DEFAULT;
         NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
         channel.setDescription(description);
         NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
         notificationManager.createNotificationChannel(channel);
      }
   }
}
