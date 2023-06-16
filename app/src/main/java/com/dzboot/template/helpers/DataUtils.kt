package com.dzboot.template.helpers

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import okhttp3.internal.and
import java.util.*


@SuppressLint("unused")
object DataUtils {

   /**
    * Copies text to clipboard
    *
    * @param context The context used
    * @param label   Label to text to copy
    * @param text    Text to copy
    */
   fun copyToClipboard(context: Context, label: String?, text: String?) {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText(label, text)
      clipboard.setPrimaryClip(clip)
   }

   /**
    * Convert byte array to hex string
    *
    * @param bytes bytes
    * @return hex string
    */
   fun bytesToHex(bytes: ByteArray): String {
      val sbuf = StringBuilder()
      for (aByte in bytes) {
         val intVal = aByte and 0xff
         if (intVal < 0x10) sbuf.append("0")
         sbuf.append(Integer.toHexString(intVal).toUpperCase(Locale.ROOT))
      }
      return sbuf.toString()
   }

   /**
    * Get utf8 byte array.
    *
    * @param str the string
    * @return array of NULL if error was found
    */
   fun getUTF8Bytes(str: String): ByteArray? {
      return try {
         str.toByteArray(charset("UTF-8"))
      } catch (ex: Exception) {
         null
      }
   }
}