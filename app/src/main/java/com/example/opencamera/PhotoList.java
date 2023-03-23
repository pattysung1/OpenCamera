package com.example.opencamera;

public class PhotoList
{
    private String photo;
    private String record;
    private int index;

    public PhotoList( int index, String photo, String record )
    {
        this.index = index;
        this.photo = photo;
        this.record = record;
    }

    public int  getIndex() {
        return index;
    }
    public String getPhoto() {
        return photo;
    }
    public String getRecord() {
        return record;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public void setRecord(String record) {
        this.record = record;
    }
}
