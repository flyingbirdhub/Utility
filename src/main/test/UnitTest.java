import entity.UserTable;
import org.cloud.convert.Convert;
import org.cloud.exception.ConvertException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UnitTest {
    private UserTable userTable;

    @Before
    public void init(){
        userTable = new UserTable(2019, "I am bast!", "2020");
    }

    @Test
    public void testNoAlias(){
        try {
            entity.noAlias.UserProtocol userProtocol = new entity.noAlias.UserProtocol();
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol.toString(), userTable.getId()+"-null-0");
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testNoConvertMethod(){
        try {
            entity.noConvertMethod.UserProtocol userProtocol = new entity.noConvertMethod.UserProtocol();
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol.toString(), userTable.getId()+"-"+userTable.getDes()+"-0");
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testConvertMethodNoneStaticSelf(){
        try {
            entity.convertMethod.noneStatic.self.UserProtocol userProtocol = new entity.convertMethod.noneStatic.self.UserProtocol();
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol, userTable);
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testConvertMethodNoneStaticOthers(){
        try {
            entity.convertMethod.noneStatic.others.UserProtocol userProtocol = new entity.convertMethod.noneStatic.others.UserProtocol();
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol, userTable);
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testConvertMethodGeneralSelf(){
        try {
            entity.convertMethod.general.self.UserProtocol userProtocol = new entity.convertMethod.general.self.UserProtocol();
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol, userTable);
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testConvertMethodGeneralOthers(){
        try {
            entity.convertMethod.general.others.UserProtocol userProtocol = new entity.convertMethod.general.others.UserProtocol();
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol, userTable);
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSelfStaticConvertMethod(){
        try {
            entity.convertMethod.general.self.UserProtocol userProtocol = new entity.convertMethod.general.self.UserProtocol();
            Convert.convert(userProtocol, userTable);
            Assert.assertEquals(userProtocol, userTable);
        }
        catch (ConvertException e){
            Assert.fail(e.getMessage());
        }
    }
}
