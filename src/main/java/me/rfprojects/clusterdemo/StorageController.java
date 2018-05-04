package me.rfprojects.clusterdemo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RequestMapping("/storage")
@RestController
public class StorageController {

    private final StorageService storageService;
    private final ClusterService clusterService;
    private RestTemplate restTemplate = new RestTemplate();

    public StorageController(StorageService storageService, ClusterService clusterService) {
        this.storageService = storageService;
        this.clusterService = clusterService;
    }

    @PostMapping("/{key}")
    public void put(@PathVariable String key, @RequestBody String value) {
        if (clusterService.isMaster()) {
            for (String host : clusterService.getHosts()) {
                restTemplate.postForObject(String.format("http://%s/storage/internal/%s", host, key), value, String.class);
            }
        } else {
            Node master = clusterService.getMaster();
            restTemplate.postForObject(String.format("http://%s:%s/storage/%s",
                    master.getAddress(), master.getPort(), key), value, String.class);
        }
    }

    @GetMapping("/{key}")
    public String get(@PathVariable String key) {
        return storageService.get(key);
    }

    @GetMapping("/")
    public String list() {
        return storageService.toString();
    }

    @PostMapping("/internal/{key}")
    public void putInternal(@PathVariable String key, @RequestBody String value) {
        storageService.put(key, value);
    }
}
