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
 */
public class Primordial {
    private static final Function<Integer, String> DATE_FUN = arg ->
            Instant.ofEpochMilli(180_000L * arg).atZone(ZoneId.systemDefault())
                    .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    private static final Random RANDOM = new Random();
    private static final double RANDOM_NUMBER_ORIGIN = -50d;
    private static final double RANDOM_NUMBER_BOUND = 50d;
    private static final Function<String, Path> DATA_FILE_CITY =
            city -> Path.of(String.format(".\\data\\city_temperatures_%s.csv",
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
            //final List<String> list = createData(city);
            //writeDataFile(city, list);
        }
    }

    /**
     * Creates the data (too much data -it creates 300GB of files).
     *
     * @return the list
     */
    private static List<String> createData(String city) {

        final List<String> list = IntStream.rangeClosed(0, 8_765_760)
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
/*
Calculations.

every 3 minutes one point, total points  8`765`760

one line 38 bytes

3GB it is:  3 * 1024 * 1024 * 1024 = 3`221`225`472
total 1000 cities lines: 84`769`091
one city of 1000  lines: 8`476`909
one point every 3 min:   8`765`760 <--- OK it is bigger

Class "Cities" has 1000 cities together but only 925 unique cities.
Created were 925 Files, total bytes: 335`742`725`454 = 313 GB
From all that were selected only 10 files (because it was too much).

Data file 'city_temperatures.csv'
10 US cities
bytes: 3,585,211,533 = 3.33 GB
*/
