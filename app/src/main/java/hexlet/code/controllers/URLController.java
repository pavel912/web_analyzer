package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.utils.BasicUtils;

import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import kong.unirest.UnirestException;
import java.util.List;
import java.util.stream.IntStream;

public class URLController {
    public static Handler addUrl = ctx -> {
        String value = ctx.formParam("url");
        if (value == null) {
            ctx.sessionAttribute("flash", "No URL provided");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }
        String truncatedUrlName;

        try {
            truncatedUrlName = BasicUtils.trimUrl(value);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Wrong URL format");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        boolean existsSameUrl = new QUrl().name.eq(truncatedUrlName).exists();

        if (existsSameUrl) {
            ctx.sessionAttribute("flash", "This URL already exists");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        Url myUrl = new Url(truncatedUrlName);
        myUrl.save();

        ctx.redirect("/urls");
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
    };

    public static Handler getURLList = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urlsList = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed().toList();

        ctx.attribute("urls", urlsList);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler getURL = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("url", url);
        ctx.render("urls/url.html");
    };

    public static Handler addUrlCheck = ctx -> {
        int urlId = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl().id.eq(urlId).findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        HttpResponse<String> response;

        try {
            response = Unirest.get(url.getName()).asString();
        } catch (UnirestException e) {
            ctx.redirect(String.format("/urls/%d", urlId));
            ctx.sessionAttribute("flash", "Такой страницы не существует");
            ctx.sessionAttribute("flash-type", "danger");
            return;
        }

        Document body = Jsoup.parse(response.getBody());

        UrlCheck urlCheck = new UrlCheck(
                response.getStatus(),
                BasicUtils.getTagValue(body, "title"),
                BasicUtils.getTagValue(body, "h1"),
                BasicUtils.getTagValue(body, "meta"),
                url);

        url.addUrlCheck(urlCheck);
        urlCheck.save();
        url.update();

        ctx.redirect(String.format("/urls/%d", urlId));
        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flash-type", "success");
    };
}
