package Block;

import Burner.GameLogic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Timer;

/**
 * A PowerUpBlock with an electric-line wave animation for smoother visuals.
 */
public class PowerUpBlock extends SmallBlock {
    public static final int SIZE = SmallBlock.SIZE;
    private static final String SPRITESHEET_PATH = "/sprites/alt_blocksprites.png";
    private static BufferedImage sheet;

    // Animation settings
    private static final int ANIM_FRAMES = 30;
    private static final int ANIM_DELAY_MS = 100;
    private static Timer animTimer;
    private static int globalFrame = 0;
    private static final Map<PowerUpType, BufferedImage[]> animFrames = new HashMap<>();

    static {
        try {
            sheet = ImageIO.read(PowerUpBlock.class.getResourceAsStream(SPRITESHEET_PATH));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load power-up sprite sheet", e);
        }
        // Pre-generate smooth wave electric-line frames for each power-up type
        for (PowerUpType t : PowerUpType.values()) {
            BufferedImage base = sheet.getSubimage(
                    t.getSheetColumn() * SIZE,
                    t.getSheetRow()    * SIZE,
                    SIZE, SIZE
            );
            BufferedImage[] frames = new BufferedImage[ANIM_FRAMES];
            int segments = 5;
            int segWidth = SIZE / (segments - 1);
            for (int i = 0; i < ANIM_FRAMES; i++) {
                BufferedImage buf = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = buf.createGraphics();
                // Draw base sprite
                g2.drawImage(base, 0, 0, null);
                // Setup for electric lines
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.setStroke(new BasicStroke(4f));
                g2.setColor(t.getCategory() == PowerUpCategory.OFFENSIVE ? Color.RED : Color.BLUE);
                // Generate smooth sine-wave electric lines
                for (int line = 0; line < 2; line++) {
                    int[] xs = new int[segments];
                    int[] ys = new int[segments];
                    for (int p = 0; p < segments; p++) {
                        xs[p] = p * segWidth;
                        double phase = 2 * Math.PI * (i + line * (ANIM_FRAMES / 2.0)) / ANIM_FRAMES;
                        double offset = p * 0.6; // spatial phase offset
                        int amplitude = SIZE / 2 - 4;
                        ys[p] = SIZE / 2 + (int)(amplitude * Math.sin(phase + offset));
                    }
                    g2.drawPolyline(xs, ys, segments);
                }
                g2.dispose();
                frames[i] = buf;
            }
            animFrames.put(t, frames);
        }
        // Start animation timer
        animTimer = new Timer(ANIM_DELAY_MS, e -> globalFrame = (globalFrame + 1) % ANIM_FRAMES);
        animTimer.start();
    }

    private final PowerUpType type;

    /**
     * Construct a PowerUpBlock of the given type.
     */
    public PowerUpBlock(PowerUpType type) {
        super(type.getSheetRow());
        this.type = type;
    }

    /**
     * Returns the current animated frame for this block with smooth electric waves.
     */
    @Override
    public BufferedImage getSprite() {
        return animFrames.get(type)[globalFrame];
    }

    /** Activate this power-up’s effect. */
    public void activate(GameLogic logic) {
        type.apply(this, logic);
    }

    public PowerUpCategory getCategory() { return type.getCategory(); }
    public PowerUpType getType()        { return type; }
}
