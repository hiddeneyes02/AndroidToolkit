package com.dzboot.template.helpers;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.CursorLoader;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


@SuppressWarnings("unused")
public class FileUtils {

   /**
    * Load UTF8withBOM or any ansi text file.
    *
    * @param filename the file name
    * @return String representation of the file
    * @throws java.io.IOException Exception when something goes wrong
    */
   @NonNull
   public static String loadFileAsString(String filename) throws java.io.IOException {
      final int BUFLEN = 1024;
      try (BufferedInputStream is = new BufferedInputStream(new FileInputStream(filename), BUFLEN)) {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFLEN);
         byte[] bytes = new byte[BUFLEN];
         boolean isUTF8 = false;
         int read, count = 0;
         while ((read = is.read(bytes)) != -1) {
            if (count == 0 && bytes[0] == (byte) 0xEF && bytes[1] == (byte) 0xBB && bytes[2] == (byte) 0xBF) {
               isUTF8 = true;
               baos.write(bytes, 3, read - 3); // drop UTF8 bom marker
            } else {
               baos.write(bytes, 0, read);
            }
            count += read;
         }
         return isUTF8 ? new String(baos.toByteArray(), "UTF-8") : new String(baos.toByteArray());
      }
   }

   /**
    * Get the file's extension
    *
    * @param file the file
    * @return the extension, empty string if could not find an extension
    */
   @NonNull
   public static String getFileExtension(@NonNull File file) {
      String fileName = file.getName();
      if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
         return fileName.substring(fileName.lastIndexOf(".") + 1);
      else return "";
   }

   /**
    * Get the file's name with extension
    *
    * @param fileCompletePath file's location
    * @return File's name with extension, null if not found
    */
   public static String getFileDisplayName(String fileCompletePath) {
      String[] ss = fileCompletePath.split("/");
      return ss.length == 0 ? null : ss[ss.length - 1];
   }


   @Nullable
   public static String getFileDisplayName(@NonNull Context context, Uri fileUri) {
      String uriString = fileUri.toString();
      File myFile = new File(uriString);
      String displayName = null;

      if (uriString.startsWith("content://")) {
         try (Cursor cursor = context.getContentResolver()
                                     .query(Uri.parse(uriString), null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
               displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
         }
      } else if (uriString.startsWith("file://")) {
         displayName = myFile.getName();
      }

      return displayName;
   }

   public static String getRealPath(Context context, Uri fileUri) {
      String realPath;
      // SDK < API11
      if (Build.VERSION.SDK_INT < 11) {
         realPath = FileUtils.getRealPathFromURI_BelowAPI11(context, fileUri);
      }
      // SDK >= 11 && SDK < 19
      else if (Build.VERSION.SDK_INT < 19) {
         realPath = FileUtils.getRealPathFromURI_API11to18(context, fileUri);
      }
      // SDK > 19 (Android 4.4) and up
      else {
         realPath = FileUtils.getRealPathFromURI_API19(context, fileUri);
      }
      return realPath;
   }


   @SuppressLint("NewApi")
   public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
      String[] proj = {MediaStore.Images.Media.DATA};
      String result = null;

      CursorLoader cursorLoader = new CursorLoader(context, contentUri, proj, null, null, null);
      Cursor cursor = cursorLoader.loadInBackground();

      if (cursor != null) {
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         result = cursor.getString(column_index);
         cursor.close();
      }
      return result;
   }

   public static String getRealPathFromURI_BelowAPI11(@NotNull Context context, Uri contentUri) {
      String[] proj = {MediaStore.Images.Media.DATA};
      Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
      int column_index = 0;
      String result = "";
      if (cursor != null) {
         column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         result = cursor.getString(column_index);
         cursor.close();
         return result;
      }
      return result;
   }

   @Nullable
   @SuppressLint("NewApi")
   public static String getRealPathFromURI_API19(final Context context, final Uri uri) {

      final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

      // DocumentProvider
      if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
         // ExternalStorageProvider
         if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            // This is for checking Main Memory
            if ("primary".equalsIgnoreCase(type)) {
               if (split.length > 1) {
                  return Environment.getExternalStorageDirectory() + "/" + split[1];
               } else {
                  return Environment.getExternalStorageDirectory() + "/";
               }
               // This is for checking SD Card
            } else {
               return "storage" + "/" + docId.replace(":", "/");
            }
         }
         // DownloadsProvider
         else if (isDownloadsDocument(uri)) {
            String fileName = getFilePath(context, uri);
            if (fileName != null) {
               return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
            }

            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                  Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            return getDataColumn(context, contentUri, null, null);
         }
         // MediaProvider
         else if (isMediaDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
               contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
               contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
               contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                  split[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
         }
      }
      // MediaStore (and general)
      else if ("content".equalsIgnoreCase(uri.getScheme())) {

         // Return the remote address
         if (isGooglePhotosUri(uri))
            return uri.getLastPathSegment();

         return getDataColumn(context, uri, null, null);
      }
      // File
      else if ("file".equalsIgnoreCase(uri.getScheme())) {
         return uri.getPath();
      }

      return null;
   }

   @Nullable
   public static String getDataColumn(@NotNull Context context, Uri uri, String selection, String[] selectionArgs) {

      Cursor cursor = null;
      final String column = "_data";
      final String[] projection = {
            column
      };

      try {
         cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                                                     null
         );
         if (cursor != null && cursor.moveToFirst()) {
            final int index = cursor.getColumnIndexOrThrow(column);
            return cursor.getString(index);
         }
      } finally {
         if (cursor != null)
            cursor.close();
      }
      return null;
   }


   @Nullable
   public static String getFilePath(@NotNull Context context, Uri uri) {

      final String[] projection = {
            MediaStore.MediaColumns.DISPLAY_NAME
      };

      try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
         if (cursor != null && cursor.moveToFirst()) {
            final int index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
            return cursor.getString(index);
         }
      }
      return null;
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is ExternalStorageProvider.
    */
   public static boolean isExternalStorageDocument(@NotNull Uri uri) {
      return "com.android.externalstorage.documents".equals(uri.getAuthority());
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is DownloadsProvider.
    */
   public static boolean isDownloadsDocument(@NotNull Uri uri) {
      return "com.android.providers.downloads.documents".equals(uri.getAuthority());
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is MediaProvider.
    */
   public static boolean isMediaDocument(@NotNull Uri uri) {
      return "com.android.providers.media.documents".equals(uri.getAuthority());
   }

   /**
    * @param uri The Uri to check.
    * @return Whether the Uri authority is Google Photos.
    */
   public static boolean isGooglePhotosUri(@NotNull Uri uri) {
      return "com.google.android.apps.photos.content".equals(uri.getAuthority());
   }


   /**
    * Creates log file and puts data in it. This needs WRITE_STORAGE_PERMISSION
    * TODO folder name must be configurable once
    *
    * @param msg the message to insert
    */

   public static void log(String msg) {

      try {
         File parent = new File(Environment.getExternalStoragePublicDirectory("VPN").getPath());
         if (!parent.exists() && !parent.mkdirs())
            throw new IOException("Can not create parent folder");

         File log = new File(parent, "log.txt");
         if (!log.exists() && !log.createNewFile())
            throw new IOException("Can not create log file");

         FileOutputStream out = new FileOutputStream(log, true);
         SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US);
         df.setTimeZone(TimeZone.getTimeZone("UTC"));
         out.write(("[" + df.format(new Date()) + "] " + msg + "\n").getBytes());
         out.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Deletes app's cache
    * @param context non-null context
    */
   //TODO needs testing
   public static void deleteCache(@NonNull Context context) {
      new Thread(() -> {
         try {
            File dir = context.getCacheDir();
            deleteDir(dir);
         } catch (Exception ignored) {}
      }).start();
   }

   private static boolean deleteDir(File dir) {
      if (dir == null)
         return false;

      if (dir.isDirectory()) {
         String[] children = dir.list();
         if (children == null)
            return  false;

         for (String child : children) {
            if (!deleteDir(new File(dir, child)))
               return false;
         }
         return dir.delete();
      }

      return dir.isFile() && dir.delete();
   }
}
