package cheetah.example.serialization;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.flink.api.common.serialization.SerializationSchema;
import cheetah.example.model.avrorecord.EventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AvroKeySerializationSchema implements SerializationSchema<EventAvro> {

    private transient DatumWriter<GenericRecord> writer;
    private transient BinaryEncoder encoder;
    private transient ByteArrayOutputStream stream;

    // Avro schema for a string key
    private static final Schema SCHEMA = Schema.create(Schema.Type.STRING);

    @Override
    public void open(InitializationContext context) {
        // Initialize resources
        writer = new SpecificDatumWriter<>(SCHEMA);
        stream = new ByteArrayOutputStream();
    }

    @Override
    public byte[] serialize(EventAvro object) {
        // Reset the stream for each serialization
        stream.reset();

        // Create a GenericRecord with the string key
        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("key", "/tmp/data/ready/measuredata/2022/03/06/TelemetryData_20220306230857077.ktd#4869");

        // Serialize the GenericRecord to byte array
        encoder = EncoderFactory.get().binaryEncoder(stream, encoder);
        try {
            writer.write(record, encoder);
            encoder.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error serializing Avro string key", e);
        }

        return stream.toByteArray();
    }
}