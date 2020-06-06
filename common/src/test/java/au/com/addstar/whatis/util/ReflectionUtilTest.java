package au.com.addstar.whatis.util;

import au.com.addstar.whatis.objects.Ball;
import au.com.addstar.whatis.objects.Fireball;
import me.botsko.prism.Prism;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Narimm on 13/05/2020.
 */
@SuppressWarnings("rawtypes")
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void testGetAllExtendingClasses() throws MalformedURLException {
        Fireball fireball = new Fireball(1,1);
        fireball.getSpeed();
        Class clazz = Ball.class;
        Map<String,Class<? extends Ball>> first = ReflectionUtil.getMapAllExtendingClasses(Thread.currentThread().getContextClassLoader(),Ball.class,null,false);
        assertEquals(2, first.size());
        Set<String> result = ReflectionUtil.getAllExtendingClasses(Thread.currentThread().getContextClassLoader(),clazz);
        assertEquals(2, result.size());
        Set<String> result2 = ReflectionUtil.getAllExtendingClasses(Thread.currentThread().getContextClassLoader(),clazz,null, true);
        assertEquals(3, result2.size());
        Set<String> result3 = ReflectionUtil.getAllExtendingClasses(Thread.currentThread().getContextClassLoader(),clazz,"getSpeed");
        assertEquals(1, result3.size());
        URL[] url = new URL[]{new URL("https://www.google.com")};
        ClassLoader loader = new URLClassLoader(url, Thread.class.getClassLoader());
        Set<String> result4 = ReflectionUtil.getAllExtendingClasses(loader,clazz);
        assertFalse(result4.contains("Fireball"));

    }
}