package fr.cidaris.craft.model;

import fr.cidaris.craft.model.enums.CostType;

public class Cost {
    private final CostType type;
    private final double amount;
    private final String id;

    public Cost(CostType type, double amount, String id) {
        this.type = type;
        this.amount = amount;
        this.id = id;
    }

    public CostType getType() { return type; }
    public double getAmount() { return amount; }
    public String getId() { return id; }

    public String display() {
        return type == CostType.MONEY ? "Money: " + amount : "Item(" + id + "): " + amount;
    }
}