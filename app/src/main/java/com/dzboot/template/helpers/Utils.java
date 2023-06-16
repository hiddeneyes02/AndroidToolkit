package com.dzboot.template.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.util.Pair;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import timber.log.Timber;


@SuppressWarnings("unused")
public class Utils {

   /**
    * Appends colored text a TextView
    *
    * @param tv    the TextView
    * @param text  the text to append
    * @param color the resource id of the text's color
    */
   public static void appendColoredText(@NotNull TextView tv, String text, @ColorRes int color) {
      int start = tv.getText().length();
      tv.append(text);
      int end = tv.getText().length();

      Spannable spannableText = (Spannable) tv.getText();
      spannableText.setSpan(new ForegroundColorSpan(tv.getContext().getResources().getColor(color)),
                            start, end, 0
      );
   }

   /**
    * Checks whether device has GPS receiver or not
    *
    * @param context Context
    * @return true if device has GPS receiver, false otherwise
    */
   public static boolean hasGPSReceiver(@NonNull Context context) {
      PackageManager packageManager = context.getPackageManager();
      return packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
   }

   /**
    * Displays a dialog to prompt for GPS activation
    *
    * @param context the context in which the dialog is displayed
    */
   public static void turnGPSOn(@NonNull Context context) {
      final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

      if (manager != null && !manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

         //         new MaterialStyledDialog.Builder(context)
         //               .setTitle(R.string.gps_disabled_title)
         //               .setDescription(R.string.gps_disabled_message)
         //               .setStyle(Style.HEADER_WITH_TITLE)
         //               .setPositiveText(R.string.contin)
         //               .onPositive((dialog, which) ->
         //                     context.startActivity(new Intent(android.provider.Settings
         //                     .ACTION_LOCATION_SOURCE_SETTINGS)))
         //               .setNegativeText(R.string.cancel)
         //               .withDialogAnimation(true)
         //               .show();
      }
   }

   public static String bitmapToString(@NonNull Bitmap bmp) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
      byte[] imageBytes = baos.toByteArray();
      return Base64.encodeToString(imageBytes, Base64.DEFAULT);
   }

   public static String getRealPathFromURI(@NonNull Context context, Uri contentURI) {
      Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
      if (cursor == null) { // Source is Dropbox or other similar local file path
         return contentURI.getPath();
      } else {
         cursor.moveToFirst();
         int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
         String result = cursor.getString(idx);
         cursor.close();
         return result;
      }
   }

   /* Checks if external storage is available for read and write */
   public static boolean isExternalStorageWritable() {
      return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
   }

   /* Checks if external storage is available to at least read */
   public static boolean isExternalStorageReadable() {
      String state = Environment.getExternalStorageState();
      return Environment.MEDIA_MOUNTED.equals(state) ||
             Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
   }

   /**
    * Forces character to be the first character in the edit text
    *
    * @param editText  The EditText object
    * @param character The character
    */
   public static void forceFirstCharInEditText(
         @NonNull EditText editText,
         @NonNull Character character
   ) {
      //white space not allowed
      if (character.equals(' ')) {
         return;
      }

      Selection.setSelection(editText.getText(), editText.getText().length());

      editText.addTextChangedListener(new TextWatcher() {

         @Override
         public void onTextChanged(CharSequence s, int start, int before, int count) {
         }

         @Override
         public void beforeTextChanged(CharSequence s, int start, int count, int after) {
         }

         @SuppressLint("SetTextI18n")
         @Override
         public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (!text.startsWith(character.toString())) {
               editText.setText(character + text);
               Selection.setSelection(editText.getText(), editText.getText().length());
            }
         }
      });
   }


   private void setLocale(@NonNull AppCompatActivity activity, @NotNull String language) {

      Locale locale;
      if (language.equals("not-set")) {
         locale = Locale.getDefault();
      } else {
         locale = new Locale(language);
      }
      Locale.setDefault(locale);
      Configuration config = new Configuration();
      config.locale = locale;
      activity.getBaseContext().getResources().updateConfiguration(
            config,
            activity.getBaseContext().getResources().getDisplayMetrics()
      );
   }

   public static void printHashKey(@NonNull Context context) {
      // Add code to print out the key hash
      try {
         PackageInfo info = context.getPackageManager().getPackageInfo(
               context.getPackageName(),
               PackageManager.GET_SIGNATURES
         );
         for (Signature signature : info.signatures) {
            MessageDigest md = MessageDigest.getInstance("SHA");
            md.update(signature.toByteArray());
            Timber.tag("KeyHash:").d(Base64.encodeToString(md.digest(), Base64.DEFAULT));
         }
      } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

      }
   }

   public static void setPartOfTextViewClickable(
         @NotNull TextView target,
         @StringRes int nonClickablePart,
         @StringRes int clickablePart,
         ClickableSpan clickableSpan
   ) {
      String nonClickablePartString = target.getContext().getString(nonClickablePart);
      String clickablePartString = target.getContext().getString(clickablePart);

      SpannableStringBuilder builder = new SpannableStringBuilder(nonClickablePartString);
      builder.append(clickablePartString);
      //TODO make position variable
      builder.setSpan(clickableSpan, builder.length() - clickablePartString.length(), builder.length(), 0);
      target.setMovementMethod(LinkMovementMethod.getInstance());
      target.setText(builder, TextView.BufferType.SPANNABLE);
   }

   /**
    * Sets parts of text clickable, and sets an action (ClickableSpan) to execute when each part is clicked
    *
    * @param target          TextView where to put the whole text
    * @param formattedString Formatted text to put into the TextView (e.g. Click %s) %s will be replaced with 'here'
    * @param partsPairs      Pair of the clickable text ('here' in the previous example) and the ClickableSpan to be
    *                        executed when the text is clicked
    */
   @SafeVarargs
   public static void setPartsOfTextViewClickable(
         TextView target,
         String formattedString,
         @NotNull Pair<String, ClickableSpan>... partsPairs
   ) {

      String[] parts = new String[partsPairs.length];
      for (int i = 0; i < partsPairs.length; i++) {
         parts[i] = partsPairs[i].first;
      }

      String finalString = String.format(formattedString, (Object[]) parts);
      SpannableStringBuilder builder = new SpannableStringBuilder(finalString);

      for (Pair<String, ClickableSpan> pair : partsPairs) {
         int start = finalString.indexOf(pair.first);
         builder.setSpan(pair.second, start, start + pair.first.length(), 0);
      }
      target.setMovementMethod(LinkMovementMethod.getInstance());
      target.setText(builder, TextView.BufferType.SPANNABLE);
   }

   public static boolean isValidUrl(String url) {
      return URLUtil.isValidUrl(url) && !url.equals("http://") && !url.equals("https://");
   }
}