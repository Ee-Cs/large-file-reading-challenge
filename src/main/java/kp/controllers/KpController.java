package kp.controllers;

import kp.services.KpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The client controller.
 *
 */
@Slf4j
@RestController
@RequestMapping
public class KpController {

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
     * Handles the request.
     *
     * @return the result
     */
    @GetMapping(value = "/large")
    public String handle() {

        final String result = kpService.process();
        log.info("handle():");
        return result;
    }

}
