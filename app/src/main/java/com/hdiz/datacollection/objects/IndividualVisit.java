package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;

// This class is implemented for saving the response of individual visit api. This api accepts Patient Visit object as
// a body and the Id of patient as a parameter. The id is generated by getting the identifier (qrcode scan)
public class IndividualVisit {

    @SerializedName("created_at")
    @Expose
    private String created_at;

    @SerializedName("device_deploy_id")
    @Expose
    private String device_deploy_id;

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("individual")
    @Expose
    private Patient individual;

    @SerializedName("individual_id")
    @Expose
    private String individual_id;

    @SerializedName("info")
    @Expose
    private TestResult info;

    @SerializedName("local_visited_at")
    @Expose
    private String local_visited_at;

    @SerializedName("note")
    @Expose
    private String note;

    @SerializedName("visited_at")
    @Expose
    private String visited_at;

    public IndividualVisit(String created_at, String device_deploy_id, String id, Patient individual,
                           String individual_id, TestResult info, String local_visited_at, String note, String visited_at){

        this.created_at = created_at ;
        this.device_deploy_id = device_deploy_id;
        this.id = id;
        this.individual = individual;
        this.individual_id = individual_id;
        this.info = info;
        this.local_visited_at = local_visited_at;
        this.note = note;
        this.visited_at = visited_at;
    }
    public String getId(){
        return id;
    }
    public String getLocal_visited_at(){return  local_visited_at; }
    public TestResult getInfo(){
        return info;
    }
    public Patient getIndividual(){
        return individual;
    }


    public boolean isEmpty()  {

        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                if (field.get(this)!=null) {
                    return false;
                }
            } catch (Exception e) {
                System.out.println("Exception occured in processing");
            }
        }
        return true;
    }

}
