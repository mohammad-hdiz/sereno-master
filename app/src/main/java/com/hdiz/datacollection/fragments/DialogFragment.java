package com.hdiz.datacollection.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hdiz.datacollection.R;


@SuppressLint("ValidFragment")
public class DialogFragment extends android.app.DialogFragment {
    private Context mContext;
    private Button done, cancel;
    private EditText  note_edt;
    private String tag, prescription;
   // public static String description;
    private Context context;





    public interface OnInputListener {
        void sendInput(String input);
    }
    public OnInputListener mOnInputListener;

    @SuppressLint("ValidFragment")
    public DialogFragment(Context context, String tag, String prescription){
        this.context = context;
        this.tag = tag;
        this.prescription = prescription;
    }




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_dialog, container);
        // set the dialog background to transparent
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // remove background dim
        getDialog().getWindow().setDimAmount(0);



        Log.d("tagggg",tag);
        done = view.findViewById(R.id.done);
        cancel = view.findViewById(R.id.cancel);
        note_edt = view.findViewById(R.id.note_edt);
        note_edt.setText(prescription);



        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("yes", "Clicked Yes");
                prescription = note_edt.getText().toString();
                mOnInputListener.sendInput(prescription + "MOH" + tag);
                getDialog().dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("no", "Clicked No");
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
                    = (OnInputListener)getActivity();
        }
        catch (ClassCastException e) {
            Log.e("TAG", "onAttach: ClassCastException: "
                    + e.getMessage());
        }
    }


}