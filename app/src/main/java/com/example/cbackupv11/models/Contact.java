package com.example.cbackupv11.models;

public class Contact {
    private String fileName;
    private String fileData;
    private String date;

    public Contact() {
    }

    public Contact(String fileName, String fileData, String date) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "fileName='" + fileName + '\'' +
                ", fileData='" + fileData + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
