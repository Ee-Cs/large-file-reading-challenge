package kp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import kp.models.YearAndAverageTemperature;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;

/**
 * The utilities.
 */
@Slf4j
public class Utilities {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(SerializationFeature.INDENT_OUTPUT, true);
    private static FileTime previousFileTime = null;

    /**
     * Compares last modified time with previous time.
     *
     * @return the result
     */
    public static boolean compareLastModifiedTimeWithPrevious(Path path) {

        final BasicFileAttributes attributes;
        try {
            attributes = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            log.error("compareLastModifiedTimeWithPrevious(): IOException[{}]", e.getMessage());
            return false;
        }
        final FileTime fileTime = attributes.lastModifiedTime();
        if (previousFileTime != null && previousFileTime.compareTo(fileTime) == 0) {
            return true;
        }
        previousFileTime = fileTime;
        return false;
    }

    /**
     * Reports the results.
     *
     * @param averageMap                    the map with the average temperatures
     * @param yearAndAverageTemperatureList the year and average temperature list
     */
    public static void report(Map<String, Map<Integer, double[]>> averageMap, List<YearAndAverageTemperature> yearAndAverageTemperatureList) {

        final StringBuilder strBld = new StringBuilder();
        averageMap.forEach((cityKey, yearMap) -> yearMap.forEach((year, totalArr) ->
                strBld.append(String.format("city[%s], year[%d], average temperature[%.2f]%n",
                        cityKey, year, totalArr[1] / totalArr[0]))
        ));
        log.info("report():\n{}", strBld);
        log.info("report(): temperaturesResponse\n{}",
                Utilities.toPrettyJson(yearAndAverageTemperatureList));
    }

    /**
     * Creates JSON with indentation.
     *
     * @param yearAndAverageTemperatureList the year and average temperature list
     * @return the JSON with indentation.
     */
    private static String toPrettyJson(List<YearAndAverageTemperature> yearAndAverageTemperatureList) {

        try {
            return objectMapper.writeValueAsString(yearAndAverageTemperatureList);
        } catch (JsonProcessingException e) {
            log.error("toPrettyJson(): JsonProcessingException[{}]", e.getMessage());
            return "";
        }
    }
}