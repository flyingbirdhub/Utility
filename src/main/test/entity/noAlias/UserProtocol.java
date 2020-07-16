package entity.noAlias;

import entity.UserTable;

public class UserProtocol {
    private long id;

    public void setId(long id){
        this.id = id;
    }

    private String content;
    public void setContent(String content){
        this.content = content;
    }

    private long selfId;
    public void setSelfId(long userId){
        this.selfId = userId;
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
            return id == target.getId() && content.equals(target.getDes()) && selfId == Integer.valueOf(target.getUserId());
        }
        else if(object instanceof UserProtocol){
            UserProtocol target = (UserProtocol)object;
            return id == target.id && content.equals(target.content) && selfId == target.selfId;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return id+"-"+content+"-"+selfId;
    }
}
