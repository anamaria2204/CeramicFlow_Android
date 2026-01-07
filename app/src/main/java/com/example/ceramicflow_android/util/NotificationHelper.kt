package com.example.ceramicflow_android.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ceramicflow_android.R

class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        private const val SYNC_CHANNEL_ID = "ceramic_flow_sync_channel"
        private const val SYNC_CHANNEL_NAME = "Data Sync"
        private const val SYNC_NOTIFICATION_ID = 1
        private const val OFFLINE_NOTIFICATION_ID = 2
        private const val BOOKING_SUCCESS_ID = 3
        private const val ONLINE_NOTIFICATION_ID = 4
        private const val BOOKING_LOCAL_SAVE_ID = 5
        private const val UPLOAD_SUCCESS_ID = 6
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                SYNC_CHANNEL_ID,
                SYNC_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for data sync status"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSyncCompleteNotification() {
        val builder = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sync Complete")
            .setContentText("Your ceramic data has been updated.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(SYNC_NOTIFICATION_ID, builder.build())
    }

    fun showBookingSuccessNotification(date: String, time: String) {
        val builder = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Programare Creată cu Succes!")
            .setContentText("Programarea dumneavoastră pentru data de $date, ora $time a fost confirmată.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(BOOKING_SUCCESS_ID, builder.build())
    }

    fun showBookingSavedLocallyNotification() {
        val builder = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Programare Salvată Local")
            .setContentText("Datele vor fi trimise la server când reveniți online.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(BOOKING_LOCAL_SAVE_ID, builder.build())
    }

    fun showUploadSuccessNotification(count: Int) {
        val message = if (count == 1) "1 programare locală a fost sincronizată." else "$count programări locale au fost sincronizate."
        val builder = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Sincronizare Finalizată")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        notificationManager.notify(UPLOAD_SUCCESS_ID, builder.build())
    }

    // --- FUNCȚIA LIPSĂ, ADĂUGATĂ ACUM ---
    fun showOfflineNotification() {
        val builder = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Conexiune la Internet Pierdută")
            .setContentText("Funcționalitatea online a aplicației este limitată.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
        notificationManager.notify(OFFLINE_NOTIFICATION_ID, builder.build())
    }

    // --- FUNCȚIA LIPSĂ, ADĂUGATĂ ACUM ---
    fun showOnlineNotification() {
        val builder = NotificationCompat.Builder(context, SYNC_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Conexiune Restabilită")
            .setContentText("Sunteți din nou online.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
        notificationManager.notify(ONLINE_NOTIFICATION_ID, builder.build())
    }
}