package com.stubhub.domain.inventory.listings.v2.newflow.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PojoTest extends PojoTestClasses {

  @Test
  public void testPojos() {
    for (Class className : classes) {
      try {
        callGettersAndSetters(className);
      } catch (Exception e) {
      }
    }
  }

  // Tests for getters and setters
  private <T> void callGettersAndSetters(Class<T> className) throws Exception {
    Object object = className.newInstance();

    // Invoke Setters
    invokeSetters(object, className.getDeclaredMethods());
    invokeGetters(object, className.getDeclaredMethods());
  }

  private void invokeSetters(Object object, Method[] declaredMethods) {
    invoke(object, declaredMethods, true);
  }

  private void invokeGetters(Object object, Method[] declaredMethods) {
    invoke(object, declaredMethods, false);
  }

  private void invoke(Object object, Method[] declaredMethods, boolean isSet) {
    for (Method method : declaredMethods) {
      try {
        Class<?>[] paramTypes = method.getParameterTypes();

        if (isSet) {
          processSetters(object, method, paramTypes);
        } else {
          processGetters(object, method, paramTypes);
        }
      } catch (Exception e) {
      }
    }
  }

  private void processSetters(Object object, Method method, Class<?>[] paramTypes)
      throws Exception {
    if (method.getName().startsWith("set")) {
      method.invoke(object, addPrimitiveTypes(paramTypes));
    }
  }

  private void processGetters(Object object, Method method, Class<?>[] paramTypes)
      throws Exception {
    if ((method.getName().startsWith("is") || method.getName().startsWith("get")
        || method.getName().equals("toString")) && paramTypes.length == 0) {
      method.invoke(object);
    }
  }

  // Add primitive parameters
  private Object[] addPrimitiveTypes(Class<?>[] paramTypes) throws Exception {
    Object[] params = new Object[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      if (paramTypes[i].isPrimitive()) {
        params[i] = primitiveTypes.get(paramTypes[i]);
      } else {
        params[i] = parameterTypes.get(paramTypes[i].getName());
      }
    }

    return params;
  }

  private static Map<String, Object> parameterTypes = new HashMap<String, Object>();

  static {
    parameterTypes.put("java.util.List", new ArrayList());
  }

  private static Map<Class<?>, Object> primitiveTypes = new HashMap<Class<?>, Object>();

  static {
    primitiveTypes.put(Boolean.TYPE, false);
    primitiveTypes.put(Byte.TYPE, (byte) 0);
    primitiveTypes.put(Character.TYPE, ' ');
    primitiveTypes.put(Short.TYPE, (short) 0);
    primitiveTypes.put(Integer.TYPE, 0);
    primitiveTypes.put(Long.TYPE, 0l);
    primitiveTypes.put(Float.TYPE, 0f);
    primitiveTypes.put(Double.TYPE, 0d);
  }
}
