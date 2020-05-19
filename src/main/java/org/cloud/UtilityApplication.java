package org.cloud;

import org.cloud.convert.Convert;
import org.cloud.demo.UserProtocol;
import org.cloud.demo.UserTable;

public class UtilityApplication {
    public static void main(String[] args){
        System.out.println("utility application run...");

        UserTable userTable = new UserTable(10, "这是用户表");
        UserProtocol userProtocol = new UserProtocol();
        try {
            Convert.convert(userProtocol, userTable);
        }
        catch (ConvertException e){
            e.printStackTrace();
        }
    }
}
