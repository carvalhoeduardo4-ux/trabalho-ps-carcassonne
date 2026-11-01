package com.carcassonne.api;

import com.carcassonne.api.dto.GameDtos.ErrorDto;
import com.carcassonne.domain.exception.GameActionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Mapeia erros de regra para respostas HTTP, mantendo o estado da partida inalterado. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GameActionException.class)
    public ResponseEntity<ErrorDto> handleRule(GameActionException e) {
        return build(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleBadRequest(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDto> handleState(IllegalStateException e) {
        return build(HttpStatus.CONFLICT, e);
    }

    private ResponseEntity<ErrorDto> build(HttpStatus status, Exception e) {
        return ResponseEntity.status(status).body(new ErrorDto(e.getClass().getSimpleName(), e.getMessage()));
    }
}
