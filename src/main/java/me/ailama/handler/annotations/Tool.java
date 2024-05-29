package me.ailama.handler.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Tool {
    String name();
    String description();

    Parameter[] parameters() default {};

    boolean rawResponse() default false;

    ResponseFormatter responseFormatter() default @ResponseFormatter(responseOrder = {}, responseVariables = {}, preFormattedResponse = "", isPreFormatted = false, isResponseOrder = false);
}
