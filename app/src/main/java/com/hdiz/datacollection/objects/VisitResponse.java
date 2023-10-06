package com.hdiz.datacollection.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VisitResponse {

    @SerializedName("individual_visit")
    @Expose
    private IndividualVisit individual_visit;
    public VisitResponse(IndividualVisit individual_visit){
        this.individual_visit = individual_visit;
    }
    public IndividualVisit getIndividual_visit() {
        return individual_visit;
    }
}
