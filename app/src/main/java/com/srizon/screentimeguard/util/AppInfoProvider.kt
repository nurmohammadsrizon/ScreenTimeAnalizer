package com.srizon.screentimeguard.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?
)

object AppInfoProvider {
    fun getLaunchableApps(context: Context): List<InstalledAppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { ai ->
                InstalledAppInfo(
                    packageName = ai.packageName,
                    appName = ai.loadLabel(pm).toString(),
                    icon = ai.loadIcon(pm)
                )
            }
            .sortedBy { it.appName.lowercase() }
    }

    fun getAppIcon(context: Context, packageName: String): Drawable? =
        try { context.packageManager.getApplicationIcon(packageName) } catch (e: Exception) { null }

    fun getAppName(context: Context, packageName: String): String =
        try {
            val ai: ApplicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(ai).toString()
        } catch (e: Exception) { packageName }
}
