/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.delivery.inventory.plugin.provider;

import java.lang.reflect.Method;
import java.util.Map;

import com.solers.delivery.inventory.plugin.PluginException;
import com.solers.delivery.inventory.plugin.PluginExecutionException;

public final class InventoryParameter implements Parameter {
    private final String name;
    private final Class<?> type;
    private final boolean encrypted;
    private final boolean mandatory;
    private final String description;
    private final boolean allowToString;
    
    public InventoryParameter(String name, Class<?> type, boolean encrypted, boolean mandatory, boolean allowToString, String description) {
        this.name = name;
        this.type = type;
        this.encrypted = encrypted;
        this.mandatory = mandatory;
        this.description = description;
        this.allowToString = allowToString;
    }
    
    public InventoryParameter(String name, Class<?> type, boolean encrypted, boolean mandatory, String description) {
        this(name, type, encrypted, mandatory, true, description);
    }
    
    @Override
    public String description() {
        return description;
    }

    @Override
    public boolean encrypted() {
        return encrypted;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> type() {
        return type;
    }
    
    @Override
    public boolean mandatory() {
        return mandatory;
    }
    
    public Object verifyAndRetrieve(Map<String, ? extends Object> parameters) throws PluginException {
        if (parameters == null) {
            if (this.mandatory()) throw new PluginExecutionException(name() + " is mandatory");
            return null;
        }
        
        Object param = parameters.get(this.name());
        if (param != null) {
            if (this.type().isAssignableFrom(param.getClass())) {
                return param;
            } else {
                param = attemptParse(this.type(), param.toString());
                if (param == null)
                    throw new PluginExecutionException(name() + " is not of correct type, expected " + this.type().getName());
                return param;
            }
        } else {
            if (mandatory()) throw new PluginExecutionException(name() + " is mandatory");
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
    
    protected Object attemptParse(Class<?> target, String param) {
        if (target.isPrimitive())
            target = primitiveToWrapper(target);
        
        try {
            //Character requires special handling
            if (target.equals(Character.class)) {
                if (param.length() == 1) return param.charAt(0);
            } else if (allowToString && target.equals(String.class)) {
                return param;
            }

            //All other wrapper types have a valueOf method that works on a String
            Method m = target.getMethod("valueOf", String.class);
            return m.invoke(target, param);
        } catch (Exception e) {
            return null;
        }
    }
    
    protected Class<?> primitiveToWrapper(Class<?> primitiveType) {
        if (boolean.class.equals(primitiveType)) {
            return Boolean.class;
        } else if (float.class.equals(primitiveType)) {
            return Float.class;
        } else if (long.class.equals(primitiveType)) {
            return Long.class;
        } else if (int.class.equals(primitiveType)) {
            return Integer.class;
        } else if (short.class.equals(primitiveType)) {
            return Short.class;
        } else if (byte.class.equals(primitiveType)) {
            return Byte.class;
        } else if (double.class.equals(primitiveType)) {
            return Double.class;
        } else if (char.class.equals(primitiveType)) {
            return Character.class;
        }
        return null;
    }
}
