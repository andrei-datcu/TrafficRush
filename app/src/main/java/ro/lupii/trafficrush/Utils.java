package ro.lupii.trafficrush;

/**
 * Created by andrei on 1/24/17.
 */

public class Utils {
    public static void doAssert(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void doAssert(boolean condition) {
        doAssert(condition, null);
    }
}
