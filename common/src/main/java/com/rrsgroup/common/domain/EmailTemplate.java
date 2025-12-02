package com.rrsgroup.common.domain;

public enum EmailTemplate {
    NEW_MESSAGE("New Customer Message", "email/new_message");

    private String subject;
    private String templateName;

    EmailTemplate(String subject, String templateName) {
        this.subject = subject;
        this.templateName = templateName;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }
}
