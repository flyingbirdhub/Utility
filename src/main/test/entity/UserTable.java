package entity;

public class UserTable {
    public UserTable(long id, String des, String userId){
        this.id = id;
        this.des = des;
        this.userId = userId;
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
