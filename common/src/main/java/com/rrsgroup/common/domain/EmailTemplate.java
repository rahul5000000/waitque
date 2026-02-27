package com.rrsgroup.common.domain;

public enum EmailTemplate {
    NEW_MESSAGE("New Customer Message", "email/new_message"),
    NEW_LEAD("New Lead Created", "email/new_lead"),
    NEW_USER("Welcome to WaitQue", "email/new_user"),
    NEW_USER_ADMIN("WaitQue User Created", "email/new_user_admin"),
    RESET_PASSWORD("Reset Your Password", "email/reset_password"),
    RESET_PASSWORD_ADMIN("WaitQue User Password Reset", "email/reset_password_admin"),
    USER_DISABLED("Your Account Has Been Disabled", "email/user_disabled"),
    USER_DISABLED_ADMIN("WaitQue User Account Has Been Disabled", "email/user_disabled_admin"),
    USER_ENABLED("Your Account Has Been Enabled", "email/user_enabled"),
    USER_ENABLED_ADMIN("WaitQue User Account Has Been Enabled", "email/user_enabled_admin"),
    USER_DELETED_ADMIN("WaitQue User Account Has Been Deleted", "email/user_deleted_admin"),
    NEW_ADMIN("Welcome to WaitQue", "email/new_admin");

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
