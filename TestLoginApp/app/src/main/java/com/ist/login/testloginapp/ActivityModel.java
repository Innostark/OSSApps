package com.ist.login.testloginapp;

/**
 * Created by Nouman on 1/20/2015.
 */
public class ActivityModel implements Item {
    public ActivityModel(){

    }
    private String Name;
    public String getName(){
        return  Name;
    }
    public void setName(String value){
        this.Name = value;
    }
    private String Date;
    public String getDate(){
        return  Date;
    }
    public void setDate(String value){
        this.Date = value;
    }
    private String FbPost;
    public String getFbPost(){
        return  FbPost;
    }
    public void setFbPost(String value){
        this.FbPost = value;
    }
    private int Points;
    public int getPoints(){
        return  Points;
    }
    public void setPoints(int value){
        this.Points = value;
    }
    private int FbPoints;
    public int getFbPoints(){
        return  FbPoints;
    }
    public void setFbPoints(int value){
        this.FbPoints = value;
    }
    private String FormattedPoints;
    public String getFormattedPoints(){
        return  FormattedPoints;
    }
    public void setFormattedPoints(String value){
        this.FormattedPoints = value;
    }
    private Boolean IsFbPost;
    public  Boolean getIsFbPost(){
        return IsFbPost;
    }
    public void setIsFbPost(Boolean value){
        this.IsFbPost = value;
    }
    private int Id;
    public void setId(int value){
        this.Id = value;
    }
    public int getId(){
        return this.Id;
    }
    @Override
    public boolean isSection(){
        return false;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
