package Block;

import Burner.GameLogic;

public class PowerUpEffects {
    // Defensive delegates
    public static void doDefensiveOne(PowerUpBlock block, GameLogic logic) {
        DefensivePowerUpEffects.doDefensiveOne(block, logic);
    }

    public static void doDefensiveTwo(PowerUpBlock block, GameLogic logic) {
        DefensivePowerUpEffects.doDefensiveTwo(block, logic);
    }

    public static void doDefensiveThree(PowerUpBlock block, GameLogic logic) {
        DefensivePowerUpEffects.doDefensiveThree(block, logic);
    }

    public static void doDefensiveFour(PowerUpBlock block, GameLogic logic) {
        DefensivePowerUpEffects.doDefensiveFour(block, logic);
    }

    public static void doDefensiveFive(PowerUpBlock block, GameLogic logic) {
        DefensivePowerUpEffects.doDefensiveFive(block, logic);
    }

    public static void doDefensiveSix(PowerUpBlock block, GameLogic logic) {
        DefensivePowerUpEffects.doDefensiveSix(block, logic);
    }

    // Offensive delegates
    public static void doOffensiveOne(PowerUpBlock block, GameLogic logic) {
        OffensivePowerUpEffects.doOffensiveOne(block, logic);
    }

    public static void doOffensiveTwo(PowerUpBlock block, GameLogic logic) {
        OffensivePowerUpEffects.doOffensiveTwo(block, logic);
    }

    public static void doOffensiveThree(PowerUpBlock block, GameLogic logic) {
        OffensivePowerUpEffects.doOffensiveThree(block, logic);
    }

    public static void doOffensiveFour(PowerUpBlock block, GameLogic logic) {
        OffensivePowerUpEffects.doOffensiveFour(block, logic);
    }

    public static void doOffensiveFive(PowerUpBlock block, GameLogic logic) {
        OffensivePowerUpEffects.doOffensiveFive(block, logic);
    }

    public static void doOffensiveSix(PowerUpBlock block, GameLogic logic) {
        OffensivePowerUpEffects.doOffensiveSix(block, logic);
    }


}