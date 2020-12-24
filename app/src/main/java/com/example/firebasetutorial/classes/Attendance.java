package com.example.firebasetutorial.classes;

public class Attendance {

    String Email, Name, DateTime_of_Attendance, Attendance_Location, Status, DateTime_Out_Work, Leave_Location;

    public Attendance() {}

    public Attendance(String email, String name, String dateTime_of_Attendance, String attendance_Location, String status, String dateTime_Out_Work, String leave_Location) {
        Email = email;
        Name = name;
        DateTime_of_Attendance = dateTime_of_Attendance;
        Attendance_Location = attendance_Location;
        Status = status;
        DateTime_Out_Work = dateTime_Out_Work;
        Leave_Location = leave_Location;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getDateTime_Out_Work() {
        return DateTime_Out_Work;
    }

    public void setDateTime_Out_Work(String dateTime_Out_Work) {
        DateTime_Out_Work = dateTime_Out_Work;
    }

    public String getLeave_Location() {
        return Leave_Location;
    }

    public void setLeave_Location(String leave_Location) {
        Leave_Location = leave_Location;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDateTime_of_Attendance() {
        return DateTime_of_Attendance;
    }

    public void setDateTime_of_Attendance(String dateTime_of_Attendance) {
        DateTime_of_Attendance = dateTime_of_Attendance;
    }

    public String getAttendance_Location() {
        return Attendance_Location;
    }

    public void setAttendance_Location(String attendance_Location) {
        Attendance_Location = attendance_Location;
    }
}
