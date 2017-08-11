package com.dnastack.beacon.adater.variants.client.ga4gh.utils;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Andrey Mochalov (mochalovandrey@gmail.com)
 */
public class AvroConverter {

    public static String avroToJson(GenericContainer record) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), os);
        DatumWriter<GenericContainer> writer = new GenericDatumWriter<>();
        if (record instanceof SpecificRecord) {
            writer = new SpecificDatumWriter<>();
        }

        writer.setSchema(record.getSchema());
        writer.write(record, encoder);
        encoder.flush();
        String jsonString = new String(os.toByteArray(), Charset.forName("UTF-8"));
        os.close();
        return jsonString;
    }

    public static SpecificRecordBase jsonToAvro(String json, Schema schema) throws IOException {
        JsonDecoder decoder = DecoderFactory.get().jsonDecoder(schema, json);

        SpecificDatumReader<SpecificRecordBase> reader = new SpecificDatumReader<>();
        reader.setSchema(schema);

        return reader.read(null, decoder);
    }

}
