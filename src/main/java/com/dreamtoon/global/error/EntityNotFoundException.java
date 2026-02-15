package com.dreamtoon.global.error;

public class EntityNotFoundException extends BusinessException {

        public EntityNotFoundException(ErrorCode errorCode) {
                super(errorCode);
        }

        public EntityNotFoundException(ErrorCode errorCode, String message) {
                super(errorCode, message);
        }
}
