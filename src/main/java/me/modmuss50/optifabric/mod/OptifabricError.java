package me.modmuss50.optifabric.mod;

public class OptifabricError {

    private static String error = null;

    public static boolean hasError() {
        return getError() != null;
    }

    public static String getError() {
        return error;
    }

    public static void setError(String error) {
        OptifabricError.error = error;
    }

}
