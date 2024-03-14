package com.beverly.hills.money.gang.validator;

public interface Validator<T> {

    ValidatorResult validate(T object);

}
