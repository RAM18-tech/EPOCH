package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGContactInfo implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    private String firstName;
    private String lastName;
    private boolean alertNotificationEmail;
    private boolean alertNotificationPush;
    private String contactId;
    private String parentalControl;
    private String isPrimaryContact;
    private String email;
    private String mobileNumber;
    private boolean allowTracking;
    private List<CGSegmentInfo> segmentInfo;

    public List<CGSegmentInfo> getSegmentInfo() {
        return segmentInfo;
    }

    public void setSegmentInfo(List<CGSegmentInfo> segmentInfo) {
        this.segmentInfo = segmentInfo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isAlertNotificationEmail() {
        return alertNotificationEmail;
    }

    public void setAlertNotificationEmail(boolean alertNotificationEmail) {
        this.alertNotificationEmail = alertNotificationEmail;
    }

    public boolean isAlertNotificationPush() {
        return alertNotificationPush;
    }

    public void setAlertNotificationPush(boolean alertNotificationPush) {
        this.alertNotificationPush = alertNotificationPush;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getParentalControl() {
        return parentalControl;
    }

    public void setParentalControl(String parentalControl) {
        this.parentalControl = parentalControl;
    }

    public String getIsPrimaryContact() {
        return isPrimaryContact;
    }

    public void setIsPrimaryContact(String isPrimaryContact) {
        this.isPrimaryContact = isPrimaryContact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public boolean isAllowTracking() {
        return allowTracking;
    }

    public void setAllowTracking(boolean allowTracking) {
        this.allowTracking = allowTracking;
    }
}
