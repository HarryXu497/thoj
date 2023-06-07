package server.handler.routes;

import coderunner.CodeRunner;
import coderunner.Submission;
import coderunner.Task;
import server.handler.Handler;
import server.handler.methods.Get;
import server.handler.methods.Post;
import server.request.Request;
import server.response.Response;
import server.response.ResponseCode;
import template.TemplateEngine;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for handling the problem submission route ("/problems/:problemId/submit")
 * @author Harry Xu
 * @version 1.0 - May 30th 2023
 */
public class SubmitRoute extends Handler implements Get, Post {

    private final TemplateEngine templateEngine;
    private final CodeRunner codeRunner;

    public SubmitRoute(TemplateEngine templateEngine, CodeRunner codeRunner) {
        this.templateEngine = templateEngine;
        this.codeRunner = codeRunner;
    }

    @Override
    public Response get(Request req) {
        // Compile template with data
        String body = this.templateEngine.compile("frontend/templates/submit.th", null);

        // Headers
        Map<String, String> headers = new HashMap<>();

        headers.put("Content-Type", "text/html; charset=iso-8859-1");
        headers.put("Vary", "Accept-Encoding");
        headers.put("Accept-Ranges", "none");

        return new Response(
                new Response.StatusLine(ResponseCode.OK),
                headers,
                body
        );
    }

    @Override
    public Response post(Request req) {
        // Get code from request
        String code;

        try {
            code = URLDecoder.decode(req.getBody().get("code"), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // This should never happen - value came from JDK's StandardCharsets
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        // Headers
        Map<String, String> headers = new HashMap<>();

        // Default error response
        Response errorResponse = new Response(
                new Response.StatusLine(ResponseCode.INTERNAL_SERVER_ERROR),
                headers,
                ""
        );

        String submissionId = UUID.randomUUID().toString();

        try {
            // TODO: interpolate the problem id into the file name
            // TODO: interpolate problem id?? into the requestId
            this.codeRunner.enqueue(new Submission(
                    new Task(
                            code,
                            "problems/problem1/Main.java",
                            Arrays.asList(
                                    "problems/problem1/input/input1.txt",
                                    "problems/problem1/input/input2.txt"
                            ),
                            Arrays.asList(
                                    "problems/problem1/output/out1.txt",
                                    "problems/problem1/output/out2.txt"
                            ),
                            Arrays.asList(
                                    "problems/problem1/answers/ans1.txt",
                                    "problems/problem1/answers/ans2.txt"
                            )
                    ),
                    submissionId
            ));
        } catch (IOException e) {
            return errorResponse;
        }

        String problemId = req.getStatusLine().getRouteParams().get("problemId");

        headers.put("Location", "http://localhost:5000/problems/" + problemId + "/tests");
        headers.put("Set-Cookie", "submissionId=" + submissionId);

        return new Response(
                new Response.StatusLine(ResponseCode.SEE_OTHER),
                headers,
                ""
        );
    }
}
