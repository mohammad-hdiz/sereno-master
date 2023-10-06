package com.hdiz.datacollection;

import com.google.gson.JsonObject;
import com.hdiz.datacollection.objects.ImgUploadResponse;
import com.hdiz.datacollection.objects.Individual;
import com.hdiz.datacollection.objects.IndividualVisit;
import com.hdiz.datacollection.objects.Patient;
import com.hdiz.datacollection.objects.PatientVisit;
import com.hdiz.datacollection.objects.Patients;
import com.hdiz.datacollection.objects.RequestData;
import com.hdiz.datacollection.objects.VisitResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RetrofitAPI {

    @Headers({"X-Requested-With: Android", "DeviceDeployID: ABCDEFGHIJKLMNOP"})
    @POST("individuals")
    Call<Patient> createPost(@Body RequestData requestData);

    @Headers({"X-Requested-With: Android", "DeviceDeployID: ABCDEFGHIJKLMNOP"})
    @POST("individual-visits")
    Call<VisitResponse> postVisit(@Query("individual_id") String id, @Body PatientVisit patientVisit);

    @Headers({"X-Requested-With: Android", "DeviceDeployID: ABCDEFGHIJKLMNOP"})
    @GET("individuals")
    Call<Patients> getPosts();

    @Headers({"X-Requested-With: Android", "DeviceDeployID: ABCDEFGHIJKLMNOP"})
    @GET("individual-search")
    Call<Patient> SearchIndividual(@Query("unique_id") String identifier);


    @Headers({"X-Requested-With: Android", "DeviceDeployID: ABCDEFGHIJKLMNOP"})
    @Multipart
    @POST("thermal-original-files")
    Call<ImgUploadResponse> uploadImgs(@Query("individual_visit_id") String id, @Part MultipartBody.Part local_captured_at,
                                       @Part MultipartBody.Part target_type, @Part MultipartBody.Part visible_file,
                                       @Part MultipartBody.Part thermal_file);

}

