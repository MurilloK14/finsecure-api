package com.murillo.finsecure_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Documented
@Constraint(validatedBy = CpfOrCnpjValidator.class)
@Target({ElementType.FIELD,ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOrCnpj {

    String message() default "CPF ou CNPJ inválido";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};

}
