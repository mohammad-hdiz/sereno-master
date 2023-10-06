package com.hdiz.datacollection.activities;

import static com.hdiz.datacollection.activities.QActivity.dbManager;
import static com.hdiz.datacollection.activities.QActivity.getCurrentDateAndTime;
import static com.hdiz.datacollection.activities.QActivity.isConnectedToInternet;
import static com.hdiz.datacollection.activities.SplashActivity.DB_NAME_PREF;
import static com.hdiz.datacollection.activities.SplashActivity.preferenceFileExist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.RetrofitAPI;
import com.hdiz.datacollection.fragments.DialogFragment;
import com.hdiz.datacollection.handler.DBManager;
import com.hdiz.datacollection.objects.Contact;
import com.hdiz.datacollection.objects.ImageProperty;
import com.hdiz.datacollection.objects.ImgUploadResponse;
import com.hdiz.datacollection.objects.IndividualVisit;
import com.hdiz.datacollection.objects.Patient;
import com.hdiz.datacollection.objects.PatientDetails;
import com.hdiz.datacollection.objects.PatientVisit;
import com.hdiz.datacollection.objects.RequestData;
import com.hdiz.datacollection.objects.TestResult;
import com.hdiz.datacollection.objects.VisitResponse;
import com.qandeelabbassi.dropsy.DropDownItem;
import com.qandeelabbassi.dropsy.DropDownView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TestResultsActivity extends AppCompatActivity implements DialogFragment.OnInputListener{

    Bitmap bmp_thermal, bmp_visible;
    FloatingActionButton verify_tick, back_to_view;
    Button self_btn, gp_btn, mamo_btn, sono_btn, patho_btn, side_btn, size_btn;

    DropDownView self_drop, gp_drop, mamo_drop,sono_drop, patho_drop, side_drop, size_drop;
    String self_str, gp_str, mamo_str, sono_str, patho_str, side_str, size_str;

    TextView self_txt, gp_txt, mamo_txt, sono_txt, patho_txt, side_txt, size_txt;
    ImageView self_edt, gp_edt, mamo_edt, sono_edt, patho_edt, side_edt, size_edt;

    String thermal_uri_str,visible_uri_str;
    EditText grade, stage;
    ImageView thermal_v, visible_v;
    String note_encoded = "";
    String note_self, note_gp, note_mamo, note_sono, note_patho, note_tumor_side, note_tumor_size;
    LinearLayout self_exam, self_exam_btn, gp_result, gp_result_btn, mamography_result, mamography_result_btn,
            sonography_result, sonography_result_btn, pathology_result,
            pathology_result_btn, tumor_side, tumor_side_btn, tumor_size, tumor_size_btn;

    String qr_code, patient_str, patient_details;
    String v_type = "gone";
    String db_id = "";


    private IndividualVisit individualVisit = null;
    private Patient patient = null;
    private CircularProgressIndicator progressIndicator;

    private void getVisitInfo(){
        note_self = individualVisit.getInfo().getSelf_exam_note();
        note_gp = individualVisit.getInfo().getGp_note();
        note_mamo = individualVisit.getInfo().getMamo_note();
        note_sono = individualVisit.getInfo().getSono_note();
        note_patho = individualVisit.getInfo().getPatho_note();
        note_tumor_side = individualVisit.getInfo().getTumor_side_note();
        note_tumor_size = individualVisit.getInfo().getTumor_size_note();

        if(!individualVisit.getInfo().getSelf_exam().equals("null")
                && !individualVisit.getInfo().getSelf_exam().equals("")){
            String self_exam = individualVisit.getInfo().getSelf_exam();
            self_str = self_exam;
            self_txt.setVisibility(View.VISIBLE);
            self_txt.setText(self_exam);
            self_edt.setVisibility(View.VISIBLE);
            self_drop.setVisibility(View.GONE);
        }

        if(!individualVisit.getInfo().getGp().equals("null")
                && !individualVisit.getInfo().getGp().equals("")){
            String gp = individualVisit.getInfo().getGp();
            gp_str = gp;
            gp_txt.setVisibility(View.VISIBLE);
            gp_txt.setText(gp);
            gp_edt.setVisibility(View.VISIBLE);
            gp_drop.setVisibility(View.GONE);
        }

        if(!individualVisit.getInfo().getMamography().equals("null")
                && !individualVisit.getInfo().getMamography().equals("")){
            String mamo = individualVisit.getInfo().getMamography();
            mamo_str = mamo;
            mamo_txt.setVisibility(View.VISIBLE);
            mamo_txt.setText(mamo);
            mamo_edt.setVisibility(View.VISIBLE);
            mamo_drop.setVisibility(View.GONE);
        }

        if(!individualVisit.getInfo().getSonography().equals("null")
                && !individualVisit.getInfo().getSonography().equals("")){
            String sono = individualVisit.getInfo().getSonography();
            sono_str = sono;
            sono_txt.setVisibility(View.VISIBLE);
            sono_txt.setText(sono);
            sono_edt.setVisibility(View.VISIBLE);
            sono_drop.setVisibility(View.GONE);
        }
        if(!individualVisit.getInfo().getPathology().equals("null")
                && !individualVisit.getInfo().getPathology().equals("")){
            String patho = individualVisit.getInfo().getPathology();
            patho_str = patho;
            patho_txt.setVisibility(View.VISIBLE);
            patho_txt.setText(patho);
            patho_edt.setVisibility(View.VISIBLE);
            patho_drop.setVisibility(View.GONE);
        }

        if(!individualVisit.getInfo().getTumor_side().equals("null")
                && !individualVisit.getInfo().getTumor_side().equals("")){
            String side = individualVisit.getInfo().getTumor_side();
            side_str = side;
            side_txt.setVisibility(View.VISIBLE);
            side_txt.setText(side);
            side_edt.setVisibility(View.VISIBLE);
            side_drop.setVisibility(View.GONE);
        }

        if(!individualVisit.getInfo().getTumor_size().equals("null")
                && !individualVisit.getInfo().getTumor_size().equals("")){
            String size = individualVisit.getInfo().getTumor_size();
            size_str = size;
            size_txt.setVisibility(View.VISIBLE);
            size_txt.setText(size);
            size_edt.setVisibility(View.VISIBLE);
            size_drop.setVisibility(View.GONE);
        }

        grade.setText(individualVisit.getInfo().getGrade());
        stage.setText(individualVisit.getInfo().getStage());
    }
    public void InitVariables(){

        if(individualVisit!=null){
           if(individualVisit.getInfo()!=null){
               getVisitInfo();
           }
        }
    }

    @Override
    public void sendInput(String input) {
        Log.d("TAG", "sendInput: got the input: " + input);
        note_encoded = input;
        String[] split = note_encoded.split("MOH");
        String note = split[0];
        String tag = split[1];

        switch (tag){
            case "self":
                note_self = note;
                break;
            case "gp":
                note_gp = note;
                break;
            case "mamo":
                note_mamo = note;
                break;
            case "sono":
                note_sono = note;
                break;
            case "patho":
                note_patho = note;
                break;
            case "tumor_side":
                note_tumor_side = note;
                break;
            case "tumor_size":
                note_tumor_size = note;
                break;
        }
    }
    private void getBundleInf(){
        thermal_v = findViewById(R.id.thermal_img_v);
        visible_v = findViewById(R.id.visible_img_v);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {

            Gson gson = new Gson();

            if (extras.containsKey("qrcode")) {
                qr_code = getIntent().getStringExtra("qrcode");
            }
            if (extras.containsKey("patient_str")) {
                patient_str = getIntent().getStringExtra("patient_str");
                Log.d("patientTest",patient_str);
                if(!patient_str.equals("null")) {
                    patient = gson.fromJson(patient_str, Patient.class);
                    if (patient.getIndividual().getVisitResponse() != null)
                        individualVisit = patient.getIndividual().getVisitResponse();
                }
                else{
                    Cursor cur = dbManager.searachQrcode(qr_code);
                    if(cur != null){
                        if (cur.moveToFirst()) {
                            do {
                                String visit = cur.getString(6);
                                individualVisit = gson.fromJson(visit,IndividualVisit.class);
                            } while (cur.moveToNext());
                        }
                        cur.close();
                    }
                }
            }

            if (extras.containsKey("patient_details")) {
                patient_details = getIntent().getStringExtra("patient_details");
            }
            if (extras.containsKey("uri_thermal")) {
                thermal_uri_str = getIntent().getStringExtra("uri_thermal");
                try {
                    bmp_thermal = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(thermal_uri_str)));
                    if(bmp_thermal != null){
                        Bitmap bmp_round_th = getRoundedCornerBitmap(bmp_thermal,25);
                        thermal_v.setImageBitmap(bmp_round_th);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (extras.containsKey("uri_visible")) {
                visible_uri_str = getIntent().getStringExtra("uri_visible");
                try {
                    bmp_visible = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(visible_uri_str)));
                    if(bmp_visible != null){
                        Bitmap bmp_round_v = getRoundedCornerBitmap(bmp_visible,25);
                        visible_v.setImageBitmap(bmp_round_v);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (extras.containsKey("v_type")) {
                v_type = getIntent().getStringExtra("v_type");

                if(v_type.equals("gone")) {
                    visible_v.setVisibility(View.GONE);
                }
                if(v_type.equals("visible")) {
                    visible_v.setVisibility(View.VISIBLE);
                }

            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_results);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        note_self = note_gp = note_mamo = note_sono = note_patho = note_tumor_side = note_tumor_size = "";
        self_str = gp_str = mamo_str = sono_str = patho_str = "";
        side_str = size_str = "";

        getBundleInf();
        getView(width);
        InitVariables();

        if(preferenceFileExist(DB_NAME_PREF,getApplicationContext())){
            SharedPreferences prefs = getSharedPreferences(DB_NAME_PREF, MODE_PRIVATE);
            db_id = prefs.getString("id", "null"); //"null" is the default value.
        }

        self_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self_drop.setVisibility(View.VISIBLE);
                self_txt.setVisibility(View.GONE);
                self_edt.setVisibility(View.GONE);
            }
        });
        gp_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gp_drop.setVisibility(View.VISIBLE);
                gp_txt.setVisibility(View.GONE);
                gp_edt.setVisibility(View.GONE);
            }
        });
        mamo_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mamo_drop.setVisibility(View.VISIBLE);
                mamo_txt.setVisibility(View.GONE);
                mamo_edt.setVisibility(View.GONE);

            }
        });
        sono_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sono_drop.setVisibility(View.VISIBLE);
                sono_txt.setVisibility(View.GONE);
                sono_edt.setVisibility(View.GONE);
            }
        });
        patho_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                patho_drop.setVisibility(View.VISIBLE);
                patho_txt.setVisibility(View.GONE);
                patho_edt.setVisibility(View.GONE);
            }
        });
        side_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                side_drop.setVisibility(View.VISIBLE);
                side_txt.setVisibility(View.GONE);
                side_edt.setVisibility(View.GONE);
            }
        });

        size_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                size_drop.setVisibility(View.VISIBLE);
                size_txt.setVisibility(View.GONE);
                size_edt.setVisibility(View.GONE);
            }
        });
        self_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(),"self",note_self);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        gp_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(),"gp", note_gp);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        mamo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(),"mamo", note_mamo);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        sono_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = new DialogFragment(v.getContext(),"sono", note_sono);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        patho_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = new DialogFragment(v.getContext(),"patho", note_patho);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        side_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = new DialogFragment(v.getContext(),"tumor_side", note_tumor_side);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        size_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = new DialogFragment(v.getContext(),"tumor_size", note_tumor_size);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        back_to_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onBackPressed();
            }
        });
        verify_tick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isNetworkConnected() || !isConnectedToInternet()) {

                    Gson gson = new Gson();
                    PatientDetails patientDetails = gson.fromJson(patient_details , PatientDetails.class);
                    TestResult testResult = new TestResult(self_str, note_self, gp_str, note_gp, mamo_str,
                            note_mamo, sono_str, note_sono, patho_str, note_patho, side_str, note_tumor_side,
                            size_str, note_tumor_size, grade.getText().toString(), stage.getText().toString(),patientDetails);

                    String test_result_str = gson.toJson(testResult);
                    Log.d("Test Results", test_result_str);
                    PatientVisit patientVisit = new PatientVisit(getCurrentDateAndTime(),testResult,"" );
                    if(patient_str.equals("null") || patient_str.equals("")){
                        dbManager.update("null",qr_code,"null"
                                ,getCurrentDateAndTime(),"no",gson.toJson(patientVisit), thermal_uri_str, visible_uri_str);

                        try {
                            backupDatabase();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                    }else{
                        Patient patient = gson.fromJson(patient_str, Patient.class);
                        dbManager.update(patient_str,qr_code,patient.getIndividual().getId()
                                ,getCurrentDateAndTime(),"no",gson.toJson(patientVisit), thermal_uri_str, visible_uri_str);

                        try {
                            backupDatabase();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }

                    PatientInfActivity.patientInfActivity.finish();
                    if (QActivity.qActivity != null)
                        QActivity.qActivity.finish();
                    Toast.makeText(getApplicationContext(), "Locally Done!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TestResultsActivity.this, VisitRetrieveActivity.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    if(patient_str.equals("null")) {
                        postData(makeRequest(qr_code));
                    }
                    else{
                        Gson gson = new Gson();
                        Log.d("patient_str",patient_str);
                        Patient patient = gson.fromJson(patient_str, Patient.class);
                        PatientDetails patientDetails = gson.fromJson(patient_details , PatientDetails.class);
                        TestResult testResult = new TestResult(self_str, note_self, gp_str, note_gp, mamo_str,
                                note_mamo, sono_str, note_sono, patho_str, note_patho, side_str, note_tumor_side,
                                size_str, note_tumor_size, grade.getText().toString(), stage.getText().toString(),patientDetails);

                        String test_result_str = gson.toJson(testResult);
                        Log.d("Test Results", test_result_str);
                        PatientVisit patientVisit = new PatientVisit(getCurrentDateAndTime(),testResult,"" );

                        dbManager.update(patient_str,qr_code,patient.getIndividual().getId()
                                ,getCurrentDateAndTime(),"no",gson.toJson(patientVisit),thermal_uri_str,visible_uri_str);

                        try {
                            backupDatabase();
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        Log.d("PatientVisit", gson.toJson(patientVisit));
                        Log.d("PatientID", patient.getIndividual().getId());
                        postInfo(patient.getIndividual().getId(),patientVisit);
                    }

                }

            }
        });

        self_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
              self_str = dropDownItem.getText() ;
            }
        });
        gp_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                gp_str = dropDownItem.getText();
            }
        });
        mamo_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                mamo_str = dropDownItem.getText();
            }
        });
        sono_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                sono_str = dropDownItem.getText();

            }
        });
        patho_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                patho_str = dropDownItem.getText();

            }
        });
        side_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                side_str = dropDownItem.getText();
            }
        });
        size_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                size_str = dropDownItem.getText();
            }
        });

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 5) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public void backupDatabase() throws IOException {
        //Open your local db as the input stream
        String inFileName = "/data/data/com.hdiz.datacollection/databases/SERENO";
        File dbFile = new File(inFileName);
        FileInputStream fis = new FileInputStream(dbFile);

        String outFileName = Environment.getExternalStorageDirectory()+ "/DB_" + db_id + ".sqlite";
        //Open the empty db as the output stream
        OutputStream output = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer))>0){
            output.write(buffer, 0, length);
        }
        //Close the streams
        output.flush();
        output.close();
        fis.close();
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

    private void postData(RequestData requestData) {

        RetrofitAPI retrofitAPI = RetrofitBuilder();
        Call<Patient> call = retrofitAPI.createPost(requestData);
        call.enqueue(new Callback<Patient>() {
            @Override
            public void onResponse(Call<Patient> call, Response<Patient> response) {

                if (!response.isSuccessful()) {
                    Log.d("isNotSuccessful:", response.message());
                    Toast.makeText(getApplicationContext(),"There is a problem. Please try again",Toast.LENGTH_SHORT).show();
                    return;
                }

                Patient responseFromAPI = response.body();
                Gson gson = new Gson();
                patient_str = gson.toJson(responseFromAPI);
                Log.d("patient_str",patient_str);


                Patient patient = gson.fromJson(patient_str, Patient.class);
                PatientDetails patientDetails = gson.fromJson(patient_details , PatientDetails.class);
                TestResult testResult = new TestResult(self_str, note_self, gp_str, note_gp, mamo_str,
                        note_mamo, sono_str, note_sono, patho_str, note_patho, side_str, note_tumor_side,
                        size_str, note_tumor_size, grade.getText().toString(), stage.getText().toString(),patientDetails);
                String test_result_str = gson.toJson(testResult);
                Log.d("Test Results", test_result_str);
                PatientVisit patientVisit = new PatientVisit(getCurrentDateAndTime(),testResult,"" );
                dbManager.update(patient_str,qr_code,responseFromAPI.getIndividual().getId()
                        ,getCurrentDateAndTime(),"no",gson.toJson(patientVisit),thermal_uri_str,visible_uri_str);

                try {
                    backupDatabase();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                Log.d("PatientVisit", gson.toJson(patientVisit));
                Log.d("PatientID", patient.getIndividual().getId());
                postInfo(patient.getIndividual().getId(),patientVisit);
            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {

                Log.d("Failure11",t.getMessage());
            }
        });
    }
    private void postInfo( String id, PatientVisit patientVisit){

        RetrofitAPI retrofitAPI = RetrofitBuilder();
        Call<VisitResponse> call = retrofitAPI.postVisit(id,patientVisit);
        call.enqueue(new Callback<VisitResponse>() {
            @Override
            public void onResponse(Call<VisitResponse> call, Response<VisitResponse> response) {

                if (!response.isSuccessful()) {
                    Log.d("isNotSuccessful:", response.message());
                    return;
                }
                VisitResponse responseFromAPI = response.body();
                Gson gson = new Gson();
                String individual_visit = gson.toJson(responseFromAPI);
                Log.d("RESPONSE:",individual_visit);

                Log.d("thermal_uri",thermal_uri_str);
                Log.d("visible_uri",visible_uri_str);

                File file_thermal = new File(thermal_uri_str);
                File file_visible = new File(visible_uri_str);

                MultipartBody.Part thermal_file = MultipartBody.Part.createFormData("thermal_file", file_thermal.getName(), RequestBody.create(MediaType.parse("image/*"), file_thermal));
                MultipartBody.Part visible_file = MultipartBody.Part.createFormData("visible_file", file_visible.getName(), RequestBody.create(MediaType.parse("image/*"), file_visible));
                MultipartBody.Part local_captured_at = MultipartBody.Part.createFormData("local_captured_at", getCurrentDateAndTime());
                MultipartBody.Part target_type = MultipartBody.Part.createFormData("target_type", "body");

                progressIndicator.setVisibility(View.VISIBLE);
                progressIndicator.show();

                UploadImgs(patientVisit, responseFromAPI.getIndividual_visit().getId(), local_captured_at, target_type, visible_file, thermal_file);

            }

            @Override
            public void onFailure(Call<VisitResponse> call, Throwable t) {

                Log.d("Failure1",t.getMessage());
            }
        });
    }

    private RetrofitAPI RetrofitBuilder() {

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ph-test.kronikare.ai/detector/sereno/api/detector/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        return retrofitAPI;
    }

    private void getView(int width){

        progressIndicator = findViewById(R.id.progress);

        //Image views
        verify_tick = findViewById(R.id.img_verify_d);
        back_to_view = findViewById(R.id.back_to_view);

        //Linearlayouts
        self_exam = findViewById(R.id.self_exam);
        self_exam_btn = findViewById(R.id.self_exam_btn);
        gp_result = findViewById(R.id.gp_result);
        gp_result_btn = findViewById(R.id.gp_result_btn);
        mamography_result = findViewById(R.id.mamography_result);
        mamography_result_btn = findViewById(R.id.mamography_result_btn);
        sonography_result = findViewById(R.id.sonography_result);
        sonography_result_btn = findViewById(R.id.sonography_result_btn);
        pathology_result = findViewById(R.id.pathology_result);
        pathology_result_btn = findViewById(R.id.pathology_result_btn);
        tumor_side = findViewById(R.id.tumor_side);
        tumor_side_btn = findViewById(R.id.tumor_side_btn);
        tumor_size = findViewById(R.id.tumor_size);
        tumor_size_btn = findViewById(R.id.tumor_size_btn);

        //Buttons
        self_btn = findViewById(R.id.self_btn);
        gp_btn = findViewById(R.id.gp_btn);
        mamo_btn = findViewById(R.id.mamo_btn);
        sono_btn = findViewById(R.id.sono_btn);
        patho_btn = findViewById(R.id.patho_btn);
        side_btn = findViewById(R.id.side_btn);
        size_btn = findViewById(R.id.size_btn);

        //Drop down
        self_drop = findViewById(R.id.self_exam_dropdown);
        gp_drop = findViewById(R.id.gp_result_dropdown);
        mamo_drop = findViewById(R.id.mamography_result_dropdown);
        sono_drop = findViewById(R.id.sonography_result_dropdown);
        patho_drop = findViewById(R.id.pathology_result_dropdown);
        side_drop = findViewById(R.id.tumor_side_dropdown);
        size_drop = findViewById(R.id.tumor_size_dropdown);

        // TextView
        self_txt = findViewById(R.id.self_txt);
        gp_txt = findViewById(R.id.gp_txt);
        mamo_txt = findViewById(R.id.mamo_txt);
        sono_txt = findViewById(R.id.sono_txt);
        patho_txt = findViewById(R.id.patho_txt);
        side_txt = findViewById(R.id.tumor_side_txt);
        size_txt = findViewById(R.id.tumor_size_txt);

        //ImageView
        self_edt = findViewById(R.id.self_edt);
        gp_edt = findViewById(R.id.gp_edt);
        mamo_edt = findViewById(R.id.mamo_edt);
        sono_edt = findViewById(R.id.sono_edt);
        patho_edt = findViewById(R.id.patho_edt);
        side_edt = findViewById(R.id.tumor_side_edt);
        size_edt = findViewById(R.id.tumor_size_edt);

        //EditTexts
        grade = findViewById(R.id.grade_edt);
        stage = findViewById(R.id.stage_edt);

        self_exam.getLayoutParams().width = (int) (width * 0.8);
        self_exam_btn.getLayoutParams().width = (int) (width * 0.2);

        gp_result.getLayoutParams().width = (int) (width * 0.8);
        gp_result_btn.getLayoutParams().width = (int) (width * 0.2);

        mamography_result.getLayoutParams().width = (int) (width * 0.8);
        mamography_result_btn.getLayoutParams().width = (int) (width * 0.2);

        sonography_result.getLayoutParams().width = (int) (width * 0.8);
        sonography_result_btn.getLayoutParams().width = (int) (width * 0.2);

        pathology_result.getLayoutParams().width = (int) (width * 0.8);
        pathology_result_btn.getLayoutParams().width = (int) (width * 0.2);

        tumor_side.getLayoutParams().width = (int) (width * 0.8);
        tumor_side_btn.getLayoutParams().width = (int) (width * 0.2);

        tumor_size.getLayoutParams().width = (int) (width * 0.8);
        tumor_size_btn.getLayoutParams().width = (int) (width * 0.2);

    }
    public Bitmap getRoundedCornerBitmap(Bitmap bitmap,int roundPixelSize) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = roundPixelSize;
        paint.setAntiAlias(true);
        canvas.drawRoundRect(rectF,roundPx,roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //this.finish();
    }

    public void UploadImgs(PatientVisit patientVisit, String visit_id, MultipartBody.Part local_captured_at, MultipartBody.Part target_type,
                           MultipartBody.Part visible_file, MultipartBody.Part thermal_file){
        RetrofitAPI retrofitAPI = RetrofitBuilder();
        Call<ImgUploadResponse> call = retrofitAPI.uploadImgs(visit_id,local_captured_at,target_type,visible_file,thermal_file);
        call.enqueue(new Callback<ImgUploadResponse>() {
            @Override
            public void onResponse(Call<ImgUploadResponse> call, Response<ImgUploadResponse> response) {

                if (!response.isSuccessful()) {
                    Log.d("isNotSuccessful:", response.message());
                    return;
                }
                ImgUploadResponse responseFromAPI = response.body();
                Gson gson = new Gson();
                String individual_visit = gson.toJson(responseFromAPI);
                Log.d("RESPONSE:",individual_visit);

                Patient patient = gson.fromJson(patient_str,Patient.class);

                dbManager.update(patient_str,qr_code,patient.getIndividual().getId()
                        ,getCurrentDateAndTime(),"yes",gson.toJson(patientVisit),thermal_uri_str,visible_uri_str);

                try {
                    backupDatabase();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                progressIndicator.hide();
                progressIndicator.setVisibility(View.INVISIBLE);


                PatientInfActivity.patientInfActivity.finish();
                if (QActivity.qActivity != null)
                    QActivity.qActivity.finish();

                Toast.makeText(getApplicationContext(), "Done!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TestResultsActivity.this, VisitRetrieveActivity.class);
                startActivity(intent);
                finish();
            }
            @Override
            public void onFailure(Call<ImgUploadResponse> call, Throwable t) {
                Log.d("Failure2",t.getMessage());

                if(t.getMessage().equals("timeout")){
                    progressIndicator.hide();
                    progressIndicator.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"Your Internet Connection is poor. Please try again Later!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}