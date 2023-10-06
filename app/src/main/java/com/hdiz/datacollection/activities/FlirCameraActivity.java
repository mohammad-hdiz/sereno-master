package com.hdiz.datacollection.activities;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.BuildConfig;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.log.ThermalLog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hdiz.datacollection.R;

import com.hdiz.datacollection.handler.CameraHandler;
import com.hdiz.datacollection.utils.Utils;
import com.hdiz.datacollection.services.hardware.FlirThermalCameraService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import timber.log.Timber;

public class FlirCameraActivity extends AppCompatActivity {

    Bitmap myBitmapThermal;
    Bitmap myBitmapVisible;
    FloatingActionButton img_capture;
    ImageView img_thermal, img_visible;
    TextView visible_txt;
    String qr_code = "";
    String patient_str = "";
    String patient_details = "";
    String v_type = "gone";
    String uri_thermal_str, uri_visible_str;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private BroadcastReceiver mUsbActionReceiver;
    private UsbManager mUsbManager;
    private CameraHandler mFlirCameraHandler = null;
    private CameraHandler.DiscoveryStatus mFlirDiscoveryStatus = null;
    private Boolean mIsFlirDiscovery = false;
    private Identity mFlirConnectedIdentity = null;
    private Boolean mIsPaused = false;
    private Disposable mDisposableThermalPreview = null;
    private Disposable mDisposableVisiblePreview = null;
    private Bitmap mFlirThermalImageBitmap = null;
    private Bitmap mFlirVisibleImageBitmap = null;

    private String conStatus = "disconnected";
    private Boolean initSearch = false;
    private final static int ALL_PERMISSIONS_RESULT = 107;
    private final static int PERMISSION_REQUEST_CODE = 2297;
    private final static String ACTION_USB_PERMISSION = "com.hdiz.datacollection.activities.FlirCameraActivity.USB_PERMISSION";
    private final static String KEY_WORK_MANAGER = "flirBackground";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        endFlirBackground();
        setContentView(R.layout.activity_flir_camera);

        img_capture = findViewById(R.id.img_capture);
        img_thermal = findViewById(R.id.img_thermal);
        img_visible = findViewById(R.id.img_visible);

        visible_txt = findViewById(R.id.visible_txt);

        if (checkPermission()) {
            requestPermissionAndContinue();
            requestPermission();
        }


