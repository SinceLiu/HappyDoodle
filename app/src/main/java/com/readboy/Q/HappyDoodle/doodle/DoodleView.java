package com.readboy.Q.HappyDoodle.doodle;

import java.util.ArrayList;
import java.util.List;

import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.data.Constant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ReadboyView;
import android.view.View;
import android.view.View.OnTouchListener;
//import android.widget.RelativeLayout;
//import android.widget.RelativeLayout.LayoutParams;

/**
 * 自定义的供填充颜色用的view。为了去掉填充后的锯齿，采用了叠加两层bitmap的技巧，底层的bitmap是保留锯齿的欲填充图，
 * 上层的是去掉了锯齿、边缘平滑的欲填充图的副本；填色时是往底层上setpixel填色，上层只是起到了一个遮盖住锯齿的作用，而且
 * 保存时也是两层bitmap叠加成一个bitmap后再保存，这样保存下来的图片也才没锯齿。
 *
 * @author css
 */
public class DoodleView extends ReadboyView implements OnTouchListener {
    private static final String TAG = "lqn-DoodleView";
    /**
     * 填充的目标bitmap，置于下层
     */
    private Bitmap bm;
    private int w;
    private int h;
    /**
     * 临时bitmap，用于填充失败（如填充点在不可填充区域）时恢复
     */
    private Bitmap temp1;
    /**
     * 起遮盖底层bitmap(即{@link #bm})锯齿作用的bitmap，至于{@link #bm}上层
     */
    private Bitmap temp2;
    /** 与{@link #bm}关联的画布，意味着所有对此画布的更改将保存到{@link #bm}中 */
    //private Canvas c;
    /**
     * 画刷
     */
    private Paint p;
    /**
     * 用于填充{@link #bm}的临时bitmap
     */
    private Bitmap temp;
	/*private int canvaswidth;
	private int canvasheight;*/
    /**
     * 填充色，默认红色
     */
    private int color = Color.RED;
    //private boolean isClear = false;
    /**
     * 是否填充了
     */
    private boolean isDraw = false;
    /**
     * 是否初始化了底图，每次更换图片后更新，只调用一次，用于一开始将有锯齿的图片刷到底层bitmap({@link #bm}通过画布{@link #c})上
     */
    private boolean isInitBm = false;
    /**
     * 线条颜色
     */
    private int color_line = Constant.LINE_COLOR;

