package com.mnsoo.reservation.exception.impl;

import com.mnsoo.reservation.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class StoreNotFoundException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "존재하지 않는 상점명 입니다.";
    }
}
