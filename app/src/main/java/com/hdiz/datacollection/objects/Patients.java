package com.hdiz.datacollection.objects;

import java.util.ArrayList;

// This class is json array form of Individuals class
public class Patients {
    private ArrayList<Individual> individuals ;
    public Patients(ArrayList<Individual> individuals){
        this.individuals = individuals;
    }
    public ArrayList<Individual> getIndividuals() {
        return individuals;
    }
}
