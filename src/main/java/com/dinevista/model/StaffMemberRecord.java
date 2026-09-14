package com.dinevista.model;

import java.io.Serializable;

/**
 * A read model over one operational staff member, owned by the Event Resource and
 * Staff Scheduling module (Wijesuriya W. A. T. D. / IT25103799). In MySQL mode this
 * mirrors a join of {@code staff_profile} and {@code user_account}; availability
 * status is the only field this module updates (staff records themselves are
 * created through account registration, not this module). Availability status is
 * one of AVAILABLE, UNAVAILABLE, ON_LEAVE.
 */
public class StaffMemberRecord implements Serializable {
    private final long id;
    private final String fullName;
    private final String employeeCode;
    private final String jobTitle;
    private final String department;
    private String availabilityStatus;

    public StaffMemberRecord(long id, String fullName, String employeeCode, String jobTitle,
                              String department, String availabilityStatus) {
        this.id = id;
        this.fullName = fullName;
        this.employeeCode = employeeCode;
        this.jobTitle = jobTitle;
        this.department = department;
        this.availabilityStatus = availabilityStatus == null ? "AVAILABLE" : availabilityStatus;
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmployeeCode() { return employeeCode; }
    public String getJobTitle() { return jobTitle; }
    public String getDepartment() { return department; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    public boolean isAvailable() {
        return "AVAILABLE".equals(availabilityStatus);
    }

    public String getStatusCss() {
        switch (availabilityStatus) {
            case "AVAILABLE": return "confirmed";
            case "ON_LEAVE": return "pending";
            default: return "cancelled";
        }
    }
}
