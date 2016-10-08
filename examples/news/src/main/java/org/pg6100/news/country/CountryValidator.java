package org.pg6100.news.country;



import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CountryValidator implements ConstraintValidator<Country,String> {
    @Override
    public void initialize(Country constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        return CountryList.isValidCountry(value);
    }
}
