package me.rfprojects.clusterdemo;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ClusterService {

    private ServerProperties serverProperties;
    private ClusterProperties clusterProperties;
    private int minElectionIntervalMs;
    private int maxElectionIntervalMs;
    private volatile ScheduledTask electionTask;
    private volatile ScheduledTask heartbeatTask;
    private volatile int term;
    private RestTemplate restTemplate = new RestTemplate();
    private volatile Node master;

    public ClusterService(ServerProperties serverProperties, ClusterProperties clusterProperties) {
        this.serverProperties = serverProperties;
        this.clusterProperties = clusterProperties;
        minElectionIntervalMs = clusterProperties.getElectionIntervalMs() -
                clusterProperties.getElectionIntervalMs() / 2;
        maxElectionIntervalMs = clusterProperties.getElectionIntervalMs() +
                clusterProperties.getElectionIntervalMs() / 2;
        electionTask = new ScheduledTask(new ElectionTaskRunnable(), () -> ThreadLocalRandom.current()
                .nextInt(minElectionIntervalMs, maxElectionIntervalMs));
    }

    public void resetTimeoutTask() {
        if (electionTask != null) {
            electionTask.reset();
        }
    }

    public boolean isMaster() {
        Node master = getMaster();
        return Objects.equals(master.getAddress(), serverProperties.getAddress()) &&
                master.getPort() == serverProperties.getPort();
    }

    public Node getMaster() {
        if (master == null) {
            throw new ElectingException();
        }
        return master;
    }

    public List<String> getHosts() {
        return Collections.unmodifiableList(clusterProperties.getHosts());
    }

    public ClusterService setMaster(Node master) {
        this.master = master;
        return this;
    }

    public int getTerm() {
        return term;
    }

    public ClusterService setTerm(int term) {
        this.term = term;
        return this;
    }

    private class ElectionTaskRunnable implements Runnable {
        @Override
        public void run() {
            term++;
            int ticks = 0;
            for (String host : clusterProperties.getHosts()) {
                if (!Objects.equals(host, serverProperties.getAddress() + ":" + serverProperties.getPort())) {
                    try {
                        String result = restTemplate.getForObject(
                                String.format("http://%s/election/tick?term=%s", host, term), String.class);
                        if (Objects.equals(result, Boolean.TRUE.toString())) {
                            ticks++;
                        }
                    } catch (RestClientException ignored) {
                    }
                }
            }

            System.out.println("got ticks:" + ticks);
            if (ticks >= (clusterProperties.getHosts().size() + 1) / 2) {
                master = new Node(term, serverProperties.getAddress(), serverProperties.getPort());
                heartbeatTask = new ScheduledTask(new HeartbeatTaskRunnable(), clusterProperties.getHeartbeatIntervalMs());
                new Thread(() -> {
                    electionTask.cancel();
                    electionTask = null;
                }).start();
            }
        }
    }

    private class HeartbeatTaskRunnable implements Runnable {
        @Override
        public void run() {
            for (String host : clusterProperties.getHosts()) {
                if (!Objects.equals(host, serverProperties.getAddress() + ":" + serverProperties.getPort())) {
                    restTemplate.postForObject(String.format("http://%s/election/heartbeat", host),
                            new Node(term, serverProperties.getAddress(), serverProperties.getPort()), String.class);
                }
            }
        }
    }
}
