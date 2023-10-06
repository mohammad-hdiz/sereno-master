package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

// This class is designed for maintaining the detail information about patient. This class is also combined with
// TestResult class and then they are uploaded to the server.
public class PatientDetails {
    @SerializedName("cancer_hist")
    @Expose
    private String cancer_hist;

    @SerializedName("visit")
    @Expose
    private ArrayList<String> visit;

    @SerializedName("visit_note")
    @Expose
    private String visit_note;

    @SerializedName("cancer_type")
    @Expose
    private String cancer_type;

    @SerializedName("cancer_note")
    @Expose
    private String cancer_note;

    @SerializedName("cancer_f_hist")
    @Expose
    private String cancer_f_hist;

    @SerializedName("cancer_f_type")
    @Expose
    private String cancer_f_type;

    @SerializedName("cancer_f_note")
    @Expose
    private String cancer_f_note;

    @SerializedName("relationship")
    @Expose
    private String relationship;

    @SerializedName("menopausal_status")
    @Expose
    private String menopausal_status;

    @SerializedName("meno_note")
    @Expose
    private String meno_note;

    @SerializedName("gender")
    @Expose
    private String gender;

    @SerializedName("gender_note")
    @Expose
    private String gender_note;

    @SerializedName("age")
    @Expose
    private String age;

    @SerializedName("age_note")
    @Expose
    private String age_note;

    @SerializedName("race")
    @Expose
    private String race;

    @SerializedName("race_note")
    @Expose
    private String race_note;

    @SerializedName("comorbidities")
    @Expose
    private ArrayList<String> comorbidities;

    @SerializedName("comorbidities_note")
    @Expose
    private String comorbidities_note;

    @SerializedName("adherence")
    @Expose
    private String adherence;

    @SerializedName("adh_note")
    @Expose
    private String adh_note;

    @SerializedName("medications")
    @Expose
    private ArrayList<String> medications;

    @SerializedName("medications_note")
    @Expose
    private String medications_note;

    public PatientDetails(ArrayList<String> visit, String visit_note, String cancer_hist, String cancer_type, String cancer_note,
                          String cancer_f_hist, String cancer_f_type, String cancer_f_note,
                          String relationship, String menopausal_status, String meno_note, String gender,
                          String gender_note, String age, String age_note, String race,
                          String race_note, ArrayList<String> comorbidities, String comorbidities_note,
                          String adherence, String adh_note, ArrayList<String> medications, String medications_note){
        this.visit = visit;
        this.visit_note = visit_note;
        this.cancer_hist = cancer_hist;
        this.cancer_type = cancer_type;
        this.cancer_note = cancer_note;
        this.cancer_f_hist = cancer_f_hist;
        this.cancer_f_type = cancer_f_type;
        this.cancer_f_note = cancer_f_note;
        this.relationship = relationship;
        this.menopausal_status = menopausal_status;
        this.meno_note = meno_note;
        this.gender = gender;
        this.gender_note = gender_note;
        this.age = age;
        this.age_note = age_note;
        this.race = race;
        this.race_note= race_note;
        this.comorbidities = comorbidities;
        this.comorbidities_note = comorbidities_note;
        this.adherence = adherence;
        this.adh_note = adh_note;
        this.medications = medications;
        this.medications_note = medications_note;
    }

    public ArrayList<String> getComorbidities() {
        return comorbidities;
    }

    public ArrayList<String> getVisit() {
        return visit;
    }

    public String getAge() {
        return age;
    }

    public String getCancer_f_hist() {
        return cancer_f_hist;
    }

    public String getAge_note() {
        return age_note;
    }

    public String getCancer_f_note() {
        return cancer_f_note;
    }

    public String getAdh_note() {
        return adh_note;
    }

    public String getCancer_f_type() {
        return cancer_f_type;
    }

    public String getCancer_hist() {
        return cancer_hist;
    }

    public String getCancer_note() {
        return cancer_note;
    }

    public String getAdherence() {
        return adherence;
    }

    public String getCancer_type() {
        return cancer_type;
    }

    public String getComorbidities_note() {
        return comorbidities_note;
    }

    public String getGender() {
        return gender;
    }

    public String getMenopausal_status() {
        return menopausal_status;
    }

    public String getGender_note() {
        return gender_note;
    }

    public String getMeno_note() {
        return meno_note;
    }

    public String getRace() {
        return race;
    }

    public String getRelationship() {
        return relationship;
    }

    public String getRace_note() {
        return race_note;
    }

    public ArrayList<String> getMedications() {
        return medications;
    }

    public String getVisit_note() {
        return visit_note;
    }

    public String getMedications_note() {
        return medications_note;
    }
}
