/*******************************************************************
 * @title FLIR THERMAL SDK
 * @file CameraHandler.java
 * @Author FLIR Systems AB
 *
 * @brief Helper class that encapsulates *most* interactions with a FLIR ONE camera
 *
 * Copyright 2019:    FLIR Systems
 ********************************************************************/
package com.hdiz.datacollection.handler;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.flir.thermalsdk.image.palettes.Palette;
import com.flir.thermalsdk.image.palettes.PaletteManager;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;
import com.flir.thermalsdk.live.remote.Battery;
import com.flir.thermalsdk.live.streaming.ThermalImageStreamListener;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/**
 * Encapsulates the handling of a FLIR ONE camera or built in emulator, discovery, connecting and start receiving images.
 * All listeners are called from Thermal SDK on a non-ui thread
 * <p/>
 * Usage:
 * <pre>
 * Start discovery of FLIR FLIR ONE cameras or built in FLIR ONE cameras emulators
 * {@linkplain #startDiscovery(DiscoveryEventListener, DiscoveryStatus)}
 * Use a discovered Camera {@linkplain Identity} and connect to the Camera
 * {@linkplain #connect(Identity, ConnectionStatusListener,String)}
 * Once connected to a camera
 * {@linkplain #startStream(StreamDataListener)}
 * </pre>
 * <p/>
 * You don't *have* to specify your application to listen or USB intents but it might be beneficial for you application,
 * we are enumerating the USB devices during the discovery process which eliminates the need to listen for USB intents.
 * See the Android documentation about USB Host mode for more information
 * <p/>
 * Please note, this is <b>NOT</b> production quality code, error handling has been kept to a minimum to keep the code as clear and concise as possible
 */
public class CameraHandler {

    private static final String TAG = "CameraHandler";
    public MutableLiveData<Integer> batteryPercentage = new MutableLiveData<>();
    public Battery.BatteryStateListener batteryStateListener = null;
    //Discovered FLIR cameras
    LinkedList<Identity> foundCameraIdentities = new LinkedList<>();
    private StreamDataListener streamDataListener;
    private String imageType;
    /**
     * Function to process a Thermal Image and update UI
     */
    private final Camera.Consumer<ThermalImage> handleIncomingImage = new Camera.Consumer<ThermalImage>() {
        @Override
        public void accept(ThermalImage thermalImage) {
            Timber.tag(TAG).d("accept() called with: thermalImage = [" + thermalImage.getDescription() + "]");
            //Will be called on a non-ui thread,
            // extract information on the background thread and send the specific information to the UI thread

            //Get a bitmap with only IR data
            Bitmap msxBitmap;
            {
                thermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
                if(imageType.contains("Red Hot")){
                    Palette palette = PaletteManager.getDefaultPalettes().get(10);
                    thermalImage.setPalette(palette);
                }
                msxBitmap = BitmapAndroid.createBitmap(thermalImage.getImage()).getBitMap();
            }

            //Get a bitmap with the visual image, it might have different dimensions then the bitmap from THERMAL_ONLY
            Bitmap dcBitmap = BitmapAndroid.createBitmap(thermalImage.getFusion().getPhoto()).getBitMap();
            Timber.tag(TAG).d("adding images to cache");
            streamDataListener.images(msxBitmap, dcBitmap);
        }
    };
    private Battery.BatteryPercentageListener batteryPercentageListener = i -> batteryPercentage.postValue(i);
    //A FLIR Camera
    private Camera camera;
    /**
     * Called whenever there is a new Thermal Image available, should be used in conjunction with {@link Camera.Consumer}
     */
    private final ThermalImageStreamListener thermalImageStreamListener = new ThermalImageStreamListener() {
        @Override
        public void onImageReceived() {
            //Will be called on a non-ui thread
            withImage(this, handleIncomingImage);
        }
    };

    public CameraHandler() {

    }

    /**
     * Start discovery of USB and Emulators
     */
    public void startDiscovery(DiscoveryEventListener cameraDiscoveryListener, DiscoveryStatus discoveryStatus) {
        Timber.tag("flirrrrrrr").d("here2");
        DiscoveryFactory.getInstance().scan(cameraDiscoveryListener, CommunicationInterface.USB);
        discoveryStatus.started();
    }

