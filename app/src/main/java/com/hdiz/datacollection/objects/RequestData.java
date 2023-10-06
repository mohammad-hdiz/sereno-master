package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// This class is the object for sending request to the server in order to get an individual based on a identifier (qrcode)
public class RequestData {

    @SerializedName("identifier")
    @Expose
    private String identifier;

    @SerializedName("local_captured_at")
    @Expose
    private String local_captured_at;

    @SerializedName("first_name")
    @Expose
    private String first_name;

    @SerializedName("middle_name")
    @Expose
    private String middle_name;

    @SerializedName("last_name")
    @Expose
    private String last_name;

    @SerializedName("gender")
    @Expose
    private String gender;

    @SerializedName("birth_date")
    @Expose
    private String birth_date;

    @SerializedName("race")
    @Expose
    private String race;

    @SerializedName("contact")
    @Expose
    private Contact contact;

    @SerializedName("note")
    @Expose
    private String note;

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public void setLocal_captured_at(String local_captured_at) {
        this.local_captured_at = local_captured_at;
    }
    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }
    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }
    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }
    public void setRace(String race) {
        this.race = race;
    }
    public void setContact(Contact contact) {
        this.contact = contact;
    }
    public void setNote(String note) {
        this.note = note;
    }
}