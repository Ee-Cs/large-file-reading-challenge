package kp;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The constants.
 */
public class Constants {
    // out of memory exception
   // public static final Path DATA_FILE = Path.of("data\\city_temperatures.csv");

  public static final Path DATA_FILE = Path.of("data_ten\\city_temperatures_Chicago.csv");
   //public static final Path DATA_FILE = Path.of("data_ten\\AAAAA.csv");


    public static final Pattern LINE_PATTERN = Pattern.compile(
            "^([^,\n]+),([^,\n]+),([^,\n]+)$",
            Pattern.MULTILINE);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final Function<String, OffsetDateTime> TO_DATE_TIME_FUN =
            str -> OffsetDateTime.of(LocalDateTime.parse(str, DATE_TIME_FORMATTER), ZoneOffset.UTC);
    public static final Function<OffsetDateTime, String> FROM_DATE_TIME_FUN =
            date -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    public static final Function<Double, String> FROM_TEMP_FUN = temp -> String.format("%.2f", temp);

    public static final String INSERT_SQL =
            "INSERT INTO data (city, date_time, temperature) VALUES (:city, :dateTime, :temperature)";
    public static final String ALL_DATA_SQL = "SELECT city, date_time, temperature FROM data";
    public static final String AVERAGE_TEMPERATURE_SQL =
            "SELECT YEAR(date_time) date_year, avg(temperature) average " +
            "FROM data WHERE city = ? GROUP BY city, date_year";



    /**
     * The constructor
     */
    private Constants() {
    }
}
