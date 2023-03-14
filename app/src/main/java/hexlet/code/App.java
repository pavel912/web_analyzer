package hexlet.code;

import hexlet.code.controllers.ChecksController;
import hexlet.code.controllers.URLController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;

import hexlet.code.controllers.RootController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;


public class App {
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.parseInt(port);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);
        app.post("/urls", URLController.addUrl);
        app.get("/urls", URLController.getURLList);
        app.get("/urls/{id}", URLController.getURL);
        app.post("/urls/{id}/checks", ChecksController.addUrlCheck);
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

    public static Javalin getApp() {

        Javalin app = Javalin.create(config -> {
            config.enableDevLogging();
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
