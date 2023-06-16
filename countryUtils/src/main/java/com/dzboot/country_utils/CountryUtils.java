package com.dzboot.country_utils;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;


public class CountryUtils {

   /**
    * Gets the full size flag resource id from the country code
    * @param context non-null context
    * @param countryCode the two letters country code
    * @return the flag resource id, 0 if the flag does not exist
    */
   @DrawableRes
   public static int getFlagIconResIdFromCountryCode(@NonNull Context context, String countryCode) {
      return context.getResources()
                    .getIdentifier("ic_flag_" + countryCode, "drawable", context.getPackageName());
   }

   /**
    * Gets the small size flag resource id from the country code
    * @param context non-null context
    * @param countryCode the two letters country code
    * @return the flag resource id, 0 if the flag does not exist
    */
   @DrawableRes
   public static int getFlagResIdFromCountryCode(@NonNull Context context, String countryCode) {
      return context.getResources()
                    .getIdentifier("flag_" + countryCode, "drawable", context.getPackageName());
   }

   /**
    * Returns two letters country code from the country's english name
    *
    * @param englishName The english name of the country
    * @return the country code or null if the english name does not exist
    */
   @Nullable
   public static String getCountryCodeFromEnglishName(String englishName) {
      for (String code : Locale.getISOCountries()) {
         if (new Locale("", code).getDisplayCountry(Locale.US).equalsIgnoreCase(englishName))
            return code;
      }

      return null;
   }

   /**
    * Returns a localized country name from its english name
    *
    * @param englishName The english name of the country
    * @return the country code or null if the english name does not exist
    */
   @Nullable
   public static String getLocalizedNameFromEnglishName(String englishName) {
      String countryCode = getCountryCodeFromEnglishName(englishName);
      return countryCode == null ? null : getLocalizedNameFromCountryCode(countryCode);
   }

   /**
    * Returns a localized country name from its country code
    *
    * @param countryCode The localized name of the country
    * @return the country name or null if the country code does not exist
    */
   @Nullable
   public static String getLocalizedNameFromCountryCode(@NonNull String countryCode) {
      String countryName = new Locale(Locale.getDefault().getLanguage(), countryCode).getDisplayCountry();
      return countryName.equals("") ? null : countryName;
   }
}
