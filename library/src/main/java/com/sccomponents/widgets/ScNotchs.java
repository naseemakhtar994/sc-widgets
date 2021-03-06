package com.sccomponents.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

/**
 * Create a series of notchs that follow an arc path
 * v1.0.3
 */
public class ScNotchs extends ScArc {

    /**
     * Private attributes
     */

    protected int mNotchsCount;
    protected float mNotchsLength;


    /**
     * Private variables
     */

    private OnDrawListener mOnDrawListener = null;


    /**
     * Constructors
     */

    public ScNotchs(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScNotchs(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScNotchs(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    /**
     * Privates methods
     */

    // Check the values limits
    private void checkValues() {
        // Notchs
        if (this.mNotchsCount < 0) this.mNotchsCount = 0;
        if (this.mNotchsLength < 0) this.mNotchsLength = 0;
    }

    // Set the painter type
    private void setPainterType() {
        // Set the stroke type
        switch (this.mStrokeType) {
            case LINE:
            case CLOSED_ARC:
                this.getPainter().setStyle(Paint.Style.STROKE);
                break;

            case FILLED_ARC:
                this.getPainter().setStyle(Paint.Style.FILL_AND_STROKE);
                break;
        }
    }

    // Init the component.
    // Retrieve all attributes with the default values if needed and create the internal using
    // objects.
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ScComponents, defStyle, 0);

        this.mNotchsCount = attrArray.getInt(
                R.styleable.ScComponents_scc_notchs, 0);
        this.mNotchsLength = attrArray.getDimension(
                R.styleable.ScComponents_scc_notchs_length, this.getStrokeSize() * 2);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INTERNAL

        this.checkValues();
        this.setPainterType();
    }

    // Draw the line
    private void drawLine(Canvas canvas, NotchInfo info, RectF area) {
        // Find the start area where find the start point
        RectF startArea = ScNotchs.inflateRect(area, info.length + info.distanceFromBorder);
        RectF endArea = ScNotchs.inflateRect(area, info.distanceFromBorder);

        // Find the start and the end points on the canvas in reference to the arc
        Point startPoint = ScNotchs.getPointFromAngle(info.angle, startArea);
        Point endPoint = ScNotchs.getPointFromAngle(info.angle, endArea);

        // Draw the line
        canvas.drawLine(
                startPoint.x, startPoint.y,
                endPoint.x, endPoint.y,
                this.getPainter()
        );
    }

    // Draw the circle
    private void drawCircle(Canvas canvas, NotchInfo info, RectF area) {
        // Find the start area where find the start point
        float padding = info.size / 2 + info.length + info.distanceFromBorder;
        RectF startArea = ScNotchs.inflateRect(area, padding);

        // Find the point on the arc starting by the angle
        Point startPoint = ScNotchs.getPointFromAngle(info.angle, startArea);

        // Draw the circle
        canvas.drawCircle(
                startPoint.x, startPoint.y,
                info.length,
                this.getPainter()
        );
    }


    /**
     * Overrides
     */

    // Draw the notchs on the canvas
    // TODO: when scaled have notchs visual issue
    @Override
    protected void internalDraw(Canvas canvas, RectF area) {
        // Draw only if the notch length and count is more of zero.
        if (this.mNotchsCount <= 0) return;

        // Calc the delta angle and the real notchs count
        int count = this.mNotchsCount + (this.getAngleSweep() >= ScNotchs.DEFAULT_ANGLE_MAX ? 0 : 1);
        float deltaAngle = this.getAngleSweep() / this.mNotchsCount;

        // Cycle all notchs
        for (int index = 0; index < count; index++) {
            // Find current the angle and length
            float currentAngle = index * deltaAngle;
            // If the current angle is outside the draw limit stop to draw
            if (!ScNotchs.withinRange(currentAngle, 0, this.getAngleDraw())) break;

            // Create the notch info to pass to the listener.
            // Note that adjust the current angle to a global angle.
            NotchInfo info = new NotchInfo();
            info.source = this;
            info.angle = currentAngle + this.mAngleStart;
            info.color = this.mStrokeColor;
            info.index = index;
            info.length = this.mNotchsLength;
            info.size = this.mStrokeSize;
            info.type = this.mStrokeType;

            // Check if the listener is linked
            if (this.mOnDrawListener != null) {
                // Call the method
                this.mOnDrawListener.onDrawNotch(info);

                // Apply the info to the painter
                this.getPainter().setStrokeWidth(info.size);
                this.getPainter().setColor(info.color);
            }

            // Draw only if visible
            if (info.visible) {
                // Draw the line by the case
                switch (info.type) {
                    case LINE:
                        this.drawLine(canvas, info, area);
                        break;

                    case CLOSED_ARC:
                    case FILLED_ARC:
                        this.drawCircle(canvas, info, area);
                        break;
                }
            }
        }
    }


    /**
     * Instance state
     */

    // Save
    @Override
    protected Parcelable onSaveInstanceState() {
        // Call the super and get the parent state
        Parcelable superState = super.onSaveInstanceState();

        // Create a new bundle for store all the variables
        Bundle state = new Bundle();
        // Save all starting from the parent state
        state.putParcelable("PARENT", superState);
        state.putInt("mNotchsCount", this.mNotchsCount);
        state.putFloat("mNotchsLength", this.mNotchsLength);

        // Return the new state
        return state;
    }

    // Restore
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Implicit conversion in a bundle
        Bundle savedState = (Bundle) state;

        // Recover the parent class state and restore it
        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        // Now can restore all the saved variables values
        this.mNotchsCount = savedState.getInt("mNotchsCount");
        this.mNotchsLength = savedState.getFloat("mNotchsLength");
    }


