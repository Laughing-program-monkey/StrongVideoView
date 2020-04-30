package customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;
/**
     *上下滑动的SeekBar
     *
     */
    public class VerticalSeekBar extends SeekBar {
        private Drawable mThumb;
        private OnSeekBarChangeListener mOnSeekBarChangeListener;

        public VerticalSeekBar(Context context) {
            super(context);
        }

        public VerticalSeekBar(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
            mOnSeekBarChangeListener = l;
        }

        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(h, w, oldh, oldw);
        }

        @Override
        protected  void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec);

            setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
        }

        protected void onDraw(Canvas c) {
            /*核心代码*/
            c.rotate(-90);
            c.translate(-getHeight(), 0);
            super.onDraw(c);
        }

        public void setThumb(Drawable thumb) {
            mThumb = thumb;
            super.setThumb(thumb);
        }

        void onStartTrackingTouch() {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onStartTrackingTouch(this);
            }
        }

        void onStopTrackingTouch() {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener.onStopTrackingTouch(this);
            }
        }

        private void attemptClaimDrag() {
            if (getParent() != null) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!isEnabled()) {
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setPressed(true);
                    onStartTrackingTouch();
                    break;

                case MotionEvent.ACTION_MOVE:
                    attemptClaimDrag();
                    setProgress(getMax() - (int) (getMax() * event.getY() / getHeight()));
                    break;
                case MotionEvent.ACTION_UP:
                    onStopTrackingTouch();
                    setPressed(false);
                    break;

                case MotionEvent.ACTION_CANCEL:
                    onStopTrackingTouch();
                    setPressed(false);
                    break;
            }
            return true;
        }
    }

