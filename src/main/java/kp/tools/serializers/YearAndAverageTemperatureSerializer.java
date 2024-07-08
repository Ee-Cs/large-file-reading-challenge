package kp.tools.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import kp.models.YearAndAverageTemperature;

import java.io.IOException;

/**
 * The custom serializer for the {@link YearAndAverageTemperature}
 */
public class YearAndAverageTemperatureSerializer extends StdSerializer<YearAndAverageTemperature> {
    /**
     * The constructor.
     */
    public YearAndAverageTemperatureSerializer() {
        this(null);
    }

    /**
     * The constructor.
     *
     * @param yearAndAverageTemperatureClass the {@link YearAndAverageTemperature} class
     */
    public YearAndAverageTemperatureSerializer(
            Class<YearAndAverageTemperature> yearAndAverageTemperatureClass) {
        super(yearAndAverageTemperatureClass);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(YearAndAverageTemperature yearAndAverageTemperature,
                          JsonGenerator jsonGenerator,
                          SerializerProvider provider) throws IOException {

        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("year",
                String.valueOf(yearAndAverageTemperature.getYear()));
        jsonGenerator.writeStringField("averageTemperature",
                String.format("%.2f", yearAndAverageTemperature.getAverageTemperature()));
        jsonGenerator.writeEndObject();
    }
}