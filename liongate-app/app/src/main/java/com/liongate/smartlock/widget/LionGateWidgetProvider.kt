package com.liongate.smartlock.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.RemoteViews
import com.liongate.smartlock.R
import com.liongate.smartlock.ble.BioLockBleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LionGateWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("liongate_prefs", Context.MODE_PRIVATE)
        val deviceName = prefs.getString("key_device_name", "No Device Paired")

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_lock)
            views.setTextViewText(R.id.tv_widget_device_name, deviceName)
            
            val intent = Intent(context, LionGateWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_UNLOCK
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            views.setOnClickPendingIntent(R.id.btn_widget_unlock, pendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_WIDGET_UNLOCK) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    handleUnlockAction(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun handleUnlockAction(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetComponent = android.content.ComponentName(context, LionGateWidgetProvider::class.java)
        
        // 1. Show Connecting State
        val views = RemoteViews(context.packageName, R.layout.widget_lock)
        views.setTextViewText(R.id.tv_widget_status, "CONNECTING...")
        views.setTextColor(R.id.tv_widget_status, 0xFFF59E0B.toInt()) // Amber
        appWidgetManager.updateAppWidget(widgetComponent, views)
        
        val prefs = context.getSharedPreferences("liongate_prefs", Context.MODE_PRIVATE)
        val mac = prefs.getString("key_device_mac", null) ?: "00:00:00:00:00:00"
        val pin = prefs.getString("key_pin", null) ?: "123456"

        val bleManager = BioLockBleManager(context.applicationContext)
        val result = bleManager.connectAndUnlock(mac, pin)
        
        if (result.isSuccess) {
            // Success Haptic Feedback
            triggerHapticFeedback(context)
            
            // Show Unlocked State
            val successViews = RemoteViews(context.packageName, R.layout.widget_lock)
            successViews.setTextViewText(R.id.tv_widget_status, "UNLOCKED")
            successViews.setTextColor(R.id.tv_widget_status, 0xFF10B981.toInt()) // Emerald Green
            appWidgetManager.updateAppWidget(widgetComponent, successViews)
            
            delay(4000) // Reset after 4 seconds
        } else {
            // Show Failed State
            val failViews = RemoteViews(context.packageName, R.layout.widget_lock)
            failViews.setTextViewText(R.id.tv_widget_status, "FAILED")
            failViews.setTextColor(R.id.tv_widget_status, 0xFFF43F5E.toInt()) // Red
            appWidgetManager.updateAppWidget(widgetComponent, failViews)
            
            delay(3000) // Reset after 3 seconds
        }

        // Reset to default State
        val defaultViews = RemoteViews(context.packageName, R.layout.widget_lock)
        defaultViews.setTextViewText(R.id.tv_widget_status, "TAP TO UNLOCK")
        defaultViews.setTextColor(R.id.tv_widget_status, 0xFFE2E8F0.toInt()) // Slate200
        
        // Re-attach pending intent
        val unlockIntent = Intent(context, LionGateWidgetProvider::class.java).apply {
            action = ACTION_WIDGET_UNLOCK
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            0, 
            unlockIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        defaultViews.setOnClickPendingIntent(R.id.btn_widget_unlock, pendingIntent)
        
        val deviceName = prefs.getString("key_device_name", "No Device Paired")
        defaultViews.setTextViewText(R.id.tv_widget_device_name, deviceName)

        appWidgetManager.updateAppWidget(widgetComponent, defaultViews)
    }

    private fun triggerHapticFeedback(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_UNLOCK = "com.liongate.smartlock.ACTION_WIDGET_UNLOCK"
    }
}
