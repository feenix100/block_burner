// PowerUpType.java
package Block;

import Burner.GameLogic;

import java.util.function.BiConsumer;

public enum PowerUpType {
    // Offensive (col=1), rows 0–5
    OFFENSE_ONE   (PowerUpCategory.OFFENSIVE, 1, 0, PowerUpEffects::doOffensiveOne),
    OFFENSE_TWO   (PowerUpCategory.OFFENSIVE, 1, 1, PowerUpEffects::doOffensiveTwo),
    OFFENSE_THREE (PowerUpCategory.OFFENSIVE, 1, 2, PowerUpEffects::doOffensiveThree),
    OFFENSE_FOUR  (PowerUpCategory.OFFENSIVE, 1, 3, PowerUpEffects::doOffensiveFour),
    OFFENSE_FIVE  (PowerUpCategory.OFFENSIVE, 1, 4, PowerUpEffects::doOffensiveFive),
    OFFENSE_SIX   (PowerUpCategory.OFFENSIVE, 1, 5, PowerUpEffects::doOffensiveSix),



    // Defensive (col=2), rows 0–5
    DEFENSE_ONE   (PowerUpCategory.DEFENSIVE, 2, 0, PowerUpEffects::doDefensiveOne),
    DEFENSE_TWO   (PowerUpCategory.DEFENSIVE, 2, 1, PowerUpEffects::doDefensiveTwo),
    DEFENSE_THREE (PowerUpCategory.DEFENSIVE, 2, 2, PowerUpEffects::doDefensiveThree),
    DEFENSE_FOUR  (PowerUpCategory.DEFENSIVE, 2, 3, PowerUpEffects::doDefensiveFour),
    DEFENSE_FIVE  (PowerUpCategory.DEFENSIVE, 2, 4, PowerUpEffects::doDefensiveFive),
    DEFENSE_SIX   (PowerUpCategory.DEFENSIVE, 2, 5, PowerUpEffects::doDefensiveSix);

    private final PowerUpCategory category;
    private final int sheetColumn, sheetRow;
    private final BiConsumer<PowerUpBlock,GameLogic> effect;

    PowerUpType(PowerUpCategory category, int col, int row,
                BiConsumer<PowerUpBlock,GameLogic> effect) {
        this.category    = category;
        this.sheetColumn = col;
        this.sheetRow    = row;
        this.effect      = effect;
    }

    public PowerUpCategory getCategory()   { return category; }
    public int              getSheetColumn(){ return sheetColumn; }
    public int              getSheetRow()   { return sheetRow; }

    /** Called by PowerUpBlock.activate() */
    public void apply(PowerUpBlock block, GameLogic logic) {
        effect.accept(block, logic);
    }
}
