package com.hdiz.datacollection.activities;


import static com.hdiz.datacollection.activities.QActivity.dbManager;
import static com.hdiz.datacollection.activities.QActivity.getCurrentDateAndTime;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.RetrofitAPI;
import com.hdiz.datacollection.adapters.MyRecyclerViewAdapter;
import com.hdiz.datacollection.fragments.DialogFragment;
import com.hdiz.datacollection.objects.IndividualVisit;
import com.hdiz.datacollection.objects.Patient;
import com.hdiz.datacollection.objects.PatientDetails;
import com.hdiz.datacollection.objects.PatientVisit;
import com.hdiz.datacollection.objects.TestResult;
import com.qandeelabbassi.dropsy.DropDownItem;
import com.qandeelabbassi.dropsy.DropDownView;

import java.lang.reflect.Field;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PatientInfActivity extends AppCompatActivity implements DialogFragment.OnInputListener {

    LinearLayout visit, visit_btn, cancer, cancer_btn, cancer_f, cancer_f_btn,
            menopausal, menopausal_btn, gender, gender_btn, race, race_btn, comorbidities, comorbidities_btn, adherence, adherence_btn, medications, medications_btn, age_lay;

    LinearLayout patient_cancer_lay, patient_f_cancer_type_lay, patient_f_cancer_rel_lay;
    DropDownView cancer_drop, cancer_f_drop, relationship, menopausal_drop, gender_drop, race_drop, adherence_drop, medication_drop;

    TextView cancer_txt, cancer_f_txt, meno_txt, rel_txt, gender_txt, adh_txt, medi_txt;
    ImageView cancer_edt, cancer_f_edt, meno_edt, rel_edt, gender_edt, adh_edt, medi_edt;

    // Strings for saving dropdown values
    private String cancer_str, cancer_f_str, relationship_str, menopausal_str, gender_str, race_str, adherence_str, medication_str;

    MaterialCheckBox a1, a2, a3, a4, c1, c2, c3, c4, yes, no, yes_f, no_f;
    EditText age_edt;
    Button vis_btn, can_btn, can_f_btn, meno_btn, gen_btn, ra_btn, como_btn, adh_btn, medi_btn;

    //Strings for getting bundle values
    private String qr_code = "" ;
    private String patient_str = "";

    //String for checkBox values
    private String a1_str, a2_str, a3_str, a4_str, c1_str, c2_str, c3_str, c4_str ;
    private String hist_str, f_hist_str ;

    //Strings for Concatenating checkBox values
    private ArrayList<String> visit_str, como_str, medication_list;

    Button visit_info_btn, save_info_btn;

    private String note_encoded = "";
    private String v_type = "gone";

    //Strings for note values
    String note_visit, note_cancer, note_cancer_f, note_meno, note_gender, note_race, note_como, note_adh, note_medi;

    RecyclerView pres_scroll;
    MyRecyclerViewAdapter myRecyclerViewAdapter;
    private IndividualVisit individualVisit = null;
    private Patient patient = null;
    public static Activity patientInfActivity ;
    @Override
    public void sendInput(String input) {
        Log.d("TAG", "sendInput: got the input: " + input);
        note_encoded = input;
        String[] split = note_encoded.split("MOH");
        String note = split[0];
        String tag = split[1];

        switch (tag) {
            case "visit":
                note_visit = note;
                break;
            case "cancer":
                note_cancer = note;
                break;
            case "cancer_f":
                note_cancer_f = note;
                break;
            case "meno":
                note_meno = note;
                break;
            case "gender":
                note_gender = note;
                break;
            case "race":
                note_race = note;
                break;
            case "como":
                note_como = note;
                break;
            case "adh":
                note_adh = note;
                break;
            case "medi":
                note_medi = note;
                break;
        }

    }

    private void getVisitInfo(){

        // Retrieve notes
        note_visit = individualVisit.getInfo().getPatient_details().getVisit_note();
        note_adh = individualVisit.getInfo().getPatient_details().getAdh_note();
        note_cancer = individualVisit.getInfo().getPatient_details().getCancer_note();
        note_cancer_f = individualVisit.getInfo().getPatient_details().getCancer_f_note();
        note_como = individualVisit.getInfo().getPatient_details().getComorbidities_note();
        note_gender = individualVisit.getInfo().getPatient_details().getGender_note();
        note_medi = individualVisit.getInfo().getPatient_details().getMedications_note();
        note_meno = individualVisit.getInfo().getPatient_details().getMeno_note();

        // Retrieve Inf
        age_edt.setText(individualVisit.getInfo().getPatient_details().getAge());

        Gson gson = new Gson();
        String v = gson.toJson(individualVisit);
        Log.d("indiVisit",v);
        if(!individualVisit.getInfo().getPatient_details().getVisit().get(0).equals("null")) {
            a1.setChecked(true);
            a1_str = "a1";
        }
        if(!individualVisit.getInfo().getPatient_details().getVisit().get(1).equals("null")) {
            a2.setChecked(true);
            a2_str = "a2";
        }
        if(!individualVisit.getInfo().getPatient_details().getVisit().get(2).equals("null")) {
            a3.setChecked(true);
            a3_str = "a3";
        }
        if(!individualVisit.getInfo().getPatient_details().getVisit().get(3).equals("null")) {
            a4.setChecked(true);
            a4_str = "a4";
        }

        if(!individualVisit.getInfo().getPatient_details().getComorbidities().get(0).equals("null")) {
            c1.setChecked(true);
            c1_str = "c1";
        }
        if(!individualVisit.getInfo().getPatient_details().getComorbidities().get(1).equals("null")) {
            c2.setChecked(true);
            c2_str = "c2";
        }
        if(!individualVisit.getInfo().getPatient_details().getComorbidities().get(2).equals("null")) {
            c3.setChecked(true);
            c3_str = "c3";
        }
        if(!individualVisit.getInfo().getPatient_details().getComorbidities().get(3).equals("null")) {
            c4.setChecked(true);
            c4_str = "c4";
        }

        if(individualVisit.getInfo().getPatient_details().getCancer_hist().equals("yes")) {
            yes.setChecked(true);
            hist_str = "yes";
            if(!individualVisit.getInfo().getPatient_details().getCancer_type().equals("null")
                    && !individualVisit.getInfo().getPatient_details().getCancer_type().equals("")){
                String cancer_type = individualVisit.getInfo().getPatient_details().getCancer_type();
                cancer_str = cancer_type;
                cancer_txt.setVisibility(View.VISIBLE);
                cancer_txt.setText(cancer_type);
                cancer_edt.setVisibility(View.VISIBLE);
                cancer_drop.setVisibility(View.GONE);
            }
        }
        else {
            patient_cancer_lay.setVisibility(View.GONE);
            cancer_str = "";
            no.setChecked(true);
            hist_str = "no";
        }

        if(individualVisit.getInfo().getPatient_details().getCancer_f_hist().equals("yes")) {
            yes_f.setChecked(true);
            f_hist_str = "yes";
            if(!individualVisit.getInfo().getPatient_details().getCancer_f_type().equals("null")
                    && !individualVisit.getInfo().getPatient_details().getCancer_f_type().equals("")){
                String cancer_f_type = individualVisit.getInfo().getPatient_details().getCancer_f_type();
                cancer_f_str = cancer_f_type;
                cancer_f_txt.setVisibility(View.VISIBLE);
                cancer_f_txt.setText(cancer_f_type);
                cancer_f_edt.setVisibility(View.VISIBLE);
                cancer_f_drop.setVisibility(View.GONE);
            }

            if(!individualVisit.getInfo().getPatient_details().getRelationship().equals("null")
                    && !individualVisit.getInfo().getPatient_details().getRelationship().equals("")){
                String rel = individualVisit.getInfo().getPatient_details().getRelationship();
                relationship_str = rel;
                rel_txt.setVisibility(View.VISIBLE);
                rel_txt.setText(rel);
                rel_edt.setVisibility(View.VISIBLE);
                relationship.setVisibility(View.GONE);
            }
        }
        else {
            patient_f_cancer_type_lay.setVisibility(View.GONE);
            patient_f_cancer_rel_lay.setVisibility(View.GONE);
            cancer_f_str = "";
            relationship_str = "";
            no_f.setChecked(true);
            f_hist_str = "no";
        }


        if(!individualVisit.getInfo().getPatient_details().getGender().equals("null")
        && !individualVisit.getInfo().getPatient_details().getRelationship().equals("")){
            String gender = individualVisit.getInfo().getPatient_details().getGender();
            gender_str = gender;
            gender_txt.setVisibility(View.VISIBLE);
            gender_txt.setText(gender);
            gender_edt.setVisibility(View.VISIBLE);
            gender_drop.setVisibility(View.GONE);
        }
        if(!individualVisit.getInfo().getPatient_details().getAdherence().equals("null")
        && !individualVisit.getInfo().getPatient_details().getAdherence().equals("")){
            String adh = individualVisit.getInfo().getPatient_details().getAdherence();
            adherence_str = adh;
            adh_txt.setVisibility(View.VISIBLE);
            adh_txt.setText(adh);
            adh_edt.setVisibility(View.VISIBLE);
            adherence_drop.setVisibility(View.GONE);
        }

        if(!individualVisit.getInfo().getPatient_details().getMenopausal_status().equals("null")
        && !individualVisit.getInfo().getPatient_details().getMenopausal_status().equals("")){
            String meno = individualVisit.getInfo().getPatient_details().getMenopausal_status();
            menopausal_str = meno;
            meno_txt.setVisibility(View.VISIBLE);
            meno_txt.setText(meno);
            meno_edt.setVisibility(View.VISIBLE);
            menopausal_drop.setVisibility(View.GONE);
        }

        if(!individualVisit.getInfo().getPatient_details().getMedications().equals("null")
        && !individualVisit.getInfo().getPatient_details().getMedications().equals("")){
            ArrayList<String> medi_list = individualVisit.getInfo().getPatient_details().getMedications();
            medication_list = medi_list;
            medi_txt.setVisibility(View.VISIBLE);
            medi_txt.setText(medi_list.toString());
            medi_edt.setVisibility(View.VISIBLE);
            medication_drop.setVisibility(View.GONE);
        }
    }
    public void InitVariables() {
        if(individualVisit != null){
            if(individualVisit.getInfo() != null){
                getVisitInfo();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_inf);
        patientInfActivity = this;

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        getView(width);
        a1_str = a2_str = a3_str = a4_str = c1_str = c2_str = c3_str = c4_str = "null";
        hist_str = f_hist_str = "no" ;
        cancer_str = cancer_f_str = relationship_str = menopausal_str = gender_str = race_str = adherence_str = medication_str = "";
        note_visit = note_cancer = note_cancer_f = note_meno = note_gender = note_race = note_como = note_adh = note_medi = "" ;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Gson gson = new Gson();

            if (extras.containsKey("image_type")) {
                v_type = getIntent().getStringExtra("image_type");
            }
            if (extras.containsKey("qrcode")) {
                qr_code = getIntent().getStringExtra("qrcode");
            }

            if (extras.containsKey("patient_str")) {
                Log.d("locally saved:", "here0");

                patient_str = getIntent().getStringExtra("patient_str");
                // already connected to internet and got the patient
                if(!patient_str.equals("null") && !patient_str.equals("")){

                    patient = gson.fromJson(patient_str, Patient.class);
                    if(patient.getIndividual().getVisitResponse() != null)
                        individualVisit = patient.getIndividual().getVisitResponse();
                // still offline but already visited
                }else{
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
        }
        medication_list = new ArrayList<>();
        InitVariables();

        MyRecyclerViewAdapter.RecyclerViewDataPass recyclerViewDataPass = new MyRecyclerViewAdapter.RecyclerViewDataPass() {
            @Override
            public void pass(ArrayList<String> dataList) {
                medication_list = dataList;
                Log.d("DataList",medication_list.toString());

            }
        };
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, medication_list,recyclerViewDataPass);
        pres_scroll.setAdapter(myRecyclerViewAdapter);



        vis_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "visit", note_visit);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        can_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "cancer", note_cancer);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        can_f_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "cancer_f", note_cancer_f);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        meno_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "meno", note_meno);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        gen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "gender", note_gender);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        ra_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "race", note_race);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        como_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "como", note_como);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        adh_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "adh", note_adh);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });
        medi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialogFragment = new DialogFragment(v.getContext(), "medi", note_medi);
                dialogFragment.show(getFragmentManager(), "MyFragment");
            }
        });

        //EDit Listeners
        cancer_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancer_drop.setVisibility(View.VISIBLE);
                cancer_edt.setVisibility(View.GONE);
                cancer_txt.setVisibility(View.GONE);
            }
        });

        cancer_f_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancer_f_drop.setVisibility(View.VISIBLE);
                cancer_f_edt.setVisibility(View.GONE);
                cancer_f_txt.setVisibility(View.GONE);
            }
        });

        rel_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relationship.setVisibility(View.VISIBLE);
                rel_edt.setVisibility(View.GONE);
                rel_txt.setVisibility(View.GONE);
            }
        });
        meno_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menopausal_drop.setVisibility(View.VISIBLE);
                meno_edt.setVisibility(View.GONE);
                meno_txt.setVisibility(View.GONE);
            }
        });
        gender_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender_drop.setVisibility(View.VISIBLE);
                gender_edt.setVisibility(View.GONE);
                gender_txt.setVisibility(View.GONE);
            }
        });
        adh_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adherence_drop.setVisibility(View.VISIBLE);
                adh_edt.setVisibility(View.GONE);
                adh_txt.setVisibility(View.GONE);
            }
        });
        medi_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                medication_drop.setVisibility(View.VISIBLE);
                medi_edt.setVisibility(View.GONE);
                medi_txt.setVisibility(View.GONE);
            }
        });
        // END edit Listeners


        cancer_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                cancer_str = dropDownItem.getText();
            }
        });
        cancer_f_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                cancer_f_str = dropDownItem.getText();
            }
        });
        relationship.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                relationship_str = dropDownItem.getText();
            }
        });
        menopausal_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                menopausal_str = dropDownItem.getText();
            }
        });
        gender_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                gender_str = dropDownItem.getText();
            }
        });
        race_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                race_str = dropDownItem.getText();
            }
        });
        adherence_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                adherence_str = dropDownItem.getText();
            }
        });
        medication_drop.setItemClickListener(new DropDownView.ItemClickListener() {
            @Override
            public void onItemClick(int i, @NonNull DropDownItem dropDownItem) {
                medication_str = dropDownItem.getText();
                myRecyclerViewAdapter.addItem(medication_str,i);
            }
        });

        a1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    a1.setChecked(true);
                    a1_str = "a1";
                } else {
                    a1_str = "null";
                }
            }
        });
        a2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    a2.setChecked(true);
                    a2_str = "a2";
                } else {
                    a2_str = "null";
                }
            }
        });
        a3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    a3.setChecked(true);
                    a3_str = "a3";
                } else {
                    a3_str = "null";
                }
            }
        });
        a4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    a4.setChecked(true);
                    a4_str = "a4";
                } else {
                    a4_str = "null";
                }
            }
        });

        c1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    c1.setChecked(true);
                    c1_str = "c1";
                } else {
                    c1_str = "null";
                }
            }
        });
        c2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    c2.setChecked(true);
                    c2_str = "c2";
                } else {
                    c2_str = "null";
                }
            }
        });
        c3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    c3.setChecked(true);
                    c3_str = "c3";
                } else {
                    c3_str = "null";
                }
            }
        });
        c4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    c4.setChecked(true);
                    c4_str = "c4";
                } else {
                    c4_str = "null";
                }
            }
        });

        yes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    patient_cancer_lay.setVisibility(View.VISIBLE);
                    yes.setChecked(true);
                    no.setChecked(false);
                    hist_str = "yes";
                } else {
                    yes.setChecked(false);
                    no.setChecked(true);
                    hist_str = "no";
                }
            }
        });
        yes_f.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    patient_f_cancer_rel_lay.setVisibility(View.VISIBLE);
                    patient_f_cancer_type_lay.setVisibility(View.VISIBLE);

                    yes_f.setChecked(true);
                    no_f.setChecked(false);
                    f_hist_str = "yes";
                } else {
                    yes_f.setChecked(false);
                    no_f.setChecked(true);
                    f_hist_str = "no";
                }
            }
        });

        no.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    patient_cancer_lay.setVisibility(View.GONE);
                    cancer_str = "";

                    no.setChecked(true);
                    yes.setChecked(false);
                    hist_str = "no";
                } else {
                    no.setChecked(false);
                    yes.setChecked(true);
                    hist_str = "yes";
                }
            }
        });
        no_f.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    patient_f_cancer_rel_lay.setVisibility(View.GONE);
                    patient_f_cancer_type_lay.setVisibility(View.GONE);
                    cancer_f_str = "";
                    relationship_str = "";

                    no_f.setChecked(true);
                    yes_f.setChecked(false);
                    f_hist_str = "no_f";
                } else {
                    no_f.setChecked(false);
                    yes_f.setChecked(true);
                    f_hist_str = "yes_f";
                }
            }
        });

        save_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                visit_str = new ArrayList<>();
                como_str = new ArrayList<>();
                visit_str.add(a1_str);
                visit_str.add(a2_str);
                visit_str.add(a3_str);
                visit_str.add(a4_str);

                Log.d("visitt",visit_str.toString());
                como_str.add(c1_str);
                como_str.add(c2_str);
                como_str.add(c3_str);
                como_str.add(c4_str);

                PatientDetails patientDetails = new PatientDetails(visit_str, note_visit, hist_str, cancer_str, note_cancer, f_hist_str
                        , cancer_f_str, note_cancer_f, relationship_str, menopausal_str, note_meno, gender_str, note_gender
                        , age_edt.getText().toString(), "null", race_str, note_race, como_str
                        , note_como, adherence_str, note_adh, medication_list, note_medi);

                Gson gson = new Gson();
                Cursor cur = dbManager.searachQrcode(qr_code);
                if(cur != null){
                    if (cur.moveToFirst()) {
                        do {
                            String visit_str = cur.getString(6);
                            PatientVisit patientVisit = null;
                            TestResult testResult = null;
                            Log.d("visitt2",visit_str);

                            if(visit_str != null && !visit_str.equals("null") && !visit_str.equals("{}")){
                                patientVisit = gson.fromJson(visit_str, PatientVisit.class);
                                testResult = patientVisit.getInfo();
                                testResult.setPatient_details(patientDetails);
                            }
                            else{
                                testResult = new TestResult("","","","",
                                "","","","","","",""
                                ,"","","","","",patientDetails);

                            }
                            patientVisit = new PatientVisit(getCurrentDateAndTime(),testResult,"");


                            if(patient_str.equals("null") || patient_str.equals(""))
                                dbManager.update("null",qr_code,"null"
                                        ,getCurrentDateAndTime(),"no",gson.toJson(patientVisit), "null", "null");
                            else{
                                Patient patient = gson.fromJson(patient_str, Patient.class);
                                dbManager.update("null",qr_code,patient.getIndividual().getId()
                                        ,getCurrentDateAndTime(),"no",gson.toJson(patientVisit), "null", "null");
                            }


                        } while (cur.moveToNext());
                    }
                    cur.close();
                }
                else{
                    TestResult testResult = new TestResult("null", "null", "null", "null", "null",
                            "null", "null", "null", "null", "null", "null", "null",
                            "null", "null", "null","null",patientDetails);
                    String test_result_str = gson.toJson(testResult);
                    Log.d("Test Results", test_result_str);
                    PatientVisit patientVisit = new PatientVisit(getCurrentDateAndTime(),testResult,"" );
                    dbManager.insert("null",qr_code,"null"
                            ,getCurrentDateAndTime(),"no",gson.toJson(patientVisit), "null", "null");

                } finish();
            }
        });
        visit_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                visit_str = new ArrayList<>();
                como_str = new ArrayList<>();
                visit_str.add(a1_str);
                visit_str.add(a2_str);
                visit_str.add(a3_str);
                visit_str.add(a4_str);

                Log.d("visitt",visit_str.toString());
                como_str.add(c1_str);
                como_str.add(c2_str);
                como_str.add(c3_str);
                como_str.add(c4_str);

                PatientDetails patientDetails = new PatientDetails(visit_str, note_visit, hist_str, cancer_str, note_cancer, f_hist_str
                        , cancer_f_str, note_cancer_f, relationship_str, menopausal_str, note_meno, gender_str, note_gender
                        , age_edt.getText().toString(), "null", race_str, note_race, como_str
                        , note_como, adherence_str, note_adh, medication_list, note_medi);

                Gson gson = new Gson();
                String patient_details = gson.toJson(patientDetails);

                Log.d("patientDetails", patient_details);
                Intent intent = new Intent(PatientInfActivity.this, FlirCameraActivity.class);
                intent.putExtra("patient_details", patient_details);
                intent.putExtra("image_type",v_type);

                if (qr_code != null || !qr_code.equals(""))
                    intent.putExtra("qrcode", qr_code);
                if (patient_str != null || !patient_str.equals("")) {
                    intent.putExtra("patient_str", patient_str);
                }
                startActivity(intent);
            }
        });

    }

    private void getView(int width) {

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);

        pres_scroll = findViewById(R.id.pres_scroll);
        pres_scroll.setLayoutManager(layoutManager);

        //Edit Text
        age_edt = findViewById(R.id.age_edt);

        // Float Button
        visit_info_btn = findViewById(R.id.visit_info_btn);
        save_info_btn = findViewById(R.id.save_info_btn);

        // Button
        vis_btn = findViewById(R.id.vis_btn);
        can_btn = findViewById(R.id.can_btn);
        can_f_btn = findViewById(R.id.can_f_btn);
        meno_btn = findViewById(R.id.meno_btn);
        gen_btn = findViewById(R.id.gen_btn);
        ra_btn = findViewById(R.id.ra_btn);
        como_btn = findViewById(R.id.como_btn);
        adh_btn = findViewById(R.id.adh_btn);
        medi_btn = findViewById(R.id.med_btn);


        //Linearlayouts
        visit = findViewById(R.id.visit);
        visit_btn = findViewById(R.id.visit_btn);
        cancer = findViewById(R.id.cancer);
        cancer_btn = findViewById(R.id.cancer_btn);
        cancer_f = findViewById(R.id.cancer_f);
        cancer_f_btn = findViewById(R.id.cancer_f_btn);
        menopausal = findViewById(R.id.menopausal);
        menopausal_btn = findViewById(R.id.menopausal_btn);
        gender = findViewById(R.id.gender);
        gender_btn = findViewById(R.id.gender_btn);
        race = findViewById(R.id.race);
        race_btn = findViewById(R.id.race_btn);
        comorbidities = findViewById(R.id.comorbidities);
        comorbidities_btn = findViewById(R.id.comorbidities_btn);
        adherence = findViewById(R.id.adherence);
        adherence_btn = findViewById(R.id.adherence_btn);
        medications = findViewById(R.id.medications);
        medications_btn = findViewById(R.id.medications_btn);
        age_lay = findViewById(R.id.age_layout);

        // Cancer Layouts
        patient_cancer_lay = findViewById(R.id.patient_cancer_layout);
        patient_f_cancer_type_lay = findViewById(R.id.patient_f_type_lay);
        patient_f_cancer_rel_lay = findViewById(R.id.patient_f_rel_lay);

        //CheckBox
        a1 = findViewById(R.id.a1);
        a2 = findViewById(R.id.a2);
        a3 = findViewById(R.id.a3);
        a4 = findViewById(R.id.a4);
        c1 = findViewById(R.id.c1);
        c2 = findViewById(R.id.c2);
        c3 = findViewById(R.id.c3);
        c4 = findViewById(R.id.c4);
        yes = findViewById(R.id.yes);
        no = findViewById(R.id.no);
        yes_f = findViewById(R.id.yes_f);
        no_f = findViewById(R.id.no_f);

        //Textview
        cancer_txt = findViewById(R.id.cancer_type_txt);
        cancer_f_txt = findViewById(R.id.cancer_f_type_txt);
        rel_txt = findViewById(R.id.rel_txt);
        meno_txt = findViewById(R.id.meno_txt);
        gender_txt = findViewById(R.id.gender_txt);
        adh_txt = findViewById(R.id.adh_txt);
        medi_txt = findViewById(R.id.medi_txt);

        //ImageView
        cancer_edt = findViewById(R.id.cancer_edt);
        cancer_f_edt = findViewById(R.id.cancer_f_edt);
        rel_edt = findViewById(R.id.rel_edt);
        meno_edt = findViewById(R.id.meno_edt);
        gender_edt = findViewById(R.id.gender_edt);
        adh_edt = findViewById(R.id.adh_edt);
        medi_edt = findViewById(R.id.medi_edt);

        //Dropdown
        cancer_drop = findViewById(R.id.cancer_type);
        cancer_f_drop = findViewById(R.id.cancer_f_type);
        relationship = findViewById(R.id.relationship_f);
        menopausal_drop = findViewById(R.id.menopausal_dropdown);
        race_drop = findViewById(R.id.race_dropdown);
        gender_drop = findViewById(R.id.gender_dropdown);
        race_drop = findViewById(R.id.race_dropdown);
        adherence_drop = findViewById(R.id.adherence_dropdown);
        medication_drop = findViewById(R.id.medication_dropdown);

        // Display Metric

        age_lay.getLayoutParams().width = (int) (width * 0.5);

        visit.getLayoutParams().width = (int) (width * 0.8);
        //visit_btn.getLayoutParams().width = (int) (width * 0.2);

        cancer.getLayoutParams().width = (int) (width * 0.8);
        //cancer_btn.getLayoutParams().width = (int) (width * 0.2);

        cancer_f.getLayoutParams().width = (int) (width * 0.8);
        //cancer_f_btn.getLayoutParams().width = (int) (width * 0.2);

        menopausal.getLayoutParams().width = (int) (width * 0.8);
        //menopausal_btn.getLayoutParams().width = (int) (width * 0.2);

        gender.getLayoutParams().width = (int) (width * 0.8);
        //gender_btn.getLayoutParams().width = (int) (width * 0.2);

        race.getLayoutParams().width = (int) (width * 0.8);
        //race_btn.getLayoutParams().width = (int) (width * 0.2);

        comorbidities.getLayoutParams().width = (int) (width * 0.8);
        //comorbidities_btn.getLayoutParams().width = (int) (width * 0.2);

        adherence.getLayoutParams().width = (int) (width * 0.8);
        //adherence_btn.getLayoutParams().width = (int) (width * 0.2);

        medications.getLayoutParams().width = (int) (width * 0.8);
        //medications_btn.getLayoutParams().width = (int) (width * 0.2);
    }

}