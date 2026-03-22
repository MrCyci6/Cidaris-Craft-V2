package fr.cidaris.craft.model;

import java.util.List;
import java.util.Map;

public class CraftDefinition {
    private final String id;
    private final String name;
    private final List<String> parents;
    private final List<Cost> costs;
    private final List<String> commands;
    private final String rarity;

    private final int guiX;
    private final int guiY;
    public CraftDefinition(String id, String name, List<String> parents, List<Cost> costs,
                           List<String> commands, String rarity,
                           int guiX, int guiY) {
        this.id = id;
        this.name = name;
        this.parents = parents;
        this.costs = costs;
        this.commands = commands;
        this.rarity = rarity;
        this.guiX = guiX;
        this.guiY = guiY;
    }
    public int getGuiX() { return guiX; }
    public int getGuiY() { return guiY; }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<String> getParents() { return parents; }
    public List<Cost> getCosts() { return costs; }
    public List<String> getCommands() {
        return commands;
    }

    public String getRarity() {
        return rarity;
    }
}