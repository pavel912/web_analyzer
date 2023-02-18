package hexlet.code;

import io.javalin.Javalin;

import hexlet.code.controllers.RootController;

public class App {
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.parseInt(port);
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);
    }

    public static Javalin getApp() {

        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
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
