package org.mastersmp.packet.nms;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public interface RandomBridge {

    Object gameSource();

    <T> Object weightedOf(Map<T, Integer> weights);

    <T> T weightedPick(Object weightedList);

    default int nextInt(int bound) {
        return ThreadLocalRandom.current().nextInt(bound);
    }

    default <T> T pickWeighted(Map<T, Integer> weights) {
        int total = 0;
        for (Integer weight : weights.values()) {
            if (weight != null && weight > 0) {
                total += weight;
            }
        }
        if (total <= 0) {
            return null;
        }
        int roll = nextInt(total);
        int cursor = 0;
        for (Map.Entry<T, Integer> entry : weights.entrySet()) {
            Integer weight = entry.getValue();
            if (weight == null || weight <= 0) {
                continue;
            }
            cursor += weight;
            if (roll < cursor) {
                return entry.getKey();
            }
        }
        return weights.keySet().stream().findFirst().orElse(null);
    }

    default <T> T pickWeighted(Collection<Map.Entry<T, Integer>> weights) {
        Map<T, Integer> map = weights.stream().collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                Integer::sum,
                java.util.LinkedHashMap::new
        ));
        return pickWeighted(map);
    }
}
