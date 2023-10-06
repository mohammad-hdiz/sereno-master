package com.hdiz.datacollection.objects;

// This class is designed for saving Individual contact Information
public class Contact {
    String address;
    String city;
    String state;
    String country;
    String postal_code;
    String phone_no;
    String email;

    public Contact(String address,String city, String state, String country
    , String postal_code, String phone_no, String email){
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postal_code = postal_code;
        this.phone_no = phone_no;
        this.email = email;
    }

    public String getAddress(){
        return address;
    }
    public String getCity(){
        return city;
    }
    public String getState(){
        return state;
    }
    public String getCountry(){
        return country;
    }
    public String getPostal_code(){
        return postal_code;
    }
    public String getPhone_no(){
        return phone_no;
    }
    public String getEmail(){
        return email;
    }
}
