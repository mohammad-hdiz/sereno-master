package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImgUploadResponse {

    @SerializedName("thermal_original_file")
    @Expose
    private ImageProperty thermal_original_file;

    public ImgUploadResponse(ImageProperty thermal_original_file){
        this.thermal_original_file = thermal_original_file;
    }
    public ImageProperty getThermal_original_file() {
        return thermal_original_file;
    }
}
