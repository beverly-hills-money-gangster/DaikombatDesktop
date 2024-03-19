package com.beverly.hills.money.gang.validator;

import java.util.Locale;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;

public class HostPortValidator implements Validator<String> {

  @Override
  public ValidatorResult validate(String object) {
    String[] hostPort = object.toLowerCase(Locale.ENGLISH).split(":");
    if (hostPort.length != 2) {
      return ValidatorResult.invalid("INVALID HOST:PORT");
    }
    String host = hostPort[0];
    if (!DomainValidator.getInstance().isValid(host) && !InetAddressValidator.getInstance()
        .isValid(host)) {
      return ValidatorResult.invalid("INVALID HOST");
    }
    int port = NumberUtils.toInt(hostPort[1], -1);
    if (port < 0 || port > 65_536) {
      return ValidatorResult.invalid("INVALID PORT");
    }
    return ValidatorResult.valid();
  }
}
