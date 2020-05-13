package au.com.addstar.whatis.util;

import me.botsko.prism.Prism;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import sun.reflect.Reflection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.Assert.*;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 13/05/2020.
 */
public class ReflectionUtilTest {

    @Test
    public  void getDeclaredFieldName() throws NoSuchMethodException {
        try {
            ReflectionUtil.getDeclaredField("plugin_name",Prism.class);   // should throw error
            assert(false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError err) {
            assert(true);
            ReflectionUtil.loadDefineClass();
            ReflectionUtil.substituteClassDef(err);
        }
        try {
            String name = "dataFolder";
            Field field = ReflectionUtil.getDeclaredField(name, Prism.class);
            assert (name.equals(field.getName()));
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }catch (NoClassDefFoundError err) {
            assert(false);
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
        boolean result = false;
        String test = "dataFolder";
        for(Field field: fields) {
            StringBuilder modifiers = new StringBuilder();
            if(test.equals(field.getName())) {
                result = true;
            }
            if(Modifier.isPrivate(field.getModifiers())) {
                modifiers.append("PRIVATE ");
            }
            if(Modifier.isPublic(field.getModifiers())) {
                modifiers.append("PUBLIC ");
            }
            if(Modifier.isProtected(field.getModifiers())) {
                modifiers.append("PROTECTED ");
            }
            if(Modifier.isFinal(field.getModifiers())) {
                modifiers.append("FINAL ");
            }
            if(Modifier.isStatic(field.getModifiers())){
                modifiers.append("STATIC ");
            }
            System.out.println(modifiers +" " + field.getName() +" / "+ field.getType() );
        }
        assert(result);
    }
}