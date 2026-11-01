package com.murillo.finsecure_api.exception;

import com.murillo.finsecure_api.dto.response.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponseDTO> illegalArgumentExceptionHandler(IllegalArgumentException ex, HttpServletRequest request){
    String mensagem =  ex.getMessage();

    String url = request.getRequestURI();
    ErrorResponseDTO corpo = new ErrorResponseDTO(400, "Bad Request", mensagem, url,null);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
}

@ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex, HttpServletRequest request){

    Map<String,String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(erro -> errors.put(erro.getField(), erro.getDefaultMessage()));

    String url = request.getRequestURI();

    ErrorResponseDTO corpo = new ErrorResponseDTO(400, "Validanton Error", "Erro de validação nos campos informados", url, errors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
}
}
