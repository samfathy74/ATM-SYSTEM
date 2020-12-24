package com.example.firebasetutorial.classes;

public class Notification {
    String DateTime_Requested;
    String Department;
    String Email;
    String Location;
    String Name;
    String Request_Case;
    String Request_Type;
    String RequestCode;
    String Signature;

    public Notification() {}

    public Notification(String dateTime_Requested, String department, String email, String location, String name, String request_Case, String request_Type, String requestCode, String signature) {
        DateTime_Requested = dateTime_Requested;
        Department = department;
        Email = email;
        Location = location;
        Name = name;
        Request_Case = request_Case;
        Request_Type = request_Type;
        RequestCode = requestCode;
        Signature = signature;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
    }

    public String getRequestCode() {
        return RequestCode;
    }

    public void setRequestCode(String requestCode) {
        RequestCode = requestCode;
    }

    public String getDateTime_Requested() {
        return DateTime_Requested;
    }

    public void setDateTime_Requested(String dateTime_Requested) {
        DateTime_Requested = dateTime_Requested;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String department) {
        Department = department;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getRequest_Case() {
        return Request_Case;
    }

    public void setRequest_Case(String request_Case) {
        Request_Case = request_Case;
    }

    public String getRequest_Type() {
        return Request_Type;
    }

    public void setRequest_Type(String request_Type) {
        Request_Type = request_Type;
    }

}