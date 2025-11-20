package com.rrsgroup.company.domain;

public enum UploadFileType {
    LOGO("logo");

    private String folder;

    UploadFileType(String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }
}
