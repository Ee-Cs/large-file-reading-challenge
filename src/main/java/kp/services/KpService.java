package kp.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The client service.
 */
@Slf4j
@Service
public class KpService {

    /**
     * The constructor.
     */
    public KpService() {
    }

    /**
     * Processes.
     *
     * @return the response
     */
    public String process() {

//        try {
//
//        } catch (IOException exception) {
//        }
        log.info("process():");
        return "OK";
    }

}
