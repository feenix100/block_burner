package Burner;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputHandler implements KeyListener {
    private final GameController controller;
    private final int leftKey, rightKey, dropKey, powerUpKey, cycleKey;

    public InputHandler(
            GameController controller,
            int leftKey,
            int rightKey,
            int dropKey,
            int powerUpKey,
            int cycleKey
    ) {
        this.controller = controller;
        this.leftKey    = leftKey;
        this.rightKey   = rightKey;
        this.dropKey    = dropKey;
        this.powerUpKey = powerUpKey;
        this.cycleKey   = cycleKey;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if      (code == leftKey)    controller.moveBlockLeft();
        else if (code == rightKey)   controller.moveBlockRight();
        else if (code == dropKey)    controller.drop();
        else if (code == powerUpKey) controller.activatePowerUp();
        else if (code == cycleKey)   controller.getCurrentBlock().cycleColors();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e)    {}
}
