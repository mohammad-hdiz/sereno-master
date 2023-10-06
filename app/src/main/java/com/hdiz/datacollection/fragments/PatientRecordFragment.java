package com.hdiz.datacollection.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.activities.FlirCameraActivity;
import com.hdiz.datacollection.activities.QActivity;
import com.hdiz.datacollection.activities.ReportVisitActivity;
import com.hdiz.datacollection.activities.ViewImgActivity;
import com.hdiz.datacollection.objects.IndividualVisit;

@SuppressLint("ValidFragment")
public class PatientRecordFragment extends android.app.DialogFragment  {
    private TextView id, date, age;
    private Button visit, report;
    private ImageView cancel;
    private Context context;
    private String qrcode, patient_str, patient_visit_str, thermal_url, visible_url;

    public PatientRecordFragment(Context context, String qrcode, String patient_str,
                                 String patient_visit_str, String thermal_url, String visible_url)  {
        this.context = context;
        this.qrcode = qrcode;
        this.patient_str = patient_str;
        this.patient_visit_str = patient_visit_str;
        this.thermal_url = thermal_url;
        this.visible_url = visible_url;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void getVIew(View view){
        id = view.findViewById(R.id.id_record);
        date = view.findViewById(R.id.date_record);
        age = view.findViewById(R.id.age_record);
        visit = view.findViewById(R.id.visit_btn_record);
        report = view.findViewById(R.id.report_btn_record);
        cancel = view.findViewById(R.id.exit_record);
    }
    private void bindData(){
        if(qrcode != null && !qrcode.equals(""))
            id.setText(qrcode);
        else
            id.setText("Not Valued");

        if(patient_visit_str != null && !patient_visit_str.equals("")){
            Gson gson = new Gson();
            IndividualVisit individualVisit = gson.fromJson(patient_visit_str, IndividualVisit.class);
            date.setText(individualVisit.getLocal_visited_at());
            if(individualVisit != null){
                if(individualVisit.getInfo() != null){
                    age.setText(individualVisit.getInfo().getPatient_details().getAge());
                }
            }
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_record, container);
        // set the dialog background to transparent
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // remove background dim
        getDialog().getWindow().setDimAmount(0);
        getVIew(view);
        bindData();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        visit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FlirCameraActivity.class);
                if (qrcode != null && !qrcode.equals(""))
                    intent.putExtra("qrcode", qrcode);
                if (patient_str != null && !patient_str.equals(""))
                    intent.putExtra("patient_str", patient_str);

                startActivity(intent);
                getActivity().finish();
            }
        });

        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ReportVisitActivity.class);
                intent.putExtra("qrcode", qrcode);
                if(patient_visit_str != null && !patient_visit_str.equals(""))
                    intent.putExtra("patient_visit_str",patient_visit_str);
                if (thermal_url != null && !thermal_url.equals(""))
                    intent.putExtra("uri_thermal", thermal_url);
                if (visible_url != null && !visible_url.equals(""))
                    intent.putExtra("uri_visible", visible_url);

                startActivity(intent);
            }
        });

        return view;
    }
}