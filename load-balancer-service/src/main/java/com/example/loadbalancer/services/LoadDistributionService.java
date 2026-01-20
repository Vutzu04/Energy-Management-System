package com.example.loadbalancer.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class LoadDistributionService {

    private static final int REPLICA_COUNT = 3;
    private static final int[] REPLICA_WEIGHTS = {5, 3, 2};  // 50%, 30%, 20%

    /**
     * Strategy 1: Consistent Hashing
     * Same device always goes to the same replica
     * This ensures data locality and consistency
     */
    public int selectReplicaConsistentHash(UUID deviceId) {
        int hash = Math.abs(deviceId.hashCode());
        int selected = (hash % REPLICA_COUNT) + 1;

        System.out.println("⚖️  Load Balancer: Consistent Hash");
        System.out.println("   Device ID: " + deviceId);
        System.out.println("   Selected Replica: " + selected);

        return selected;
    }

    /**
     * Strategy 2: Round Robin
     * Distributes messages evenly across replicas
     */
    private AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public int selectReplicaRoundRobin() {
        int selected = (roundRobinCounter.getAndIncrement() % REPLICA_COUNT) + 1;

        System.out.println("⚖️  Load Balancer: Round Robin");
        System.out.println("   Selected Replica: " + selected);

        return selected;
    }

    /**
     * Strategy 3: Weighted Distribution
     * 50% to Replica 1, 30% to Replica 2, 20% to Replica 3
     */
    public int selectReplicaWeighted() {
        Random random = new Random();
        int randomValue = random.nextInt(10);

        int selected;
        if (randomValue < 5) {
            selected = 1;  // 50%
        } else if (randomValue < 8) {
            selected = 2;  // 30%
        } else {
            selected = 3;  // 20%
        }

        System.out.println("⚖️  Load Balancer: Weighted Distribution");
        System.out.println("   Random Value: " + randomValue);
        System.out.println("   Selected Replica: " + selected);

        return selected;
    }

    /**
     * Default strategy: Use Consistent Hashing
     * This is the most suitable for ensuring data consistency
     */
    public int selectReplica(UUID deviceId) {
        return selectReplicaConsistentHash(deviceId);
    }

}

