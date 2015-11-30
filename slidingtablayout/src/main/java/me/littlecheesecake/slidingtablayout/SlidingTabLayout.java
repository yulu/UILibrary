package me.littlecheesecake.slidingtablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * customized sliding tab layout
 * Created by yulu on 22/11/15.
 */
public class SlidingTabLayout extends HorizontalScrollView implements ViewPager.OnPageChangeListener {
    private Context context;
    private ViewPager vp;
    private String[] titles;
    private LinearLayout tabsContainer;
    private int currentTab;
    private float currentPositionOffset;
    private int tabCount;
    private Rect indicatorRect = new Rect();
    private Rect tabRect = new Rect();
    private GradientDrawable indicatorDrawable = new GradientDrawable();

    private Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint trianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path trianglePath = new Path();
    private static final int STYLE_NORMAL = 0;
    private static final int STYLE_TRIANGLE = 1;
    private static final int STYLE_BLOCK = 2;
    private int indicatorStyle = STYLE_NORMAL;

    private float tabPadding;
    private boolean tabSpaceEqual;
    private float tabWidth;

    /** indicator **/
    private int indicatorColor;
    private float indicatorHeight;
    private float indicatorWidth;
    private float indicatorCornerRadius;
    private float indicatorMarginLeft;
    private float indicatorMarginTop;
    private float indicatorMarginRight;
    private float indicatorMarginBottom;
    private float indicatorGravity;
    private boolean indicatorWidthEqualTitle;

    /** underline **/
    private int underlineColor;
    private float underlineHeight;
    private float underlineGravity;

    /** divider **/
    private int dividerColor;
    private float dividerWidth;
    private float dividerPadding;

    /** title **/
    private float textSize;
    private int textSelectColor;
    private int textUnselectColor;
    private boolean textBold;
    private boolean textAllCaps;

    private int lastScrollX;
    private int h;

    private OnTabSelectListener listener;
    private float margin;
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SparseArray<Boolean> initSetMap = new SparseArray<>();

    public SlidingTabLayout(Context context) {
        this(context, null, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFillViewport(true);
        setWillNotDraw(false); //override onDraw, use this to clear the flag
        setClipChildren(false);
        setClipToPadding(false);

        this.context = context;
        tabsContainer = new LinearLayout(context);
        addView(tabsContainer);

        obtainAttributes(context, attrs);

        //get layout_height
        String height = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "layout_height");

        //create ViewPager
        if (height.equals(ViewGroup.LayoutParams.MATCH_PARENT + "")) {
        } else if (height.equals(ViewGroup.LayoutParams.WRAP_CONTENT + "")) {
        } else {
            int[] systemAttrs = {android.R.attr.layout_height};
            TypedArray a = context.obtainStyledAttributes(attrs, systemAttrs);
            h = a.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            a.recycle();
        }
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingTabLayout);

