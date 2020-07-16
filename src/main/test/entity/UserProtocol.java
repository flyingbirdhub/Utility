package entity;

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

    @AliasField(methodName = "String2Int", parameters = {String.class})
    private long userId;
    public void setUserId(long userId){
        this.userId = userId;
    }

    public static int String2Int(String userId){
        return Integer.valueOf(userId);
    }

    @Override
    public boolean equals(Object object){
        if(object == null){
            return false;
        }
        else if(object == this){
            return true;
        }
        else if(object instanceof UserTable){
            UserTable target = (UserTable)object;
            return id == target.getId() && content.equals(target.getDes()) && userId == Integer.valueOf(target.getUserId());
        }
        else if(object instanceof UserProtocol){
            UserProtocol target = (UserProtocol)object;
            return id == target.id && content.equals(target.content) && userId == target.userId;
        }
        else {
            return false;
        }
    }
}
