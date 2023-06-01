package com.ycyin.minio.server;

public class MinioException extends Exception {
    public MinioException(String message, Throwable cause) {
        super(message, cause);
    }
}