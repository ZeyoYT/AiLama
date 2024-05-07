package me.ailama.handler.annotations;

public @interface ResponseFormatter {

    String[] responseOrder();

    String[] responseVariables();

    String preFormattedResponse() default "";

    boolean isPreFormatted() default false;

    // if false, then direct response will be returned
    boolean isResponseOrder() default false;
}
