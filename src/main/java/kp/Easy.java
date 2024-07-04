package kp;

import lombok.extern.slf4j.Slf4j;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;

import static kp.Constants.DATA_FILE;
import static kp.Constants.LINE_PATTERN;

/**
 * The implementation of the simplified logic.
 * <p>
 * This alternative was created because
 * the logic, which uses the database, causes the out of memory error.
 * </p>
 */
@Slf4j
public class Easy {
    private static final Map<String, Map<Integer, double[]>> AVERAGE_MAP = new TreeMap<>();

    /**
     * Processes.
     *
     * @return the response
     */
    public String easyProcess() {

        final byte[] destArr = new byte[Short.MAX_VALUE];
        try (FileChannel fileChannel = FileChannel.open(DATA_FILE, StandardOpenOption.READ)) {
            log.info("easyProcess(): file channel size[{}]", fileChannel.size());
            long position = 0;
            while (position < fileChannel.size()) {
                long regionSize = Math.min(Integer.MAX_VALUE, fileChannel.size() - position);
                log.info("easyProcess(): position[{}], regionSize[{}]", position, regionSize);
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
                        log.error("easyProcess(): mappedByteBuffer exc[{}]", e.getMessage());
                        return "ERROR";
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
                    log.error("easyProcess(): BREAK# ZERO previousIndexInBuffer");
                    break;
                }
                position += previousIndexInBuffer;
                if (position >= fileChannel.size()) {
                    log.error("easyProcess(): BREAK# position[{}] >= file channel[{}]",
                            position, fileChannel.size());
                    break;
                }
            }
        } catch (Exception e) {
            log.error("easyProcess(): exception[{}]", e.getMessage());
            return "ERROR";
        }
        final StringBuilder strBld = new StringBuilder();
        AVERAGE_MAP.forEach((city, yearMap) -> yearMap.forEach((year, totalArr) ->
                strBld.append(String.format("city[%s], year[%d], average temperature[%.2f]%n",
                        city, year, totalArr[1] / totalArr[0]))
        ));
        log.info("easyProcess():\n{}", strBld);
        return "OK";
    }

    /**
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
                        System.out.println(matcher.group(0));
                        System.out.println(str);
                        System.out.println(e.getMessage());
                        System.exit(2);
                    }
                    return null;
                });
        if (cityOpt.isEmpty() || yearOpt.isEmpty() || temperatureOpt.isEmpty()) {
            return;
        }
        AVERAGE_MAP.putIfAbsent(cityOpt.get(), new TreeMap<>());
        final Map<Integer, double[]> cityMap = AVERAGE_MAP.get(cityOpt.get());
        cityMap.putIfAbsent(yearOpt.get(), new double[2]);
        final double[] totalArr = cityMap.get(yearOpt.get());
        totalArr[0]++;
        totalArr[1] = totalArr[1] + temperatureOpt.get();
    }

}
