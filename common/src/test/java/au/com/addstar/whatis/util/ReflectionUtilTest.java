package au.com.addstar.whatis.util;

import me.botsko.prism.Prism;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Created by Narimm on 13/05/2020.
 */
public class ReflectionUtilTest {

    @Test
    public  void getDeclaredFieldName() throws NoSuchMethodException {
        String name = "dataFolder";
        try {
            ReflectionUtil.getDeclaredField("plugin_name",Prism.class);
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