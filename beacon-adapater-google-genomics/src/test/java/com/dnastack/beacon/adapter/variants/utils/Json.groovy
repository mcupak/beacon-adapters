package com.dnastack.beacon.adapter.variants.utils

import com.fasterxml.jackson.dataformat.avro.AvroMapper
import com.fasterxml.jackson.dataformat.avro.AvroSchema
import org.apache.avro.Schema

/**
 * Json helper that is able to correctly convert protobuf objects to json.
 *
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
public class Json {

    public static String toJson(Object src, Schema schema) {
        return new String(AvroMapper.newInstance()
                .writer(new AvroSchema(schema))
                .writeValueAsBytes(src))
    }

}