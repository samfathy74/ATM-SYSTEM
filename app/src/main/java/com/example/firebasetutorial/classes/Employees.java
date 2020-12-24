package com.example.firebasetutorial.classes;

public class Employees {
    String ID, First_Name, Last_Name, Email,Department,Password,DateTime_of_last_update, Phone, Role, DateTime_of_account_created, Profile_Image;
    public Employees() {}

    public Employees(String ID, String first_Name, String last_Name, String email, String department, String password, String dateTime_of_last_update, String phone, String role, String dateTime_of_account_created, String profile_Image) {
        this.ID = ID;
        First_Name = first_Name;
        Last_Name = last_Name;
        Email = email;
        Department = department;
        Password = password;
        DateTime_of_last_update = dateTime_of_last_update;
        Phone = phone;
        Role = role;
        DateTime_of_account_created = dateTime_of_account_created;
        Profile_Image = profile_Image;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDepartment() {
        return Department;
    }

    public void setDepartment(String department) {
        Department = department;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getDateTime_of_last_update() {
        return DateTime_of_last_update;
    }

    public void setDateTime_of_last_update(String dateTime_of_last_update) {
        DateTime_of_last_update = dateTime_of_last_update;
    }

    public String getFirst_Name() {
        return First_Name;
    }

    public void setFirst_Name(String first_Name) {
        First_Name = first_Name;
    }

    public String getLast_Name() {
        return Last_Name;
    }

    public void setLast_Name(String last_Name) {
        Last_Name = last_Name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getRole() {
        return Role;
    }

    public void setRole(String role) {
        Role = role;
    }

    public String getDateTime_of_account_created() {
        return DateTime_of_account_created;
    }

    public void setDateTime_of_account_created(String dateTime_of_account_created) {
        DateTime_of_account_created = dateTime_of_account_created;
    }

    public String getProfile_Image() {
        return Profile_Image;
    }

    public void setProfile_Image(String profile_Image) {
        Profile_Image = profile_Image;
    }
}
