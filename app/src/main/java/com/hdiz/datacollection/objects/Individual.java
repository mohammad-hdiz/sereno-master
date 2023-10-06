package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// This class is implemented for each individual information
public class Individual {
    private String age;
    private String birth_date;
    private String captured_at;
    private String created_at;
    private String first_name;
    private String gender;
    private String id;
    private IndividualVisit last_individual_visit;
    private String identifier;
    private String last_name;
    private String local_captured_at;
    private String middle_name;
    private String note;
    private String race;
    private Contact contact;


    public Individual(String age, String birth_date, String captured_at, String created_at,
                      String first_name, String gender, String id, IndividualVisit last_individual_visit, String identifier, String last_name,
                      String local_captured_at, String middle_name, String note, String race, Contact contact) {
        this.age = age;
        this.birth_date = birth_date;
        this.captured_at = captured_at;
        this.created_at = created_at;
        this.first_name = first_name;
        this.gender = gender;
        this.id = id;
        this.last_individual_visit = last_individual_visit;
        this.identifier = identifier;
        this.last_name = last_name;
        this.local_captured_at = local_captured_at;
        this.middle_name = middle_name;
        this.note = note;
        this.race = race;
        this.contact = contact;
    }

    public String getAge() {
        return age;
    }
    public String getBirth_date() {
        return birth_date;
    }
    public String getCaptured_at() {
        return captured_at;
    }
    public String getCreated_at() {
        return created_at;
    }
    public String getFirstName() {
        return first_name;
    }
    public String getGender() {
        return gender;
    }
    public String getId() {
        return id;
    }
    public IndividualVisit getVisitResponse() {
        return last_individual_visit;
    }
    public String getIdentifier() {
        return identifier;
    }
    public String getLastName() {
        return last_name;
    }
    public String getLocal_captured_at() {
        return local_captured_at;
    }
    public String getMiddle_name() {
        return middle_name;
    }
    public String getNote() {
        return note;
    }
    public String getRace() {
        return race;
    }
    public Contact getContact(){
        return contact;
    }


    public void setAge(String age) {
        this.age = age;
    }
    public void setBirth_date(String birth_date) {
        this.birth_date = birth_date;
    }
    public void setCaptured_at(String captured_at) {
        this.captured_at = gender;
    }
    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
    public void setFirstName(String first_name) {
        this.first_name = first_name;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public void setLastName(String last_name) {
        this.last_name = last_name;
    }
    public void setLocal_captured_at(String local_captured_at) {
        this.local_captured_at = local_captured_at;
    }
    public void setMiddle_name(String middle_name) {
        this.middle_name = middle_name;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public void setRace(String race) {
        this.race = race;
    }
}
