package org.cloud.demo;

import org.cloud.annotation.AliasField;

public class UserTable {
    public UserTable(long id, String des){
        this.id = id;
        this.des = des;
    }

    private long id;

    @AliasField(name={"content"})
    private String des;

    public long getId(){
        return id;
    }

    public String getDes(){
        return des;
    }
}
