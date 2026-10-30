package com.filmox.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.widget.ImageView;

/** ImageView com cantos arredondados (clip por path). */
public class RoundedImageView extends ImageView {
    private final float radius;
    private final Path path = new Path();
    public RoundedImageView(Context c, float radiusDp) {
        super(c);
        this.radius = Ui.dp(c, radiusDp);
        setScaleType(ScaleType.CENTER_CROP);
    }
    @Override protected void onDraw(Canvas canvas) {
        path.reset();
        path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), radius, radius, Path.Direction.CW);
        canvas.save();
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.restore();
    }
}
