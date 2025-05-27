package top.catnies.firnettyinjector.util;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 *  反射工具类
 */
public class ReflectiveUtil {

    public static Field getFieldByType(final Object object, final Class<?> type) throws NoSuchFieldException {
        for (final Field field : getInheritedDeclaredFields(getClassFromObject(object))) {
            if (type.isAssignableFrom(field.getType())) return field;
        }

        throw new NoSuchFieldException("Type: " + type.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(final Object object, final Field field) throws IllegalAccessException {
        field.setAccessible(true);
        return (T) field.get(object);
    }

    private static Class<?> getClassFromObject(final Object object) {
        if (object instanceof Class<?>) return (Class<?>) object;
        return object.getClass();
    }

    private static Field[] getInheritedDeclaredFields(final Class<?> klass) {
        final Field[] inheritedFields;
        if (klass.equals(Object.class)) inheritedFields = new Field[0];
        else inheritedFields = getInheritedDeclaredFields(klass.getSuperclass());

        final Field[] ownFields = klass.getDeclaredFields();

        final Field[] allFields = Arrays.copyOf(ownFields, ownFields.length + inheritedFields.length);
        System.arraycopy(inheritedFields, 0, allFields, ownFields.length, inheritedFields.length);

        return allFields;
    }

}
