package org.cloud.demo;

import org.cloud.annotation.AliasField;

public class UserProtocol {
    private long id;

    public void setId(long id){
        this.id = id;
    }

    @AliasField(name={"des"})
    private String content;
    public void setContent(String content){
        this.content = content;
    }

    @AliasField(methodClass = "org.cloud.demo.PropertyConvertUtil", methodName = "String2Int")
    private long userId;
    private void setUserId(long userId){
        this.userId = userId;
    }
}
