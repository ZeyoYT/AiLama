package me.ailama.tools;

import me.ailama.handler.annotations.Args;
import me.ailama.handler.annotations.Tool;

public class MathTools {

    @Tool(name = "add", description = "Addition ('+') of two numbers like N1+N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String add(Number a, Number b) {
        return String.valueOf(a.doubleValue() + b.doubleValue());
    }

    @Tool(name = "subtract", description = "Subtraction ('-') of two numbers like N1-N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String subtract(Number a, Number b) {
        return String.valueOf(a.doubleValue() - b.doubleValue());
    }

    @Tool(name = "multiply", description = "Multiplication ('*') of two numbers like N1*N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String multiply(Number a, Number b) {
        return String.valueOf(a.doubleValue() * b.doubleValue());
    }

    @Tool(name = "divide", description = "Division ('/') of two numbers like N1/N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String divide(Number a, Number b) {
        return String.valueOf(a.doubleValue() / b.doubleValue());
    }

    @Tool(name = "modulus", description = "Modulus ('%') of two numbers like N1%N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String modulus(Number a, Number b) {
        return String.valueOf(a.doubleValue() % b.doubleValue());
    }

    @Tool(name = "power", description = "Power ('^') of two numbers like N1^N2", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number")
    })
    public String power(Number a, Number b) {
        return String.valueOf(Math.pow(a.doubleValue(), b.doubleValue()));
    }

    @Tool(name = "sqrt", description = "Square root of a number like sqrt(N1)", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String sqrt(Number a) {
        return String.valueOf(Math.sqrt(a.doubleValue()));
    }

    @Tool(name = "cubeRoot", description = "Cube root of a number", arguments = {
            @Args(name = "a", Type = "number")
    })
    public String cubeRoot(Number a) {
        return String.valueOf(Math.cbrt(a.doubleValue()));
    }

}
