package main.web.handler;

import main.exception.AlreadyExistException;
import main.exception.EntityNotFoundException;
import main.exception.RefreshTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class WebAppExceptionHandler {

    @ExceptionHandler(value = RefreshTokenException.class)
    public ResponseEntity<ErrorResponseBody> refreshTokenExceptionHandler(RefreshTokenException ex, WebRequest webRequest){
        return buildResponse(HttpStatus.FORBIDDEN, ex, webRequest);
    }

    @ExceptionHandler(value = AlreadyExistException.class)
    public ResponseEntity<ErrorResponseBody> alreadyExistHandler(RefreshTokenException ex, WebRequest webRequest){
        return buildResponse(HttpStatus.FORBIDDEN, ex, webRequest);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseBody> notFoundHandler(RefreshTokenException ex, WebRequest webRequest){
        return buildResponse(HttpStatus.FORBIDDEN, ex, webRequest);
    }

    private ResponseEntity<ErrorResponseBody> buildResponse(HttpStatus status, Exception ex, WebRequest request){
        return ResponseEntity.status(status)
                .body(ErrorResponseBody.builder()
                        .message(ex.getMessage())
                        .description(request.getDescription(false))
                        .build());
    }
}
