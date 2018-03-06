package yukiito.campusmap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;


/**
 * Class that represents UW map and paths
 */
public class MapDrawView extends AppCompatImageView {

    private float[] points = null; // Array of floats for the paths lines, null if empty
    private float[] start = null; // Coordinate for the start building, null if empty
    private float[] end = null; // Coordinate for the destination building, null if empty
    Context context;

    public MapDrawView(Context context) {
        super(context);
        this.context = context;
    }

    public MapDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

    }

    public MapDrawView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.scale(0.25f, 0.25f); // Scaling the canvas to fit the map
        if (this.points != null) {
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(15);
            canvas.drawLines(points, paint);
        }
        if (this.start != null) {
            float x = start[0];
            float y = start[1];
            Paint paint = new Paint();
            paint.setColor(Color.CYAN);
            canvas.drawCircle(x - 25, y - 25, 25, paint);
        }
        if (this.end != null) {
            float x = end[0];
            float y = end[1];
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            canvas.drawCircle(x - 25, y - 25, 25, paint);
        }

    }

    /**
     * sets the path between two buildings
     * @param points array of points for the path.
     *               It should be [x1, y1, x2, y2, x2, y2, x3, y3, x3, y3, ...]
     *               since it draws separate lines.
     */
    public void setPaths(float[] points) {
        this.points = points;
        this.invalidate();
    }

    /**
     * sets the starting building
     * @param coord coordiate for the start building in the format [x, y]
     *              can be null for deleting the mark on the map
     */
    public void setStart(float[] coord) {
        this.start = coord;
        this.invalidate();
    }

    /**
     * sets the destinatino building
     * @param coord coordiate for the end building in the format [x, y]
     *               can be null for deleting the mark on the map
     */
    public void setEnd(float[] coord) {
        this.end = coord;
        this.invalidate();
    }

    /**
     * clears all drawings everything off the map
     */
    public void clear() {
        this.points = null;
        this.start = null;
        this.end = null;
        this.invalidate();
    }
}