    /**
     * Public methods
     */

    // The following specific class was created only for pass the notch information to the listener
    // as you can see in the following code.
    // Changing the values of properties inside this you will manage the single notch rendering.
    @SuppressWarnings("unused")
    public class NotchInfo {

        public ScNotchs source = null;
        public float angle = 0.0f;
        public int index = 0;
        public float length = 0.0f;
        public float size = 0.0f;
        public int color = Color.BLACK;
        public float distanceFromBorder = 0.0f;
        public StrokeTypes type = StrokeTypes.LINE;
        public boolean visible = true;

    }


    /**
     * Public properties
     */

    // Notchs count
    @SuppressWarnings("unused")
    public int getNotchs() {
        return this.mNotchsCount;
    }

    @SuppressWarnings("unused")
    public void setNotchs(int value) {
        // Check if value is changed
        if (this.mNotchsCount != value) {
            // Store the new value
            this.mNotchsCount = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }

    // Progress size
    @SuppressWarnings("unused")
    public float getNotchsLength() {
        return this.mNotchsLength;
    }

    @SuppressWarnings("unused")
    public void setNotchsLength(float value) {
        // Check if value is changed
        if (this.mNotchsLength != value) {
            // Store the new value
            this.mNotchsLength = value;
            // Check and refresh the component
            this.checkValues();
            this.requestLayout();
        }
    }


    /**
     * Deprecated
     */

    // Enum for define what type of draw method calling for render the notch
    @SuppressWarnings("unused")
    @Deprecated
    public enum NotchsTypes {
        LINE,
        CIRCLE,
        CIRCLE_FILLED
    }

    // Notchs type
    @SuppressWarnings("unused")
    @Deprecated
    public NotchsTypes getNotchsType() {
        return NotchsTypes.values()[this.mStrokeType.ordinal()];
    }

    @SuppressWarnings("unused")
    @Deprecated
    public void setNotchsType(NotchsTypes value) {
        this.mStrokeType = StrokeTypes.values()[value.ordinal()];
    }


    /**
     * Public listener and interface
     */

    // Before draw
    @SuppressWarnings("unused")
    public interface OnDrawListener {

        void onDrawNotch(NotchInfo info);

    }

    @SuppressWarnings("unused")
    public void setOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

}
