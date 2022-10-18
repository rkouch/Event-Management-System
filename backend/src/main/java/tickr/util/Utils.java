package tickr.util;

public class Utils {
    public static boolean isValidUrl (String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }
}
