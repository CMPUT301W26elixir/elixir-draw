package com.example.allot.view.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Lets the user pan and zoom an image within a fixed square crop window.
 */
public class ProfilePhotoCropView extends AppCompatImageView {
    private static final float MAX_SCALE_MULTIPLIER = 4f;

    private final Matrix drawMatrix = new Matrix();
    private final RectF cropRect = new RectF();
    private final RectF imageRect = new RectF();
    private final RectF mappedImageRect = new RectF();
    private final Paint scrimPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final ScaleGestureDetector scaleGestureDetector;

    private Bitmap bitmap;
    private float baseScale = 1f;
    private float currentScale = 1f;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging;

    /**
     * Creates a new ProfilePhotoCropView instance.
     */
    public ProfilePhotoCropView(@NonNull Context context) {
        this(context, null);
    }

    /**
     * Creates a new ProfilePhotoCropView instance.
     */
    public ProfilePhotoCropView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a new ProfilePhotoCropView instance.
     */
    public ProfilePhotoCropView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        scrimPaint.setColor(0xAA000000);
        scrimPaint.setStyle(Paint.Style.FILL);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dpToPx(2));
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    /**
     * Updates crop bitmap.
     */
    public void setCropBitmap(@Nullable Bitmap bitmap) {
        this.bitmap = bitmap;
        if (bitmap == null) {
            setImageDrawable(null);
            return;
        }

        imageRect.set(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
        setImageBitmap(bitmap);
        post(this::resetImagePosition);
    }

    /**
     * Builds cropped bitmap.
     */
    @Nullable
    public Bitmap buildCroppedBitmap(int outputSizePx) {
        if (bitmap == null || cropRect.width() <= 0f || cropRect.height() <= 0f) {
            return null;
        }

        Matrix inverse = new Matrix();
        if (!drawMatrix.invert(inverse)) {
            return null;
        }

        RectF sourceRect = new RectF(cropRect);
        inverse.mapRect(sourceRect);
        sourceRect.intersect(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
        if (sourceRect.width() <= 0f || sourceRect.height() <= 0f) {
            return null;
        }

        Rect src = new Rect(
                Math.max(0, Math.round(sourceRect.left)),
                Math.max(0, Math.round(sourceRect.top)),
                Math.min(bitmap.getWidth(), Math.round(sourceRect.right)),
                Math.min(bitmap.getHeight(), Math.round(sourceRect.bottom))
        );
        if (src.width() <= 0 || src.height() <= 0) {
            return null;
        }

        Bitmap output = Bitmap.createBitmap(outputSizePx, outputSizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawBitmap(bitmap, src, new Rect(0, 0, outputSizePx, outputSizePx), null);
        return output;
    }

    /**
     * Handles on Size Changed.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateCropRect(w, h);
        resetImagePosition();
    }

    /**
     * Handles on Touch Event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (bitmap == null) {
            return false;
        }

        scaleGestureDetector.onTouchEvent(event);
        if (scaleGestureDetector.isInProgress()) {
            return true;
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                isDragging = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!isDragging) {
                    return false;
                }
                float dx = event.getX() - lastTouchX;
                float dy = event.getY() - lastTouchY;
                drawMatrix.postTranslate(dx, dy);
                constrainImage();
                setImageMatrix(drawMatrix);
                invalidate();
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    /**
     * Handles on Draw.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (cropRect.width() <= 0f || cropRect.height() <= 0f) {
            return;
        }

        canvas.drawRect(0f, 0f, getWidth(), cropRect.top, scrimPaint);
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, scrimPaint);
        canvas.drawRect(cropRect.right, cropRect.top, getWidth(), cropRect.bottom, scrimPaint);
        canvas.drawRect(0f, cropRect.bottom, getWidth(), getHeight(), scrimPaint);
        canvas.drawRect(cropRect, borderPaint);
    }

    /**
     * Updates crop rect.
     */
    private void updateCropRect(int width, int height) {
        float margin = dpToPx(24);
        float size = Math.min(width - (margin * 2f), height - (margin * 2f));
        float left = (width - size) / 2f;
        float top = (height - size) / 2f;
        cropRect.set(left, top, left + size, top + size);
    }

    /**
     * Handles reset Image Position.
     */
    private void resetImagePosition() {
        if (bitmap == null || getWidth() == 0 || getHeight() == 0 || cropRect.width() <= 0f) {
            return;
        }

        drawMatrix.reset();
        float bitmapWidth = bitmap.getWidth();
        float bitmapHeight = bitmap.getHeight();
        baseScale = Math.max(cropRect.width() / bitmapWidth, cropRect.height() / bitmapHeight);
        currentScale = baseScale;
        float scaledWidth = bitmapWidth * baseScale;
        float scaledHeight = bitmapHeight * baseScale;
        float translateX = cropRect.centerX() - (scaledWidth / 2f);
        float translateY = cropRect.centerY() - (scaledHeight / 2f);
        drawMatrix.postScale(baseScale, baseScale);
        drawMatrix.postTranslate(translateX, translateY);
        setImageMatrix(drawMatrix);
        invalidate();
    }

    /**
     * Handles constrain Image.
     */
    private void constrainImage() {
        if (bitmap == null) {
            return;
        }

        drawMatrix.mapRect(mappedImageRect, imageRect);
        float dx = 0f;
        float dy = 0f;

        if (mappedImageRect.left > cropRect.left) {
            dx = cropRect.left - mappedImageRect.left;
        } else if (mappedImageRect.right < cropRect.right) {
            dx = cropRect.right - mappedImageRect.right;
        }

        if (mappedImageRect.top > cropRect.top) {
            dy = cropRect.top - mappedImageRect.top;
        } else if (mappedImageRect.bottom < cropRect.bottom) {
            dy = cropRect.bottom - mappedImageRect.bottom;
        }

        drawMatrix.postTranslate(dx, dy);
    }

    /**
     * Handles dp To Px.
     */
    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        /**
         * Handles on Scale.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (bitmap == null) {
                return false;
            }

            float scaleFactor = detector.getScaleFactor();
            float targetScale = currentScale * scaleFactor;
            float minScale = baseScale;
            float maxScale = baseScale * MAX_SCALE_MULTIPLIER;

            if (targetScale < minScale) {
                scaleFactor = minScale / currentScale;
                targetScale = minScale;
            } else if (targetScale > maxScale) {
                scaleFactor = maxScale / currentScale;
                targetScale = maxScale;
            }

            drawMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            currentScale = targetScale;
            constrainImage();
            setImageMatrix(drawMatrix);
            invalidate();
            return true;
        }
    }
}
