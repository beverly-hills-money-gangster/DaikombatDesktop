package com.beverly.hills.money.gang.validator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HostPortValidatorTest {

    private final Validator<String> hostPortValidator = new HostPortValidator();

    @Test
    public void testValidateEmpty() {
        var result = hostPortValidator.validate("");
        assertFalse(result.isValid());
        assertEquals("INVALID HOST:PORT", result.getMessage());
    }

    @Test
    public void testValidateNoPort() {
        var result = hostPortValidator.validate("127.0.0.1");
        assertFalse(result.isValid());
        assertEquals("INVALID HOST:PORT", result.getMessage());
    }

    @Test
    public void testValidateInvalidPort() {
        var result = hostPortValidator.validate("127.0.0.1:ABC");
        assertFalse(result.isValid());
        assertEquals("INVALID PORT", result.getMessage());
    }

    @Test
    public void testValidateInvalidDomain() {
        var result = hostPortValidator.validate("$not.real.domain_:1234");
        assertFalse(result.isValid());
        assertEquals("INVALID HOST", result.getMessage());
    }

    @Test
    public void testValidateInvalidIpV4() {
        var result = hostPortValidator.validate("300.500.100.0:1234");
        assertFalse(result.isValid());
        assertEquals("INVALID HOST", result.getMessage());
    }


    @Test
    public void testValidateDomain() {
        var result = hostPortValidator.validate("daikombat.com:1234");
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }


    @Test
    public void testValidateIpV4() {
        var result = hostPortValidator.validate("63.11.87.22:1234");
        assertTrue(result.isValid());
        assertNull(result.getMessage());
    }
}
