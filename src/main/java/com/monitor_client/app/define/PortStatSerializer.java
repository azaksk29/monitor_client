package com.monitor_client.app.define;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.List;

public class PortStatSerializer extends JsonSerializer<List<PortStat>> {
    @Override
    public void serialize(List<PortStat> ports, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeFieldName("ports");
        gen.writeStartArray();
        for(PortStat port : ports) {
            gen.writeStartObject();
            gen.writeFieldName("port");
            gen.writeNumber(port.getPort());

            gen.writeFieldName("state");
            gen.writeString(port.getState().getState());
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}
