package com.dnastack.beacon.adater.variants.client.ga4gh.retro;

import com.dnastack.beacon.adater.variants.client.ga4gh.utils.AvroConverter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.avro.specific.SpecificRecordBase;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A simple converter Json <-> protobuf DTOs.
 *
 * @author Artem (tema.voskoboynick@gmail.com)
 * @author Miro Cupak (mirocupak@gmail.com)
 * @version 1.0
 */
public class AvroJsonConverter extends Converter.Factory {

    public static AvroJsonConverter create() {
        return new AvroJsonConverter();
    }

    private boolean isConvertible(Type type) {
        return getConvertibleClass(type) != null;
    }

    private Class<?> getConvertibleClass(Type type) {
        if (!(type instanceof Class)) {
            return null;
        }

        Class<?> clazz = (Class) type;
        if (!(SpecificRecordBase.class.isAssignableFrom(clazz))) {
            return null;
        }

        return clazz;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        Class<?> clazz = getConvertibleClass(type);
        if (clazz == null) {
            return null;
        }

        return new Converter<ResponseBody, Object>() {

            private SpecificRecordBase createObject(Class<?> clazz) {
                try {
                    return  (SpecificRecordBase) clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Couldn't create object", e);
                }
            }

            @Override
            public Object convert(ResponseBody responseBody) throws IOException {
                String json = responseBody.string();

                SpecificRecordBase specificRecordBase = createObject(clazz);

                return AvroConverter.jsonToAvro(json, specificRecordBase.getSchema());
            }
        };
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        if (!isConvertible(type)) {
            return null;
        }

        return o -> {
            SpecificRecordBase message = (SpecificRecordBase) o;
            String json = AvroConverter.avroToJson(message);

            return RequestBody.create(MediaType.parse("application/json"), json);
        };
    }

    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return super.stringConverter(type, annotations, retrofit);
    }
}
