package com.volleyball.finder.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = QuotaValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface QuotaConstraint {
    String message() default "名額設定不符合邏輯規則";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

