package com.hdiz.datacollection.services;

import static com.hdiz.datacollection.activities.QActivity.getCurrentDateAndTime;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hdiz.datacollection.RetrofitAPI;
import com.hdiz.datacollection.handler.DBManager;
import com.hdiz.datacollection.objects.Contact;
import com.hdiz.datacollection.objects.Error;
import com.hdiz.datacollection.objects.ImgUploadResponse;
import com.hdiz.datacollection.objects.Patient;
import com.hdiz.datacollection.objects.PatientDetails;
import com.hdiz.datacollection.objects.PatientVisit;
import com.hdiz.datacollection.objects.RequestData;
import com.hdiz.datacollection.objects.TestResult;
import com.hdiz.datacollection.objects.VisitResponse;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyJobService extends JobService {
    private JobParameters jobParameters;
    private String patient_str, server_id, qrcode, uri_thermal, uri_visible, patient_visit_str;
    private DBManager dbManager;

    @Override
    public boolean onStartJob(JobParameters params) {
        this.jobParameters = params;

        Log.d("serviceHere","4");
        Boolean ok = fetchLocalData();
        Log.d("DataExist",ok+"");
        if(ok){
            Log.d("Start Service:","Running!");
            Log.d("serverID:",server_id);
            Log.d("qrcode:",qrcode);
            Gson gsonn = new Gson();
            RequestData requestData = makeRequest(qrcode);
            String req_str = gsonn.toJson(requestData,RequestData.class);
            Log.d("reqstr",req_str);

            if(server_id.equals("null")){
                postData(makeRequest(qrcode));
            }else {
                if(!patient_visit_str.equals("null") && !uri_thermal.equals("null") && !uri_visible.equals("null")){
                    Gson gson = new Gson();
                    PatientVisit patientVisit = gson.fromJson(patient_visit_str, PatientVisit.class);
                    postInfo(server_id,patientVisit);
                }
                else{
                    dbManager.update(patient_str,qrcode,server_id
                            ,getCurrentDateAndTime(),"no","null","null","null");
                    jobFinished(jobParameters,true);
                }
            }
        } else
            jobFinished(jobParameters,true);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d("Job Cancelled:","Done!");
        return false;
    }

    private Boolean fetchLocalData(){
        Boolean isDataExist = false;
        dbManager = new DBManager(this);
        dbManager.open();
        Cursor cursor = dbManager.SyncablePatient("no");
        if(!isCursorEmpty(cursor)){
            isDataExist = true;
            patient_str = cursor.getString(1);
            qrcode = cursor.getString(2);
            server_id = cursor.getString(3);
            patient_visit_str = cursor.getString(6);
            uri_thermal = cursor.getString(7);
            uri_visible = cursor.getString(8);
        }
        else
            isDataExist = false;

        return isDataExist;

    }
    private RetrofitAPI RetrofitBuilder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ph-test.kronikare.ai/detector/sereno/api/detector/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        return retrofitAPI;
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
                    if (response.code() == 400) {
                        Gson gson = new GsonBuilder().create();
                        Error mError = new Error();
                        try {
                            mError= gson.fromJson(response.errorBody().string(),Error.class);
                            Log.d("isNotSuccessful:", mError.getMessage());

                            if(mError.getMessage().equals("A similar individual already exists in the database")){
                                dbManager.update(patient_str,qrcode,server_id
                                        ,getCurrentDateAndTime(),"yes",patient_visit_str,uri_thermal,uri_visible);
                                jobFinished(jobParameters,true);
                            }

                        } catch (IOException e) {
                            // handle failure to read error
                        }
                    }
                    return;
                }

                Patient responseFromAPI = response.body();
                Gson gson = new Gson();
                patient_str = gson.toJson(responseFromAPI);
                Log.d("patient_str",patient_str);


                Patient patient = gson.fromJson(patient_str, Patient.class);

                Log.d("visit_str",patient_visit_str);
                Log.d("uri_th",uri_thermal);
                Log.d("uri_vi",uri_visible);
                if(!patient_visit_str.equals("null") && !uri_thermal.equals("null") && !uri_visible.equals("null")){
                    PatientVisit patientVisit = gson.fromJson(patient_visit_str, PatientVisit.class);
                    postInfo(patient.getIndividual().getId(),patientVisit);
                }
                else{
                    dbManager.update(patient_str,qrcode,responseFromAPI.getIndividual().getId()
                            ,getCurrentDateAndTime(),"no","null","null","null");
                    jobFinished(jobParameters,true);
                }

            }

            @Override
            public void onFailure(Call<Patient> call, Throwable t) {

                Log.d("Failure",t.getMessage());
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



                File file_thermal = new File(uri_thermal);
                File file_visible = new File(uri_visible);

                MultipartBody.Part thermal_file = MultipartBody.Part.createFormData("thermal_file", file_thermal.getName(), RequestBody.create(MediaType.parse("image/*"), file_thermal));
                MultipartBody.Part visible_file = MultipartBody.Part.createFormData("visible_file", file_visible.getName(), RequestBody.create(MediaType.parse("image/*"), file_visible));
                MultipartBody.Part local_captured_at = MultipartBody.Part.createFormData("local_captured_at", getCurrentDateAndTime());
                MultipartBody.Part target_type = MultipartBody.Part.createFormData("target_type", "body");

                UploadImgs(responseFromAPI.getIndividual_visit().getId(), local_captured_at, target_type, visible_file, thermal_file);
            }

            @Override
            public void onFailure(Call<VisitResponse> call, Throwable t) {

                Log.d("Failure",t.getMessage());
            }
        });
    }
    private boolean isCursorEmpty(Cursor cursor){
        return !cursor.moveToFirst() || cursor.getCount() == 0;
    }
    private void UploadImgs(String visit_id, MultipartBody.Part local_captured_at, MultipartBody.Part target_type,
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
                Patient patient = gson.fromJson(patient_str, Patient.class);
                dbManager.update(patient_str,qrcode,patient.getIndividual().getId()
                        ,getCurrentDateAndTime(),"yes",patient_visit_str,uri_thermal,uri_visible);
                jobFinished(jobParameters,true);

            }
            @Override
            public void onFailure(Call<ImgUploadResponse> call, Throwable t) {
                Log.d("Failure",t.getMessage());
            }
        });
    }

}
