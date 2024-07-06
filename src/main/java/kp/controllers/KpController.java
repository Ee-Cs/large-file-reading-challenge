package kp.controllers;

import kp.models.YearAndAverageTemperature;
import kp.services.KpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The controller.
 */
@Slf4j
@RestController
@RequestMapping
public class KpController {

    public static final String AVERAGE_TEMPERATURES_PATH = "/average_temperatures";
    private final KpService kpService;

    /**
     * The constructor.
     *
     * @param kpService the {@link KpService}
     */
    public KpController(@Autowired KpService kpService) {
        this.kpService = kpService;
    }

    /**
     * Gets the average temperatures.
     *
     * @param city the city
     * @return the response with average temperatures
     */
    @GetMapping(value = AVERAGE_TEMPERATURES_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<YearAndAverageTemperature> getAverageTemperatures(String city) {

        Optional<String> cityOpt = Optional.ofNullable(city)
                .filter(Predicate.not(String::isBlank));
        if (cityOpt.isEmpty()) {
            log.info("getAverageTemperatures(): city parameter is null or empty");
            return new ArrayList<>();
        }
        final List<YearAndAverageTemperature> yearAndAverageTemperatureList =
                kpService.process(cityOpt.get());
        log.info("getAverageTemperatures(): city[{}], list size[{}]",
                cityOpt.get(), yearAndAverageTemperatureList.size());
        return yearAndAverageTemperatureList;
    }

}
