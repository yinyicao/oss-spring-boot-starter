package com.ycyin.minio.core;

import com.ycyin.minio.server.MinioException;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Classname： MinioTemplate1
 * Description： MinioTemplate1类
 * Date： 2023/5/11 14:44
 * author： ycyin
 */
public interface MinioTemplate {
    List<Item> list();

    List<Item> fullList();

    List<Item> list(String String);

    List<Item> getFullList(String String);

    InputStream get(String String) throws MinioException;

    StatObjectResponse getMetadata(String String) throws MinioException;

    Map<String, StatObjectResponse> getMetadata(Iterable<String> Strings);

    void getAndSave(String source, String fileName) throws MinioException;

    void upload(String source, InputStream file, Map<String, String> headers) throws
            MinioException;

    void upload(String source, InputStream file) throws
            MinioException;

    void upload(String source, InputStream file, String contentType, Map<String, String> headers) throws
            MinioException;

    void upload(String source, InputStream file, String contentType) throws
            MinioException;

    void upload(String source, File file) throws
            MinioException;

    void remove(String source) throws MinioException;
}
