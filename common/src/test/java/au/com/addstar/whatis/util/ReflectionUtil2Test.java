package au.com.addstar.whatis.util;

import au.com.addstar.whatis.helpers.SeparateClassloaderTestRunner;
import me.botsko.prism.Prism;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created for the Charlton IT Project.
 * Created by benjicharlton on 14/05/2020.
 */
@RunWith(SeparateClassloaderTestRunner.class)
public class ReflectionUtil2Test {
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
        Assert.assertTrue(result);
    }
}
