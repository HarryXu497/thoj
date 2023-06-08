package server.response;

/**
 * represents an HTTP response code and its associated message
 * @author Harry Xu
 * @version 1.0 - May 20th 2023
 */
public enum ResponseCode {
    OK(200, "OK"),
    CREATED(201, "Created"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Interval Server Error");

    /** HTTP response code */
    private final int code;

    /** HTTP response message*/
    private final String message;

    /**
     * constructs an enum value with a code and message
     * @param code the response code
     * @param message the response message
     */
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * getCode
     * gets the response code of the enum value
     * @return the HTTP response code.
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">Response Codes</a>
     */
    public int getCode() {
        return this.code;
    }

    /**
     * getMessage
     * gets the response message corresponding to the response code
     * @return the HTTP response message
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">Response Codes</a>
     */
    public String getMessage() {
        return this.message;
    }
}
