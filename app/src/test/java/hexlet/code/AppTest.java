package hexlet.code;
import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {
    static Javalin app;
    static String baseUrl;

    @BeforeAll
    static void addUrl() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(baseUrl + "/urls");

        List<NameValuePair> params = new ArrayList<>(1);
        params.add(new BasicNameValuePair("url", "https://www.google.com/doodles"));
        UrlEncodedFormEntity form = new UrlEncodedFormEntity(params, "UTF-8");
        httpPost.setEntity(form);

        httpClient.execute(httpPost);
    }

    @Test
    void testShowUrl() throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        Url url = new QUrl().name.eq("https://www.google.com").findOne();

        assertNotNull(url);

        HttpGet httpGet = new HttpGet(baseUrl + String.format("/urls/%d", url.getId()));

        HttpResponse getResponse = httpClient.execute(httpGet);

        assertEquals(200, getResponse.getStatusLine().getStatusCode());
    }

    @Test
    void testShowListUrls() throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        Url url = new QUrl().name.eq("https://www.google.com").findOne();

        assertNotNull(url);

        HttpGet httpGet = new HttpGet(baseUrl + "/urls");

        HttpResponse getResponse = httpClient.execute(httpGet);
        String responseBody = new String(getResponse
                .getEntity()
                .getContent()
                .readAllBytes());
        assertTrue(responseBody.contains("https://www.google.com"));
    }

    @AfterAll
    static void afterAll() {
        app.stop();
    }
}
