package com.hdiz.datacollection.handler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hdiz.datacollection.R;

public class PopupWindowHelper {

    public static void showPopupWindow(Context context, View anchorView, String data) {
        // Create a new popup window
        PopupWindow popupWindow = new PopupWindow(context);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set the layout for the popup window
        View layout = LayoutInflater.from(context).inflate(R.layout.popup_layout, null);
        popupWindow.setContentView(layout);

        // Set the width and height of the popup window
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set focusable to true to allow the user to interact with the popup window
        popupWindow.setFocusable(true);

        // Show the popup window
        popupWindow.showAsDropDown(anchorView);

        // Fetch the data to display in the popup window
        fetchData(data, new OnDataFetchedListener() {
            @Override
            public void onDataFetched(String data) {
                // Update the TextView with the fetched data
                TextView dataTextView = layout.findViewById(R.id.dataTextView);
                dataTextView.setText(data);
            }
        });
    }

    private static void fetchData(String data, OnDataFetchedListener listener) {

        // Pass the fetched data to the listener
        listener.onDataFetched(data);
    }

    public interface OnDataFetchedListener {
        void onDataFetched(String data);
    }

}
