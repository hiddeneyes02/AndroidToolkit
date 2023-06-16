package com.dzboot.template.helpers

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import com.dzboot.template.R


@SuppressLint("unused")
object PackageUtils {

    /**
     * Checks if a package is installed on a device
     *
     * @param packageName package name
     * @param context     the context
     * @return true if installed, false if not
     */
    fun isPackageInstalled(packageName: String?, context: Context) = try {
        context.packageManager.getPackageInfo(packageName!!, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    /**
     * Launches the Google Play store page of specific app
     *
     * @param context     the context
     * @param packageName the app's package name
     */
    fun launchAndroidMarket(context: Context, packageName: String) = try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (anfe: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }

    /**
     * Launches the share app intent
     *
     * @param context the context
     */
    fun shareApp(context: Context) = with(Intent()) {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_message, context.packageName))
        context.startActivity(this)
    }

    /**
     * Launches the Google play page for this app
     *
     * @param context the context
     */
    fun rateApp(context: Context) = try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + context.packageName)
            )
        )
    } catch (anfe: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)
            )
        )
    }

    /**
     * Launches Google Play page for this developer
     *
     * @param context     the context
     * @param developerId the developer ID
     */
    fun showMoreApps(context: Context, developerId: String) = try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://search?q=pub:$developerId")
            )
        )
    } catch (anfe: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/developer?id=$developerId")
            )
        )
    }

    /**
     * Launches the email app
     *
     * @param context     the context
     * @param email       receiver's email as string
     * @param subject     the subject
     * @param message     the message
     * @param chooserPrompt the intent title
     */
    fun emailUs(
        context: Context, email: String,
        @StringRes subject: Int,
        @StringRes message: Int,
        @StringRes chooserPrompt: Int = R.string.email_prompt
    ) = with(Intent(Intent.ACTION_SENDTO)) {
        //look for closest candidate email app
        type = "text/plain"
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, message)
        if (resolveActivity(context.packageManager) != null) {
            context.startActivity(this)
            return
        }

        //fallback to chooser intent
        action = Intent.ACTION_SEND
        type = "message/rfc822"
        try {
            context.startActivity(Intent.createChooser(this, context.getString(chooserPrompt)))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Launches web browser. Requires adding queries in the Manifest
     *
     * @param context the context
     * @param url     the url to visit
     */
    fun Context.browseUrl(url: String) {
        val newUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "http://$url"
        with(Intent(Intent.ACTION_VIEW, Uri.parse(newUrl))) {
            if (resolveActivity(packageManager) != null) startActivity(this)
            else Toast.makeText(this@browseUrl, R.string.no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Launches dialer with the number pre-dialed
     *
     * @param context the context
     * @param number  the number to dial
     */
    fun dialNumber(context: Context, number: String?) =
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))

    /**
     * Calls the number without passing through the dialer. This requires [android.Manifest.permission.CALL_PHONE]
     * permission
     *
     * @param context the context
     * @param number  the number to call
     */
    fun callNumber(context: Context, number: String?) =
        context.startActivity(Intent(Intent.ACTION_CALL, Uri.fromParts("tel", number, null)))

    /**
     * Launches current app uninstall process
     *
     * @param context the context
     */
    fun uninstall(context: Context) = with(Intent(Intent.ACTION_DELETE)) {
        data = Uri.parse("package:" + context.packageName)
        context.startActivity(this)
    }

    /**
     * Returns app that have specific permission
     *
     * @param context the context
     * @param permissions the permissions to look for
     */
    fun getAppsWithPermissions(context: Context, permissions: ArrayList<String>): List<String> {
        val packageNames = ArrayList<String>()
        val apps = context.packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        for (packageInfo in apps) {
            if (packageInfo.requestedPermissions == null) continue
            for (perm in packageInfo.requestedPermissions) {
                if (permissions.contains(perm)) {
                    packageNames.add(packageInfo.packageName)
                    break
                }
            }
        }
        return packageNames
    }
}