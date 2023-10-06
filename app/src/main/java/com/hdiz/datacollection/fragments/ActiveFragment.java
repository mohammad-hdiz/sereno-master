package com.hdiz.datacollection.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.Gson;
import com.hdiz.datacollection.activities.MainActivity;
import com.hdiz.datacollection.adapters.CustomGrid;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.objects.Patients;


public class ActiveFragment extends Fragment implements MainActivity.OnAboutDataReceivedListener {

    private MainActivity mActivity;
    GridView grid;
    Patients patients_;
    CustomGrid customGrid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (MainActivity) getActivity();
        mActivity.setAboutDataListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_active,
                container, false);

        //CustomGrid adapter = new CustomGrid(getActivity().getApplicationContext(), patients_);
        //Log.d("patientSize",patients_.getIndividuals().size()+"");
        grid = (GridView) rootView.findViewById(R.id.grid_active);
        //grid.setAdapter(adapter);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onDataReceived(Patients patients) {
        Log.d("here","3");
        patients_ = patients;

        if(patients_ != null) {
            Gson gson = new Gson();
            String successResponse = gson.toJson(patients);
            Log.d("Individuals: ", "successResponse: " + successResponse);
            customGrid = new CustomGrid(getActivity().getApplicationContext(), patients_);
            grid.setAdapter(customGrid);

        }
    }
}