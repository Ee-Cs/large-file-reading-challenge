package kp.services;

import kp.models.YearAndAverageTemperature;
import kp.tools.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * The service.
 */
@Slf4j
@Service
public class KpService {
    private static final boolean USE_FULL_FILE = true;
    private static final boolean PROLIX = false;

    private static final Path FULL_DATA_FILE = Path.of("data\\city_temperatures.csv");
    private static final Path ONE_CITY_DATA_FILE = Path.of("data_cities\\city_temperatures_New_York_City.csv");
    private static final Pattern LINE_PATTERN = Pattern.compile(
            "^([^,\n]+),([^,\n]+),([^,\n]+)$", Pattern.MULTILINE);
    /**
     * The map with the data read from the big file.
     */
    private static final Map<String, Map<Integer, double[]>> AVERAGE_MAP = new TreeMap<>();

    /**
     * The constructor
     */
    public KpService() {
    }

    /**
     * Processes.
     *
     * @param city the city
     * @return the response
     */
    public List<YearAndAverageTemperature> process(String city) {

        final Path path = USE_FULL_FILE ? FULL_DATA_FILE : ONE_CITY_DATA_FILE;
        if (Utilities.compareLastModifiedTimeWithPrevious(path)) {
            final List<YearAndAverageTemperature> yearAndAverageTemperatureList = getAveragesList(city);
            log.info("process(): data file unchanged, city[{}]", city);
            return yearAndAverageTemperatureList;
        }
        if (!readFile(path)) {
            log.error("process(): file reading failed");
            return List.of();
        }
        final List<YearAndAverageTemperature> yearAndAverageTemperatureList = getAveragesList(city);
        log.info("process(): city[{}]", city);
        return yearAndAverageTemperatureList;
    }

    /**
     * Reads the file.
     *
     * @param path the file path
     */
    private boolean readFile(Path path) {

        final byte[] destArr = new byte[Short.MAX_VALUE];
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            log.info("readFile(): path[{}], file channel size[{}]", path, fileChannel.size());
            long position = 0;
            while (position < fileChannel.size()) {
                long regionSize = Math.min(Integer.MAX_VALUE, fileChannel.size() - position);
                log.info("readFile(): position[{}], regionSize[{}]", position, regionSize);
                final MappedByteBuffer mappedByteBuffer =
                        fileChannel.map(FileChannel.MapMode.READ_ONLY, position, regionSize);
                long index = 0;
                long previousIndexInBuffer = 0;
                boolean matchedFlag = true;
                while (matchedFlag) {
                    Arrays.fill(destArr, (byte) 0);
                    int destLength = (int) Math.min(Math.min(Short.MAX_VALUE, regionSize), fileChannel.size() - index);
                    try {
                        mappedByteBuffer.get((int) index, destArr, 0, destLength);
                    } catch (Exception e) {
                        log.error("readFile(): path[{}], mappedByteBuffer exception[{}]",
                                path, e.getMessage());
                        return false;
                    }
                    previousIndexInBuffer = index;
                    final String text = new String(destArr, StandardCharsets.UTF_8);
                    int lastIndexInText = text.lastIndexOf("\r\n");
                    if (lastIndexInText == -1) {
                        matchedFlag = false;
                        continue;
                    }
                    matchedFlag = readLines(text.substring(0, lastIndexInText));
                    index += lastIndexInText + 2;
                    if (index >= regionSize || index + destLength >= regionSize) {
                        matchedFlag = false;
                    }
                }
                if (previousIndexInBuffer == 0) {
                    log.debug("readFile(): break on zero previousIndexInBuffer");
                    break;
                }
                position += previousIndexInBuffer;
                if (position >= fileChannel.size()) {
                    log.debug("readFile(): break on position[{}] >= file channel[{}]",
                            position, fileChannel.size());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("readFile(): path[{}], exception[{}]", path, e.getMessage());
            return false;
        }
        log.info("readFile(): OK");
        return true;
    }

    /**
     * Reads the text lines.
     *
     * @param text the text
     * @return the matched flag
     */
    private boolean readLines(String text) {

        final Matcher matcher = LINE_PATTERN.matcher(text);
        boolean matchedFlag = false;
        while (matcher.find()) {
            matchedFlag = true;
            readMatchedLine(matcher);
        }
        return matchedFlag;
    }

    /**
     * Reads the matched line.
     *
     * @param matcher the matcher
     */
    private void readMatchedLine(Matcher matcher) {

        final Optional<String> cityOpt = Optional.ofNullable(matcher.group(1));
        final Optional<Integer> yearOpt = Optional.ofNullable(matcher.group(2))
                .map(str -> str.substring(0, 4)).map(Integer::valueOf);
        final Optional<Double> temperatureOpt = Optional.ofNullable(matcher.group(3))
                .map(str -> {
                    try {
                        return Double.parseDouble(str);
                    } catch (Exception e) {
                        log.error("readMatchedLine(): entire pattern[{}], avg temp[{}], exception[{}]",
                                matcher.group(0), str, e.getMessage());
                        return null;
                    }
                });
        if (cityOpt.isEmpty() || yearOpt.isEmpty() || temperatureOpt.isEmpty()) {
            log.warn("readMatchedLine(): something was not matched, " +
                     "city empty[{}], year empty[{}], avg temp empty[{}]",
                    cityOpt.isEmpty(), yearOpt.isEmpty(), temperatureOpt.isEmpty());
            return;
        }
        computeAverages(cityOpt.get(), yearOpt.get(), temperatureOpt.get());
    }

    /**
     * Gets the averages list.
     *
     * @param city the city
     * @return the list
     */
    private List<YearAndAverageTemperature> getAveragesList(String city) {

        final List<YearAndAverageTemperature> yearAndAverageTemperatureList =
                AVERAGE_MAP.entrySet().stream()
                        .filter(cityEntry -> city.equals(cityEntry.getKey()))
                        .map(Map.Entry::getValue)
                        .flatMap(yearMap -> yearMap.keySet().stream().map(
                                year -> new YearAndAverageTemperature(year,
                                        yearMap.get(year)[1] / yearMap.get(year)[0])))
                        .toList();
        if (PROLIX) {
            Utilities.report(AVERAGE_MAP, yearAndAverageTemperatureList);
        }
        return yearAndAverageTemperatureList;
    }

    /**
     * Reads the matched line.
     *
     * @param city        the city
     * @param year        the year
     * @param temperature the temperature
     */
    private void computeAverages(String city, Integer year, Double temperature) {

        AVERAGE_MAP.putIfAbsent(city, new TreeMap<>());
        final Map<Integer, double[]> cityMap = AVERAGE_MAP.get(city);
        cityMap.putIfAbsent(year, new double[2]);
        final double[] totalArr = cityMap.get(year);
        totalArr[0]++;
        totalArr[1] = totalArr[1] + temperature;
    }

}
