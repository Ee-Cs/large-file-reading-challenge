package primordial;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Characteristic of the earliest stage of the development.
 * <p>
 * "copy *.csv ..\data\city_temperatures.csv"
 * </p>
 */
public class Primordial {
    private static final Function<Integer, String> DATE_FUN = arg ->
            Instant.ofEpochMilli(1_800_000L * arg).atZone(ZoneId.systemDefault())
                    .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    private static final Random RANDOM = new Random();
    private static final double RANDOM_NUMBER_ORIGIN = -50d;
    private static final double RANDOM_NUMBER_BOUND = 50d;
    private static final Function<String, Path> DATA_FILE_CITY =
            city -> Path.of(String.format(".\\data_cities\\city_temperatures_%s.csv",
                    city.replace(" ", "_")
                            .replace("/", "_")));

    /**
     * The hidden constructor.
     */
    private Primordial() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        for (String city : Cities.list) {
            System.out.println(DATA_FILE_CITY.apply(city));
            final List<String> list = createData(city);
            writeDataFile(city, list);
        }
    }

    /**
     * Creates the data (too much data -it creates 300GB of files).
     *
     * @return the list
     */
    private static List<String> createData(String city) {

        final List<String> list = IntStream.rangeClosed(0, 876_573)
                .boxed().map(DATE_FUN)
                .map(date -> String.format("%s,%s,%.2f", city, date,
                        RANDOM.nextDouble(RANDOM_NUMBER_ORIGIN, RANDOM_NUMBER_BOUND)))
                .toList();
        System.out.printf("list size[%d]%n", list.size());
        System.out.printf("%s%n%s%n", list.getFirst(), list.getLast());
        return list;
    }

    /**
     * Writes the data file.
     *
     * @param city the city
     * @param list the list
     */
    private static void writeDataFile(String city, List<String> list) {

        try (BufferedWriter bufferedWriter =
                     Files.newBufferedWriter(DATA_FILE_CITY.apply(city))) {
            for (String line : list) {
                bufferedWriter.write(line.concat(System.lineSeparator()));
            }
        } catch (IOException e) {
            System.out.printf("writeDataFile(): IOException[%s]%n", e.getMessage());
        }

    }
}
