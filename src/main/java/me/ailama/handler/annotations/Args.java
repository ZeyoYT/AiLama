package me.ailama.handler.annotations;

public @interface Args {
    String name();
    String Type();
    String description() default "";

    boolean noNull() default false;
    boolean required() default true;
}