        //-------  FLIR code
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        registerForUsbAction();
        checkIfFlirAlreadyConnected();
        //--- end

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("image_type")) {
                v_type = getIntent().getStringExtra("image_type");
                if(v_type.equals("gone")){
                    img_visible.setVisibility(View.GONE);
                    visible_txt.setVisibility(View.GONE);
                }
                if(v_type.equals("visible")){
                    img_visible.setVisibility(View.VISIBLE);
                    visible_txt.setVisibility(View.VISIBLE);
                }
            }
            if (extras.containsKey("qrcode")) {
                qr_code = getIntent().getStringExtra("qrcode");

            }
            if (extras.containsKey("patient_details")) {
                patient_details = getIntent().getStringExtra("patient_details");
            }

            if (extras.containsKey("patient_str")) {
                patient_str = getIntent().getStringExtra("patient_str");
            }
        }

        img_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureThermalImages();
            }
        });


        permissions.add(CAMERA);
        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.

        if (SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }
    }

    private void endFlirBackground() {
        Utils.setIsFlirServiceEnabled(false);
    }


    @Override
    protected void onDestroy() {
        unRegisterForUsbAction();
        stopFlirCamera();
        disposeThreads();
//        if (isCameraLoaded()) {
            startFlirService();
//        }
        super.onDestroy();
    }

    private void startFlirService() {
        PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(FlirThermalCameraService.
                class, 15, TimeUnit.MINUTES);
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest(builder);
        Utils.setIsFlirServiceEnabled(true);
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(KEY_WORK_MANAGER,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                periodicWorkRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                    Log.d("onActivityResult","Hereee");
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        //img.recycle();
        return rotatedImg;
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        //outState.putParcelable("pic_uri_thermal", uri_thermal);
        //outState.putParcelable("pic_uri_visible", uri_visible);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        //uri_thermal = savedInstanceState.getParcelable("pic_uri_thermal");
        //uri_visible = savedInstanceState.getParcelable("pic_uri_visible");
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    private boolean canMakeSmores() {
        return (SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (hasPermission(perms)) {

                    } else {

                        permissionsRejected.add(perms);
                    }
                }
                if (permissionsRejected.size() > 0) {
                    if (SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (SDK_INT >= Build.VERSION_CODES.M) {

                                                //Log.d("API123", "permisionrejected " + permissionsRejected.size());
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean READ_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WRITE_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (READ_EXTERNAL_STORAGE && WRITE_EXTERNAL_STORAGE) {
                        // perform action when allow permission success
                        Log.d("onRequestPermission","Hereee");

                    } else {
                        Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }
    //------------------------ FLIR Code -------------------------------


    /**
     *  get usb event like connect and disconnect
     */
    public void registerForUsbAction() {
        //Local broadcast receiver
        mUsbActionReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                        UsbDevice flirUsbCamera = getUsbConnectedFlirDevice();
                        if (flirUsbCamera != null && checkForPermission(flirUsbCamera)) {
                            startFlirCamera();
                        }
                        break;
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        Timber.d("ACTION_USB_DEVICE_DETACHED");
                        if (getUsbConnectedFlirDevice() == null) {
                            stopFlirCamera();
                        }
                        break;
                    case ACTION_USB_PERMISSION:
                        Log.d("flirrrrrrr","here0");
                        startFlirCamera();
                        break;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        //registering
        registerReceiver(mUsbActionReceiver, filter);
    }

    /**
     * We must unregister the local broadcast receiver before kill the activity
     */
    public void unRegisterForUsbAction() {
        unregisterReceiver(mUsbActionReceiver);
    }

    /**
     *  Check if the flir already is connected to the phone, if it is connected then we go to next
     */
    private void checkIfFlirAlreadyConnected() {

        UsbDevice flirUsbCamera = getUsbConnectedFlirDevice();
        if(flirUsbCamera != null){
        }

        if (flirUsbCamera != null && checkForPermission(flirUsbCamera)) {
            Log.d("flirrrr","here0202");
            //start the FLIR camera
            startFlirCamera();
        }
    }

    /**
     * This fun will filter the FLIR device from all other usb connected devices
     * @return FLIR usb device object
     */
    public UsbDevice getUsbConnectedFlirDevice() {
        for (UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            if (usbDevice.getProductName().startsWith("FLIR")) {
                return usbDevice;
            }
        }
        return null;
    }
    private void requestPermission() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2296);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 2296);
            }
        } else {

            //below android 11
            ActivityCompat.requestPermissions(FlirCameraActivity.this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }
    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }
    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(FlirCameraActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                , READ_EXTERNAL_STORAGE}, 200);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(FlirCameraActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, 200);
            }
        } else {

        }
    }


    /**
     * Usb host android permission check
     *
     * @param usbDevice usb device
     * @return is permission provided by user
     */
    public Boolean checkForPermission(UsbDevice usbDevice) {
        if (mUsbManager.hasPermission(usbDevice)) {
            return true;
        } else {
            mUsbManager.requestPermission(usbDevice,
                    PendingIntent.getBroadcast(getApplicationContext(),
                            0,
                            new Intent(ACTION_USB_PERMISSION),
                            (SDK_INT >= Build.VERSION_CODES.S) ?
                                    PendingIntent.FLAG_MUTABLE : 0
                    )
            );
            return false;
        }
    }

    /**
     * Initialize the FLIR sdk and start the scanning for the FLIR ONE device.
     */
    public void startFlirCamera() {

        Log.d("flirrrrrrr","here1");
        ThermalSdkAndroid.init(
                getApplicationContext(),
                (BuildConfig.DEBUG) ? ThermalLog.LogLevel.DEBUG : ThermalLog.LogLevel.INFO);
        mFlirCameraHandler = new CameraHandler();
        mFlirCameraHandler.startDiscovery(getCameraDiscoveryListener(), getDiscoveryStatusListener());
    }

    /**
     *  Stop all FLIR camera streams and scanning process.
     */
    public void stopFlirCamera() {
        if (mFlirConnectedIdentity != null) {
            mFlirCameraHandler.disconnect();
            mFlirConnectedIdentity = null;
        }
        if (mIsFlirDiscovery) {
            mFlirCameraHandler.stopDiscovery(mFlirDiscoveryStatus);
        }
        mFlirCameraHandler = null;
    }

    private Boolean isCameraLoaded() {
        return mFlirConnectedIdentity != null;
    }

    /**
     * It scan the usb device for the FLIR one Camera
     * @return scanning obj
     */
    private DiscoveryEventListener getCameraDiscoveryListener() {
        Log.d("flirrrrrrr","here3");

        return new DiscoveryEventListener() {

            @Override
            public void onCameraFound(Identity identity) {
                Log.d("flirrrrrrr","here4");

                Log.d("flirrrrrrr",identity.cameraType.toString());
                Log.d("flirrrrrrr",identity.deviceId);

                mFlirCameraHandler.add(identity);
                connectFlirCamera(identity);
            }

            @Override
            public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
                Log.d("flirrrrrrr", errorCode.getMessage() + "  16");
            }
        };
    }

    /**
     * fun to get camera status
     * @return return camera listener obj
     */
    private CameraHandler.DiscoveryStatus getDiscoveryStatusListener() {
        Log.d("flirrrrrrr","here13");

        mFlirDiscoveryStatus = new CameraHandler.DiscoveryStatus() {
            @Override
            public void started() {
                Log.d("flirrrrrrr","here14");

                Timber.d("Started discovery for FLIR One camera attached");
                mIsFlirDiscovery = true;
            }

            @Override
            public void stopped() {
                Log.d("flirrrrrrr","here15");
                mIsFlirDiscovery = false;
            }
        };
        return mFlirDiscoveryStatus;
    }

    /**
     * Connect the camera for getting the thermal imaging
     * @param identity of the FLIR camera
     */
    private void connectFlirCamera(Identity identity) {
        Log.d("flirrrrrrr","here5");

        if (mFlirConnectedIdentity == null) {
            Log.d("flirrrrrrr","here6");

            Timber.d("Connecting to new Flir device");
            mFlirConnectedIdentity = identity;
            //Frame-ware permission checker
            if (UsbPermissionHandler.isFlirOne(identity)) {
                Log.d("flirrrrrrr","here7");

                //Usb still missing some permissions from the SDK level.
                try {
                    Log.d("flirrrrrrr","here8");

                    UsbPermissionHandler framewarePermissionHandler = new UsbPermissionHandler();
                    framewarePermissionHandler.requestFlirOnePermisson(identity,
                            getApplicationContext(),
                            getFlirExtraPermissionListener());
                } catch (Exception connectionUsbIssue) {
                    Log.d("flirrrrrrr",connectionUsbIssue.getMessage() + " here17");

                }
            } else {
                Log.d("flirrrrrrr","here9");

                connectFlirCameraStream(identity);
            }
        } else {
            Log.d("flirrrrrrr","here10");

            connectFlirCameraStream(identity);
            //Timber.e("Already connected to one device, Can't connect another now");
        }
    }

    /**
     *  the FLIR one device may need more permission from the system to run
     * @return permission obj
     */
    private UsbPermissionHandler.UsbPermissionListener getFlirExtraPermissionListener() {
        return new UsbPermissionHandler.UsbPermissionListener() {
            @Override
            public void permissionGranted(@NonNull Identity identity) {
                connectFlirCameraStream(identity);
            }

            @Override
            public void permissionDenied(@NonNull Identity identity) {
                Timber.e("Permission was denied for identity %s", identity.deviceId);
            }

            @Override
            public void error(ErrorType errorType, Identity identity) {
                Timber.d("Error when asking for permission for FLIR ONE, error:%s identity:%s", errorType.name(), identity.deviceId);
            }
        };
    }

    /**
     * Connect the camera
     * @param identity  from scanning.
     */
    private void connectFlirCameraStream(Identity identity) {
        Log.d("flirrrrrrr","here11");

        mFlirCameraHandler.connect(identity, getFlirConnectionStatusListener(), "WhiteHot");
    }

    /**
     * Status of the Flir one device
     * @return status obj
     */
    private ConnectionStatusListener getFlirConnectionStatusListener() {
        return (connectionStatus, errorCode) -> {
            switch (connectionStatus) {
                case DISCONNECTED:
                    Log.d("flirrrrrrr","here18");
                    conStatus = "disconnected";
                    //mFlirCameraHandler.startDiscovery(getCameraDiscoveryListener(), mFlirDiscoveryStatus);
                    break;
                case DISCONNECTING:
                    Log.d("flirrrrrrr","here19");
                    conStatus = "disconnecting";
                    break;
                case CONNECTING:
                    Log.d("flirrrrrrr","here20");
                    conStatus = "connecting";

                    break;
                case CONNECTED:
                    Log.d("flirrrrrrr","here21");
                    conStatus = "connected";
                    mFlirCameraHandler.startStream(getFlirFrameStreamListener());
                    break;
            }
        };
    }

    /**
     *  The Flir will gave us the stream from the camera in this fun
     *  Please note that bitmap msx and dc will come at rate of 15 frame pre second so please handle the threading in this fun very carefully.
     * @return obj
     */
    private CameraHandler.StreamDataListener getFlirFrameStreamListener() {
        return (msxBitmap, dcBitmap) -> {
            //Thermal image
            mDisposableThermalPreview = Single.fromCallable(() -> {
                        // apply any orientation to image or pre-processing to the preview image here
                        return msxBitmap;
                    })
                    .filter(bitmap -> !mIsPaused)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        mFlirThermalImageBitmap = bitmap;
                        //mFlirThermalImageBitmap = rotateImage(mFlirThermalImageBitmap, -90);
                        img_thermal.setImageBitmap(rotateImage(mFlirThermalImageBitmap,-90));
                    });

            //Visible image
            mDisposableVisiblePreview = Single.fromCallable(() -> {
                        // apply any orientation to image or pre-processing to the preview image here
                        return dcBitmap;
                    })
                    .filter(bitmap -> !mIsPaused)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        mFlirVisibleImageBitmap = bitmap;
                        //mFlirVisibleImageBitmap = rotateImage(mFlirVisibleImageBitmap,-90);
                        img_visible.setImageBitmap(rotateImage(mFlirVisibleImageBitmap, -90));
                    });

        };
    }

    private String SaveImage(Bitmap finalBitmap, String type, int num) {

        String fname = null;
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File myDir = new File(root + "/sereno");
        myDir.mkdirs();


        if(type.equals("thermal"))
            fname = qr_code + "_thermal_" + num +".jpg";
        if(type.equals("visible"))
            fname = qr_code + "_visible_" + num +".jpg";

        File file = new File (myDir, fname);

        Log.d("pathhh",file.getPath());
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }
    private void saveTextFile(String filename){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/textDir
        File directory = cw.getDir("textDir", Context.MODE_PRIVATE);
        // Create TextDir
        File mypath = new File(directory,filename);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fos);
            myOutWriter.append("mohammad");
            myOutWriter.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Uri saveToInternalStorage(Bitmap bitmapImage, String img_name){
        Uri uri ;
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory,img_name);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        uri = Uri.fromFile(mypath);

        Log.d("URII",uri.getPath());
        // return directory.getAbsolutePath();
        return uri;
    }

    /**
     * Please complete as the application's flow the bitmaps mFlirThermalImageBitmap and mFlirVisibleImageBitmap
     * is holding last frame of the greyscale and thermal preview so you can do anything with it.
     */
    private void captureThermalImages() {
//        Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                R.drawable.camera);
//        mFlirThermalImageBitmap = icon;
//        mFlirVisibleImageBitmap = icon;

        if(mFlirThermalImageBitmap!=null){
            mIsPaused = true;
            myBitmapThermal = mFlirThermalImageBitmap;
            myBitmapVisible = mFlirVisibleImageBitmap;


            Random rand = new Random();
            int n = rand.nextInt(89);
            n +=10;

            uri_thermal_str = SaveImage(rotateImage(myBitmapThermal,-90),"thermal", n);
            uri_visible_str = SaveImage(rotateImage(myBitmapVisible,-90),"visible", n);
            //uri_thermal = saveToInternalStorage(icon, getSaltString() + "_thermal.jpg");
            //uri_visible = saveToInternalStorage(icon,getSaltString() + "_visible.jpg");

            Intent intent = new Intent(FlirCameraActivity.this, ViewImgActivity.class);
            intent.putExtra("v_type",v_type);
            if (qr_code != null && !qr_code.equals(""))
                intent.putExtra("qrcode", qr_code);
            if (patient_str != null && !patient_str.equals(""))
                intent.putExtra("patient_str", patient_str);
            if (patient_details != null && !patient_details.equals(""))
                intent.putExtra("patient_details", patient_details);
            if (uri_thermal_str != null)
                intent.putExtra("uri_thermal", uri_thermal_str);
            if (uri_visible_str != null)
                intent.putExtra("uri_visible", uri_visible_str);
            startActivity(intent);
            finish();
        }
        else{
            Toast.makeText(this, "Connecting to the Flir Camera", Toast.LENGTH_LONG).show();
            if (mIsFlirDiscovery) {
                stopFlirCamera();
                mIsFlirDiscovery = false;
                startFlirCamera();
            }
        }

    }

    /**
     * Disposed the Rx java's threads
     */
    private void disposeThreads() {
        if (mDisposableThermalPreview != null)
            mDisposableThermalPreview.dispose();
        if (mDisposableVisiblePreview != null)
            mDisposableVisiblePreview.dispose();
    }

}