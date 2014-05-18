package org.greencheek.web.filter.memcached.hystrix.config;

/**
 * Created by dominictootell on 18/05/2014.
 */
public class BackendConfig {
    private final int semaphoreSize;
    private final int backendTimeoutInMillis;
    private final boolean enabled;


    public BackendConfig(int semaphoreSize, int backendTimeout,boolean enabled) {
        this.semaphoreSize = semaphoreSize;
        this.backendTimeoutInMillis = backendTimeout;
        this.enabled = enabled;

    }

    public int getSemaphoreSize() {
        return semaphoreSize;
    }

    public int getBackendTimeoutInMillis() {
        return backendTimeoutInMillis;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
