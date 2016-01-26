package karataiev.dmytro.connectfour;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * The panel for displaying the current status of the game itself.
 */

public class Connect4Panel extends AppCompatActivity {
    private Connect4Game myGame;    // the game to display
    private int slotRadius;  // size of the individual slots (radius)
    private int slotSpacing; // space between slots
    Activity current;
    ImageView ll;
    int[] dimensions;
    Bitmap bg;

    /**
     * Creates a new Connect4Panel with a given game.
     *
     * @param game the game to display.
     */
    public Connect4Panel(Connect4Game game, Activity current) {
        this.myGame = game;
        this.slotRadius = 71;
        this.slotSpacing = slotRadius + 5;
        this.ll = (ImageView) current.findViewById(R.id.gameField);
        this.current = current;
    }

    /**
     * Paints the current status of the game.
     */
    public void paint() {
        int[] dimensions = imageViewSize();
        Paint paint = new Paint();

        paint.setColor(Color.parseColor("#CD5C5C"));
        bg = Bitmap.createBitmap(dimensions[0], dimensions[1], Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);

        // formula to spread out slots to whole canvas
        slotRadius = (int) ((float) (dimensions[0]) / 7 - (float) dimensions[0] / 7 * 0.07) / 2;
        slotSpacing = slotRadius + (int) ((float) dimensions[0] / 7 * 0.07);

        for (int i = 0; i < myGame.getColumnCount(); i++) {
            for (int j = 0; j < myGame.getRowCount(); j++) {
                Connect4Column column = myGame.getColumn(i);
                Connect4Slot currentSlot = column.getSlot(j);

                if (!currentSlot.getIsFilled()) {
                    paint.setColor(Color.WHITE);
                } else {
                    if (currentSlot.getIsRed()) {
                        paint.setColor(Color.RED);
                    } else {
                        paint.setColor(Color.YELLOW);
                    }
                }
                int x = ((i + 1) * slotSpacing) + (i * slotRadius);
                int y = ((j + 1) * slotSpacing) + (j * slotRadius);
                y += dimensions[1] - dimensions[0] + 2 * slotRadius;

                drawSlot(canvas, x, y, slotRadius, paint);

                // highlight if it is in winning line or if it was filled last
                if (currentSlot.getIsHighlighted() || currentSlot.getLastFilled()) {
                    if (currentSlot.getIsRed()) {
                        paint.setColor(Color.RED);
                    } else {
                        paint.setColor(Color.YELLOW);
                    }
                    drawHighlight(canvas, x, y, slotRadius, paint);
                }
            }
        }
        ll.setImageDrawable(new BitmapDrawable(current.getResources(), bg));
    }

    /**
     * Draw a single slot.
     *
     * @param canvas the canvas to draw on
     * @param x      the center x-coordinate where to draw the slot.
     * @param y      the center y-coordinate where to draw the slot.
     * @param color  the color for the slot.
     */
    public void drawSlot(Canvas canvas, int x, int y, int slotRadius, Paint color) {
        canvas.drawCircle(x, y, slotRadius, color);
    }

    /**
     * Highlight a slot.
     * the graphics object with which to paint.
     *
     * @param canvas the canvas to draw on
     * @param x      the top-left x-coordinate where to draw the highlight.
     * @param y      the top-left y-coordinate where to draw the highlight.
     * @param paint  color to use
     */
    public void drawHighlight(Canvas canvas, int x, int y, int slotRadius, Paint paint) {
        Paint old = new Paint();
        old.setColor(paint.getColor());

        paint.setColor(Color.GREEN);
        canvas.drawCircle(x, y, slotRadius, paint);
        canvas.drawCircle(x, y, slotRadius - 10, old);
    }

    /**
     * Method to get dimensions ot the screen
     *
     * @return dimensions of the current screen
     */
    public int[] imageViewSize() {
        ImageView iv = (ImageView) current.findViewById(R.id.gameField);
        dimensions = new int[2];

        dimensions[0] = iv.getWidth();
        dimensions[1] = iv.getHeight();

        return dimensions;
    }
}

