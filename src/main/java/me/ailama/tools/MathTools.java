package me.ailama.tools;

import me.ailama.handler.annotations.Args;
import me.ailama.handler.annotations.Tool;

public class MathTools {

    @Tool(name = "basicMathOperations", description = "Basic math operations, only does Addition (+), Subtraction (-), Multiplication (*), Division (/), Modulus (%), Power (^)", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "b", Type = "number"),
            @Args(name = "operation_symbol", Type = "string")
    })
    public String basicMathOperations(Number a, Number b, String operation) {
        return switch (operation) {
            case "+" -> add(a, b);
            case "-" -> subtract(a, b);
            case "*" -> multiply(a, b);
            case "/" -> divide(a, b);
            case "%" -> modulus(a, b);
            case "^" -> power(a, b);
            default -> "Invalid operation";
        };
    }

    public String add(Number a, Number b) {
        return String.valueOf(a.doubleValue() + b.doubleValue());
    }

    public String subtract(Number a, Number b) {
        return String.valueOf(a.doubleValue() - b.doubleValue());
    }

    public String multiply(Number a, Number b) {
        return String.valueOf(a.doubleValue() * b.doubleValue());
    }

    public String divide(Number a, Number b) {
        return String.valueOf(a.doubleValue() / b.doubleValue());
    }

    public String modulus(Number a, Number b) {
        return String.valueOf(a.doubleValue() % b.doubleValue());
    }

    public String power(Number a, Number b) {
        return String.valueOf(Math.pow(a.doubleValue(), b.doubleValue()));
    }

    @Tool(name = "squareOrCube", description = "Square or cube of a number, symbols are square_root and cube_root", arguments = {
            @Args(name = "a", Type = "number"),
            @Args(name = "operation_symbol", Type = "string")
    })
    public String squareOrCube(Number a, String operation) {
        return switch (operation) {
            case "square_root" -> sqrt(a);
            case "cube_root" -> cubeRoot(a);
            default -> "Invalid operation";
        };
    }

    public String sqrt(Number a) {
        return String.valueOf(Math.sqrt(a.doubleValue()));
    }

    public String cubeRoot(Number a) {
        return String.valueOf(Math.cbrt(a.doubleValue()));
    }

}
