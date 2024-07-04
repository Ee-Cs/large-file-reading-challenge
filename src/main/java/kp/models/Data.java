package kp.models;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.Matcher;

import static kp.Constants.TO_DATE_TIME_FUN;

/**
 * The data record used with database.
 *
 * @param city        the city
 * @param dateTime    the date and time
 * @param temperature the temperature
 */
public record Data(String city, OffsetDateTime dateTime, Double temperature) {
    /**
     * Creates the data record from matcher subsequences.
     *
     * @param matcher the {@link Matcher}
     * @return the data
     */
    public static Optional<Data> of(Matcher matcher) {

        final Optional<String> cityOpt = Optional.ofNullable(matcher.group(1));
        final Optional<OffsetDateTime> dateTimeOpt = Optional.ofNullable(matcher.group(2)).map(TO_DATE_TIME_FUN);
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
        if (cityOpt.isEmpty() || dateTimeOpt.isEmpty() || temperatureOpt.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Data(cityOpt.get(), dateTimeOpt.get(), temperatureOpt.get()));
    }
}