        indicatorStyle = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_style, STYLE_NORMAL);
        indicatorColor = ta.getColor(R.styleable.SlidingTabLayout_tl_indicator_color,
                Color.parseColor(indicatorStyle == STYLE_BLOCK ? "#4B6A87" : "#ffffff"));
        indicatorHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_height,
                dp2px(indicatorStyle == STYLE_TRIANGLE ? 4 : (indicatorStyle == STYLE_BLOCK ? -1 : 2)));
        indicatorWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_width,
                dp2px(indicatorStyle == STYLE_TRIANGLE ? 10 : -1));
        indicatorCornerRadius = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_corner_radius,
                dp2px(indicatorStyle == STYLE_BLOCK ? -1 : 0));
        indicatorMarginLeft = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_left, dp2px(0));
        indicatorMarginTop = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_top,
                dp2px(indicatorStyle == STYLE_BLOCK ? 7 : 0));
        indicatorMarginRight = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_right, dp2px(0));
        indicatorMarginBottom = ta.getDimension(R.styleable.SlidingTabLayout_tl_indicator_margin_bottom,
                dp2px(indicatorStyle == STYLE_BLOCK ? 7 : 0));
        indicatorGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_indicator_gravity, Gravity.BOTTOM);
        indicatorWidthEqualTitle = ta.getBoolean(R.styleable.SlidingTabLayout_tl_indicator_width_equal_title, false);

        underlineColor = ta.getColor(R.styleable.SlidingTabLayout_tl_underline_color,
                Color.parseColor("#ffffff"));
        underlineHeight = ta.getDimension(R.styleable.SlidingTabLayout_tl_underline_height, dp2px(0));
        underlineGravity = ta.getInt(R.styleable.SlidingTabLayout_tl_underline_gravity, Gravity.BOTTOM);

        dividerColor = ta.getColor(R.styleable.SlidingTabLayout_tl_divider_color,
                Color.parseColor("#ffffff"));
        dividerWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_width, dp2px(0));
        dividerPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_divider_padding, dp2px(12));

        textSize = ta.getDimension(R.styleable.SlidingTabLayout_tl_textSize, sp2px(14));
        textSelectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textSelectColor,
                Color.parseColor("#ffffff"));
        textUnselectColor = ta.getColor(R.styleable.SlidingTabLayout_tl_textUnselectColor,
                Color.parseColor("#AAffffff"));
        textBold = ta.getBoolean(R.styleable.SlidingTabLayout_tl_textBold, false);
        textAllCaps = ta.getBoolean(R.styleable.SlidingTabLayout_tl_textAllCaps, false);

        tabSpaceEqual = ta.getBoolean(R.styleable.SlidingTabLayout_tl_tab_space_equal, false);
        tabWidth = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_width, dp2px(-1));
        tabPadding = ta.getDimension(R.styleable.SlidingTabLayout_tl_tab_padding,
                tabSpaceEqual || tabWidth > 0 ? dp2px(0) : dp2px(20));

        ta.recycle();
    }

    /**
     * associate ViewPager
     **/
    public void setViewPager(ViewPager vp) {
        if (vp == null || vp.getAdapter() == null) {
            throw new IllegalStateException("ViewPager or ViewPager adapter cannot be NULL");
        }

        this.vp = vp;
        this.vp.removeOnPageChangeListener(this);
        this.vp.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    /**
     * associate ViewPager
     */
    public void setViewPager(ViewPager vp, String[] titles) {
        if (vp == null || vp.getAdapter() == null) {
            throw new IllegalStateException("ViewPager or ViewPager adapter cannot be NULL");
        }

        if (titles == null || titles.length == 0) {
            throw new IllegalStateException("Titles cannot be EMPTY");
        }

        if (titles.length != vp.getAdapter().getCount()) {
            throw new IllegalStateException("Titles length must be the same as the page count");
        }

        this.vp = vp;
        this.titles = titles;

        this.vp.removeOnPageChangeListener(this);
        this.vp.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    /**
     * asscociate ViewPager
     */
    public void setViewPager(ViewPager vp, String[] titles, FragmentActivity fa, ArrayList<Fragment> fragments) {
        if (vp == null) {
            throw new IllegalStateException("ViewPager cannot be NULL");
        }

        if (titles == null || titles.length == 0) {
            throw new IllegalStateException("Titles cannot be EMPTY");
        }

        this.vp = vp;
        this.vp.setAdapter(new InnerPagerAdapter(fa.getSupportFragmentManager(), fragments, titles));

        this.vp.removeOnPageChangeListener(this);
        this.vp.addOnPageChangeListener(this);
        notifyDataSetChanged();
    }

    public void notifyDataSetChanged() {
        tabsContainer.removeAllViews();
        this.tabCount = titles == null ? vp.getAdapter().getCount() : titles.length;
        View tabView;
        for (int i = 0; i < tabCount; i ++) {
            if (vp.getAdapter() instanceof CustomTabProvider) {
                tabView = ((CustomTabProvider) vp.getAdapter()).getCustomTabView(this, i);
            } else {
                tabView = View.inflate(context, R.layout.layout_tab, null);
            }

            CharSequence pageTitle = titles == null ? vp.getAdapter().getPageTitle(i) : titles[i];
            addTab(i, pageTitle.toString(), tabView);
        }

        updateTabStyles();
    }

    /**
     * create and add tab
     **/
    private void addTab(final int position, String title, View tabView) {
        TextView tv_tab_title = (TextView) tabView.findViewById(R.id.tv_tab_title);
        if (tv_tab_title != null) {
            if (title != null) {
                tv_tab_title.setText(title);
            }
        }

        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vp.getCurrentItem() != position) {
                    vp.setCurrentItem(position);
                    if (listener != null) {
                        listener.onTabSelect(position);
                    }
                } else {
                    if (listener != null) {
                        listener.onTabReselect(position);
                    }
                }
            }
        });

        LinearLayout.LayoutParams lp_tab = tabSpaceEqual ?
                new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f) :
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        if (tabWidth > 0) {
            lp_tab = new LinearLayout.LayoutParams((int) tabWidth, LayoutParams.MATCH_PARENT);
        }

        tabsContainer.addView(tabView, position, lp_tab);
    }

    private void updateTabStyles() {
        for (int i = 0; i < tabCount; i++) {
            View v = tabsContainer.getChildAt(i);
            TextView tv_tab_title = (TextView) v.findViewById(R.id.tv_tab_title);
            if (tv_tab_title != null) {
                tv_tab_title.setTextColor(i == currentTab ? textSelectColor : textUnselectColor);
                tv_tab_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tv_tab_title.setPadding((int) tabPadding, 0, (int) tabPadding, 0);
                if (textAllCaps) {
                    tv_tab_title.setText(tv_tab_title.getText().toString().toUpperCase());
                }

                if (textBold) {
                    tv_tab_title.getPaint().setFakeBoldText(textBold);
                }
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        this.currentTab = position;
        this.currentPositionOffset = positionOffset;
        scrollToCurrentTab();
        invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        updateTabSelection(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /** HorizontalScrollView scroll to tab centered **/
    private void scrollToCurrentTab() {
        if (tabCount <= 0) {
            return;
        }

        int offset = (int) (currentPositionOffset * tabsContainer.getChildAt(currentTab).getWidth());
        int newScrollX = tabsContainer.getChildAt(currentTab).getLeft() + offset;

        if (currentTab > 0 || offset > 0) {
            newScrollX -= getWidth() / 2 - getPaddingLeft();
            calcIndicatorRect();
            newScrollX += ((tabRect.right - tabRect.left) / 2);
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    private void updateTabSelection(int position) {
        for (int i = 0; i < tabCount; ++i) {
            View tabView = tabsContainer.getChildAt(i);
            final boolean isSelect = i == position;
            TextView tab_title = (TextView) tabView.findViewById(R.id.tv_tab_title);

            if (tab_title != null) {
                tab_title.setTextColor(isSelect ? textSelectColor : textUnselectColor);
            }

            if (vp.getAdapter() instanceof CustomTabProvider) {
                if (isSelect) {
                    ((CustomTabProvider) vp.getAdapter()).tabSelect(tabView);
                } else {
                    ((CustomTabProvider) vp.getAdapter()).tabUnselect(tabView);
                }
            }
        }
    }

    private void calcIndicatorRect() {
        View currentTabView = tabsContainer.getChildAt(this.currentTab);
        float left = currentTabView.getLeft();
        float right = currentTabView.getRight();

        //for indicatorWidthEqualTitle
        if (indicatorStyle == STYLE_NORMAL && indicatorWidthEqualTitle) {
            TextView tab_title = (TextView) currentTabView.findViewById(R.id.tv_tab_title);
            textPaint.setTextSize(textSize);
            float textWidth = textPaint.measureText(tab_title.getText().toString());
            margin = (right - left - textWidth) / 2;
        }

        if (this.currentTab < tabCount - 1) {
            View nextTabView = tabsContainer.getChildAt(this.currentTab + 1);
            float nextTabLeft = nextTabView.getLeft();
            float nextTabRight = nextTabView.getRight();

            left = left + currentPositionOffset * (nextTabLeft - left);
            right = right + currentPositionOffset * (nextTabRight - right);

            //for indicatorWidthEqualTitle
            if (indicatorStyle == STYLE_NORMAL && indicatorWidthEqualTitle) {
                TextView next_tab_title = (TextView) nextTabView.findViewById(R.id.tv_tab_title);
                textPaint.setTextSize(textSize);
                float nextTextWidth = textPaint.measureText(next_tab_title.getText().toString());
                float nextMargin = (nextTabRight - nextTabLeft - nextTextWidth) / 2;
                margin = margin + currentPositionOffset * (nextMargin - margin);
            }
        }

        indicatorRect.left = (int) left;
        indicatorRect.right = (int) right;
        //for indicatorWidthEqualTitle
        if (indicatorStyle == STYLE_NORMAL && indicatorWidthEqualTitle) {
            indicatorRect.left = (int) (left + margin - 1);
            indicatorRect.right = (int) (right - margin - 1);
        }

        tabRect.left = (int) left;
        tabRect.right = (int) right;

        if (indicatorWidth < 0) {
        } else {
            float indicatorLeft = currentTabView.getLeft() + (currentTabView.getWidth() - indicatorWidth) / 2;

            if (this.currentTab < tabCount - 1) {
                View nextTab = tabsContainer.getChildAt(this.currentTab + 1);
                indicatorLeft = indicatorLeft + currentPositionOffset * (currentTabView.getWidth() / 2 + nextTab.getWidth() / 2);
            }

            indicatorRect.left = (int) indicatorLeft;
            indicatorRect.right = (int) (indicatorRect.left + indicatorWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || tabCount <= 0) {
            return;
        }

        int height = getHeight();
        int paddingLeft = getPaddingLeft();
        //draw divider
        if (dividerWidth > 0) {
            dividerPaint.setStrokeWidth(dividerWidth);
            dividerPaint.setColor(dividerColor);
            for (int i = 0; i < tabCount; i++) {
                View tab = tabsContainer.getChildAt(i);
                canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
            }
        }

        //draw underline
        if (underlineHeight > 0) {
            rectPaint.setColor(underlineColor);
            if (underlineGravity == Gravity.BOTTOM) {
                canvas.drawRect(paddingLeft, height - underlineHeight, tabsContainer.getWidth() + paddingLeft, height, rectPaint);
            } else {
                canvas.drawRect(paddingLeft, 0, tabsContainer.getWidth() + paddingLeft, underlineHeight, rectPaint);
            }
        }

        //draw indicator line
        calcIndicatorRect();
        if (indicatorStyle == STYLE_TRIANGLE) {
            if (indicatorHeight > 0) {
                trianglePaint.setColor(indicatorColor);
                trianglePath.reset();
                trianglePath.moveTo(paddingLeft + indicatorRect.left, height);
                trianglePath.lineTo(paddingLeft + indicatorRect.left / 2 + indicatorRect.right / 2, height - indicatorHeight);
                trianglePath.lineTo(paddingLeft + indicatorRect.right, height);
                trianglePath.close();
                canvas.drawPath(trianglePath, trianglePaint);
            }
        } else if (indicatorStyle == STYLE_BLOCK) {
            if (indicatorHeight < 0) {
                indicatorHeight = height - indicatorMarginTop - indicatorMarginBottom;
            }

            if (indicatorHeight > 0) {
                if (indicatorCornerRadius < 0 || indicatorCornerRadius > indicatorHeight / 2) {
                    indicatorCornerRadius = indicatorHeight / 2;
                }

                indicatorDrawable.setColor(indicatorColor);
                indicatorDrawable.setBounds(paddingLeft + (int) indicatorMarginLeft + indicatorRect.left,
                        (int) indicatorMarginTop, (int) (paddingLeft + indicatorRect.right - indicatorMarginRight),
                        (int) (indicatorMarginTop + indicatorHeight));
                indicatorDrawable.setCornerRadius(indicatorCornerRadius);
                indicatorDrawable.draw(canvas);
            }
        } else {
            if (indicatorHeight > 0) {
                indicatorDrawable.setColor(indicatorColor);

                if (indicatorGravity == Gravity.BOTTOM) {
                    indicatorDrawable.setBounds(paddingLeft + (int) indicatorMarginLeft + indicatorRect.left,
                            height - (int) indicatorHeight - (int) indicatorMarginBottom,
                            paddingLeft + indicatorRect.right - (int) indicatorMarginRight,
                            height - (int) indicatorMarginBottom);
                } else {
                    indicatorDrawable.setBounds(paddingLeft + (int) indicatorMarginLeft + indicatorRect.left,
                            (int) indicatorMarginTop,
                            paddingLeft + indicatorRect.right - (int) indicatorMarginRight,
                            (int) indicatorHeight + (int) indicatorMarginTop);
                }

                indicatorDrawable.setCornerRadius(indicatorCornerRadius);
                indicatorDrawable.draw(canvas);
            }
        }
    }

    public void setOnTabSelectListener(OnTabSelectListener listener) {
        this.listener = listener;
    }

    class InnerPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments = new ArrayList<>();
        private String[] titles;

        public InnerPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments, String[] titles) {
            super(fm);
            this.fragments = fragments;
            this.titles = titles;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // override empty
            // super.destroyItem(container, position, object);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }
    }

    public interface CustomTabProvider {
        View getCustomTabView(ViewGroup parent, int position);

        void tabSelect(View tab);

        void tabUnselect(View tab);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putInt("currentTab", currentTab);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            currentTab = bundle.getInt("currentTab");
            state = bundle.getParcelable("instanceState");
            if (currentTab != 0 && tabsContainer.getChildCount() > 0) {
                updateTabSelection(currentTab);
                scrollToCurrentTab();
            }
        }
        super.onRestoreInstanceState(state);
    }

    protected int dp2px(float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected int sp2px(float sp) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }
}
