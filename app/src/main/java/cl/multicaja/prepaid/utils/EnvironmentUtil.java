package cl.multicaja.prepaid.utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class EnvironmentUtil {

    private EnvironmentUtil() {}

    public static <T> T getVariable(String name, Function<String, T> mapping, Supplier<T> defaultValue) {
        return getVariable(name).map(mapping).orElseGet(defaultValue);
    }

    public static String getVariable(String name, Supplier<String> defaultValue) {
        return getVariable(name).orElseGet(defaultValue);
    }

    public static Optional<String> getVariable(String name) {
        return Optional.ofNullable(System.getenv(name));
    }
}