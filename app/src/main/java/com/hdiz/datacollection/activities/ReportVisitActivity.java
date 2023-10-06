package com.hdiz.datacollection.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.handler.PopupWindowHelper;
import com.hdiz.datacollection.objects.IndividualVisit;
import com.hdiz.datacollection.objects.TestResult;

import java.io.File;
import java.io.IOException;

public class ReportVisitActivity extends AppCompatActivity  implements View.OnClickListener {

    private String qr_code, patient_visit_str, uri_thermal, uri_visible;
    private IndividualVisit individualVisit = null;
    private TestResult testResult = null;
    private TextView age_txt, gender_txt, reason_note_txt, can_type_txt, can_f_type_txt,
            can_note_txt, can_f_note_txt, como_note_txt, adh_note_txt, medi_note_txt,
            meno_note_txt, mamo_note_txt, sono_note_txt, patho_note_txt, gp_note_txt,
            self_note_txt, grade_txt, stage_txt, tumor_side_txt, tumor_size_txt, tumor_note_txt;
    private ImageView thermal_img, visible_img;
    private Button visit_btn, como_btn, adh_btn, meno_btn, self_btn, gp_btn,medi_btn,
            mamo_btn, sono_btn, patho_btn;

    private FloatingActionButton ok_report;
    private Bitmap getRoundedCornerBitmap(Bitmap bitmap,int roundPixelSize) {
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
    private void bindImages(){
        try {
            Bitmap bmp_thermal = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(uri_thermal)));
            Bitmap bmp_visible = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(uri_visible)));

            if(bmp_thermal != null){
                Bitmap bmp_round_th = getRoundedCornerBitmap(bmp_thermal,25);
                thermal_img.setImageBitmap(bmp_round_th);
            }
            if(bmp_visible!= null){
                Bitmap bmp_round_th = getRoundedCornerBitmap(bmp_visible,25);
                visible_img.setImageBitmap(bmp_round_th);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void bindTexts(){
        if(individualVisit.getInfo()!= null){
            testResult = individualVisit.getInfo();

            if(!TextUtils.isEmpty(testResult.getPatient_details().getAge()))
                age_txt.setText(testResult.getPatient_details().getAge());
            else
                age_txt.setText("Empty");

            if (!TextUtils.isEmpty(testResult.getPatient_details().getGender()))
                gender_txt.setText(testResult.getPatient_details().getGender());
            else
                gender_txt.setText("Empty");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getVisit_note()))
                reason_note_txt.setText(testResult.getPatient_details().getVisit_note());
            else
                reason_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getCancer_type()))
                can_type_txt.setText(testResult.getPatient_details().getCancer_type());
            else
                can_type_txt.setText("Empty");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getCancer_f_type()))
                can_f_type_txt.setText(testResult.getPatient_details().getCancer_f_type());
            else
                can_f_type_txt.setText("Empty");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getCancer_note()))
                can_note_txt.setText(testResult.getPatient_details().getCancer_note());
            else
                can_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getCancer_f_note()))
                can_f_note_txt.setText(testResult.getPatient_details().getCancer_f_note());
            else
                can_f_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getComorbidities_note()))
                como_note_txt.setText(testResult.getPatient_details().getComorbidities_note());
            else
                como_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getAdh_note()))
                 adh_note_txt.setText(testResult.getPatient_details().getAdh_note());
            else
                adh_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getMedications_note()))
                medi_note_txt.setText(testResult.getPatient_details().getMedications_note());
            else
                medi_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getPatient_details().getMeno_note()))
                meno_note_txt.setText(testResult.getPatient_details().getMeno_note());
            else
                meno_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getMamo_note()))
                mamo_note_txt.setText(testResult.getMamo_note());
            else
                mamo_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getSono_note()))
                sono_note_txt.setText(testResult.getSono_note());
            else
                sono_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getPatho_note()))
                patho_note_txt.setText(testResult.getPatho_note());
            else
                patho_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getGp_note()))
                gp_note_txt.setText(testResult.getGp_note());
            else
                gp_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getSelf_exam_note()))
                self_note_txt.setText(testResult.getSelf_exam_note());
            else
                self_note_txt.setText("note ..");

            if(!TextUtils.isEmpty(testResult.getGrade()))
                grade_txt.setText(testResult.getGrade());
            else
                grade_txt.setText("Empty");

            if(!TextUtils.isEmpty(testResult.getStage()))
                stage_txt.setText(testResult.getStage());
            else
                stage_txt.setText("Empty");

            if(!TextUtils.isEmpty(testResult.getTumor_side()))
                tumor_side_txt.setText(testResult.getTumor_side());
            else
                tumor_side_txt.setText("Empty");

            if(!TextUtils.isEmpty(testResult.getTumor_size()))
                tumor_size_txt.setText(testResult.getTumor_size());
            else
                tumor_size_txt.setText("Empty");

            if(!TextUtils.isEmpty(testResult.getTumor_side_note()) ||
                    !TextUtils.isEmpty(testResult.getTumor_size_note()))
                tumor_note_txt.setText("Tumor Side: "+ testResult.getTumor_side_note()
                        + "Tumor Size:" + testResult.getTumor_size_note());
            else
                tumor_note_txt.setText("note ..");
        }

    }
    private void initView(){

        // Text Views
        age_txt = findViewById(R.id.age_report);
        gender_txt = findViewById(R.id.gender_report);
        reason_note_txt = findViewById(R.id.reason_note_report);

        can_type_txt = findViewById(R.id.cancer_type_report);
        can_f_type_txt = findViewById(R.id.cancer_f_type_report);
        can_note_txt = findViewById(R.id.patient_note_report);
        can_f_note_txt = findViewById(R.id.patient_f_note_report);

        como_note_txt = findViewById(R.id.como_note_report);
        adh_note_txt = findViewById(R.id.adh_note_report);
        medi_note_txt = findViewById(R.id.medi_note_report);
        meno_note_txt = findViewById(R.id.meno_note_report);
        mamo_note_txt = findViewById(R.id.mamo_note_report);
        sono_note_txt = findViewById(R.id.sono_note_report);
        patho_note_txt = findViewById(R.id.patho_note_report);

        gp_note_txt = findViewById(R.id.gp_note_report);
        self_note_txt = findViewById(R.id.self_note_report);
        grade_txt = findViewById(R.id.grade_report);
        stage_txt = findViewById(R.id.stage_report);
        tumor_note_txt = findViewById(R.id.tumor_note);
        tumor_side_txt = findViewById(R.id.tumor_side_report);
        tumor_size_txt = findViewById(R.id.tumor_size_report);

        // Image Views
        thermal_img = findViewById(R.id.thermal_report);
        visible_img = findViewById(R.id.visible_report);
        ok_report = findViewById(R.id.ok_report);

        // Buttons
        visit_btn = findViewById(R.id.visit_report_btn);
        como_btn = findViewById(R.id.como_report_btn);
        adh_btn = findViewById(R.id.adh_report_btn);
        medi_btn = findViewById(R.id.medi_report_btn);
        meno_btn = findViewById(R.id.meno_report_btn);
        self_btn = findViewById(R.id.self_report_btn);
        gp_btn = findViewById(R.id.gp_report_btn);
        mamo_btn = findViewById(R.id.mamo_report_btn);
        sono_btn = findViewById(R.id.sono_report_btn);
        patho_btn = findViewById(R.id.patho_report_btn);

    }
    private void setButtonListeners(){
        visit_btn.setOnClickListener(this);
        como_btn.setOnClickListener(this);
        adh_btn.setOnClickListener(this);
        medi_btn.setOnClickListener(this);
        meno_btn.setOnClickListener(this);
        self_btn.setOnClickListener(this);
        gp_btn.setOnClickListener(this);
        mamo_btn.setOnClickListener(this);
        sono_btn.setOnClickListener(this);
        patho_btn.setOnClickListener(this);
        ok_report.setOnClickListener(this);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_visit);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Gson gson = new Gson();

            if (extras.containsKey("qrcode")) {
                qr_code = getIntent().getStringExtra("qrcode");
            }
            if (extras.containsKey("patient_visit_str")) {
                patient_visit_str = getIntent().getStringExtra("patient_visit_str");
                individualVisit = gson.fromJson(patient_visit_str, IndividualVisit.class);
            }
            if (extras.containsKey("uri_thermal")) {
                uri_thermal = getIntent().getStringExtra("uri_thermal");
            }
            if (extras.containsKey("uri_visible")) {
                uri_visible = getIntent().getStringExtra("uri_visible");
            }
        }
        initView();
        bindTexts();
        bindImages();
        setButtonListeners();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.visit_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, visit_btn,
                        testResult.getPatient_details().getVisit().toString());
                break;
            case R.id.como_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, como_btn,
                        testResult.getPatient_details().getComorbidities().toString());
                break;
            case R.id.adh_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, adh_btn,
                        testResult.getPatient_details().getAdherence());
                break;

            case R.id.medi_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, medi_btn,
                        testResult.getPatient_details().getMedications().toString());
                break;
            case R.id.meno_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, meno_btn,
                        testResult.getPatient_details().getMenopausal_status());
                break;
            case R.id.self_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, self_btn,
                        testResult.getSelf_exam());
                break;
            case R.id.gp_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, gp_btn,
                        testResult.getGp());
                break;
            case R.id.mamo_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, mamo_btn,
                        testResult.getMamography());
                break;
            case R.id.sono_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, sono_btn,
                        testResult.getSonography());
                break;
            case R.id.patho_report_btn:
                PopupWindowHelper.showPopupWindow(ReportVisitActivity.this, patho_btn,
                        testResult.getPathology());
                break;
            case R.id.ok_report:
                finish();
                break;
            default:
                break;
        }
    }
}