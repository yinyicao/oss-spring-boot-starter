package com.ycyin.minio.server;

import com.ycyin.minio.core.MinioProperties;
import com.ycyin.minio.core.MinioTemplate;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @Author ycyin
 * @Description MinioTemplate的实现类
 * @ClassName MinioTemplateImpl
 * @Date 2023/3/18 19:02
 **/
@RequiredArgsConstructor
public class MinioTemplateImpl implements MinioTemplate {


    private final MinioClient minioClient;
    private final MinioProperties configurationProperties;

    /**
     * List all objects at root of the bucket
     *
     * @return List of items
     */
    @Override
    public List<Item> list() {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(configurationProperties.getBucket())
                .prefix("")
                .recursive(false)
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * List all objects at root of the bucket
     *
     * @return List of items
     */
    @Override
    public List<Item> fullList() {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(configurationProperties.getBucket())
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * List all objects with the prefix given in parameter for the bucket.
     * Simulate a folder hierarchy. Objects within folders (i.e. all objects which match the pattern {@code {prefix}/{objectName}/...}) are not returned
     *
     * @param path Prefix of seeked list of object
     * @return List of items
     */
    @Override
    public List<Item> list(String path) {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(configurationProperties.getBucket())
                .prefix(path.toString())
                .recursive(false)
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * List all objects with the prefix given in parameter for the bucket
     * <p>
     * All objects, even those which are in a folder are returned.
     *
     * @param path Prefix of seeked list of object
     * @return List of items
     */
    @Override
    public List<Item> getFullList(String path) {
        ListObjectsArgs args = ListObjectsArgs.builder()
                .bucket(configurationProperties.getBucket())
                .prefix(path.toString())
                .build();
        Iterable<Result<Item>> myObjects = minioClient.listObjects(args);
        return getItems(myObjects);
    }

    /**
     * Utility method which map results to items and return a list
     *
     * @param myObjects Iterable of results
     * @return List of items
     */
    private List<Item> getItems(Iterable<Result<Item>> myObjects) {
        return StreamSupport
                .stream(myObjects.spliterator(), true)
                .map(itemResult -> {
                    try {
                        return itemResult.get();
                    } catch (Exception e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Get an object from Minio
     *
     * @param path Path with prefix to the object. Object name must be included.
     * @return The object as an InputStream
     * @throws MinioException if an error occur while fetch object
     */
    @Override
    public InputStream get(String path) throws MinioException {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(path.toString())
                    .build();
            return minioClient.getObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Get metadata of an object from Minio
     *
     * @param path Path with prefix to the object. Object name must be included.
     * @return Metadata of the  object
     * @throws MinioException if an error occur while fetching object metadatas
     */
    @Override
    public StatObjectResponse getMetadata(String path) throws MinioException {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(path.toString())
                    .build();
            return minioClient.statObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Get metadata for multiples objects from Minio
     *
     * @param paths Paths of all objects with prefix. Objects names must be included.
     * @return A map where all paths are keys and metadatas are values
     */
    @Override
    public Map<String, StatObjectResponse> getMetadata(Iterable<String> paths) {
        return StreamSupport.stream(paths.spliterator(), false)
                .map(path -> {
                    try {
                        StatObjectArgs args = StatObjectArgs.builder()
                                .bucket(configurationProperties.getBucket())
                                .object(path.toString())
                                .build();
                        return new HashMap.SimpleEntry<>(path, minioClient.statObject(args));
                    } catch (Exception e) {
                        throw new MinioFetchException("Error while parsing list of objects", e);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get a file from Minio, and save it in the {@code fileName} file
     *
     * @param source   Path with prefix to the object. Object name must be included.
     * @param fileName Filename
     * @throws MinioException if an error occur while fetch object
     */
    @Override
    public void getAndSave(String source, String fileName) throws MinioException {
        try {
            DownloadObjectArgs args = DownloadObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .filename(fileName)
                    .build();
            minioClient.downloadObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param headers     Additional headers to put on the file. The map MUST be mutable. All custom headers will start with 'x-amz-meta-' prefix when fetched with {@code getMetadata()} method.
     * @throws MinioException if an error occur while uploading object
     */
    @Override
    public void upload(String source, InputStream file, Map<String, String> headers) throws
            MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @throws MinioException if an error occur while uploading object
     */
    @Override
    public void upload(String source, InputStream file) throws
            MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .build();
            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     * @param headers     Additional headers to put on the file. The map MUST be mutable
     * @throws MinioException if an error occur while uploading object
     */
    @Override
    public void upload(String source, InputStream file, String contentType, Map<String, String> headers) throws
            MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .headers(headers)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     *
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an inputstream
     * @param contentType MIME type for the object
     * @throws MinioException if an error occur while uploading object
     */
    @Override
    public void upload(String source, InputStream file, String contentType) throws
            MinioException {
        try {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .stream(file, file.available(), -1)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }

    /**
     * Upload a file to Minio
     * upload file bigger than Xmx size
     * @param source      Path with prefix to the object. Object name must be included.
     * @param file        File as an Filename
     * @throws MinioException if an error occur while uploading object
     */
    @Override
    public void upload(String source, File file) throws
            MinioException {
        try {
            UploadObjectArgs args = UploadObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .filename(file.getAbsolutePath())
                    .build();
            minioClient.uploadObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }


    /**
     * Remove a file to Minio
     *
     * @param source Path with prefix to the object. Object name must be included.
     * @throws MinioException if an error occur while removing object
     */
    @Override
    public void remove(String source) throws MinioException {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(configurationProperties.getBucket())
                    .object(source.toString())
                    .build();
            minioClient.removeObject(args);
        } catch (Exception e) {
            throw new MinioException("Error while fetching files in Minio", e);
        }
    }
}
