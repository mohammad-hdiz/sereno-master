package com.hdiz.datacollection.fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.hdiz.datacollection.activities.SplashActivity.IDENTIFIER_PREF;
import static com.hdiz.datacollection.activities.SplashActivity.preferenceFileExist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hdiz.datacollection.R;
import com.hdiz.datacollection.activities.QActivity;


@SuppressLint("ValidFragment")
public class IdentifierFragment extends android.app.DialogFragment {
    private Button done;
    private EditText  identifier;
    private Context context;
    private String identifier_str = "";

    @SuppressLint("ValidFragment")
    public IdentifierFragment(Context context){
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_identifier, container);
        // set the dialog background to transparent
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // remove background dim
        getDialog().getWindow().setDimAmount(0);


        done = view.findViewById(R.id.verify_id);
        identifier = view.findViewById(R.id.identifier_app);


        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                identifier_str = identifier.getText().toString();
                if (identifier_str.equals(""))
                    Toast.makeText(view.getContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
                else{
                    SharedPreferences.Editor editor = view.getContext().getSharedPreferences(IDENTIFIER_PREF, MODE_PRIVATE).edit();
                    editor.putString("identifier", identifier_str);
                    editor.apply();

                    getDialog().dismiss();
                    Intent intent = new Intent(getActivity(), QActivity.class);
                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Call Only, if you wants to clears the activity stack else ignore it.
                    intent.putExtra("identifier",identifier_str);
                    startActivity(intent);
                    getActivity().finish();
                }
                Log.e("yes", "Clicked Yes");

            }
        });
        return view;
    }

}