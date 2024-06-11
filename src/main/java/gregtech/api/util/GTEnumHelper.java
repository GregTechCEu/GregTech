package gregtech.api.util;

import net.minecraftforge.classloading.FMLForgePlugin;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLLog;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Taken from parts of Forge's EnumHelper, with certain methods made public for easier use.
 */
public class GTEnumHelper {

    private static Object reflectionFactory = null;
    private static Method newConstructorAccessor = null;
    private static Method newInstance = null;
    private static boolean isSetup = false;

    static {
        setup();
    }

    private static void setup() {
        if (isSetup) {
            return;
        }

        try {
            Method getReflectionFactory = Class.forName("sun.reflect.ReflectionFactory")
                    .getDeclaredMethod("getReflectionFactory");
            reflectionFactory = getReflectionFactory.invoke(null);
            newConstructorAccessor = Class.forName("sun.reflect.ReflectionFactory")
                    .getDeclaredMethod("newConstructorAccessor", Constructor.class);
            newInstance = Class.forName("sun.reflect.ConstructorAccessor").getDeclaredMethod("newInstance",
                    Object[].class);
        } catch (Exception e) {
            FMLLog.log.error("Error setting up EnumHelper.", e);
        }

        isSetup = true;
    }

    public static <T extends Enum<?>> T makeEnum(Class<T> enumClass, @Nullable String value, int ordinal,
                                                 Class<?>[] additionalTypes,
                                                 @Nullable Object[] additionalValues) throws Exception {
        setup();
        int additionalParamsCount = additionalValues == null ? 0 : additionalValues.length;
        Object[] params = new Object[additionalParamsCount + 2];
        params[0] = value;
        params[1] = ordinal;
        if (additionalValues != null) {
            System.arraycopy(additionalValues, 0, params, 2, additionalValues.length);
        }
        return enumClass
                .cast(newInstance.invoke(getConstructorAccessor(enumClass, additionalTypes), new Object[] { params }));
    }

    private static Object getConstructorAccessor(Class<?> enumClass,
                                                 Class<?>[] additionalParameterTypes) throws Exception {
        Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
        return newConstructorAccessor.invoke(reflectionFactory, enumClass.getDeclaredConstructor(parameterTypes));
    }

    public static Field getValuesField(final Class<? extends Enum<?>> enumType) {
        Field valuesField = null;
        Field[] fields = enumType.getDeclaredFields();

        for (Field field : fields) {
            String name = field.getName();
            if (name.equals("$VALUES") || name.equals("ENUM$VALUES")) // Added 'ENUM$VALUES' because Eclipse's internal
            // compiler doesn't follow standards
            {
                valuesField = field;
                break;
            }
        }

        int flags = (FMLForgePlugin.RUNTIME_DEOBF ? Modifier.PUBLIC : Modifier.PRIVATE) | Modifier.STATIC |
                Modifier.FINAL | 0x1000 /* SYNTHETIC */;
        if (valuesField == null) {
            String valueType = String.format("[L%s;", enumType.getName().replace('.', '/'));

            for (Field field : fields) {
                if ((field.getModifiers() & flags) == flags &&
                        field.getType().getName().replace('.', '/').equals(valueType)) // Apparently some JVMs return
                // .'s and some don't..
                {
                    valuesField = field;
                    break;
                }
            }
        }
        return valuesField;
    }

    public static void cleanEnumCache(Class<?> enumClass) throws Exception {
        blankField(enumClass, "enumConstantDirectory");
        blankField(enumClass, "enumConstants");
        // Open J9
        blankField(enumClass, "enumVars");
    }

    private static void blankField(Class<?> enumClass, String fieldName) throws Exception {
        for (Field field : Class.class.getDeclaredFields()) {
            if (field.getName().contains(fieldName)) {
                field.setAccessible(true);
                EnumHelper.setFailsafeFieldValue(field, enumClass, null);
                break;
            }
        }
    }
}
