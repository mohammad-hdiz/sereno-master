package com.hdiz.datacollection.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hdiz.datacollection.R;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewImgActivity extends AppCompatActivity {

    private ImageView visible_img,thermal_img;
    private TextView visible_txt, thermal_txt;
    private FloatingActionButton capture_img, verify_img;
    String qr_code = "";
    String v_type = "gone";
    String patient_str = "";
    String patient_details = "";
    Bitmap bmp_thermal, bmp_visible;
    String uri_thermal = "" ;
    String uri_visible = "" ;

    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_img);

        visible_img = findViewById(R.id.img_visible_view);
        thermal_img = findViewById(R.id.img_thermal_view);
        capture_img = findViewById(R.id.img_capture_view);
        verify_img = findViewById(R.id.img_verify_view);
        visible_txt = findViewById(R.id.visible_v_txt);
        thermal_txt = findViewById(R.id.thermal_v_txt);

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            if (extras.containsKey("qrcode")) {
                qr_code = getIntent().getStringExtra("qrcode");
            }
            if (extras.containsKey("patient_details")) {
                patient_details = getIntent().getStringExtra("patient_details");
            }

            if (extras.containsKey("v_type")) {
                v_type = getIntent().getStringExtra("v_type");

                if(v_type.equals("gone")) {
                    visible_txt.setVisibility(View.GONE);
                    visible_img.setVisibility(View.GONE);
                }
                if(v_type.equals("visible")) {
//                    visible_txt.setVisibility(View.VISIBLE);
//                    thermal_txt.setVisibility(View.VISIBLE);
                    visible_img.setVisibility(View.VISIBLE);
                }
            }
            if (extras.containsKey("patient_str")) {
                patient_str = getIntent().getStringExtra("patient_str");
            }
            if (extras.containsKey("uri_thermal")) {
                uri_thermal = getIntent().getStringExtra("uri_thermal");
                try {
                    bmp_thermal = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(uri_thermal)));
                    if(bmp_thermal != null){
                        Bitmap bmp_round_th = getRoundedCornerBitmap(bmp_thermal,25);
                        thermal_img.setImageBitmap(bmp_round_th);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (extras.containsKey("uri_visible")) {
                uri_visible = getIntent().getStringExtra("uri_visible");
                try {
                    bmp_visible = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(new File(uri_visible)));
                    if(bmp_visible!= null){
                        Bitmap bmp_round_th = getRoundedCornerBitmap(bmp_visible,25);
                        visible_img.setImageBitmap(bmp_round_th);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        capture_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewImgActivity.this , FlirCameraActivity.class);
                if (qr_code != null || !qr_code.equals(""))
                    intent.putExtra("qrcode", qr_code);
                if (v_type != null || !v_type.equals(""))
                    intent.putExtra("image_type", v_type);
                if (patient_str != null || !patient_str.equals(""))
                    intent.putExtra("patient_str", patient_str);
                if (patient_details != null || !patient_details.equals(""))
                    intent.putExtra("patient_details", patient_details);

                startActivity(intent);
                finish();
            }
        });

        verify_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ViewImgActivity.this, TestResultsActivity.class);
                intent.putExtra("v_type",v_type);
                Log.d("patient_view",patient_str);

                if (qr_code != null && !qr_code.equals(""))
                    intent.putExtra("qrcode", qr_code);
                if (patient_str != null && !patient_str.equals(""))
                    intent.putExtra("patient_str", patient_str);
                if (patient_details != null && !patient_details.equals(""))
                    intent.putExtra("patient_details", patient_details);
                if (v_type != null && !v_type.equals(""))
                    intent.putExtra("v_type", v_type);
                if (uri_thermal != null && !uri_thermal.equals(""))
                    intent.putExtra("uri_thermal", uri_thermal);
                if (uri_visible != null && !uri_visible.equals(""))
                    intent.putExtra("uri_visible", uri_visible);

                startActivity(intent);
                finish();
            }
        });
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