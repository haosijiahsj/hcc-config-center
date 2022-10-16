package com.hcc.config.center.server.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson2ObjectCustomizer
 *
 * @author shengjun.hu
 * @date 2022/9/27
 */
@Configuration
public class Jackson2ObjectCustomizer {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
//            builder.serializerByType(BaseEnum.class, new JsonSerializer<BaseEnum>() {
//                @Override
//                public void serialize(BaseEnum baseEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
//                    JsonStreamContext outputContext = jsonGenerator.getOutputContext();
//                    String currentName = outputContext.getCurrentName();
//                    Object currentValue = outputContext.getCurrentValue();
//                    try {
//                        Method getDescMethod = currentValue.getClass().getMethod("getDesc");
//                        Object value = getDescMethod.invoke(currentValue);
//                        jsonGenerator.writeStringField(currentName + "Desc", value.toString());
//                    } catch (Exception e) {}
//                    if (baseEnum instanceof Enum) {
//                        jsonGenerator.writeString(((Enum)baseEnum).name());
//                    }
//                }
//            });
            builder.serializerByType(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                    if (localDateTime != null) {
                        jsonGenerator.writeString(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(localDateTime));
                    }
                }
            });
        };
    }

}
