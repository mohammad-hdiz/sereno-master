package com.hdiz.datacollection.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hdiz.datacollection.R;
import com.hdiz.datacollection.objects.Patients;

public class CustomGrid extends BaseAdapter{
    private Context mContext;
    private Patients patients_;

    public CustomGrid(Context c, Patients patients) {
        mContext = c;
        this.patients_ = patients;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return patients_.getIndividuals().size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = new View(mContext);
            grid = inflater.inflate(R.layout.grid_single_active, null);
            TextView name = (TextView) grid.findViewById(R.id.patient_name);
            TextView birth_date = (TextView) grid.findViewById(R.id.patient_birthdate);
            TextView gender = (TextView) grid.findViewById(R.id.patient_gender);

            name.setText(patients_.getIndividuals().get(position).getFirstName());
            birth_date.setText(patients_.getIndividuals().get(position).getBirth_date());
            gender.setText(patients_.getIndividuals().get(position).getGender());

        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}