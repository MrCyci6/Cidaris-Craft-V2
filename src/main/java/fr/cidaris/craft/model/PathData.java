package fr.cidaris.craft.model;

import java.util.HashSet;
import java.util.Set;

public class PathData {
    private final Set<String> targetCrafts = new HashSet<>();

    public void addTarget(String craftId) {
        this.targetCrafts.add(craftId);
    }

    public Set<String> getTargets() {
        return targetCrafts;
    }
}