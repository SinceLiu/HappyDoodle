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
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
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
     * 退出时停止线程
     */
    private boolean isFinished = false;
    /**
     * 线条颜色
     */
    private int color_line = Constant.LINE_COLOR;

    public DoodleView(Context context) {
        super(context);
        isFinished = false;
    }

    public DoodleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isFinished = false;
		/*canvaswidth = 751;
		canvasheight = 415;*/

		/*bm = Bitmap.createBitmap(canvaswidth, canvasheight,
				Bitmap.Config.ARGB_8888);
		temp1 = Bitmap.createBitmap(bm);
		c = new Canvas(bm);//将画布关联bitmap
		color_line = Constant.LINE_COLOR;*/

        p = new Paint();
        p.setAntiAlias(true);
        //Log.e(TAG, "bm="+bm);
        //invalidate();//没必要

        setOnTouchListener(this);

    }

    public void initBitmap() {
        //Log.e(TAG, "isInitBm="+isInitBm);
        if (!isInitBm) {
			/*c.drawBitmap(temp, (canvaswidth - temp.getWidth()) / 2,
					(canvasheight - temp.getHeight()) / 2, p);*/
            setBm(temp.copy(Bitmap.Config.ARGB_8888, true));
            isInitBm = true;

        }

        if (!isDraw) {
            shaomiao();
        }

        //Log.e(TAG, "initBitmap end");
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
		/*if (!isInitBm) {
			c.drawBitmap(temp, (canvaswidth - temp.getWidth()) / 2,
					(canvasheight - temp.getHeight()) / 2, p);
			isInitBm = true;

		}*/
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        //Log.e(TAG, "temp2="+temp2+",isDoodleViewInitEnd="+DoodleActivity.isDoodleViewInitEnd);
        if (bm != null && temp2 != null && DoodleActivity.isDoodleViewInitEnd) {
            canvas.drawBitmap(bm, 0, 0, p);
            canvas.drawBitmap(temp2, 0,
                    0, p);
        }
        if (bm != null && !bm.isRecycled()) {
//			LayoutParams layoutParams = new LayoutParams(bm.getWidth(), bm.getHeight());
//			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//			setLayoutParams(layoutParams);
        }
    }

    /**
     * 清除底图
     */
    public void clear() {

		/*Log.e(TAG, "clear+++++++++");
		Paint paint2 = new Paint();
		paint2.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		Log.e(TAG, "clear+++++++++ 11111111111111"); 
		c.drawPaint(paint2);
		Log.e(TAG, "clear+++++++++ 222222222222222"); 
		paint2.setXfermode(new PorterDuffXfermode(Mode.SRC));*/
        //Log.e(TAG, "clear+++++++++ 333333333333333 bm="+bm);
        if (bm != null && !bm.isRecycled()) {
            bm.recycle();
        }
        //Log.e(TAG, "clear+++++++++ 444444444444 w,h="+canvaswidth+","+canvasheight);
        //bm = Bitmap.createBitmap(canvaswidth, canvasheight,
        //		Bitmap.Config.ARGB_8888);
        //Log.e(TAG, "clear+++++++++ 555555555555 bm="+bm);
        //c = new Canvas(bm);
        //Log.e(TAG, "clear+++++++++ 666666666666666");
        //invalidate();

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
        //Log.e(TAG, "temp="+temp);
        if (this.temp != null && !this.temp.isRecycled()) {
            this.temp.recycle();
        }
        //Log.e(TAG, "setTemp++++++++++");
        float width = HappyDoodleApp.getScreenWidth();
        if (width >= 2560 && width != Constant.C20_WIDTH) {
            float scaleX = 1.5f;//width / 1280f;
            Matrix matrix = new Matrix();
            matrix.setScale(scaleX, scaleX);// 缩小为原来的一半
            Bitmap tmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(),
                    temp.getHeight(), matrix, true);
            if (temp != null && !temp.isRecycled()) {
                temp.recycle();
            }
            temp = tmp;
        }

        this.temp = temp;
        isDraw = false;
        isInitBm = false;
        clear();
        initBitmap();
        //invalidate();
        postInvalidate();
    }

	/*public boolean isClear() {
		return isClear;
	}

	public void setClear(boolean isClear) {
		this.isClear = isClear;
	}*/

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

        float width = HappyDoodleApp.getScreenWidth();
        if (width >= 2560 && width != Constant.C20_WIDTH) {
            float scaleX = 1.5f;//width / 1280f;
            Matrix matrix = new Matrix();
            matrix.setScale(scaleX, scaleX);// 缩小为原来的一半
            Bitmap tmp = Bitmap.createBitmap(temp2, 0, 0, temp2.getWidth(),
                    temp2.getHeight(), matrix, true);
            if (temp2 != null && !temp2.isRecycled()) {
                temp2.recycle();
            }
            temp2 = tmp;
        }
        //Log.e(TAG, "++++++++++====setTemp2");
        this.temp2 = temp2;
        //Log.e(TAG, "----------temp2="+this.temp2);
    }

	/*public boolean getIsInitBm() {
		return isInitBm;
	}

	public void setIsInitBm(boolean isInitBm) {
		this.isInitBm = isInitBm;
	}*/

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
            //Log.e("DoodleView", "ForbidOp!!!");
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
		/*if (!isDraw) {
			shaomiao();

		}*/

        if (e.getAction() == MotionEvent.ACTION_DOWN && bm.getPixel(x, y) != color_line) {
            if (temp1 != null && temp1.isRecycled()) {
                temp1.recycle();
            }
            temp1 = Bitmap.createBitmap(bm);
            fillarea(x, y, color);
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

    public void setFinished() {
        isFinished = true;
    }

    /**
     * 第一次填色先全图扫描，把线条与不能填充的白色背景区域填充同一种颜色，但因为此处填的是下层bitmap，虽然
     * 不能填充的白色背景区域也被填成了与线条颜色相同的同一颜色，但因为有上层遮盖了，所以看不到。
     * 注：存在一个问题，即color_line不能与可以填充的新颜色值相同。
     */
    private void shaomiao() {
        int curcolor = bm.getPixel(2, 2);
        //Log.e(TAG, "curcolor=" + curcolor);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (isFinished) {
                    return;
                }
                if (bm.getPixel(j, i) != curcolor) {
                    bm.setPixel(j, i, color_line);
                }
            }
        }
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
    private void fillarea(int x, int y, int color) {
        Point tmp = new Point(x, y);
        points.add(tmp);
        while (points.size() > 0) {
            tmp = points.get(0);
            if (bm.getPixel(tmp.x, tmp.y) != color) {
                fillaline(tmp.x, tmp.y, color);
                if (isOutOfArea) {
                    points.clear();
                    break;
                }
            }
            points.remove(0);
        }
    }

    private void fillaline(int x, int y, int color) {
        int curcolor = bm.getPixel(x, y);
        bm.setPixel(x, y, color);
        int leftx = x, rightx = x;
        while (leftx > 1) {
            leftx--;
            if (bm.getPixel(leftx, y) != curcolor) {
                break;
            }
            bm.setPixel(leftx, y, color);
            // canvas.drawPoint(leftx, y, paint);
        }
        if (leftx == 1) {
            isOutOfArea = true;
            return;
        }
//        if (leftx == 1) {
//            bm.setPixel(leftx - 1, y, color);
//        }
        while (rightx < w - 1) {
            rightx++;
            if (bm.getPixel(rightx, y) != curcolor) {
                break;
            }
            bm.setPixel(rightx, y, color);
            // canvas.drawPoint(rightx, y, paint);
        }
        if (rightx == w - 1) {
            isOutOfArea = true;
            return;
        }
        // canvas.drawLine(leftx, y, rightx, y, paint);
        if (y - 1 >= 0) {
            for (int i = leftx + 1; i <= rightx; i++) {
                if (bm.getPixel(i, y - 1) == curcolor && bm.getPixel(i + 1, y - 1) != curcolor) {
                    points.add(new Point(i, y - 1));
                }
            }
            if (bm.getPixel(rightx - 1, y - 1) == curcolor) {
                points.add(new Point(rightx - 1, y - 1));
            }
        }

        if (y + 1 < h) {
            for (int i = leftx + 1; i < rightx; i++) {
                if (bm.getPixel(i, y + 1) == curcolor && bm.getPixel(i + 1, y + 1) != curcolor) {
                    points.add(new Point(i, y + 1));
                }
            }
            if (bm.getPixel(rightx - 1, y + 1) == curcolor) {
                points.add(new Point(rightx - 1, y + 1));
            }
        }
    }
}
