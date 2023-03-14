package hexlet.code.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class BasicUtils {
    public static String trimUrl(String urlPath) throws MalformedURLException {
        URL url = new URL(urlPath);
        return url.getProtocol().concat("://").concat(url.getAuthority());
    }
}
