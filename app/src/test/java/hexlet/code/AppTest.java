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
import kong.unirest.JsonNode;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {
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

        MockResponse response = new MockResponse()
                .setBody("<head><title>MockWebServer</title><meta name=\"description\" content=\"This is a MockWebServer\"></head><body><h1>Try your HTTP requests</h1></body>")
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
    void addTestDate(TestInfo info) throws IOException {
        new QUrl().delete();

        if (info.getDisplayName().equals("addCorrectUrl")) {
            return;
        }

        Url url = new Url(BasicUtils.trimUrl(serverUrl));
        url.save();
    }

    @Test
    void addCorrectUrl() throws MalformedURLException {
        HttpResponse<JsonNode> response = Unirest.post(baseUrl + "/urls").field("url", serverUrl).asJson();

        assertEquals(200, response.getStatus());

        assertTrue(new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).exists());
    }

    @Test
    void addSameUrl() throws MalformedURLException {
        Unirest.post(baseUrl + "/urls").field("url", serverUrl).asJson();

        assertEquals(1, new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).findCount());
    }

    @Test
    void addIncorrectUrl() throws MalformedURLException {
        Unirest.post(baseUrl + "/urls").field("url", "fsdfsdf").asJson();

        assertFalse(new QUrl().name.eq("fsdfsdf").exists());
    }

    @Test
    void testShowUrl() throws IOException {
        Url url = new QUrl().name.eq(BasicUtils.trimUrl(serverUrl)).findOne();

        assertNotNull(url);

        HttpResponse<JsonNode> response = Unirest
                .get(baseUrl + "/urls/{id}")
                .routeParam("id", String.valueOf(url.getId()))
                .asJson();

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

        HttpResponse<JsonNode> response = Unirest
                .post(baseUrl + "/urls/{id}/checks")
                .routeParam("id", String.valueOf(url.getId()))
                .asJson();

        assertEquals(302, response.getStatus());

        UrlCheck urlCheck = new QUrlCheck().url.eq(url).findOne();

        assertNotNull(urlCheck);

        assertEquals(200, urlCheck.getStatusCode());

        assertEquals("MockWebServer", urlCheck.getTitle());
        assertEquals("This is a MockWebServer", urlCheck.getDescription());
        assertEquals("Try your HTTP requests", urlCheck.getH1());
    }
}
