package com.hdiz.datacollection.fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.hdiz.datacollection.activities.SplashActivity.IDENTIFIER_PREF;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hdiz.datacollection.R;
import com.hdiz.datacollection.activities.QActivity;

import java.util.HashMap;


@SuppressLint("ValidFragment")
public class SettingFragment extends android.app.DialogFragment {
    Button done;
    EditText  identifier;
    SwitchMaterial switchMaterial;

    private Context context;
    private String identifier_str = "";
    private Boolean state;
    private String v_type = "gone";

    public interface OnInputListener {
        void sendInput(String input, Boolean state);
    }
    public SettingFragment.OnInputListener mOnInputListener;


    @SuppressLint("ValidFragment")
    public SettingFragment(Context context, Boolean state){
        this.context = context;
        this.state = state;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_setting, container);
        // set the dialog background to transparent
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // remove background dim
        getDialog().getWindow().setDimAmount(0);


        done = view.findViewById(R.id.verify_setting);
        identifier = view.findViewById(R.id.identifier_setting);
        switchMaterial = view.findViewById(R.id.switch_imgs_setting);

        if(state)
            switchMaterial.setChecked(true);
        else
            switchMaterial.setChecked(false);

        switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                   v_type = "visible";
                   state = true;
                }
                else{
                   v_type = "gone";
                   state = false;
                }
            }
        });


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                identifier_str = identifier.getText().toString();
                if (!identifier_str.equals("")){
                    SharedPreferences.Editor editor = view.getContext().getSharedPreferences(IDENTIFIER_PREF, MODE_PRIVATE).edit();
                    editor.putString("identifier", identifier_str);
                    editor.apply();
                }
                mOnInputListener.sendInput(v_type, state);
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try {
            mOnInputListener
                    = (SettingFragment.OnInputListener)getActivity();
        }
        catch (ClassCastException e) {
            Log.e("TAG", "onAttach: ClassCastException: "
                    + e.getMessage());
        }
    }



}