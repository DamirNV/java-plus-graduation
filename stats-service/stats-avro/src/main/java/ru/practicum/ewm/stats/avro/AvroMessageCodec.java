package ru.practicum.ewm.stats.avro;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class AvroMessageCodec {

    private AvroMessageCodec() {
    }

    public static <T extends SpecificRecord> byte[] serialize(T record) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            SpecificDatumWriter<T> writer =
                    new SpecificDatumWriter<>(record.getSchema());

            BinaryEncoder encoder =
                    EncoderFactory.get().binaryEncoder(output, null);

            writer.write(record, encoder);
            encoder.flush();

            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to serialize Avro message",
                    exception
            );
        }
    }

    public static <T extends SpecificRecord> T deserialize(
            byte[] payload,
            Class<T> type
    ) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            SpecificDatumReader<T> reader =
                    new SpecificDatumReader<>(instance.getSchema());

            BinaryDecoder decoder =
                    DecoderFactory.get().binaryDecoder(payload, null);

            return reader.read(null, decoder);
        } catch (ReflectiveOperationException | IOException exception) {
            throw new IllegalStateException(
                    "Failed to deserialize Avro message",
                    exception
            );
        }
    }
}
