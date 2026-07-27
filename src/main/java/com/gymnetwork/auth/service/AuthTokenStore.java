package com.gymnetwork.auth.service;

import java.util.concurrent.TimeUnit;

public interface AuthTokenStore {

    void set(String key, Object value, long timeout, TimeUnit unit);

    Object get(String key);

    void delete(String key);
}
