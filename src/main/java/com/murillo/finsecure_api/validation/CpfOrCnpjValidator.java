package com.murillo.finsecure_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfOrCnpjValidator implements ConstraintValidator<CpfOrCnpj, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Se for nulo ou vazio, deixamos o @NotBlank cuidar disso
        if (value == null || value.isBlank()) {
            return true;
        }

        // Remove tudo que não for número (pontos, traços, barras)
        String digits = value.replaceAll("\\D", "");

        if (digits.length() == 11) {
            return isValidCpf(digits);
        } else if (digits.length() == 14) {
            return isValidCnpj(digits);
        }

        return false;
    }

    // Algoritmo matemático oficial do CPF (Módulo 11)
    private boolean isValidCpf(String cpf) {
        // Rejeita sequências conhecidas de dígitos iguais (ex: 00000000000, 11111111111)
        if (cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += (cpf.charAt(i) - '0') * (10 - i);
            }
            int firstCheck = 11 - (sum % 11);
            if (firstCheck >= 10) firstCheck = 0;

            if (firstCheck != (cpf.charAt(9) - '0')) return false;

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += (cpf.charAt(i) - '0') * (11 - i);
            }
            int secondCheck = 11 - (sum % 11);
            if (secondCheck >= 10) secondCheck = 0;

            return secondCheck == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }

    // Algoritmo matemático oficial do CNPJ (Módulo 11)
    private boolean isValidCnpj(String cnpj) {
        // Rejeita sequências de dígitos iguais
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += (cnpj.charAt(i) - '0') * weights1[i];
            }
            int remainder = sum % 11;
            int firstCheck = remainder < 2 ? 0 : 11 - remainder;

            if (firstCheck != (cnpj.charAt(12) - '0')) return false;

            int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            sum = 0;
            for (int i = 0; i < 13; i++) {
                sum += (cnpj.charAt(i) - '0') * weights2[i];
            }
            remainder = sum % 11;
            int secondCheck = remainder < 2 ? 0 : 11 - remainder;

            return secondCheck == (cnpj.charAt(13) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}