package hexlet.code;

import hexlet.code.controllers.URLController;
import hexlet.code.controllers.RootController;

import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.parseInt(port);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);

        app.routes(() -> path("urls", () -> {
            get(URLController.getURLList);
            post(URLController.addUrl);
            path("{id}", () -> {
                get(URLController.getURL);
                post("checks", URLController.addUrlCheck);
            });
        }));
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    private static String getMode() {
        String mode = System.getenv().getOrDefault("APP_ENV", "development");
        LOGGER.info("Mode: {}", mode);
        return mode;
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    public static Javalin getApp() {

        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            JavalinThymeleaf.configure(getTemplateEngine());
            config.enableWebjars();
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}
