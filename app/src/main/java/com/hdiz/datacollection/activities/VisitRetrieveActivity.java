package com.hdiz.datacollection.activities;

import static com.hdiz.datacollection.activities.QActivity.dbManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.adapters.VisitRetrieveAdapter;
import com.hdiz.datacollection.objects.IndividualVisit;

import java.util.ArrayList;

public class VisitRetrieveActivity extends AppCompatActivity {

    private VisitRetrieveAdapter visitListAdapter;
    private RecyclerView visitList;
    private FloatingActionButton finish_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_retrieve);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);

        finish_btn = findViewById(R.id.finish_btn);
        visitList = findViewById(R.id.visit_list);
        visitList.setLayoutManager(layoutManager);

        Cursor cursor = dbManager.queueAll();

        visitListAdapter = new VisitRetrieveAdapter(this, cursor, getFragmentManager());
        visitList.setAdapter(visitListAdapter);

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VisitRetrieveActivity.this, QActivity.class);
                startActivity(intent);
                finish();
            }
        });
        Log.d("CursorSize",cursor.getCount()+"");

    }

}