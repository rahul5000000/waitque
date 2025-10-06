package com.rrsgroup.customer.domain;

public enum CrmType {
    MOCK("mockCrmService"),
    WAITQUE("waitqueCrmService");

    private final String crmServiceName;

    CrmType(String crmServiceName) {
        this.crmServiceName = crmServiceName;
    }

    public String getCrmServiceName() {
        return crmServiceName;
    }
}
