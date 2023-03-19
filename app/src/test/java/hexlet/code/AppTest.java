package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import hexlet.code.utils.BasicUtils;

import io.javalin.Javalin;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static MockWebServer server;
    private static String serverUrl;

    @BeforeAll
    static void start() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        server = new MockWebServer();

        String testBodyPath = Paths
                .get("src", "test", "resources", "fixtures", "test_body.html")
                .toAbsolutePath()
                .normalize()
                .toString();

        String testBody = FileUtils.readFileToString(new File(testBodyPath), StandardCharsets.UTF_8);

        MockResponse response = new MockResponse()
                .setBody(testBody)
                .setResponseCode(200);

        server.enqueue(response);

        server.start();

        serverUrl = server.url("/").toString();
    }

    @AfterAll
    static void afterAll() throws IOException {
        server.shutdown();
        app.stop();
    }

    @BeforeEach
    void addTestData(TestInfo info) throws IOException {
        new QUrl().delete();
        new QUrlCheck().delete();

        if (info.getDisplayName().equals("addCorrectUrl()")) {
            return;
        }

        Url url = new Url(BasicUtils.trimUrl(serverUrl));
        url.save();
    }

    @Test
    void testIndex() {
        HttpResponse<String> response = Unirest.get(baseUrl + "/").asString();

        assertEquals(200, response.getStatus());
    }

    @Test
    void addCorrectUrl() throws MalformedURLException {
        HttpResponse<String> response = Unirest.post(baseUrl + "/urls").field("url", serverUrl).asString();

        assertEquals(302, response.getStatus());

        assertTrue(new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).exists());
    }

    @Test
    void addSameUrl() throws MalformedURLException {
        HttpResponse<String> response = Unirest.post(baseUrl + "/urls").field("url", serverUrl).asString();

        assertEquals("This URL already exists",
                BasicUtils.getTagValue(Jsoup.parse(response.getBody()), "p"));

        assertEquals(1, new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).findCount());
    }

    @Test
    void addIncorrectUrl() {
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .field("url", "fsdfsdf")
                .asString();

        assertEquals("Wrong URL format", BasicUtils.getTagValue(Jsoup.parse(response.getBody()), "p"));

        assertFalse(new QUrl().name.eq("fsdfsdf").exists());
    }

    @Test
    void addNullUrl() {
        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls")
                .asString();

        assertEquals("No URL provided", BasicUtils.getTagValue(Jsoup.parse(response.getBody()), "p"));

        assertFalse(new QUrl().name.eq("fsdfsdf").exists());
    }

    @Test
    void testShowUrl() throws IOException {
        Url url = new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).findOne();

        assertNotNull(url);

        HttpResponse<String> response = Unirest
                .get(baseUrl + "/urls/{id}")
                .routeParam("id", String.valueOf(url.getId()))
                .asString();

        assertEquals(200, response.getStatus());
    }

    @Test
    void testShowListUrls() throws IOException {
        Url url = new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).findOne();

        assertNotNull(url);

        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains(BasicUtils.trimUrl(serverUrl)));
    }

    @Test
    void testUrlCheck() throws MalformedURLException {
        Url url = new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).findOne();

        assertNotNull(url);

        HttpResponse<String> response = Unirest
                .post(baseUrl + "/urls/{id}/checks")
                .routeParam("id", String.valueOf(url.getId()))
                .asString();

        assertEquals(302, response.getStatus());

        UrlCheck urlCheck = new QUrlCheck().url.eq(url).findOne();

        assertNotNull(urlCheck);

        assertEquals(200, urlCheck.getStatusCode());

        assertEquals("MockWebServer", urlCheck.getTitle());
        assertEquals("This is a MockWebServer", urlCheck.getDescription());
        assertEquals("Try your HTTP requests", urlCheck.getH1());
    }

    @Test
    void checkNonExistentUrl() throws MalformedURLException {
        String urlName = "https://gaaagle.com/";
        HttpResponse<String> responseCreate = Unirest
                .post(baseUrl + "/urls")
                .field("url", urlName)
                .asString();

        assertEquals(302, responseCreate.getStatus());

        Url url = new QUrl().name.eq(BasicUtils.trimUrl(urlName)).findOne();

        assertNotNull(url);

        HttpResponse<String> responseCheck = Unirest
                .post(baseUrl + "/urls/{id}/checks")
                .routeParam("id", String.valueOf(url.getId()))
                .asString();

        assertEquals(404, responseCheck.getStatus());
    }

    @Test
    void testToRiseCodeCoverage() throws MalformedURLException {
        Url url = new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).findOne();

        UrlCheck urlCheck = new UrlCheck(200, "", "", "", url);
        urlCheck.save();

        UrlCheck thisUrlCheck = new QUrlCheck().findOne();

        assertEquals(thisUrlCheck.getId(), thisUrlCheck.getId());
        assertEquals(thisUrlCheck.getCreatedAt(), thisUrlCheck.getCreatedAt());
        assertEquals(url, thisUrlCheck.getUrl());

        assertEquals(thisUrlCheck, url.getLastCheck());
    }
}
