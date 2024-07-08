package kp.controllers;

import kp.models.YearAndAverageTemperature;
import kp.services.KpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.function.UnaryOperator;

import static kp.controllers.KpController.AVERAGE_TEMPERATURES_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * The tests for the controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
class KpControllerTest {

    private final MockMvc mockMvc;
    @MockBean
    private KpService kpService;
    private static final boolean VERBOSE = false;
    private static final int FIRST_YEAR = 1970;
    private static final double FIRST_AVERAGE = 1.1;
    private static final int LAST_YEAR = 2019;
    private static final double LAST_AVERAGE = 3.3;
    private final List<YearAndAverageTemperature> YEAR_AND_AVG_TEMPERATURE_LIST = List.of(
            new YearAndAverageTemperature(FIRST_YEAR, FIRST_AVERAGE),
            new YearAndAverageTemperature(1995, 2.2),
            new YearAndAverageTemperature(LAST_YEAR, LAST_AVERAGE)
    );
    private static final UnaryOperator<String> AVERAGE_TEMPERATURES_URL_FUN =
            city -> String.format("http://localhost/%s?city=%s",
                    AVERAGE_TEMPERATURES_PATH, city);
    private static final String CITY = "Chicago";
    private static final String UNKNOWN_CITY = "Unknown";

    /**
     * The constructor.
     *
     * @param mockMvc the {@link MockMvc}
     */
    KpControllerTest(@Autowired MockMvc mockMvc) {
        super();
        this.mockMvc = mockMvc;
    }

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        Mockito.when(kpService.process(CITY)).thenReturn(YEAR_AND_AVG_TEMPERATURE_LIST);
    }

    /**
     * Should get the average temperatures.
     *
     * @throws Exception the exception
     */
    @Test
    @DisplayName("🟩 should get the average temperatures")
    void shouldGetAverageTemperatures() throws Exception {
        // GIVEN
        final MockHttpServletRequestBuilder requestBuilder =
                get(AVERAGE_TEMPERATURES_URL_FUN.apply(CITY))
                        .accept(MediaType.APPLICATION_JSON_VALUE);
        // WHEN
        final ResultActions resultActions = mockMvc.perform(requestBuilder);
        // THEN
        if (VERBOSE) {
            resultActions.andDo(print());
        }
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        resultActions.andExpect(jsonPath("[0].year").value(FIRST_YEAR));
        resultActions.andExpect(jsonPath("[0].averageTemperature").value(FIRST_AVERAGE));
        resultActions.andExpect(jsonPath("[2].year").value(LAST_YEAR));
        resultActions.andExpect(jsonPath("[2].averageTemperature").value(LAST_AVERAGE));
    }

    /**
     * Should not get the average temperatures with unknown city.
     *
     * @throws Exception the exception
     */
    @Test
    @DisplayName("🟥 should not get the average temperatures with unknown city")
    void shouldNotGetAverageTemperaturesWithUnknownCity() throws Exception {
        // GIVEN
        final MockHttpServletRequestBuilder requestBuilder =
                get(AVERAGE_TEMPERATURES_URL_FUN.apply(UNKNOWN_CITY))
                        .accept(MediaType.APPLICATION_JSON_VALUE);
        // WHEN
        final ResultActions resultActions = mockMvc.perform(requestBuilder);
        // THEN
        if (VERBOSE) {
            resultActions.andDo(print());
        }
        resultActions.andExpect(status().isOk());
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
        resultActions.andExpect(jsonPath("$").isArray());
        resultActions.andExpect(jsonPath("$").isEmpty());
    }
}