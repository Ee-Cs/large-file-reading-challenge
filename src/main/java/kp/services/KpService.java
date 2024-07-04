package kp.services;

import kp.Easy;
import kp.models.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;

import static kp.Constants.*;

/**
 * The service.
 *
 */
@Slf4j
@Service
public class KpService {

    private final JdbcBatchItemWriter<Data> jdbcBatchItemWriter;
    private final JdbcClient jdbcClient;

    // for test run with big 3 GB file
    private static final boolean SELECTED_EASY_LOGIC = !false;

    /**
     * The constructor.
     *
     * @param jdbcBatchItemWriter thr writer
     * @param jdbcClient          the JDBC client
     */
    public KpService(JdbcBatchItemWriter<Data> jdbcBatchItemWriter, JdbcClient jdbcClient) {
        this.jdbcBatchItemWriter = jdbcBatchItemWriter;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Processes.
     *
     * @return the response
     */
    public String process() {

        if (SELECTED_EASY_LOGIC) {
            return new Easy().easyProcess();
        }
        final byte[] destArr = new byte[Short.MAX_VALUE];
        try (FileChannel fileChannel = FileChannel.open(DATA_FILE, StandardOpenOption.READ)) {
            long position = 0;
            while (position < fileChannel.size()) {
                long regionSize = Math.min(Integer.MAX_VALUE, fileChannel.size() - position);
                log.info("process(): FileChannel::map,  position[{}], regionSize[{}]", position, regionSize);
                final MappedByteBuffer mappedByteBuffer =
                        fileChannel.map(FileChannel.MapMode.READ_ONLY, position, regionSize);
                long index = 0;
                long previousIndexInBuffer = 0;
                boolean matchedFlag = true;
                while (matchedFlag) {
                    Arrays.fill(destArr, (byte) 0);
                    int destLength = (int) Math.min(Math.min(Short.MAX_VALUE, regionSize), fileChannel.size() - index);
                    mappedByteBuffer.get((int) index, destArr, 0, destLength);
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
                    break;
                }
                position += previousIndexInBuffer;
                if (position >= fileChannel.size()) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("process(): exception[{}]", e.getMessage());
            return "ERROR";
        }
        String city = "Chicago";
        final Map<Integer, Double> avgTempMap = computeAverageTemperatures(city);
        avgTempMap.forEach((key, value) -> log.info(
                "process(): year[{}], average temperature[{}]",
                key, FROM_TEMP_FUN.apply(value)));
        //selectData();
        log.info("process():");
        return "OK";
    }

    /**
     * Reads the text lines.
     *
     * @param text the text
     * @return the matched flag
     */
    private boolean readLines(String text) {

        final Matcher matcher = LINE_PATTERN.matcher(text);
        final List<Data> dataList = new ArrayList<>();
        boolean matchedFlag = false;
        while (matcher.find()) {
            matchedFlag = true;
            Optional<Data> dataOpt = Data.of(matcher);
            dataOpt.ifPresent(dataList::add);
        }
        insertData(dataList);
        return matchedFlag;
    }


    /**
     * Inserts the data into the database.
     *
     * @param dataList the data list
     */
    public void insertData(List<Data> dataList) {

        final Chunk<Data> chunk = new Chunk<>();
        dataList.forEach(chunk::add);
        try {
            jdbcBatchItemWriter.write(chunk);
        } catch (Exception e) {
            log.error("insertData(): exception[{}]", e.getMessage());
        }
    }

    /**
     * Computes the average temperatures.
     *
     * @param city the city
     * @return the map
     */
    private Map<Integer, Double> computeAverageTemperatures(String city) {

        return jdbcClient.sql(AVERAGE_TEMPERATURE_SQL)
                .param(city)
                .query((ResultSet resultSet) -> {
                    final Map<Integer, Double> map = new TreeMap<>();
                    while (resultSet.next()) {
                        map.put(resultSet.getInt("date_year"),
                                resultSet.getDouble("average"));
                    }
                    return map;
                });
    }

    /**
     * Selects the data from the database.
     *
     */
    private void selectData() {

        // yearly average temperatures for a given city
        // array of objects with the following fields:
        // year, averageTemperature
        final List<Data> dataList = jdbcClient.sql(ALL_DATA_SQL).query(Data.class).list();
        log.info("""
                        selectData(): data list size[{}],
                        first city[{}], dateTime[{}], temperature[{}]
                        last  city[{}], dateTime[{}], temperature[{}]
                        """,
                dataList.size(),
                dataList.getFirst().city(),
                FROM_DATE_TIME_FUN.apply(dataList.getFirst().dateTime()),
                FROM_TEMP_FUN.apply(dataList.getFirst().temperature()),
                dataList.getLast().city(),
                FROM_DATE_TIME_FUN.apply(dataList.getLast().dateTime()),
                FROM_TEMP_FUN.apply(dataList.getLast().temperature()));
    }
}