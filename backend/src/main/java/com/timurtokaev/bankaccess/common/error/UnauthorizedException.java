package com.timurtokaev.bankaccess.common.error;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("Authentication failed");
    }
}