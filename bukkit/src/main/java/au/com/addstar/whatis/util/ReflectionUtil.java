package au.com.addstar.whatis.util;

import com.esotericsoftware.asm.ClassWriter;
import com.esotericsoftware.asm.Type;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtil
{
	private static Logger logger = Logger.getAnonymousLogger();

	public static Field getDeclaredField(String name, Class<?> clazz) throws NoSuchFieldException
	{
		try
		{
			return clazz.getDeclaredField(name);
		}
		catch (NoSuchFieldException e)
		{
			clazz = clazz.getSuperclass();
			while(!clazz.equals(Object.class))
			{
				try
				{
					return clazz.getDeclaredField(name);
				}
				catch(NoSuchFieldException ex)
				{
					clazz = clazz.getSuperclass();
				}
			}
		}
		throw new NoSuchFieldException("Unknown field " + name);
	}
	
	public static Iterable<Field> getAllFields(Class<?> clazz)
	{
		Iterable<Field> classFields = Arrays.asList(clazz.getDeclaredFields());
		
		clazz = clazz.getSuperclass();
		while(!clazz.equals(Object.class))
		{
			classFields = Iterables.concat(classFields, Arrays.asList(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		
		return classFields;
	}

	/* #GETVISIT
    ClassReader substReader = null;
    public void loadSubstReader() throws IOException {
        substReader = new ClassReader(getResource("SubstituteClass.class"));
    }*/
	private static Method defineClass = null; //probably going to call it a lot, cache it.

	protected static void loadDefineClass() throws NoSuchMethodException {
		defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
		defineClass.setAccessible(true);
	}

	private static void loadClass(String className, byte[] bytecode) { //could change return type to Class
		try {
			ClassLoader loader = ClassLoader.getSystemClassLoader();
			defineClass.invoke(loader, className, bytecode, 0, bytecode.length);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException("Could not load class " + className + ": ", ex);
		}
	}

	/**
	 * Creates a new, blank class with the missing class's name and package
	 *
	 * @param error The NoClassDefFoundError produced because of the missing class
	 */
	public static void substituteClassDef(NoClassDefFoundError error) {
		final String target = error.toString();
		final String targetInternalName = target.substring(target.lastIndexOf(" ") + 1, target.length()-1);
		final String targetBytecodeName = targetInternalName.substring(1);
		final String targetClassFullName = Type.getObjectType(targetInternalName).getClassName();//todo: maybe have to manually pattern match
		final String targetClassName = targetClassFullName.substring(1);//todo: maybe have to manually pattern match
		logger.log(Level.INFO,"Creating substitution class {0}", targetBytecodeName);
		final ClassWriter writer = new ClassWriter(0);
		writer.visit(51, 33, targetBytecodeName, null, "java/lang/Object", new String[]{});//may break with Java updates, in which case comment writer.visit lines and uncomment #GETVISIT code to see the correct values
		writer.visitEnd();
		//#GETVISIT
        /*ClassVisitor renamer = new ClassVisitor(Opcodes.ASM4, writer) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                getLogger().log(Level.INFO, "visit({0}, {1}, targetBytecodeName, {2}, {3}, new String[]{})", new Object[]{version, access, signature, superName});
                super.visit(version, access, targetBytecodeName, signature, superName, interfaces); //rename to targetBytecodeName
            }

        };
        substReader.accept(renamer, ClassReader.SKIP_DEBUG);*/
		loadClass(targetClassName, writer.toByteArray());
	}

	public static void substituteClassDefs(Class clazz) throws IOException {
		substituteFieldDefs(clazz, 0);
		substituteMethodDefs(clazz, 0); //do one at a time so that getFields() isn't called repeatedly once it begins to succeed
		substituteConstructorDefs(clazz, 0);
		if (clazz.getSuperclass() != null) {
			substituteClassDefs(clazz.getSuperclass());
		}
	}

	public static void substituteFieldDefs(Class clazz, int recursions) throws IOException {
		try {
			clazz.getDeclaredFields();
		} catch (NoClassDefFoundError ex) {
			substituteClassDef(ex);
			recursions++;
			if (recursions > 100) {
				throw new RuntimeException("Too many class def substitutions for fields of class " + clazz.getSimpleName() + "! Aborting substitution process.");
			}
			substituteFieldDefs(clazz, recursions);
		}
	}

	public static void substituteMethodDefs(Class clazz, int recursions) throws IOException {
		try {
			clazz.getDeclaredMethods();
		} catch (NoClassDefFoundError ex) {
			substituteClassDef(ex);
			recursions++;
			if (recursions > 100) {
				throw new RuntimeException("Too many class def substitutions for methods of class " + clazz.getSimpleName() + "! Aborting substitution process.");
			}
			substituteFieldDefs(clazz, recursions);
		}
	}

	public static void substituteConstructorDefs(Class clazz, int recursions) throws IOException {
		try {
			clazz.getDeclaredConstructors();
		} catch (NoClassDefFoundError ex) {
			substituteClassDef(ex);
			recursions++;
			if (recursions > 100) {
				throw new RuntimeException("Too many class def substitutions for constructors of class " + clazz.getSimpleName() + "! Aborting substitution process.");
			}
			substituteFieldDefs(clazz, recursions);
		}
	}

	public static void setLogger(Logger logger) {
		ReflectionUtil.logger = logger;
	}
}
