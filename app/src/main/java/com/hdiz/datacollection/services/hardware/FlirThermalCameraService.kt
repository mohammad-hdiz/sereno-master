package com.hdiz.datacollection.services.hardware

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.flir.thermalsdk.ErrorCode
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler
import com.flir.thermalsdk.live.CommunicationInterface
import com.flir.thermalsdk.live.Identity
import com.flir.thermalsdk.live.connectivity.ConnectionStatus
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener
import com.flir.thermalsdk.log.ThermalLog
import com.hdiz.datacollection.BuildConfig
import com.hdiz.datacollection.R
import com.hdiz.datacollection.handler.CameraHandler
import com.hdiz.datacollection.utils.Utils
import kotlinx.coroutines.*
import timber.log.Timber


class FlirThermalCameraService(private val mAppContext: Context, params: WorkerParameters) :
    CoroutineWorker(mAppContext, params) {
    companion object {
        private const val ACTION_USB_PERMISSION =
            "com.hdiz.datacollection.services.hardware.FlirThermalCameraService.USB_PERMISSION"

        private const val CHANNEL_ID = "Hardware"
        private const val NOTIFICATION_ID = 1456
    }


    private lateinit var mUsbManager: UsbManager
    private lateinit var mCameraHandler: CameraHandler
    private var mConnectedIdentity: Identity? = null
    private val mUsbPermissionHandler = UsbPermissionHandler()
    private var mLastPerfectThermalVisibleBitMap: Bitmap? = null
    private var mLastPerfectThermalGreyBitMap: Bitmap? = null
    private var mThermalImageDisplay: String = "null**"
    private var mAlreadyKilled = false
    private var mUsbDevice: UsbDevice? = null


    private var mUsbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Timber.d("Usb Detached")
                    notifyNotificationStopped()
                    mAlreadyKilled = true
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    Timber.d("Usb attached")
                    Utils.getFlirDevice(mUsbManager.deviceList)?.let { flirDevice ->
                        openFlir(flirDevice)
                    }
                }
                ACTION_USB_PERMISSION -> {
                    Timber.d("Usb Detached")
                }
            }
        }
    }

    private val mCameraDiscoveryListener = object :
        DiscoveryEventListener {
        override fun onCameraFound(identity: Identity) {
            Timber.d(
                "onCameraFound identity:$identity"
            )
            mCameraHandler.add(identity)
            if (mCameraHandler.flirOne?.deviceId == "FLIR ONE Camera") {
                if (Utils.isFlirServiceEnabled()) {
                    connect(mCameraHandler.flirOne)
                }
            }
        }

        override fun onDiscoveryError(
            communicationInterface: CommunicationInterface,
            errorCode: ErrorCode
        ) {
            Timber.d(
                "onDiscoveryError communicationInterface:$communicationInterface errorCode:$errorCode"
            )
            stopDiscovery()
        }
    }

    private val mDiscoveryStatusListener: CameraHandler.DiscoveryStatus =
        object : CameraHandler.DiscoveryStatus {

            override fun started() {
                Timber.d("stopped")
            }

            override fun stopped() {
                Timber.d("stopped")
            }
        }

    private val mPermissionListener: UsbPermissionHandler.UsbPermissionListener = object :
        UsbPermissionHandler.UsbPermissionListener {
        override fun permissionGranted(identity: Identity) {
            Timber.d("testing thermal colour type in Flir permission granted ")
            mCameraHandler.connect(identity, mConnectionStatusListener, mThermalImageDisplay)
        }

        override fun permissionDenied(identity: Identity) {
            Timber.d("Permission was denied for identity ")
        }

        override fun error(
            errorType: UsbPermissionHandler.UsbPermissionListener.ErrorType,
            identity: Identity
        ) {
            Timber.d("Error when asking for permission for FLIR ONE, error:$errorType identity:$identity")
        }
    }


    private val mStreamDataListener: CameraHandler.StreamDataListener =
        CameraHandler.StreamDataListener { msxBitmap, dcBitmap ->

            /*override fun images(dataHolder: FrameDataHolder) {
                    mLastPerfectThermalGreyBitMap = dataHolder.msxBitmap
                    mLastPerfectThermalVisibleBitMap = dataHolder.dcBitmap
                    Timber.d("images are streaming**")
                }*/

            /*try {
                mFrameBuffer.put(FrameDataHolder(msxBitmap, dcBitmap))
            } catch (e: InterruptedException) { //if interrupted while waiting for adding a new item in the queue
                Timber.e(
                    "images(), unable to add incoming images to frames buffer, exception:$e"
                )
            }
            val poll: FrameDataHolder = mFrameBuffer.poll()*/
            mLastPerfectThermalGreyBitMap = msxBitmap
            mLastPerfectThermalVisibleBitMap = dcBitmap
            Timber.d("images are streaming**2")
            Thread.sleep(2_000)
        }

    private fun stopAllKill() {
        Timber.d("****** stop all kill *********")
        mCameraHandler.let {
            it.clear()
            it.disconnect()
            it.stopDiscovery(mDiscoveryStatusListener)
            it.kill()
        }
        mConnectedIdentity = null
        Timber.d("****** stop all kill *********")
    }

    private val mConnectionStatusListener =
        ConnectionStatusListener { connectionStatus, errorCode ->
            Timber.d(
                "onConnectionStatusChanged connectionStatus:$connectionStatus errorCode:$errorCode"
            )
            when (connectionStatus) {
                ConnectionStatus.CONNECTING -> {

                }
                ConnectionStatus.CONNECTED -> {
                    mCameraHandler.startStream(mStreamDataListener)
                }
                ConnectionStatus.DISCONNECTING -> {
                }
                ConnectionStatus.DISCONNECTED -> {
                    mCameraHandler.startDiscovery(
                        mCameraDiscoveryListener,
                        mDiscoveryStatusListener
                    )
                }
            }

        }


    override suspend fun doWork(): Result {
        val textContentText = mAppContext.getText(R.string.running_background)

        val channelId: String =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel() else ""
        val notification = NotificationCompat.Builder(mAppContext, channelId)
            .setSmallIcon(R.drawable.logo)
            .setContentText(textContentText)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.FLAG_ONGOING_EVENT)
            .build()

        val foregroundInfo = ForegroundInfo(NOTIFICATION_ID, notification)
        setForeground(foregroundInfo)
        //Initialize usb
        mUsbManager = mAppContext.getSystemService(Context.USB_SERVICE) as UsbManager
        Timber.d("Initialize usb")
        val filter = IntentFilter(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        mAppContext.registerReceiver(mUsbReceiver, filter)

        //Check if Flir alreadyConnected

        Utils.getFlirDevice(mUsbManager.deviceList)?.let { flirDevice ->
            openFlir(flirDevice)
            Timber.d("Flir already connected")
        } ?: let {
            Timber.d("Flir not connected yet")
        }
        var timeCounter = 0
        var notificationCountNo = 1
        while (Utils.isFlirServiceEnabled()) {
            Timber.d("serviceRunning")
            timeCounter++
            delay(1000)
            if (timeCounter >= 50) {
                notifyNotificationMessage(notificationCountNo)
                timeCounter = 0
                notificationCountNo++
                if (notificationCountNo > 3) {
                    notificationCountNo = 1
                }
            }
        }
        stopAllKill()
        mAppContext.unregisterReceiver(mUsbReceiver)
        return Result.success()
    }

    private fun notifyNotificationStopped() {
        (NotificationManagerCompat.from(mAppContext)).let { manager ->
            val notification = NotificationCompat.Builder(mAppContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentText("Device DisConnected")
                .setOngoing(false)
                .setSilent(true)
                .setPriority(NotificationCompat.FLAG_ONGOING_EVENT)
                .build()
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun notifyNotificationMessage(notificationCountNo: Int) {
        val textContentText = mAppContext.getText(R.string.running_background).toString()
        val text = (0..notificationCountNo).joinToString { "."}
        (NotificationManagerCompat.from(mAppContext)).let { manager ->
            val notification = NotificationCompat.Builder(mAppContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentText(textContentText + text)
                .setOngoing(true)
                .setSilent(true)
                .setPriority(NotificationCompat.FLAG_ONGOING_EVENT)
                .build()
            manager.notify(NOTIFICATION_ID, notification)
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = mAppContext.getString(R.string.notfiy_channel_name)
        val descriptionText = mAppContext.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            mAppContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        return CHANNEL_ID
    }


    private fun openFlir(flirDevice: UsbDevice) {
        mUsbDevice = flirDevice
        if (mUsbManager.hasPermission(flirDevice)) {
            Timber.d("Have permission to use FLIR")
            ThermalSdkAndroid.init(
                mAppContext,
                if (BuildConfig.DEBUG) ThermalLog.LogLevel.DEBUG else ThermalLog.LogLevel.INFO
            )
            mCameraHandler = CameraHandler()
            mCameraHandler.startDiscovery(mCameraDiscoveryListener, mDiscoveryStatusListener)
        } else {
            Timber.d("Not have permission to use FLIR")
            val permissionIntent =
                PendingIntent.getBroadcast(
                    mAppContext,
                    0,
                    Intent(ACTION_USB_PERMISSION),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
                )
            mUsbManager.requestPermission(flirDevice, permissionIntent)
        }
    }

    private fun connect(identity: Identity?) { //We don't have to stop a discovery but it's nice to do if we have found the camera that we are looking for
        mCameraHandler.stopDiscovery(mDiscoveryStatusListener)
        if (mConnectedIdentity != null) {
            Timber.d(
                "connect(), in *this* code sample we only support one camera connection at the time"
            )
            stopAllKill()
            Thread.sleep(10000)
            Utils.getFlirDevice(mUsbManager.deviceList)?.let { flirDevice ->
                openFlir(flirDevice)
                Timber.d("**Flir connected")
            } ?: let {
                Timber.d("**Flir not connected")
            }

        } else
            if (identity == null) {
                Timber.d(
                    "connect(), can't connect, no camera available"
                )
            } else {
                mConnectedIdentity = identity
                //IF your using "USB_DEVICE_ATTACHED" and "usb-device vendor-id" in the Android Manifest
                // you don't need to request permission, see documentation for more information
                if (UsbPermissionHandler.isFlirOne(identity)) {
                    try {
                        mUsbPermissionHandler.requestFlirOnePermisson(
                            identity,
                            mAppContext,
                            mPermissionListener
                        )
                    } catch (permissionError: Exception) {
                        Timber.d("Device not found!, Please check the connection and retry")
                    }
                } else {
                    Timber.d("testing thermal colour type in Flir usb $mThermalImageDisplay")
                    mCameraHandler.connect(
                        identity,
                        mConnectionStatusListener,
                        mThermalImageDisplay
                    )
                }
            }
    }

    private fun stopDiscovery() {
        mCameraHandler.stopDiscovery(mDiscoveryStatusListener)
    }
}