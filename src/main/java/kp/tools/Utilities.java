package kp.tools;

//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Optional;

/**
 * The utilities.
 */
@Slf4j
public class Utilities {
//    private static final ObjectMapper objectMapper = new ObjectMapper()
//            .configure(SerializationFeature.INDENT_OUTPUT, true);
    /**
     * Absolutely do not use this solution on production!
     */
    private static final File DEVELOPER_SAMPLE_FILE = null;

    /**
     * Creates temporary sample file for upload.
     *
     * @return the optional with the sample file
     */
    public static Optional<File> createSampleFile() {

/*
        final Optional<MultipartFile> multipartFileOpt =
                Optional.ofNullable(audioRequest).map(AudioRequest::getFile);
        if (multipartFileOpt.isEmpty()) {
            DEVELOPER_SAMPLE_FILE = null;
            log.warn("createSampleFile(): file is absent");
            return Optional.empty();
        }
        try {
            byte[] contents = multipartFileOpt.get().getBytes();
            final Path path = Files.createTempFile(
                    Path.of(System.getProperty(TMP_DIR_KEY)), PREFIX, SUFFIX);
            try (OutputStream outputStream = Files.newOutputStream(path)) {
                outputStream.write(contents);
            }
            DEVELOPER_SAMPLE_FILE = path.toFile();
            return Optional.of(path.toFile());
        } catch (IOException e) {
            log.error("createSampleFile(): JsonProcessingException[{}]", e.getMessage());
            return Optional.empty();
        }
		*/
        return null;
    }

    /**
     * Creates JSON with indentation.
     *
     * @param messagesList the list with {@link Message}
     * @return the JSON with indentation.
     */
	 /*
    public static String toPrettyJsonMessages(List<Message> messagesList) {

        try {
            return objectMapper.writeValueAsString(messagesList);
        } catch (JsonProcessingException e) {
            log.error("toPrettyJsonMessages(): JsonProcessingException[{}]", e.getMessage());
            return "";
        }
    }
	*/

}