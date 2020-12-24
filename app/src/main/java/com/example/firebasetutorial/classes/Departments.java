package com.example.firebasetutorial.classes;

public class Departments {
    private String Description;
    private String DateTime_of_Department_Created;
    private String Name;
    private String Manager;

    public Departments() {
    }

    public Departments(String description, String dateTime_of_department_created, String name,String manager) {
        Description = description;
        DateTime_of_Department_Created = dateTime_of_department_created;
        Name = name;
        Manager = manager;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDateTime_of_Department_Created() {
        return DateTime_of_Department_Created;
    }

    public void setDateTime_of_Department_Created(String dateTime_of_Department_Created) {
        DateTime_of_Department_Created = dateTime_of_Department_Created;
    }

    public String getManager() {
        return Manager;
    }

    public void setManager(String manager) {
        Manager = manager;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
