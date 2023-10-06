package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImageProperty {
    @SerializedName("captured_at")
    @Expose
    private String captured_at;

    @SerializedName("created_at")
    @Expose
    private String created_at;

    @SerializedName("device_deploy_id")
    @Expose
    private String device_deploy_id;

    @SerializedName("height")
    @Expose
    private String height;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("individual_visit_id")
    @Expose
    private String individual_visit_id;

    @SerializedName("local_captured_at")
    @Expose
    private String local_captured_at;

    @SerializedName("target_type")
    @Expose
    private String target_type;

    @SerializedName("thermal_size")
    @Expose
    private String thermal_size;

    @SerializedName("visible_size")
    @Expose
    private String visible_size;

    @SerializedName("width")
    @Expose
    private String width;

    public ImageProperty(String captured_at, String created_at, String device_deploy_id, String height,
                         String id, String individual_visit_id, String local_captured_at, String target_type,
                         String thermal_size, String visible_size, String width){
        this.captured_at = captured_at;
        this.created_at = created_at;
        this.device_deploy_id = device_deploy_id;
        this.height = height;
        this.id = id;
        this.individual_visit_id = individual_visit_id;
        this.local_captured_at = local_captured_at;
        this.target_type = target_type;
        this.thermal_size = thermal_size;
        this.visible_size = visible_size;
        this.width = width;
    }
}
