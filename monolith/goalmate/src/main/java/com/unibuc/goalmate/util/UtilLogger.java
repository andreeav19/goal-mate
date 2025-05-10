package com.unibuc.goalmate.util;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindingResult;

@RequiredArgsConstructor
public class UtilLogger {
    private static final Logger logger = LoggerFactory.getLogger(UtilLogger.class);

    public static void logBindingResultErrors(BindingResult bindingResult, String customMessage) {
        logger.warn(customMessage);
        bindingResult.getAllErrors().forEach(error ->
                logger.warn(" - {}", error.getDefaultMessage())
        );
    }

    public static void logInfoMessage(String customMessage) {
        logger.info(customMessage);
    }
}
