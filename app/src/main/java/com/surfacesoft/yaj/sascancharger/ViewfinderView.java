
package com.surfacesoft.yaj.sascancharger;

import com.surfacesoft.yaj.sascancharger.R;
import com.surfacesoft.yaj.sascancharger.camera.CameraManager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the result text.
 * <p>
 * The code for this class was adapted from the ZXing project: http://code.google.com/p/zxing
 */
public final class ViewfinderView extends View {
    //private static final long ANIMATION_DELAY = 80L;

    private CameraManager cameraManager;
    private final Paint paint;
    private final int maskColor;
    private final int frameColor;
    private final int cornerColor;
    //  Rect bounds;
    private Rect previewFrame;
    private Rect rect;

    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);
        frameColor = resources.getColor(R.color.viewfinder_frame);
        cornerColor = resources.getColor(R.color.viewfinder_corners);

        //    bounds = new Rect();
        previewFrame = new Rect();
        rect = new Rect();
    }

    public void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    @SuppressWarnings("unused")
    @Override
    public void onDraw(Canvas canvas) {
        Rect frame = cameraManager.getFramingRect();
        if (frame == null) {
            return;
        }
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        // Draw a two pixel solid border inside the framing rect
        paint.setAlpha(0);
        paint.setStyle(Style.FILL);
        paint.setColor(frameColor);
        canvas.drawRect(frame.left, frame.top, frame.right + 1, frame.top + 2, paint);
        canvas.drawRect(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1, paint);
        canvas.drawRect(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1, paint);
        canvas.drawRect(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1, paint);

        // Draw the framing rect corner UI elements
        paint.setColor(cornerColor);
        canvas.drawRect(frame.left - 15, frame.top - 15, frame.left + 15, frame.top, paint);
        canvas.drawRect(frame.left - 15, frame.top, frame.left, frame.top + 15, paint);
        canvas.drawRect(frame.right - 15, frame.top - 15, frame.right + 15, frame.top, paint);
        canvas.drawRect(frame.right, frame.top - 15, frame.right + 15, frame.top + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom, frame.left + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.left - 15, frame.bottom - 15, frame.left, frame.bottom, paint);
        canvas.drawRect(frame.right - 15, frame.bottom, frame.right + 15, frame.bottom + 15, paint);
        canvas.drawRect(frame.right, frame.bottom - 15, frame.right + 15, frame.bottom + 15, paint);


        // Request another update at the animation interval, but don't repaint the entire viewfinder mask.
        //postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }

    public void drawViewfinder() {
        invalidate();
    }

}
