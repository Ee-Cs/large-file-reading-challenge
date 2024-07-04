package kp.services;

import kp.models.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.LongStream;

import static kp.Constants.*;

/**
 * The client service.
 */
@Slf4j
@Service
public class KpService {

    private final JdbcBatchItemWriter<Data> jdbcBatchItemWriter;
    private final JdbcClient jdbcClient;

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

        final byte[] destArr = new byte[Short.MAX_VALUE];
        try (FileChannel fileChannel = FileChannel.open(DATA_FILE, StandardOpenOption.READ)) {
            int position = 0;
            while(position < fileChannel.size()) {
                long regionSize = Math.min(Integer.MAX_VALUE, fileChannel.size() - position);
                log.info("process(): FileChannel::map,  position[{}], regionSize[{}]", position, regionSize);
                final MappedByteBuffer mappedByteBuffer =
                        fileChannel.map(FileChannel.MapMode.READ_ONLY, position, regionSize);
                int index = 0;
                boolean matchedFlag = true;
                while (matchedFlag) {
                    int destLength = (int)Math.min(Short.MAX_VALUE, fileChannel.size() - index);
                    //log.info("process(): MappedByteBuffer::get,  index[{}], destLength[{}]", index, destLength);
                    mappedByteBuffer.get(index, destArr, 0, destLength);
                    final String text = new String(destArr, StandardCharsets.UTF_8);
                    int lastIndex = text.lastIndexOf("\r\n");
                    if (lastIndex == -1) {
                        matchedFlag = false;
                        continue;
                    }
                    position = index;
                    index += lastIndex + 2;
                    matchedFlag = readLines(text.substring(0, lastIndex));
                }
            }
        } catch (Exception e) {
            log.error("process(): exception[{}]", e.getMessage());
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
     * Inserts data into the database.
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
     * Computes average temperatures.
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