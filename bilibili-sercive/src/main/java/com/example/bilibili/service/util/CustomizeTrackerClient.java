package com.example.bilibili.service.util;

import com.github.tobato.fastdfs.domain.conn.TrackerConnectionManager;
import com.github.tobato.fastdfs.domain.fdfs.StorageNode;
import com.github.tobato.fastdfs.domain.fdfs.StorageNodeInfo;
import com.github.tobato.fastdfs.domain.fdfs.TrackerLocator;
import com.github.tobato.fastdfs.service.DefaultTrackerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Fixed the problem that new version of fastDFS is retrieving Storage port as 0
 * This is a customized TrackerClient that overrides the default implementation
 * with @Primary annotation to take precedence over the default TrackerClient bean
 */
@Primary
@Component
public class CustomizeTrackerClient extends DefaultTrackerClient {

    private static final int DEFAULT_PORT = 23000;

    @Autowired
    private TrackerConnectionManager trackerConnectionManager;

    @Autowired
    private TrackerLocator trackerLocator;

    public CustomizeTrackerClient() {
        super();
    }

    @PostConstruct
    public void init() {
        // Manually inject the TrackerConnectionManager via reflection
        if (trackerConnectionManager != null && trackerLocator != null) {
            try {
                // Set trackerConnectionManager in parent class via reflection
                java.lang.reflect.Field trackerField = DefaultTrackerClient.class.getDeclaredField("trackerConnectionManager");
                trackerField.setAccessible(true);
                trackerField.set(this, trackerConnectionManager);

                // Set trackerLocator in TrackerConnectionManager via reflection
                java.lang.reflect.Field locatorField = TrackerConnectionManager.class.getDeclaredField("trackerLocator");
                locatorField.setAccessible(true);
                locatorField.set(trackerConnectionManager, trackerLocator);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject dependencies into CustomizeTrackerClient", e);
            }
        }
    }

    @Override
    public StorageNode getStoreStorage() {
        StorageNode res = super.getStoreStorage();
        if (res != null) {
            res.setPort(getPort(res.getPort()));
        }
        return res;
    }

    @Override
    public StorageNode getStoreStorage(String groupName) {
        StorageNode res = super.getStoreStorage(groupName);
        if (res != null) {
            res.setPort(getPort(res.getPort()));
        }
        return res;
    }

    @Override
    public StorageNodeInfo getFetchStorage(String groupName, String filename) {
        StorageNodeInfo res = super.getFetchStorage(groupName, filename);
        if (res != null) {
            res.setPort(getPort(res.getPort()));
        }
        return res;
    }

    @Override
    public StorageNodeInfo getUpdateStorage(String groupName, String filename) {
        StorageNodeInfo res = super.getUpdateStorage(groupName, filename);
        if (res != null) {
            res.setPort(getPort(res.getPort()));
        }
        return res;
    }

    private int getPort(int port){
        if(port == 0){
            return DEFAULT_PORT;
        }
        return port;
    }
}