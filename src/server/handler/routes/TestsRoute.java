package server.handler.routes;

import coderunner.CodeRunner;
import coderunner.Submission;
import database.Database;
import database.model.User;
import server.handler.Handler;
import server.handler.methods.Get;
import server.request.Request;
import server.response.Response;
import server.response.ResponseCode;
import template.TemplateEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TestsRoute extends Handler implements Get {

    /** The template engine which contains and compiles the templates */
    private final TemplateEngine templateEngine;

    /** The database which holds users for user auth*/
    private final Database database;

    /**
     * constructs a TestsRoute handler
     * @param templateEngine the template engine which holds and compiles the templates
     */
    public TestsRoute(TemplateEngine templateEngine, Database database) {
        this.templateEngine = templateEngine;
        this.database = database;
    }

    @Override
    public Response get(Request req) {
        String username = req.getCookies().get("username");
        String hashedPassword = req.getCookies().get("password");

        User currentUser = this.database.users().authenticate(username, hashedPassword);

        if (currentUser == null) {
            Map<String, String> redirectHeaders = new HashMap<>();

            redirectHeaders.put("Location", "http://localhost:5000/log-in?next=" + req.getStatusLine().getLocation());

            return new Response(
                    new Response.StatusLine(ResponseCode.FOUND),
                    redirectHeaders,
                    ""
            );
        }

        // Compile template with data
        String body = this.templateEngine.compile("frontend/templates/tests.th", null);

        // Headers
        Map<String, String> headers = Handler.htmlHeaders();

        return new Response(
                new Response.StatusLine(ResponseCode.OK),
                headers,
                body
        );
    }
}
