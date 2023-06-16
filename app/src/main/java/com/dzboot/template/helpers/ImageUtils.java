package com.dzboot.template.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import static com.dzboot.template.helpers.DisplayUtils.dpToPx;


@SuppressWarnings("unused")
public class ImageUtils {

   /**
    * Resize a drawable
    *
    * @param context  the context
    * @param drawable the drawable to resize
    * @param sizeDp   the new size in dp
    * @return new resized drawable
    */
   @NonNull
   public static Drawable resizeDrawable(@NonNull Context context, @DrawableRes int drawable, int sizeDp) {
      int size = dpToPx(context, sizeDp);
      Bitmap b = ((BitmapDrawable) context.getResources().getDrawable(drawable)).getBitmap();
      Bitmap bitmapResized = Bitmap.createScaledBitmap(b, size, size, false);
      return new BitmapDrawable(context.getResources(), bitmapResized);
   }

   /**
    * This function will decode the file stream from path with appropriate size you need.
    * //Specify the max size in the class
    *
    * @param f         the file
    * @param maxHeight height in px
    * @param maxWidth  width in px
    * @return decoded file
    * @throws Exception Something went wrong
    */
   public static Bitmap decodeFile(File f, int maxHeight, int maxWidth) throws Exception {
      //Decode image size
      BitmapFactory.Options o = new BitmapFactory.Options();
      o.inJustDecodeBounds = true;

      FileInputStream fis = new FileInputStream(f);
      BitmapFactory.decodeStream(fis, null, o);
      fis.close();

      int scale = 1;
      int maxSize = Math.max(maxHeight, maxWidth);
      if (o.outHeight > maxHeight || o.outWidth > maxWidth) {
         scale = (int) Math.pow(2, (int) Math.ceil(Math.log(maxSize /
               (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
      }

      //Decode with inSampleSize
      BitmapFactory.Options o2 = new BitmapFactory.Options();
      o2.inSampleSize = scale;
      fis = new FileInputStream(f);

      Bitmap b = BitmapFactory.decodeStream(fis, null, o2);
      fis.close();

      return b;
   }

   /**
    * Makes drawable fit inside TextView, for now this is to set DrawableStart, implement the others
    *
    * @param textView          the textview
    * @param drawable          the drawable resource id
    * @param drawablePaddingDp drawable padding id dp
    */
   @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
   public static void fitDrawableStartIntoTextView(@NonNull TextView textView,
                                                   @DrawableRes int drawable,
                                                   int drawablePaddingDp) {
      textView.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
               @Override
               public void onGlobalLayout() {
                  Drawable img = textView.getResources().getDrawable(drawable);
                  img.setBounds(0, 0,
                        img.getIntrinsicWidth() * textView.getMeasuredHeight() / img.getIntrinsicHeight(),
                        textView.getMeasuredHeight());
                  textView.setCompoundDrawablesRelative(img, null, null, null);
                  textView.setCompoundDrawablePadding(dpToPx(textView.getContext(), drawablePaddingDp));
                  textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
               }
            });
   }
}
