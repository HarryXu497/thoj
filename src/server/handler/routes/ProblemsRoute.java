package server.handler.routes;

import database.Database;
import database.model.Problem;
import database.model.User;
import server.handler.Handler;
import server.handler.methods.Get;
import server.request.Request;
import server.response.Response;
import server.response.ResponseCode;
import template.TemplateEngine;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProblemsRoute extends Handler implements Get {

    /** The template engine which contains and compiles the templates */
    private final TemplateEngine templateEngine;

    /** The database which contains application state */
    private final Database database;

    /**
     * constructs a ProblemsRoute handler
     * @param templateEngine the template engine which holds and compiles the templates
     */
    public ProblemsRoute(TemplateEngine templateEngine, Database database) {
        this.templateEngine = templateEngine;
        this.database = database;
    }

    @Override
    public Response get(Request req) {
        String username = req.getCookies().get("username");
        String hashedPassword = req.getCookies().get("password");

        User user = null;

        try {
            user = this.database.users().getByUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        boolean loggedIn = false;

        List<Integer> solvedList = null;

        // User with username exists
        if (user != null) {
            loggedIn = this.database.users().logInMatched(user, hashedPassword);

            if (loggedIn) {
                solvedList = this.database.solvedProblems().getAllSolvedProblems(user.getUserID());


            }
        }


        // Get problems from database
        List<Problem> problems = this.database.problems().getAllProblems();

        List<TemplateProblem> templateProblems = new ArrayList<>();

        for (Problem problem : problems) {
            boolean solved = false;

            if ((solvedList != null) && (solvedList.contains(problem.getProblemID()))) {
                solved = true;
            }

            templateProblems.add(new TemplateProblem(problem, solved));
        }

        // Compile template with data
        String body = this.templateEngine.compile("frontend/templates/problems.th", new Data(
                templateProblems
        ));

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

    /**
     * container for the template data
     * @author Harry Xu
     * @version 1.0 - May 23rd 2023
     */
    public static class Data {
        public List<TemplateProblem> problems;

        public Data(List<TemplateProblem> problems) {
            this.problems = problems;
        }
    }

    /**
     * A wrapper of a problem which makes its properties available for templating
     * @author Harry Xu
     * @version 1.0 - June 6th 2023
     */
    public class TemplateProblem {
        public String title;
        public int difficulty;
        public String content;
        public String type;
        public int id;
        public String authorName;
        public boolean solvedByUser;
        public boolean notSolvedByUser;

        public TemplateProblem(Problem problem, boolean solvedByUser) {
            this.title = problem.getTitle();
            this.difficulty = problem.getDifficulty();
            this.content = problem.getContent();
            this.id = problem.getProblemID();
            this.type = problem.getType();
            this.solvedByUser = solvedByUser;
            this.notSolvedByUser = !solvedByUser;

            try {
                this.authorName = database.users().getUserById(
                        problem.getAuthorID()
                ).getUserName();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
