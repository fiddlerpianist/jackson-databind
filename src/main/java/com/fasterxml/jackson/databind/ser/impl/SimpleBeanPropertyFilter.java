package com.fasterxml.jackson.databind.ser.impl;

import java.util.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.ser.*;

/**
 * Simple {@link PropertyFilter} implementation that only uses property name
 * to determine whether to serialize property as is, or to filter it out.
 *<p>
 * Use of this class as the base implementation for any custom
 * {@link PropertyFilter} implementations is strongly encouraged,
 * because it can provide default implementation for any methods that may
 * be added in {@link PropertyFilter} (as unfortunate as additions may be).
 */
@SuppressWarnings("deprecation")
public abstract class SimpleBeanPropertyFilter
    implements BeanPropertyFilter, PropertyFilter
        // sub-classes must also implement java.io.Serializable
{
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    protected SimpleBeanPropertyFilter() { }

    /**
     * Factory method to construct filter that filters out all properties <b>except</b>
     * ones includes in set
     */
    public static SimpleBeanPropertyFilter filterOutAllExcept(Set<String> properties) {
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter filterOutAllExcept(String... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new FilterExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter serializeAllExcept(Set<String> properties) {
        return new SerializeExceptFilter(properties);
    }

    public static SimpleBeanPropertyFilter serializeAllExcept(String... propertyArray) {
        HashSet<String> properties = new HashSet<String>(propertyArray.length);
        Collections.addAll(properties, propertyArray);
        return new SerializeExceptFilter(properties);
    }

    /**
     * Helper method to ease transition from {@link BeanPropertyWriter} into
     * {@link PropertyWriter}
     * 
     * @since 2.3
     */
    public static PropertyFilter from(final BeanPropertyFilter src)
    {
        return new PropertyFilter() {
            @Override
            public void serializeAsField(Object pojo, JsonGenerator jgen,
                    SerializerProvider prov, PropertyWriter writer)
                throws Exception {
                src.serializeAsField(pojo, jgen, prov, (BeanPropertyWriter) writer);
            }

            @Deprecated
            @Override
            public void depositSchemaProperty(PropertyWriter writer,
                    ObjectNode propertiesNode, SerializerProvider provider)
                throws JsonMappingException {
                src.depositSchemaProperty((BeanPropertyWriter) writer, propertiesNode, provider);
            }

            @Override
            public void depositSchemaProperty(PropertyWriter writer,
                    JsonObjectFormatVisitor objectVisitor,
                SerializerProvider provider) throws JsonMappingException {
                src.depositSchemaProperty((BeanPropertyWriter) writer, objectVisitor, provider);
            }
            
        };
    }

    /*
    /**********************************************************
    /* Methods for sub-classes
    /**********************************************************
     */

    /**
     * Method called to determine whether property will be included
     * (if 'true' returned) or filtered out (if 'false' returned)
     */
    protected abstract boolean include(BeanPropertyWriter writer);

    /**
     * @since 2.3
     */
    protected abstract boolean include(PropertyWriter writer);

    /*
    /**********************************************************
    /* BeanPropertyFilter implementation
    /**********************************************************
     */
    
    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen,
            SerializerProvider provider, BeanPropertyWriter writer) throws Exception
    {
        if (include(writer)) {
            writer.serializeAsField(bean, jgen, provider);
        } else if (!jgen.canOmitFields()) { // since 2.3
            writer.serializeAsOmittedField(bean, jgen, provider);
        }
    }

    @Override
    public void depositSchemaProperty(BeanPropertyWriter writer,
            ObjectNode propertiesNode, SerializerProvider provider)
        throws JsonMappingException
    {
        if (include(writer)) {
            writer.depositSchemaProperty(propertiesNode, provider);
        }
    }

    @Override
    public void depositSchemaProperty(BeanPropertyWriter writer,
            JsonObjectFormatVisitor objectVisitor, SerializerProvider provider)
        throws JsonMappingException
    {
        if (include(writer)) {
            writer.depositSchemaProperty(objectVisitor);
        }
    }

    /*
    /**********************************************************
    /* PropertyFilter implementation
    /**********************************************************
     */
    
    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen,
            SerializerProvider provider, PropertyWriter writer)
        throws Exception
    {
        if (include(writer)) {
            writer.serializeAsField(pojo, jgen, provider);
        } else if (!jgen.canOmitFields()) { // since 2.3
            writer.serializeAsOmittedField(pojo, jgen, provider);
        }
    }

    @Deprecated
    @Override
    public void depositSchemaProperty(PropertyWriter writer,
            ObjectNode propertiesNode, SerializerProvider provider)
            throws JsonMappingException
    {
        if (include(writer)) {
            writer.depositSchemaProperty(propertiesNode, provider);
        }
    }

    @Override
    public void depositSchemaProperty(PropertyWriter writer,
            JsonObjectFormatVisitor objectVisitor,
            SerializerProvider provider) throws JsonMappingException 
    {
        if (include(writer)) {
            writer.depositSchemaProperty(objectVisitor);
        }
    }

    /*
    /**********************************************************
    /* Sub-classes
    /**********************************************************
     */

    /**
     * Filter implementation which defaults to filtering out unknown
     * properties and only serializes ones explicitly listed.
     */
    public static class FilterExceptFilter
        extends SimpleBeanPropertyFilter
        implements java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         * Set of property names to serialize.
         */
        protected final Set<String> _propertiesToInclude;

        public FilterExceptFilter(Set<String> properties) {
            _propertiesToInclude = properties;
        }

        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return _propertiesToInclude.contains(writer.getName());
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            return _propertiesToInclude.contains(writer.getName());
        }
    }

    /**
     * Filter implementation which defaults to serializing all
     * properties, except for ones explicitly listed to be filtered out.
     */
    public static class SerializeExceptFilter
        extends SimpleBeanPropertyFilter
    {
        /**
         * Set of property names to filter out.
         */
        protected final Set<String> _propertiesToExclude;

        public SerializeExceptFilter(Set<String> properties) {
            _propertiesToExclude = properties;
        }

        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return !_propertiesToExclude.contains(writer.getName());
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            return !_propertiesToExclude.contains(writer.getName());
        }
    }
}