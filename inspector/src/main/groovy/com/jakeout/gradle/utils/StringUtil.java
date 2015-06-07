package com.jakeout.gradle.utils;

import com.google.common.base.Function;
import com.google.common.base.Strings;

public class StringUtil {
    public static final String TAB_CHARACTER = "  ";

    public static Function<String, String> tabAppender(final int numTabs) {
        return new Function<String, String>() {
            public String apply(String s) {
                return Strings.repeat(TAB_CHARACTER, numTabs) + s.replace("\n", "\n" + Strings.repeat(TAB_CHARACTER, numTabs));
            }
        };
    }
}
