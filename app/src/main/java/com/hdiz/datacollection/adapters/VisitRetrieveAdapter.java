package com.hdiz.datacollection.adapters;

import static com.hdiz.datacollection.activities.QActivity.dbManager;
import static com.hdiz.datacollection.activities.QActivity.getCurrentDateAndTime;

import android.app.FragmentManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.RetrofitAPI;
import com.hdiz.datacollection.fragments.DialogFragment;
import com.hdiz.datacollection.fragments.PatientRecordFragment;
import com.hdiz.datacollection.handler.DBManager;
import com.hdiz.datacollection.objects.Contact;
import com.hdiz.datacollection.objects.Error;
import com.hdiz.datacollection.objects.ImgUploadResponse;
import com.hdiz.datacollection.objects.IndividualVisit;
import com.hdiz.datacollection.objects.Patient;
import com.hdiz.datacollection.objects.PatientVisit;
import com.hdiz.datacollection.objects.RequestData;
import com.hdiz.datacollection.objects.VisitResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VisitRetrieveAdapter extends RecyclerView.Adapter<VisitRetrieveAdapter.ViewHolder> {

    private Cursor mData;
    private LayoutInflater mInflater;
    private Gson gson;
    private IndividualVisit individualVisit = null;
    private Context context;
    private String patient_str, server_id, patient_visit_str, uri_thermal, uri_visible, qrcode ;
    private String visited_date;
    Boolean ok = false;
    private android.app.FragmentManager fragmentManager;
    // data is passed into the constructor
    public VisitRetrieveAdapter(Context context, Cursor data, FragmentManager fragmentManager) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
        this.fragmentManager = fragmentManager;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.visit_list_adapter, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        mData.moveToPosition(position);
        gson = new Gson();
        String age = "";

        individualVisit = gson.fromJson(mData.getString(6),IndividualVisit.class);
        if(individualVisit != null){

            if(individualVisit.getInfo() != null){
               age  = individualVisit.getInfo().getPatient_details().getAge();
            }
        }
//        visited_date = mData.getString(4);
        holder.p_visit_id.setText(mData.getString(2));
        holder.p_visit_age.setText(age);
        holder.p_visit_date.setText(mData.getString(4));
        if(mData.getString(5).equals("yes"))
            holder.p_visit_status.setText("Complete");
        else
            holder.p_visit_status.setText("Pending");

        try {
            String thermal_uri_str = mData.getString(7);
            Bitmap bmp_thermal = MediaStore.Images.Media.getBitmap(mInflater.getContext().getContentResolver(), Uri.fromFile(new File(thermal_uri_str)));
            if(bmp_thermal != null){
                Bitmap bmp_round_th = getRoundedCornerBitmap(bmp_thermal,25);
                holder.p_visit_img.setImageBitmap(bmp_round_th);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.getCount();
    }


    private Boolean fetchLocalData(){
        Boolean isDataExist = false;
        Cursor cursor = dbManager.SyncablePatient("no");
        if(!isCursorEmpty(cursor)){
            isDataExist = true;
        }
        else
            isDataExist = false;

        return isDataExist;

    }

        // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView p_visit_img, visit_sync;
        TextView p_visit_date, p_visit_id, p_visit_age, p_visit_status;
        LinearLayout visit_layout;

        ViewHolder(View itemView) {
            super(itemView);

            patient_str = server_id = patient_visit_str = uri_thermal = uri_visible = qrcode = "null";
            ok = fetchLocalData();

            // TextViews
            p_visit_date = itemView.findViewById(R.id.p_visit_date);
            p_visit_id = itemView.findViewById(R.id.p_visit_id);
            p_visit_age = itemView.findViewById(R.id.p_visit_age);
            p_visit_status = itemView.findViewById(R.id.p_visit_status);

            // ImageViews
            p_visit_img = itemView.findViewById(R.id.p_visit_img);
            visit_sync = itemView.findViewById(R.id.visit_sync);
            visit_sync.setOnClickListener(this);

            // LinearLayout
            visit_layout = itemView.findViewById(R.id.visit_layout);
            visit_layout.setOnClickListener(this);
          }

        @Override
        public void onClick(View view) {
            mData.moveToPosition(getPosition());
            patient_str = mData.getString(1);
            qrcode = mData.getString(2);
            server_id = mData.getString(3);
            patient_visit_str = mData.getString(6);
            uri_thermal = mData.getString(7);
            uri_visible = mData.getString(8);

            if(view.equals(visit_sync)){
                Log.d("DataExist",ok+"");
                if(ok){
                    Log.d("serverID:",server_id);
                    Log.d("qrcode:",qrcode);

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
                                    ,getCurrentDateAndTime(),"no",patient_visit_str,uri_thermal,uri_visible);
                        }
                    }
                }
                else
                    Toast.makeText(mInflater.getContext(),"This visit has already synchronized!",Toast.LENGTH_SHORT).show();
            }
            if(view.equals(visit_layout)){
                Log.d("VIEWWWWWWWWWWW","HEREE");

                PatientRecordFragment patientRecord = new PatientRecordFragment(view.getContext(), qrcode,
                            patient_str, patient_visit_str, uri_thermal, uri_visible);
                    patientRecord.show(fragmentManager, "MyFragment");
            }
        }


    }

    private RetrofitAPI RetrofitBuilder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ph-test.kronikare.ai/detector/sereno/api/detector/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        return retrofitAPI;
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

                            if(mError.getMessage().equals("A similar patient already exists in the database")){
                                dbManager.update(patient_str,qrcode,server_id
                                        ,getCurrentDateAndTime(),"no",patient_visit_str,uri_thermal,uri_visible);
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
                            ,getCurrentDateAndTime(),"no",patient_visit_str,uri_thermal,uri_visible);
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
                notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call<ImgUploadResponse> call, Throwable t) {
                Log.d("Failure",t.getMessage());
            }
        });
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
    private boolean isCursorEmpty(Cursor cursor){
        return !cursor.moveToFirst() || cursor.getCount() == 0;
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.getString(id);
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

}
