package com.ist.login.testloginapp;

/**
 * Created by Nouman on 1/27/2015.
 */
public class RewardModel implements Item {
    private int RewardId;
    public int getRewardId(){
        return  this.RewardId;
    }
    public void setRewardId(int value){ this.RewardId  = value;}

    private String RewardName;
    public String getRewardName(){
        return  this.RewardName;
    }
    public void setRewardName(String value){ this.RewardName  = value;}

    private String RewardPercentage;
    public String getRewardPercentage(){
        return  this.RewardPercentage;
    }
    public void setRewardPercentage(String value){ this.RewardPercentage  = value;}

    @Override
    public boolean isSection() {
        return false;
    }
}
