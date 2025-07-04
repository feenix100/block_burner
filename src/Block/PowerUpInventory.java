package Block;

import java.util.ArrayList;
import java.util.List;

public class PowerUpInventory {
    private static final int MAX_STORED = 10;
    private final List<PowerUpBlock> stored = new ArrayList<>();

    /** Add a cleared power-up to the stash, up to MAX_STORED */
    public void add(PowerUpBlock pu) {
        if (stored.size() < MAX_STORED) {
            stored.add(pu);
        }
        // if inventory is full, ignore additional power-ups until some are used
    }

    /** Remove (and return) the first stored power-up, or null if none left */
    public PowerUpBlock use() {
        if (stored.isEmpty()) return null;
        return stored.remove(0);
    }

    /** Get an immutable snapshot for rendering */
    public List<PowerUpBlock> getStored() {
        return List.copyOf(stored);
    }
}
