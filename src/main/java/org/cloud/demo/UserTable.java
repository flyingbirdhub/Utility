package org.cloud.demo;

public class UserTable {
    public UserTable(long id, String des){
        this.id = id;
        this.des = des;
    }

    private long id;

    private String des;

    private String userId;

    public long getId(){
        return id;
    }

    public String getDes(){
        return des;
    }

    public String getUserId(){
        return userId;
    }
}
