package com.example.bilibili.service.config;

import com.github.tobato.fastdfs.domain.conn.ConnectionPoolConfig;
import com.github.tobato.fastdfs.domain.conn.FdfsConnectionPool;
import com.github.tobato.fastdfs.domain.conn.PooledConnectionFactory;
import com.github.tobato.fastdfs.domain.conn.TrackerConnectionManager;
import com.github.tobato.fastdfs.domain.fdfs.ThumbImageConfig;
import com.github.tobato.fastdfs.domain.fdfs.TrackerLocator;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.DefaultAppendFileStorageClient;
import com.github.tobato.fastdfs.service.DefaultFastFileStorageClient;
import com.github.tobato.fastdfs.service.DefaultGenerateStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.github.tobato.fastdfs.service.GenerateStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;

import java.util.Collections;

/**
 * Manual FastDFS configuration for Spring Boot 3.x compatibility
 *
 * This configuration creates all necessary FastDFS beans manually.
 * TrackerLocator will be injected into TrackerConnectionManager via reflection
 * in CustomizeTrackerClient's @PostConstruct method.
 */
@Configuration
@EnableMBeanExport(registration = RegistrationPolicy.IGNORE_EXISTING)
public class FastDFSConfig {

    @Value("${fdfs.tracker-list[0]}")
    private String trackerServer;

    @Value("${fdfs.so-timeout:1500}")
    private int soTimeout;

    @Value("${fdfs.connect-timeout:600}")
    private int connectTimeout;

    @Value("${fdfs.thumb-image.width:150}")
    private int thumbImageWidth;

    @Value("${fdfs.thumb-image.height:150}")
    private int thumbImageHeight;

    /**
     * TrackerLocator - manages tracker server addresses
     */
    @Bean
    public TrackerLocator trackerLocator() {
        return new TrackerLocator(Collections.singletonList(trackerServer));
    }

    /**
     * ThumbImageConfig - configuration for thumbnail images
     */
    @Bean
    public ThumbImageConfig thumbImageConfig() {
        return new ThumbImageConfig() {
            @Override
            public int getWidth() {
                return thumbImageWidth;
            }

            @Override
            public int getHeight() {
                return thumbImageHeight;
            }

            @Override
            public String getPrefixName() {
                return "_" + thumbImageWidth + "x" + thumbImageHeight;
            }

            @Override
            public String getThumbImagePath(String masterFilename) {
                int dotIndex = masterFilename.lastIndexOf('.');
                if (dotIndex == -1) {
                    return masterFilename + getPrefixName();
                }
                return masterFilename.substring(0, dotIndex) + getPrefixName() + masterFilename.substring(dotIndex);
            }
        };
    }

    /**
     * PooledConnectionFactory - creates connections to FastDFS servers
     */
    @Bean
    public PooledConnectionFactory pooledConnectionFactory() {
        PooledConnectionFactory factory = new PooledConnectionFactory();
        factory.setSoTimeout(soTimeout);
        factory.setConnectTimeout(connectTimeout);
        return factory;
    }

    /**
     * ConnectionPoolConfig - configuration for connection pool
     */
    @Bean
    public ConnectionPoolConfig connectionPoolConfig() {
        return new ConnectionPoolConfig();
    }

    /**
     * FdfsConnectionPool - manages connection pool
     */
    @Bean
    public FdfsConnectionPool fdfsConnectionPool(PooledConnectionFactory factory, ConnectionPoolConfig config) {
        return new FdfsConnectionPool(factory, config);
    }

    /**
     * TrackerConnectionManager - manages connections to tracker servers
     * TrackerLocator will be injected later via reflection in CustomizeTrackerClient
     */
    @Bean
    public TrackerConnectionManager trackerConnectionManager(FdfsConnectionPool pool) {
        return new TrackerConnectionManager(pool);
    }

    /**
     * GenerateStorageClient - generates storage paths
     */
    @Bean
    public GenerateStorageClient generateStorageClient() {
        return new DefaultGenerateStorageClient();
    }

    /**
     * FastFileStorageClient - handles standard file operations
     */
    @Bean
    public FastFileStorageClient fastFileStorageClient() {
        return new DefaultFastFileStorageClient();
    }

    /**
     * AppendFileStorageClient - handles appender file operations (for sliced uploads)
     */
    @Bean
    public AppendFileStorageClient appendFileStorageClient() {
        return new DefaultAppendFileStorageClient();
    }
}