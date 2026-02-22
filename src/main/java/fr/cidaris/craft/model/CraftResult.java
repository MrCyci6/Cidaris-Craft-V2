package fr.cidaris.craft.model;

import fr.cidaris.craft.model.enums.ResultMode;
import java.util.List;

public class CraftResult {
    private final ResultMode mode;
    private final int amount;
    private final List<String> commands;

    public CraftResult(ResultMode mode, int amount, List<String> commands) {
        this.mode = mode;
        this.amount = amount;
        this.commands = commands;
    }

    public ResultMode getMode() { return mode; }
    public int getAmount() { return amount; }
    public List<String> getCommands() { return commands; }
}