package tickr.server.exceptions;

public class NotFoundException extends ServerException {
    public NotFoundException (String reason, Throwable cause) {
        super(404, "Not Found", reason, cause);
    }

    public NotFoundException (String reason) {
        super(404, "Not Found", reason);
    }
}
