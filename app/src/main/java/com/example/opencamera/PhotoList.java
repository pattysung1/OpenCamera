package com.example.opencamera;

public class PhotoList
{
    private String photo;
    private String record;

    public PhotoList( String photo, String record )
    {
        this.photo = photo;
        this.record = record;
    }

    public String getPhoto() {
        return photo;
    }
    public String getRecord() {
        return record;
    }
}
