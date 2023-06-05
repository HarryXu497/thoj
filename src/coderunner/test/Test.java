package coderunner.test;

import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * Represents a singular test case for a problem submission
 * @author Harry Xu
 * @version 1.0 - June 4th 2023
 */
public class Test {
    /** The path of the class file to execute */
    private final String compiledFilePath;

    /** The path of the input */
    private final String inputFilePath;

    /** The path of the output file */
    private final String outputFilePath;

    /** The path of the answer file */
    private final String answerFilePath;

    /** The result of the test */
    private TestResult result;

    /**
     * Constructs a Test instance to test a compiled file
     * @param compiledFilePath the path to the compiled .class file
     * @param inputFilePath the path to the file containing the input
     * @param outputFilePath the path to the file where the process's output is redirected
     * @param answerFilePath the path to the file containing the correct output for the problem
     */
    public Test(String compiledFilePath, String inputFilePath, String outputFilePath, String answerFilePath) {
        this.compiledFilePath = compiledFilePath;
        this.inputFilePath = inputFilePath;
        this.outputFilePath = outputFilePath;
        this.answerFilePath = answerFilePath;
    }

    /**
     * execute
     * creates and executes a java process running the compiled file
     * @throws IOException if an IO error occurs during the creation of execution of the process
     * @throws InterruptedException if the current thread is interrupted while waiting.
     */
    public void execute() throws IOException, InterruptedException {

        // Create process
        String[] commands = { "java ", this.compiledFilePath };

        ProcessBuilder process = new ProcessBuilder(commands);

        // Redirect input and output streams
        String dir = System.getProperty("user.dir") + "\\";

        process.redirectInput(new File(dir + this.inputFilePath));
        process.redirectOutput(new File(dir + this.outputFilePath));

        // Start process execution
        Process executionProcess = process.start();

        System.out.println("HERE 1");

        // Handle runtime exceptions
        StringBuilder runtimeException = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(executionProcess.getErrorStream()))) {
            int c;

            if (br.ready()) {
                while ((c = br.read()) != -1) {
                    runtimeException.append((char) c);
                }
            }
        }

        String stackTrace = runtimeException.toString();

        if (stackTrace.length() != 0) {
            this.result = new TestResult(TestCode.RUNTIME_ERROR, stackTrace);
            return;
        }

        // TODO: maybe make this customizable
        // Timeout after 3000 milliseconds
        if (!executionProcess.waitFor(3000, TimeUnit.MILLISECONDS)) {
            executionProcess.destroy();
            this.result = new TestResult(TestCode.TIME_LIMIT_EXCEEDED, null);
            System.out.println("timeout");
        }
    }

    /**
     * test
     * checks and compares the contents of the output and answer files.
     * @throws IOException if an IO error occurs while reading from the two files
     */
    public TestResult test() throws IOException {
        // Timeout occurred when executing file - no need to test
        if (this.result != null) {
            return this.result;
        }

        // Get working directory
        String workingDirectory = System.getProperty("user.dir") + "\\";

        // Read all content from output file
        StringBuilder outputFileContents = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(workingDirectory + this.outputFilePath))) {
            int s;

            while ((s = br.read()) != -1) {
                outputFileContents.append((char) s);
            }
        }

        // Read all content from answer file
        StringBuilder answerFileContents = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(workingDirectory + this.answerFilePath))) {
            int s;

            while ((s = br.read()) != -1) {
                answerFileContents.append((char) s);
            }
        }

        // Normalize file endings
        String outputContent = outputFileContents.toString().replaceAll("\\r\\n?", "\n");
        String answerContent = answerFileContents.toString().replaceAll("\\r\\n?", "\n");

        // Performs a one-to-one check
        if (outputContent.equals(answerContent)) {
            return new TestResult(TestCode.ACCEPTED, null);
        } else {
            return new TestResult(TestCode.WRONG_ANSWER, null);
        }
    }
}
