package me.ailama.handler.annotations;

import java.util.LinkedHashMap;

public @interface Parameter {
    String name();
    String Type();
    String description() default "";

    boolean required() default true;
}
