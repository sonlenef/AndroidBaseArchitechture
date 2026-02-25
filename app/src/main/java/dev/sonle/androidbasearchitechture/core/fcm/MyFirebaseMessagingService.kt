package dev.sonle.androidbasearchitechture.core.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Firebase Cloud Messaging service for handling push notifications
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    val notificationHelper: NotificationHelper by inject()
    
    companion object {
        private const val CHANNEL_ID = "baseapp_notifications"
        private const val CHANNEL_NAME = "BaseApp Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for BaseApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Timber.d("FCM message received: ${remoteMessage.messageId}")
        
        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            Timber.d("Message data payload: ${remoteMessage.data}")
        }
        
        // Handle notification payload
        remoteMessage.notification?.let { notification ->
            Timber.d("Message notification payload: ${notification.title} - ${notification.body}")
            
            // Show notification
            notificationHelper.showNotification(
                title = notification.title ?: "BaseApp",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        Timber.d("FCM token refreshed: $token")
        
        // Send token to server if needed
        sendTokenToServer(token)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Implement token sending to server
        Timber.d("Would send token to server: $token")
    }
}
