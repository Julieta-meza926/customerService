package com.mycompany.clientservice.exception.handler;
import com.mycompany.clientservice.model.dto.ErrorResponse;
import com.mycompany.clientservice.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //Validaciones con @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Campos inválidos: " + message, request);
    }

    //JSON mal formado
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonParsingError(HttpMessageNotReadableException ex, WebRequest request) {
        log.error("JSON mal formado: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la petición es inválido o mal formado", request);
    }

    // Not Found
    @ExceptionHandler({ClientNotFoundException.class, UserNotFoundException.class, RoleNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex, WebRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    //Conflictos (duplicados, integridad de datos)
    @ExceptionHandler({EmailAlreadyExistsException.class, DataIntegrityException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex, WebRequest request) {
        log.warn("Conflicto de datos: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    //Errores de autenticación / autorización
    @ExceptionHandler(JwtAuthException.class)
    public ResponseEntity<ErrorResponse> handleJwtAuth(JwtAuthException ex, WebRequest request) {
        log.warn("Error de autenticación JWT: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // Errores de caché u operaciones internas
    @ExceptionHandler({CacheException.class, OperationFailedException.class})
    public ResponseEntity<ErrorResponse> handleInternal(RuntimeException ex, WebRequest request) {
        log.error("Error interno de aplicación: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    //Fallback genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, WebRequest request) {
        log.error("Error inesperado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request);
    }

    //Método común para construir respuestas
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .error(message)
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }
}
