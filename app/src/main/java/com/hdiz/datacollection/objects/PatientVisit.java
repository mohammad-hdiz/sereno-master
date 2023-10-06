package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// This class is the object for sending request to the Individual visit api
public class PatientVisit {
    @SerializedName("local_visited_at")
    @Expose
    private String local_visited_at;

    @SerializedName("info")
    @Expose
    private TestResult info;

    @SerializedName("note")
    @Expose
    private String note;

    public PatientVisit(String local_visited_at, TestResult info, String note){
        this.local_visited_at = local_visited_at;
        this.info = info;
        this.note = note;
    }

    public TestResult getInfo() {
        return info;
    }
}
