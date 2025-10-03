package co.com.pragma.model.exception;

public class IdDocumentAlreadyExistsException extends RuntimeException {
    public IdDocumentAlreadyExistsException(String message) {
        super(message);
    }
}