    /**
     * Stop discovery of USB and Emulators
     */
    public void stopDiscovery(DiscoveryStatus discoveryStatus) {
        Timber.d("**stopDiscovery in");
        DiscoveryFactory.getInstance().stop(CommunicationInterface.USB);
        discoveryStatus.stopped();
    }

    public void connect(Identity identity, ConnectionStatusListener connectionStatusListener, String imageDisplayType) {
        Timber.tag("flirrrrrrr").d("here12");

        camera = new Camera();
        camera.connect(identity, connectionStatusListener);
        imageType = imageDisplayType;
        Timber.tag("flirrrrrrr").d("here26");


    }

    public void disconnect() {
        if (camera == null) {
            return;
        }
        if (camera.isGrabbing()) {
            camera.unsubscribeAllStreams();
        }

        camera.disconnect();
    }

    /**
     * Start a stream of {@link ThermalImage}s images from a FLIR ONE or emulator
     */
    public void startStream(StreamDataListener listener) {
        Timber.d("here22");

        this.streamDataListener = listener;

        camera.subscribeStream(thermalImageStreamListener);
        try {

            Timber.tag("flirrrrrrr").d("here23");
            Timber.tag("flirrrrrrr").d("%s  here29", camera.getConnectionStatus());
            Timber.tag("flirrrrrrr").d("%s  here30", camera.getRemoteControl());
            Timber.tag("flirrrrrrr").d("%s  here31", camera.getIdentity());

            Objects.requireNonNull(Objects.requireNonNull(camera.getRemoteControl()).getBattery()).subscribePercentage(batteryPercentageListener);
        } catch (Exception e) {
            Timber.tag("flirrrrrrr").d("%s  here24", e.getMessage());

            e.printStackTrace();
        }
        if (batteryStateListener != null) {
            try {
                Timber.tag("flirrrrrrr").d("here25");

                Objects.requireNonNull(Objects.requireNonNull(camera.getRemoteControl()).getBattery()).subscribeChargingState(batteryStateListener);
            } catch (Exception e) {
                Timber.tag("flirrrrrrr").d("%s  here28", e.getMessage());

            }
        }
    }
    /**
     * Stop a stream of {@link ThermalImage}s images from a FLIR ONE or emulator
     */
    public void stopStream() {
        Timber.d("** Stop stream in %s", this);
        camera.unsubscribeStream(thermalImageStreamListener);
        camera.getRemoteControl().getBattery().unsubscribePercentage();
    }

    /**
     * Add a found camera to the list of known cameras
     */
    public void add(Identity identity) {
        foundCameraIdentities.add(identity);
    }

    @Nullable
    public Identity get(int i) {
        return foundCameraIdentities.get(i);
    }

    /**
     * Get a read only list of all found cameras
     */
    @Nullable
    public List<Identity> getCameraList() {
        return Collections.unmodifiableList(foundCameraIdentities);
    }

    /**
     * Clear all known network cameras
     */
    public void clear() {
        foundCameraIdentities.clear();
    }

    @Nullable
    public Identity getCppEmulator() {
        for (Identity foundCameraIdentity : foundCameraIdentities) {
            if (foundCameraIdentity.deviceId.contains("C++ Emulator")) {
                return foundCameraIdentity;
            }
        }
        return null;
    }

    @Nullable
    public Identity getFlirOneEmulator() {
        for (Identity foundCameraIdentity : foundCameraIdentities) {
            if (foundCameraIdentity.deviceId.contains("EMULATED FLIR ONE")) {
                return foundCameraIdentity;
            }
        }
        return null;
    }

    @Nullable
    public Identity getFlirOne() {
        for (Identity foundCameraIdentity : foundCameraIdentities) {
            boolean isFlirOneEmulator = foundCameraIdentity.deviceId.contains("EMULATED FLIR ONE");
            boolean isCppEmulator = foundCameraIdentity.deviceId.contains("C++ Emulator");
            if (!isFlirOneEmulator && !isCppEmulator) {
                return foundCameraIdentity;
            }
        }

        return null;
    }

    private void withImage(ThermalImageStreamListener listener, Camera.Consumer<ThermalImage> functionToRun) {
        camera.withImage(listener, functionToRun);
    }


    public interface StreamDataListener {
        void images(Bitmap msxBitmap, Bitmap dcBitmap);
    }

    public interface DiscoveryStatus {
        void started();

        void stopped();
    }

    public void kill() {
        try {
            camera.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        camera = null;
    }

}
