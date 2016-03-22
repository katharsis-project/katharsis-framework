package io.katharsis.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import java.util.Set;

public class KatharsisFieldPropertyFilter extends SimpleBeanPropertyFilter {

    private final Set<String> properties;
    private final Object resource;

    public KatharsisFieldPropertyFilter(Set<String> properties, Object resource) {
        this.properties = properties;
        this.resource = resource;
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider provider, BeanPropertyWriter writer)
        throws Exception {
        if (include(bean, writer)) {
            super.serializeAsField(bean, jgen, provider, writer);
        }
    }

    @Override
    public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer)
        throws Exception {
        if (include(pojo, writer)) {
            super.serializeAsField(pojo, jgen, provider, writer);
        }
    }

    private boolean include(Object bean, PropertyWriter writer) {
        return bean != resource || properties.contains(writer.getName());
    }
}
