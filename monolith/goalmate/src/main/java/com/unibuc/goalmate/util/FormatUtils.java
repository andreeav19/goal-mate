package com.unibuc.goalmate.util;

import org.springframework.stereotype.Component;

@Component("formatUtils")
public class FormatUtils {

    public String formatSmartDecimal(Float value) {
        if (value == null) return "";
        return value % 1 == 0 ? String.format("%.0f", value) : String.format("%.2f", value);
    }
}
