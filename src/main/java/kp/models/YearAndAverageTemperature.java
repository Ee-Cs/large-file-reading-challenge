package kp.models;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import kp.tools.serializers.YearAndAverageTemperatureSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * The year and the average temperature.
 */
@Data
@AllArgsConstructor
@JsonSerialize(using = YearAndAverageTemperatureSerializer.class)
public class YearAndAverageTemperature implements Serializable {
    private Integer year;
    private Double averageTemperature;
}
