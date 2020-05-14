package au.com.addstar.whatis.util;

import au.com.addstar.whatis.helpers.SeparateClassloaderTestRunner;
import me.botsko.prism.Prism;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 13/05/2020.
 */
@RunWith(SeparateClassloaderTestRunner.class)
public class ReflectionUtilTest {

    @Test
    public  void getDeclaredFieldName() throws NoSuchMethodException {
        String name = "dataFolder";
        try {
            ReflectionUtil.getDeclaredField("plugin_name",Prism.class);   // should throw error
            Assert.fail();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError err) {
            ReflectionUtil.loadDefineClass();
            ReflectionUtil.substituteClassDef(err);
        }
        try {

            Field field = ReflectionUtil.getDeclaredField(name, Prism.class);
            Assert.assertEquals(name, field.getName());
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }catch (NoClassDefFoundError err) {
            Assert.fail();
        }
    }

}