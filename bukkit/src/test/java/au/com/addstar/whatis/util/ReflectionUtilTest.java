package au.com.addstar.whatis.util;

import me.botsko.prism.Prism;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import sun.reflect.Reflection;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 13/05/2020.
 */
public class ReflectionUtilTest {

    @Test
    public  void getDeclaredFieldName() throws IOException,NoSuchMethodException {
        ReflectionUtil.loadDefineClass();
        ReflectionUtil.substituteClassDefs(Prism.class);
        try {
            Field field = ReflectionUtil.getDeclaredField("plugin_name",Prism.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void getDeclaredField() {
        Iterable<Field> fields;
        try{
           fields = ReflectionUtil.getAllFields(Prism.class);
        }catch (NoClassDefFoundError error){
            try {
                ReflectionUtil.loadDefineClass();
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Could not load class definer: ", ex);
            }
            ReflectionUtil.substituteClassDef(error);
            fields = ReflectionUtil.getAllFields(Prism.class);

        }
        for(Field field: fields) {
            System.out.println(field.getName());
        }
    }
}