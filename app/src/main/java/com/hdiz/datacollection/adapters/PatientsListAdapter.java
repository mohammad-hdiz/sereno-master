package com.hdiz.datacollection.adapters;

import static com.hdiz.datacollection.activities.QActivity.dbManager;
import static com.hdiz.datacollection.activities.QActivity.getCurrentDateAndTime;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.activities.FlirCameraActivity;
import com.hdiz.datacollection.activities.PatientInfActivity;
import com.hdiz.datacollection.activities.QActivity;
import com.hdiz.datacollection.activities.ReportVisitActivity;
import com.hdiz.datacollection.activities.ViewImgActivity;
import com.hdiz.datacollection.objects.Individual;
import com.hdiz.datacollection.objects.IndividualVisit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PatientsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER = 0;
    private static final int ITEM = 1;
    private Map<Date, Integer> viewTypeMap = new HashMap<>();
    private List<Object> items_all;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", new Locale("en"));
    private Date ConvertDateTimeFormat(String date, String inputFormat, String outputFormat) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
        Date testDate = null;
        try {
            testDate = sdf.parse(date);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        SimpleDateFormat formatter = new SimpleDateFormat(outputFormat);
        String newFormat = formatter.format(testDate);
        System.out.println(".....Date..."+newFormat);
        Date date_formatted = formatter.parse(newFormat);

        return date_formatted;
    }
    public PatientsListAdapter(List<Individual> items) throws ParseException {
        // Initialize the items list with headers and items
        this.items_all = new ArrayList<>();

        Date currentDate = null;

        for (Individual item : items) {
            //Date date = simpleDateFormat.parse(item.getCaptured_at());
            Date date = ConvertDateTimeFormat(item.getCaptured_at(), "yyyy-MM-dd'T'HH:mm:ss", "MMMM yyyy");

            if (currentDate == null || !currentDate.equals(date)) {
                currentDate = date;
                items_all.add(currentDate);
            }
            items_all.add(item);
        }

        // Generate a view type for each unique date in the data
        int viewType = 1;
        for (Object item : items_all) {
            if (item instanceof Date) {
                viewTypeMap.put((Date) item, viewType);
                viewType++;
            }
        }

        Log.d("viewType:", viewTypeMap+"");
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items_all.get(position);
        if (item instanceof Date) {
            return HEADER;
        } else {
            String date_str = ((Individual)item).getCaptured_at();
            Date date = null;
            try {
                date = ConvertDateTimeFormat(date_str, "yyyy-MM-dd'T'HH:mm:ss", "MMMM yyyy");
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return viewTypeMap.get(date);

        }
    }

    @Override
    public int getItemCount() {
        return items_all.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the appropriate layout for each view type
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == HEADER) {
            View view = inflater.inflate(R.layout.patient_header_list, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.patients_item_list, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object item = items_all.get(position);
        if (item instanceof Date) {
            ((HeaderViewHolder) holder).bind((Date) item);
        } else {
            ((ItemViewHolder) holder).bind((Individual) item);
        }
    }

    // Define view holder classes for headers and items
    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView headerText;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.header_text);
        }

        public void bind(Date date) {
            headerText.setText(formatDate(date));
        }

        private String formatDate(Date date) {
            // Format the date as a string here
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM yyyy", new Locale("en"));
            String formattedDate = simpleDateFormat.format(date);

            return formattedDate;
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Define views for this view holder
        private TextView patient_qr, patient_date, patient_age;
        private ImageView edit_img, summary_img, visit_img;
        private String qrCode = "" ;
        private String patient_str = "";
        public ItemViewHolder(View itemView) {
            super(itemView);
            // Initialize views here
            patient_qr = itemView.findViewById(R.id.patient_qr);
            patient_date = itemView.findViewById(R.id.patient_date);
            patient_age = itemView.findViewById(R.id.patient_age);

            edit_img = itemView.findViewById(R.id.edit_patient_list);
            edit_img.setOnClickListener(this);
            summary_img = itemView.findViewById(R.id.summary_patient_list);
            summary_img.setOnClickListener(this);
            visit_img = itemView.findViewById(R.id.visit_patient_list);
            visit_img.setOnClickListener(this);

        }

        public void bind(Individual item) {
            // Bind data to views here
            qrCode = item.getIdentifier();
            patient_str = dbManager.searchPatient(qrCode);

            patient_qr.setText(item.getIdentifier());
            if(item.getVisitResponse().getInfo()!= null)
                patient_age.setText(item.getVisitResponse().getInfo().getPatient_details().getAge());
            else
                patient_age.setText("");

            patient_date.setText(item.getCaptured_at());
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.edit_patient_list:

                    Intent i = new Intent(view.getContext(), PatientInfActivity.class);
                    i.putExtra("qrcode", qrCode);
                    i.putExtra("patient_str", patient_str);
                    view.getContext().startActivity(i);
                    break;
                case R.id.summary_patient_list:
                    Cursor cur = dbManager.searachQrcode(qrCode);

                    if(cur != null){
                        if (cur.moveToFirst()) {
                            do {
                                String patient_visit_str = cur.getString(6);
                                String thermal_url = cur.getString(7);
                                String visible_url = cur.getString(8);

                                Intent intent = new Intent(view.getContext(), ReportVisitActivity.class);
                                intent.putExtra("qrcode", qrCode);
                                if(patient_visit_str != null && !patient_visit_str.equals(""))
                                    intent.putExtra("patient_visit_str",patient_visit_str);
                                if (thermal_url != null && !thermal_url.equals(""))
                                    intent.putExtra("uri_thermal", thermal_url);
                                if (visible_url != null && !visible_url.equals(""))
                                    intent.putExtra("uri_visible", visible_url);

                                view.getContext().startActivity(intent);

                            } while (cur.moveToNext());
                        }
                        cur.close();
                    }
                    else
                        Toast.makeText(view.getContext(), "This patient has no visit history yet!",Toast.LENGTH_SHORT).show();

                    break;
                case R.id.visit_patient_list:
                    if (patient_str != null) {
                        Intent intent = new Intent(view.getContext(), FlirCameraActivity.class);
                        intent.putExtra("qrcode", qrCode);
                        intent.putExtra("patient_str", patient_str);
                        view.getContext().startActivity(intent);
                        //finish();
                    } else {
                        dbManager.insert("null", qrCode, "null", getCurrentDateAndTime(), "no", "null", "null", "null");
                        Intent intent = new Intent(view.getContext(), FlirCameraActivity.class);
                        intent.putExtra("qrcode", qrCode);
                        intent.putExtra("patient_str", "null");
                        view.getContext().startActivity(intent);
                    }
                        break;
                default:
                    break;
            }
        }
    }
    public void updateList(List<Individual> individuals) throws ParseException {
        items_all.clear();

        Date currentDate = null;

        for (Individual item : individuals) {
            Date date = ConvertDateTimeFormat(item.getCaptured_at(), "yyyy-MM-dd'T'HH:mm:ss", "MMMM yyyy");

            if (currentDate == null || !currentDate.equals(date)) {
                currentDate = date;
                items_all.add(currentDate);
            }
            items_all.add(item);
        }
    }

}
