package com.rrsgroup.customer.domain;

public enum UploadFileType {
    LEAD("lead");

    private String folder;

    UploadFileType(String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }
}
