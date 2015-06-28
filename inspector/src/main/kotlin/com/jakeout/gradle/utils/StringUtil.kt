package com.jakeout.gradle.utils

import com.google.common.base.Function
import com.google.common.base.Strings

public object StringUtil {

    val TAB_CHARACTER: String = "  "

    fun tabAppender(numTabs: Int): Function<String, String> {
        return object : Function<String, String> {
            override fun apply(s: String?): String? {
                return Strings.repeat(TAB_CHARACTER, numTabs) + s!!.replace("\n", "\n" + Strings.repeat(TAB_CHARACTER, numTabs))
            }
        }
    }
}
