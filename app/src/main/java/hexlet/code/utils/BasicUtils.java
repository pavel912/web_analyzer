package hexlet.code.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;

public class BasicUtils {
    public static String trimUrl(String urlPath) throws MalformedURLException {
        URL url = new URL(urlPath);
        return url.getProtocol().concat("://").concat(url.getAuthority());
    }

    public static String getTagValue(Document doc, String tagName) {
        if (tagName.equals("meta")) {
            Element tag = doc.selectFirst("meta[name=description]");
            if (tag != null && tag.hasAttr("content")) {
                return tag.attr("content");
            }
        }

        if (tagName.equals("p")) {
            Element tag = doc.selectFirst("p[class=m-0]");
            if (tag != null && tag.hasAttr("text")) {
                return tag.attr("text");
            }
        }

        Element tag = doc.selectFirst(tagName);

        if (tag != null) {
            return tag.text();
        }

        return new String();
    }
}
