package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// This class is implemented for saving the results of test
public class TestResult {

    @SerializedName("self_exam")
    @Expose
    private String self_exam;

    @SerializedName("self_exam_note")
    @Expose
    private String self_exam_note;

    @SerializedName("gp")
    @Expose
    private String gp;

    @SerializedName("gp_note")
    @Expose
    private String gp_note;

    @SerializedName("mamography")
    @Expose
    private String mamography;

    @SerializedName("mamo_note")
    @Expose
    private String mamo_note;

    @SerializedName("sonography")
    @Expose
    private String sonography;

    @SerializedName("sono_note")
    @Expose
    private String sono_note;

    @SerializedName("pathology")
    @Expose
    private String pathology;

    @SerializedName("patho_note")
    @Expose
    private String patho_note;

    @SerializedName("tumor_side")
    @Expose
    private String tumor_side;

    @SerializedName("tumor_side_note")
    @Expose
    private String tumor_side_note;

    @SerializedName("tumor_size")
    @Expose
    private String tumor_size;

    @SerializedName("tumor_size_note")
    @Expose
    private String tumor_size_note;


    @SerializedName("grade")
    @Expose
    private String grade;

    @SerializedName("stage")
    @Expose
    private String stage;

    @SerializedName("patient_details")
    @Expose
    private PatientDetails patient_details;

    public TestResult(String self_exam, String self_exam_note, String gp, String gp_note, String mamography,
                      String mamo_note, String sonography, String sono_note, String pathology, String patho_note,
                      String tumor_side, String tumor_side_note, String tumor_size, String tumor_size_note,
                      String grade, String stage, PatientDetails patient_details){
        this.self_exam = self_exam;
        this.self_exam_note = self_exam_note;
        this.gp = gp;
        this.gp_note = gp_note;
        this.mamography = mamography;
        this.mamo_note = mamo_note;
        this.sonography = sonography;
        this.sono_note = sono_note;
        this.pathology = pathology;
        this.patho_note = patho_note;
        this.tumor_side = tumor_side;
        this.tumor_side_note = tumor_side_note;
        this.tumor_size = tumor_size;
        this.tumor_size_note = tumor_size_note;
        this.grade = grade;
        this.stage = stage;
        this.patient_details = patient_details;
    }

    public PatientDetails getPatient_details() {
        return patient_details;
    }

    public void setPatient_details(PatientDetails patient_details) {
        this.patient_details = patient_details;
    }

    public String getGp() {
        return gp;
    }
    public String getGp_note() {
        return gp_note;
    }
    public String getGrade() {
        return grade;
    }

    public String getMamo_note() {
        return mamo_note;
    }

    public String getMamography() {
        return mamography;
    }

    public String getPatho_note() {
        return patho_note;
    }

    public String getPathology() {
        return pathology;
    }

    public String getSelf_exam() {
        return self_exam;
    }

    public String getSelf_exam_note() {
        return self_exam_note;
    }

    public String getSono_note() {
        return sono_note;
    }

    public String getSonography() {
        return sonography;
    }

    public String getStage() {
        return stage;
    }

    public String getTumor_side() {
        return tumor_side;
    }

    public String getTumor_side_note() {
        return tumor_side_note;
    }

    public String getTumor_size() {
        return tumor_size;
    }

    public String getTumor_size_note() {
        return tumor_size_note;
    }


}
