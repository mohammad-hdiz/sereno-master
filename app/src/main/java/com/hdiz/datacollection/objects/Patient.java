package com.hdiz.datacollection.objects;

import java.util.ArrayList;

// This class is a json form of an individual object
public class Patient {

    private Individual individual;
    public Patient(Individual individual){
        this.individual = individual;
    }
    public Individual getIndividual() {
        return individual;
    }

}
