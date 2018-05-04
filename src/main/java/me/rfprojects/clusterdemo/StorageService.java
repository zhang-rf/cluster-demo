package me.rfprojects.clusterdemo;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class StorageService {

    private ConcurrentMap<String, String> map = new ConcurrentHashMap<>();

    public void put(String key, String value) {
        map.put(key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

    public String list() {
        return map.toString();
    }

    public Map<String, String> map() {
        return Collections.unmodifiableMap(map);
    }
}
