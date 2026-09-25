package com.ticketmgmt.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.RecordComponent;

public class AtLeastOneFieldPresentValidator implements ConstraintValidator<AtLeastOneFieldPresent, Object> {

    private String[] fields;

    @Override
    public void initialize(AtLeastOneFieldPresent constraintAnnotation) {
        this.fields = constraintAnnotation.fields();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        if (!(value instanceof Record)) {
            return true;
        }

        RecordComponent[] components = value.getClass().getRecordComponents();
        for (String fieldName : fields) {
            RecordComponent component = null;
            for (RecordComponent candidate : components) {
                if (candidate.getName().equals(fieldName)) {
                    component = candidate;
                    break;
                }
            }
            if (component == null) {
                continue;
            }
            try {
                Object fieldValue = component.getAccessor().invoke(value);
                if (fieldValue != null) {
                    return true;
                }
            } catch (ReflectiveOperationException ex) {
                return false;
            }
        }
        return false;
    }
}
