package me.littlecheesecake.imageviewwidget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import static me.littlecheesecake.maskedimageview.R.styleable;

/**
 * An {@link android.widget.ImageView} that draws its contents inside a mask and
 * draws a border drawable on top. This is useful for applying a bezel look to image
 * contents, but is also flexible enough for use with other desired aesthetics
 *
 * Created by yulu on 21/11/15.
 */
public class MaskedImageView extends ImageView {
    private Paint blackPaint;
    private Paint maskedPaint;

    private Rect bounds;
    private RectF boundsF;

    private Drawable borderDrawable;
    private Drawable maskDrawable;

    private ColorMatrixColorFilter desaturateColorFilter;
    private boolean desaturateOnPress = false;

    private boolean cacheValid = false;
    private Bitmap cacheBitmap;
    private int cachedWidth;
    private int cachedHeight;

    public MaskedImageView(Context context) {
        this(context, null);
    }

    public MaskedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setLayerType(LAYER_TYPE_HARDWARE, null);

        final TypedArray a = context.obtainStyledAttributes(attrs, styleable.MaskedImageView,
                defStyleAttr, 0);

        maskDrawable = a.getDrawable(styleable.MaskedImageView_maskDrawable);
        if (maskDrawable != null) {
            maskDrawable.setCallback(this);
        }

        borderDrawable = a.getDrawable(styleable.MaskedImageView_borderDrawable);
        if (borderDrawable != null) {
            borderDrawable.setCallback(this);
        }

        desaturateOnPress = a.getBoolean(styleable.MaskedImageView_desaturateOnPress,
                desaturateOnPress);

        a.recycle();

        // other initialization
        blackPaint = new Paint();
        blackPaint.setColor(0xffff0000);
        blackPaint.setAntiAlias(true);

        maskedPaint = new Paint();
        maskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        maskedPaint.setAntiAlias(true);

        cacheBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        if (desaturateOnPress) {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            desaturateColorFilter = new ColorMatrixColorFilter(cm);
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        final boolean changed = super.setFrame(l, t, r, b);
        bounds = new Rect(0, 0, r - l, b - t);
        boundsF = new RectF(bounds);

        if (borderDrawable != null) {
            borderDrawable.setBounds(bounds);
        }

        if (maskDrawable != null) {
            maskDrawable.setBounds(bounds);
        }

        if (changed) {
            cacheValid = false;
        }

        return changed;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bounds == null) {
            return;
        }

        int width = bounds.width();
        int height = bounds.height();

        if (width == 0 || height == 0) {
            return;
        }

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));

        if (!cacheValid || width != cachedWidth || height != cachedHeight) {
            if (width == cachedWidth && height == cachedHeight) {
                // Have a correct-sized bitmap cache already allocated, just erase it
                cacheBitmap.eraseColor(0);
            } else {
                // Allocate a new bitmap with correct dimensions
                cacheBitmap.recycle();
                cacheBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                cachedWidth = width;
                cachedHeight = height;
            }

            Canvas cacheCanvas = new Canvas(cacheBitmap);
            cacheCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            if (maskDrawable != null) {
                int sc = cacheCanvas.save();
                maskDrawable.draw(cacheCanvas);
                maskedPaint.setColorFilter((desaturateOnPress && isPressed()) ? desaturateColorFilter : null);
                cacheCanvas.saveLayer(boundsF, maskedPaint,
                        Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
                Log.d("ImageView: ", String.valueOf(isPressed()));
                super.onDraw(cacheCanvas);
                cacheCanvas.restoreToCount(sc);
            } else if (desaturateOnPress && isPressed()) {
                int sc = cacheCanvas.save();
                cacheCanvas.drawRect(0, 0, cachedWidth, cachedHeight, blackPaint);
                maskedPaint.setColorFilter(desaturateColorFilter);
                cacheCanvas.saveLayer(boundsF, maskedPaint,
                        Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG);
                super.onDraw(cacheCanvas);
                cacheCanvas.restoreToCount(sc);
            } else {
                super.onDraw(cacheCanvas);
            }

            if (borderDrawable != null) {
                borderDrawable.draw(cacheCanvas);
            }
        }

        canvas.drawBitmap(cacheBitmap, bounds.left, bounds.top, null);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (borderDrawable != null && borderDrawable.isStateful()) {
            borderDrawable.setState(getDrawableState());
        }
        if (maskDrawable != null && maskDrawable.isStateful()) {
            maskDrawable.setState(getDrawableState());
        }
        if (isDuplicateParentStateEnabled()) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        if (who == borderDrawable || who == maskDrawable) {
            invalidate();
        } else {
            super.invalidateDrawable(who);
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == borderDrawable || who == maskDrawable || super.verifyDrawable(who);
    }
}
