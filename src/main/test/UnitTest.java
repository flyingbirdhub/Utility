import entity.UserProtocol;
import entity.UserTable;
import org.cloud.convert.Convert;
import org.cloud.exception.ConvertException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

public class UnitTest {
    private UserTable userTable;
    private UserProtocol userProtocol;

    @Before
    public void init(){
        userTable = new UserTable(2019, "I am bast!", "2020");
        userProtocol = new UserProtocol();
    }

    @Test
    public void testSelfStaticConvertMethod(){
        try {
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol, userTable);
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }
}
