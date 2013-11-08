package com.example.lawyerapp;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table CASE_CONTACTS.
 */
public class CaseContacts {

    private Long id;
    private Long caseID;
    private Long contactID;

    public CaseContacts() {
    }

    public CaseContacts(Long id) {
        this.id = id;
    }

    public CaseContacts(Long id, Long caseID, Long contactID) {
        this.id = id;
        this.caseID = caseID;
        this.contactID = contactID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCaseID() {
        return caseID;
    }

    public void setCaseID(Long caseID) {
        this.caseID = caseID;
    }

    public Long getContactID() {
        return contactID;
    }

    public void setContactID(Long contactID) {
        this.contactID = contactID;
    }

}
