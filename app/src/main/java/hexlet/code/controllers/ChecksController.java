package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ChecksController {
    public static Handler addUrlCheck = ctx -> {
        int urlId = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl().id.eq(urlId).findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        HttpResponse<String> response = Unirest.get(url.getName()).asString();

        String body = response.getBody();

        UrlCheck urlCheck = new UrlCheck(
                response.getStatus(),
                getTagValue(body, "title"),
                getTagValue(body, "h1"),
                getTagValue(body, "description"),
                url);

        url.addUrlCheck(urlCheck);
        urlCheck.save();
        url.update();

        ctx.redirect(String.format("/urls/%d", urlId));
        ctx.sessionAttribute("flash", "Site was checked successfully");
        ctx.sessionAttribute("flash-type", "success");
    };

    public static String getTagValue(String body, String tagName) {
        Document doc = Jsoup.parse(body);
        Element tag = doc.selectFirst(tagName);

        if (tag != null) {
            return tag.text();
        }

        return new String();
    }
}
