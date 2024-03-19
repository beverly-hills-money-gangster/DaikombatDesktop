package com.beverly.hills.money.gang.validator;

import lombok.Getter;

public class ValidatorResult {

  @Getter
  private final boolean valid;
  @Getter
  private final String message;

  private ValidatorResult(boolean valid, String message) {
    this.valid = valid;
    this.message = message;
  }

  public static ValidatorResult invalid(String message) {
    return new ValidatorResult(false, message);
  }

  public static ValidatorResult valid() {
    return new ValidatorResult(true, null);
  }

}
