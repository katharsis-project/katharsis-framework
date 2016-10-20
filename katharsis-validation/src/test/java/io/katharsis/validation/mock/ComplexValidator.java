package io.katharsis.validation.mock;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.katharsis.validation.mock.models.Project;
import io.katharsis.validation.mock.models.Task;

public class ComplexValidator implements ConstraintValidator<ComplexValid, Object> {

	public static final String INVALID_NAME = "invalid";

	@Override
	public void initialize(final ComplexValid a) {
	}

	@Override
	public boolean isValid(final Object t, final ConstraintValidatorContext cvc) {
		if (t instanceof Project) {
			Project p = (Project) t;
			return !INVALID_NAME.equals(p.getName());
		}
		if (t instanceof Task) {
			Task p = (Task) t;
			return !INVALID_NAME.equals(p.getName());
		}
		return true;
	}
}