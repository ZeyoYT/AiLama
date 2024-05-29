package me.ailama.handler.annotations;

public @interface Parameter {
    String name();
    String Type();
    String description() default "";

    boolean required() default true;
}