    public DoodleView(Context context) {
        super(context);
    }

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
		color_line = Constant.LINE_COLOR;
        p = new Paint();
        p.setAntiAlias(true);
        setOnTouchListener(this);

    }

    public void initBitmap() {
        if (!isInitBm) {
            setBm(temp.copy(Bitmap.Config.ARGB_8888, true));
            isInitBm = true;
        }

        if (!isDraw) {
            saomiao();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (bm != null) {
            setMeasuredDimension(bm.getWidth(), bm.getHeight());
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onPaint(Canvas canvas) {
        super.onPaint(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        if (bm != null && temp2 != null && DoodleActivity.isDoodleViewInitEnd) {
            canvas.drawBitmap(bm, 0, 0, p);
            canvas.drawBitmap(temp2, 0,
                    0, p);
        }
    }

    /**
     * 清除底图
     */
    public void clear() {
        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
        }
    }

    public Bitmap getTemp() {
        return temp;
    }

    /**
     * 供外部调用来设置填充底图
     *
     * @param temp 填充底图
     */
    public void setTemp(Bitmap temp) {
        if (this.temp != null && !this.temp.isRecycled()) {
            this.temp.recycle();
        }
        //TODO 若添加了其它尺寸的图片就不需要伸缩
        //这里以2880x1920 C20的为基准
        float widthScale = HappyDoodleApp.getScreenWidth() / 2880.0f;
        float heightScale = HappyDoodleApp.getScreenHeight() / 1920.0f;
        Matrix matrix = new Matrix();
        matrix.setScale(widthScale, heightScale);
        Bitmap tmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(),
                temp.getHeight(), matrix, true);
        if (!temp.isRecycled()) {
            temp.recycle();
        }
        temp = tmp;

        this.temp = temp;
        isDraw = false;
        isInitBm = false;
        clear();
        initBitmap();
        //invalidate();
        postInvalidate();
    }

    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        if (this.bm != null && !this.bm.isRecycled()) {
            this.bm.recycle();
        }
        this.bm = bm;
        w = bm.getWidth();
        h = bm.getHeight();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public boolean isDraw() {
        return isDraw;
    }

    public void setDraw(boolean isDraw) {
        this.isDraw = isDraw;
    }

    public Bitmap getTemp2() {
        return temp2;
    }

    /**
     * 设置背景图
     *
     * @param temp2
     */
    public void setTemp2(Bitmap temp2) {
        if (this.temp2 != null && !this.temp2.isRecycled()) {
            this.temp2.recycle();
        }

        //TODO 若添加了其它尺寸的图片就不需要伸缩
        //这里以2880x1920 C20的为基准
        float widthScale = HappyDoodleApp.getScreenWidth() / 2880.0f;
        float heightScale = HappyDoodleApp.getScreenHeight() / 1920.0f;
        Matrix matrix = new Matrix();
        matrix.setScale(widthScale, heightScale);
        Bitmap tmp = Bitmap.createBitmap(temp2, 0, 0, temp2.getWidth(),
                temp2.getHeight(), matrix, true);
        if (!temp2.isRecycled()) {
            temp2.recycle();
        }
        temp2 = tmp;

        this.temp2 = temp2;
    }

    /**
     * 回收图片
     */
    public void recycleBitmap() {
        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
        }
        if (temp != null && !temp.isRecycled()) {
            temp.recycle();
        }
        if (temp1 != null && !temp1.isRecycled()) {
            temp1.recycle();
        }
        if (temp2 != null && !temp2.isRecycled()) {
            temp2.recycle();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (DoodleActivity.mIsForbidOp || !DoodleActivity.isDoodleViewInitEnd) {
            return true;
        }

        if (!isInitBm) {
            return false;
        }
        int x = (int) e.getX();
        int y = (int) e.getY();
        if (x < 0 || x > bm.getWidth() || y < 0 || y > bm.getHeight()) {
            Log.e(TAG, "touch point OutOfArea,point=" + x + "," + y);
            return false;
        }

        if (e.getAction() == MotionEvent.ACTION_DOWN && bm.getPixel(x, y) != color_line) {
            if (temp1 != null && temp1.isRecycled()) {
                temp1.recycle();
            }
            temp1 = Bitmap.createBitmap(bm);
            fillArea(x, y, color);
            if (HappyDoodleApp.DEBUG) {
                Log.i(TAG, "isOutOfArea=" + isOutOfArea);
            }
            if (isOutOfArea) {
                setBm(Bitmap.createBitmap(temp1));
                isOutOfArea = false;
            } else {
                isDraw = true;
            }

            invalidate();

        }
        return false;
    }

    /**
     * 第一次填色先全图扫描，把线条与不能填充的白色背景区域填充同一种颜色，但因为此处填的是下层bitmap，虽然
     * 不能填充的白色背景区域也被填成了与线条颜色相同的同一颜色，但因为有上层遮盖了，所以看不到。
     * 注：存在一个问题，即color_line不能与可以填充的新颜色值相同。
     */
    private void saomiao() {
        int curcolor = bm.getPixel(2, 2);
        int size = w * h;
        int[] pixels = new int[size];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        for (int i = 0; i < size; i++) {
            if (pixels[i] != curcolor) {
                pixels[i] = color_line;
            }
        }
        bm.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    public static Bitmap small(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(1.0f, 0.9f); // 长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    List<Point> points = new ArrayList<Point>();
    /**
     * 是否填充失败（如填充点在不可能填充区域）
     */
    private boolean isOutOfArea = false;

    // 扫描种子填充算法
    private void fillArea(int x, int y, int color) {
        Point tmp = new Point(x, y);
        points.add(tmp);
        int curColor = bm.getPixel(x, y);
        int[] pixels = new int[w * h];
        bm.getPixels(pixels, 0, w, 0, 0, w, h);
        while (points.size() > 0) {
            tmp = points.get(0);
            if (pixels[tmp.y * w + tmp.x] != color) {
                fillaline(pixels, curColor, tmp.x, tmp.y, color);
                if (isOutOfArea) {
                    points.clear();
                    break;
                }
            }
            points.remove(0);
        }
        bm.setPixels(pixels, 0, w, 0, 0, w, h);
    }

    private void fillaline(int[] pixels, int curColor, int x, int y, int color) {
        int index = y * w + x;
        pixels[index] = color;
        int leftx = x, rightx = x;
        while (leftx > 1) {
            leftx--;
            index = y * w + leftx;
            if (pixels[index] != curColor) {
                break;
            }
            pixels[index] = color;
        }
        if (leftx == 1) {
            isOutOfArea = true;
            return;
        }

        while (rightx < w - 1) {
            rightx++;
            index = y * w + rightx;
            if (pixels[index] != curColor) {
                break;
            }
            pixels[index] = color;
        }
        if (rightx == w - 1) {
            isOutOfArea = true;
            return;
        }
        if (y - 1 >= 0) {
            for (int i = leftx + 1; i <= rightx; i++) {
                if (pixels[(y - 1) * w + i] == curColor && pixels[(y - 1) * w + i + 1] != curColor) {
                    points.add(new Point(i, y - 1));
                }
            }
            if (pixels[(y - 1) * w + rightx - 1] == curColor) {
                points.add(new Point(rightx - 1, y - 1));
            }
        }

        if (y + 1 < h) {
            for (int i = leftx + 1; i < rightx; i++) {
                if (pixels[(y + 1) * w + i] == curColor && pixels[(y + 1) * w + i + 1] != curColor) {
                    points.add(new Point(i, y + 1));
                }
            }
            if (pixels[(y + 1) * w + rightx - 1] == curColor) {
                points.add(new Point(rightx - 1, y + 1));
            }
        }
    }
}
