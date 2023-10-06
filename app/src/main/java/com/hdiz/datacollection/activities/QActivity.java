package com.hdiz.datacollection.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.RetrofitAPI;
import com.hdiz.datacollection.fragments.SettingFragment;
import com.hdiz.datacollection.handler.DBManager;
import com.hdiz.datacollection.objects.Contact;
import com.hdiz.datacollection.objects.IndividualVisit;
import com.hdiz.datacollection.objects.Patient;
import com.hdiz.datacollection.objects.RequestData;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class QActivity extends AppCompatActivity implements SettingFragment.OnInputListener{
    private static final String TAG = "MLKit Barcode";
    private static final int PERMISSION_CODE = 1001;
    private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    private PreviewView previewView;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private Preview previewUseCase;
    private ImageAnalysis analysisUseCase;
    private String qrCode , patient_str, image_type = "gone";
    private String thermal_str = "null";
    private String visible_str = "null";
    private String visit_str = "null";

    private CircleImageView qrCodeFoundButton;
    private ImageView setting;
    private Boolean state = false;
    private CircularProgressIndicator progressIndicator;
    private Patient patient = null;
    private boolean success = false;
    public static DBManager dbManager;

    public static Activity qActivity ;

    View view;



    public static String getCurrentDateAndTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", new Locale("en"));
        String formattedDate = simpleDateFormat.format(c);
        return formattedDate;
    }

    public boolean checkQrcodeValidity(String qr_code){
        Boolean isValid = false;
        if(qr_code.matches("[A-Z0-9]+") && qr_code.length() == 14){
            isValid = true;
        }
        return isValid;
    }
    private RequestData makeRequest(String qr_code){
        String currentDate = getCurrentDateAndTime();
        Log.d("Date", currentDate);
        Contact contact = new Contact("","","","",
                "","","");
        RequestData requestData = new RequestData();
        requestData.setIdentifier(qr_code);
        requestData.setLocal_captured_at(currentDate);
        requestData.setFirst_name("");
        requestData.setMiddle_name("");
        requestData.setLast_name("");
        requestData.setBirth_date("1393-12-02");
        requestData.setGender("");
        requestData.setRace("");
        requestData.setNote("");
        requestData.setContact(contact);

        return requestData;
    }
    public static boolean isConnectedToInternet() {

        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public void sendInput(String input, Boolean state_switch) {
        Log.d("TAG", "sendInput: got the input: " + input);
        image_type = input;
        state = state_switch;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        qActivity = this;

        qrCode = "";
        dbManager = new DBManager(this);
        dbManager.open();

        previewView = findViewById(R.id.activity_main_previewView);
        qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton);
        progressIndicator = findViewById(R.id.progress_circular);
        setting = findViewById(R.id.setting);
        view = findViewById(R.id.img_qr_box);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SettingFragment settingFragment = new SettingFragment(getApplicationContext(),state);
                settingFragment.show(getFragmentManager(),"setting");
            }
        });

            qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkQrcodeValidity(qrCode)) {
                    if (!isNetworkConnected() || !isConnectedToInternet()) {
                        String patient_str = dbManager.searchPatient(qrCode);
                        if (patient_str != null) {
                            Log.d("patient_str", patient_str);
                            Intent intent = new Intent(QActivity.this, PatientInfActivity.class);
                            intent.putExtra("qrcode", qrCode);
                            intent.putExtra("patient_str", patient_str);
                            intent.putExtra("image_type",image_type);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.d("patient_str", "here");
                            dbManager.insert("null", qrCode, "null", getCurrentDateAndTime(), "no", "null", "null", "null");
                            Intent intent = new Intent(QActivity.this, PatientInfActivity.class);

                            intent.putExtra("qrcode", qrCode);
                            intent.putExtra("patient_str", "null");
                            intent.putExtra("image_type",image_type);

                            startActivity(intent);
                            finish();
                        }
                    } else {
                        progressIndicator.setVisibility(View.VISIBLE);
                        progressIndicator.show();
                        Log.d("search:", "Hereeee");
                        SearchIndividual(qrCode);
                    }


                }
                else
                    Toast.makeText(getApplicationContext(),"Your Qr Code is not acceptable", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private RetrofitAPI RetrofitBuilder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ph-test.kronikare.ai/detector/sereno/api/detector/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        return retrofitAPI;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
    private void postData(RequestData requestData) {

        RetrofitAPI retrofitAPI = RetrofitBuilder();
        Call<Patient> call = retrofitAPI.createPost(requestData);
        call.enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {

                if (!response.isSuccessful()) {
                    //textView.setText("Code " + response.code());
                    progressIndicator.hide();
                    progressIndicator.setVisibility(View.INVISIBLE);

                    Log.d("isNotSuccessful:", response.message());
                    Toast.makeText(getApplicationContext(),"There is a problem. Please try again",Toast.LENGTH_SHORT).show();
                    return;
                }

                progressIndicator.hide();
                progressIndicator.setVisibility(View.INVISIBLE);

                Patient responseFromAPI = response.body();
                Gson gson = new Gson();
                patient_str = gson.toJson(responseFromAPI);
                dbManager.insert(patient_str,qrCode, responseFromAPI.getIndividual().getId(),
                        getCurrentDateAndTime(), "no","null", "null", "null");
                Log.d("responseeeeeeeee",patient_str);
                Intent intent = new Intent(QActivity.this, FlirCameraActivity.class);
                intent.putExtra("qrcode", qrCode);
                intent.putExtra("image_type",image_type);
                if(patient_str != null)
                    intent.putExtra("patient_str",patient_str);
                startActivity(intent);
                //finish();
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {

                Log.d("Failure",t.getMessage());
            }
        });
    }

    private void SearchIndividual(String identifier){

        RetrofitAPI retrofitAPI = RetrofitBuilder();
        Call<Patient> listCall = retrofitAPI.SearchIndividual(identifier);
        listCall.enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {
                if (!response.isSuccessful()) {
                    Log.d("isNotSuccessful:", response.message());
                    Log.d("CodeHere:","Here");
                    postData(makeRequest(qrCode));
                    return;
                }

                progressIndicator.hide();
                progressIndicator.setVisibility(View.INVISIBLE);
                patient = response.body();

                Gson gson = new Gson();
                String response_api = gson.toJson(patient);

                Log.d("JSONNNNN",response_api);
                patient_str = response_api;
                String saved_visit = "null";
                if(patient.getIndividual().getVisitResponse()!= null){
                    IndividualVisit visitResponse = patient.getIndividual().getVisitResponse();
                    String visit_response_str = gson.toJson(visitResponse);
                    saved_visit = visit_response_str;
                }


                if(dbManager.searachQrcode(qrCode) != null){
                    Cursor cur = dbManager.searachQrcode(qrCode);
                    if (cur.moveToFirst()) {
                        do {
                            visit_str = cur.getString(6);
                            thermal_str = cur.getString(7);
                            visible_str = cur.getString(8);
                        } while (cur.moveToNext());
                    }
                    cur.close();

                    if(!visit_str.equals("null"))
                        saved_visit = visit_str;

                    dbManager.update(patient_str,qrCode, patient.getIndividual().getId(),
                            getCurrentDateAndTime(), "no",saved_visit, thermal_str, visible_str);

                }

                Intent intent = new Intent(QActivity.this, PatientInfActivity.class);
                intent.putExtra("qrcode", qrCode);
                intent.putExtra("image_type",image_type);
                if(patient_str != null)
                    intent.putExtra("patient_str",patient_str);
                startActivity(intent);
                finish();
                Log.d("response_api",response_api);
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {
                Log.d("failllll", t.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }
    public void startCamera() {
        if(ContextCompat.checkSelfPermission(this, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        } else {
            getPermissions();
        }
    }

    private void getPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION}, PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (requestCode == PERMISSION_CODE) {
            setupCamera();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setupCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        int lensFacing = CameraSelector.LENS_FACING_BACK;
        cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindAllCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "cameraProviderFuture.addListener Error", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindAllCameraUseCases() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
            bindPreviewUseCase();
            bindAnalysisUseCase();
        }
    }

    private void bindPreviewUseCase() {
        if (cameraProvider == null) {
            return;
        }

        if (previewUseCase != null) {
            cameraProvider.unbind(previewUseCase);
        }

        Preview.Builder builder = new Preview.Builder();
        //builder.setTargetRotation(getRotation());

        previewUseCase = builder.build();
        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());

        try {
            cameraProvider
                    .bindToLifecycle(this, cameraSelector, previewUseCase);
        } catch (Exception e) {
            Log.e(TAG, "Error when bind preview", e);
        }
    }

    private void bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return;
        }

        if (analysisUseCase != null) {
            cameraProvider.unbind(analysisUseCase);
        }

        Executor cameraExecutor = Executors.newSingleThreadExecutor();

        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
        //builder.setTargetRotation(getRotation());

        analysisUseCase = builder.build();
        analysisUseCase.setAnalyzer(cameraExecutor, this::analyze);

        try {
            cameraProvider
                    .bindToLifecycle(this, cameraSelector, analysisUseCase);
        } catch (Exception e) {
            Log.e(TAG, "Error when bind analysis", e);
        }
    }

    protected int getRotation() throws NullPointerException {
        return previewView.getDisplay().getRotation();
    }

    @SuppressLint("UnsafeOptInUsageError")
    private void analyze(@NonNull ImageProxy image) {
        if (image.getImage() == null) return;

        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(),
                image.getImageInfo().getRotationDegrees()
        );

        BarcodeScanner barcodeScanner = BarcodeScanning.getClient();
        barcodeScanner.process(inputImage)
                .addOnSuccessListener(this::onSuccessListener)
                .addOnFailureListener(e -> Log.e(TAG, "Barcode process failure", e))
                .addOnCompleteListener(task -> image.close());
    }

    private void onSuccessListener(List<Barcode> barcodes) {
        if (barcodes.size() > 0) {
            qrCode = barcodes.get(0).getDisplayValue();
            view.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.scanning_box_2));
            //Toast.makeText(this, barcodes.get(0).getDisplayValue(), Toast.LENGTH_SHORT).show();
        }
        else{
            view.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.scanning_box));
        }
    }
}