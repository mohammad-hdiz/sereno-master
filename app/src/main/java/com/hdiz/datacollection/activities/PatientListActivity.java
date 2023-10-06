package com.hdiz.datacollection.activities;

import static com.hdiz.datacollection.activities.QActivity.dbManager;
import static com.hdiz.datacollection.activities.QActivity.isConnectedToInternet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.RetrofitAPI;
import com.hdiz.datacollection.adapters.PatientsListAdapter;
import com.hdiz.datacollection.handler.DBManager;
import com.hdiz.datacollection.objects.Individual;
import com.hdiz.datacollection.objects.IndividualVisit;
import com.hdiz.datacollection.objects.Patient;
import com.hdiz.datacollection.objects.Patients;

import java.text.ParseException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PatientListActivity extends AppCompatActivity {

    private Patients patients ;
    private ArrayList<Individual> individuals = new ArrayList<>();
    private PatientsListAdapter patientsListAdapter;
    private RecyclerView recyclerView;
    private LinearLayout linearLayout;
    private BottomNavigationView bottomNavigationView;
    private ArrayList<Individual> individual_list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
        recyclerView = findViewById(R.id.patient_list);
        linearLayout = findViewById(R.id.empty_layout);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.menu_patient_list);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_qrcode:
                        // Handle the Qrcode button click
                        Intent intent = new Intent(PatientListActivity.this, QActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu_patient_list:
                        // Handle the patientList button click
                        return true;
                    default:
                        return false;
                }
            }
        });

        dbManager = new DBManager(this);
        dbManager.open();

        initializePatientList();

    }


    private RetrofitAPI RetrofitBuilder() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ph-test.kronikare.ai/detector/sereno/api/detector/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI retrofitAPI = retrofit.create(RetrofitAPI.class);
        return retrofitAPI;
    }
    private ArrayList<Individual> getLocalPatient(){
        Cursor cursor = dbManager.queueAll();
        Gson gson = new Gson();

        if(cursor != null){
                linearLayout.setVisibility(View.GONE);
                if (cursor.moveToFirst()) {
                    do {
                        if(!cursor.isNull(1)){
                            String patient_str = cursor.getString(1);
                            String qr_code = cursor.getString(2);
                            IndividualVisit individualVisit = gson.fromJson(cursor.getString(6), IndividualVisit.class);

                            if(patient_str.equals("") || patient_str.equals("null")){
                                String age = "--", captured_at = "", created_at = "";
                                if(individualVisit != null){
                                    if(individualVisit.getInfo() != null){
                                        age  = individualVisit.getInfo().getPatient_details().getAge();
                                        captured_at = individualVisit.getLocal_visited_at();
                                        created_at = individualVisit.getLocal_visited_at();
                                        Individual individual = new Individual(age,"",captured_at,created_at
                                                ,"","","",individualVisit,qr_code,"",captured_at,
                                                "","","",null);

                                        individuals.add(individual);
                                        Log.d("len indiv",individuals.size()+"");
                                    }
                                }

                            }
                            else{
                                Patient patient = gson.fromJson(patient_str,Patient.class);
                                individuals.add(patient.getIndividual());
                            }

                        }
                    } while (cursor.moveToNext());
                }

        }
        else
            linearLayout.setVisibility(View.VISIBLE);

        return individuals;
    }
    private ArrayList<Individual> getServerPatient(){
        RetrofitAPI retrofitAPI = RetrofitBuilder();
        Call<Patients> listCall = retrofitAPI.getPosts();
        listCall.enqueue(new Callback<Patients>() {
            @Override
            public void onResponse(Call<Patients> call, Response<Patients> response) {

                if (!response.isSuccessful()) {
                    //textView.setText("Code " + response.code());
                    Log.d("isNotSuccessfulListt:", response.message());
                    return;
                }
                patients = response.body();
                Log.d("patients Listtttt:", patients.toString());

                if(patients!= null)
                    individuals = patients.getIndividuals();
                else
                    linearLayout.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(Call<Patients> call, Throwable t) {
                Log.d("failllll", t.getMessage());
            }
        });
        return individuals;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    private void initializePatientList() {
        if (!isNetworkConnected() || !isConnectedToInternet()) {
            individual_list = getLocalPatient();
        } else {
            individual_list = getServerPatient();
        }

        try {
            if (patientsListAdapter == null) {
                Log.d("here1","here");
                patientsListAdapter = new PatientsListAdapter(individual_list);
                recyclerView.setAdapter(patientsListAdapter);
            } else {
                Log.d("here2","here");
                patientsListAdapter.updateList(individual_list);
                patientsListAdapter.notifyDataSetChanged();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        initializePatientList();
    }
}