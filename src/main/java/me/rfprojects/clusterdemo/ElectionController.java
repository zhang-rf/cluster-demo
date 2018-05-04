package me.rfprojects.clusterdemo;

import org.springframework.web.bind.annotation.*;

@RequestMapping("/election")
@RestController
public class ElectionController {

    private final ClusterService clusterService;
    private int lastTerm;

    public ElectionController(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @PostMapping("/heartbeat")
    public void heartbeat(@RequestBody Node master) {
        clusterService.setMaster(master);
        clusterService.resetTimeoutTask();
    }

    @GetMapping("/tick")
    public boolean heartbeat(@RequestParam int term) {
        try {
            return !clusterService.isMaster();
        } catch (Exception ignored) {
        }
        if (term > clusterService.getTerm() || term != lastTerm) {
            lastTerm = term;
            clusterService.setTerm(term);
            clusterService.resetTimeoutTask();
            return true;
        }
        return false;
    }
}
