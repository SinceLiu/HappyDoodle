package com.readboy.Q.HappyDoodle.data;

import android.graphics.Color;


/**
 * 常量类
 * @author css
 *
 */
public class Constant {
	/**ap模式*/
	public static final int MODE_NORMAL = 0;		//正常模式
	public static final int MODE_SILKWORM = 1;		//蚕宝宝成长记
	public static final int MODE_LADYBUG = 2;		//瓢虫的世界
	public static final int MODE_BABYPLAN = 3;		//宝贝计划跳转进来
	
	public static final String SILKWORM_NAME = "silkworm";	//蚕宝宝成长记图片名字
	public static final String LADYBUG_NAME = "ladybug";		//瓢虫的世界图片名字
	
	public final static int MOUSE_NORMAL_SUM = 10;		//老鼠常态动画图片总数
	public final static int MOUSE_CLICK_SUM = 12;		//老鼠点击动画图片总数
	public static final String MOUSE_NORMAL_PIC = "mouse_normal";		//老鼠常态图片名字
	public static final String MOUSE_CLICK_PIC = "mouse_click";			//老鼠点击图片名字
	
	public static final int CLICKTIME_GAP = 500;		//两次点击的时间间隔
	
	/** 导航界面总共6张背景图片 */
	public static final int GUIDE_BK_TOTAL_PIC = 6;
	/** 导航界面总共50张标题动画图片 */
	public static final int GUIDE_TITLE_TOTAL_PIC = 50;
	
	/** 导航界面背景动画图片路径 */
	public static final String GUIDE_BK_TOTAL_PIC_DIR = "pic/launch/bk/";
	/** 导航界面标题动画图片路径 */
	public static final String GUIDE_TITLE_TOTAL_PIC_DIR = "pic/launch/title/";
	/** 导航界面标题动画图片路径， 蚕宝宝成长记*/
	public static final String GUIDE_TITLE1_TOTAL_PIC_DIR = "pic/launch/title/";
	/** 导航界面标题动画图片路径 ，瓢虫的世界*/
	public static final String GUIDE_TITLE2_TOTAL_PIC_DIR = "pic/launch/title/";
	/** 声音在assert中的目录 */
	public static final String SND_DIR = "sound/";
	
	public static final String SAVE_PATH = "/.快乐涂鸦/";
	
	public static final int SILKWORM_INDEX = 43;		//蚕宝宝成长记
	public static final int LADYBUG_INDEX = 43;			//瓢虫的世界


	public static final int C20_WIDTH=2880;
	/** 最多保存84个作品 */
	public static final int MAX_OPUS = 14*6;
	/** 最小磁盘剩余空间，单位字节 */
	public static final long K = 1;
	public static final long M = (10*1024*1024);
	public static final long MIN_DISK_SPACE = K * M;//(10*1024*1024);
	/** 作品缩略图宽 */
	public static final int OPUS_THUMB_WIDTH = 343;
	/** 作品缩略图高 */
	public static final int OPUS_THUMB_HEIGHT = 273;
	/** 选择画布界面每页最多几个图标 */
	public static final int MAX_ITEM_PER_CANVAS = 6;
	/** 对应年龄的画布总数,依次为3-4,5-6,7岁 ，7岁与5-6岁用同样的数据*/
	public static final int[] TOTAL_CANVAS_EACH_AGE = {44,23,23};
	
	/** 颜色共有多少页 */
	public static final int DOODLE_COLOR_PAGES = 3;
	/** 每页最多几种颜色 */
	public static final int MAX_COLOR_PER_PAGE = 12;
	/** 颜色总数 */
	public static final int TOTAL_COLORS = 36;
	/** 颜色缩略图宽 */
	public static final int COLOR_THUMB_WIDTH = 89;
	/** 颜色缩略图高 */
	public static final int COLOR_THUMB_HEIGHT = 59;
	
	/** 待填充图片的线条颜色，注意，为了使线条不被填上色，此颜色值不应该与下面的调色板颜色中的任一值相同 */
	public static final int LINE_COLOR = 0xFF696A6B;
	/** 调色板颜色 */
	private static int[] color = new int[] {
		
		Color.argb(255,229,2,0),Color.argb(255,244,91,0), Color.argb(255,224,161,143), 
		Color.argb(255,215,140,160),Color.argb(255,255,148,255), Color.argb(255,233,93,96),
		Color.argb(255,208,77,111), Color.argb(255,173,39,76),Color.argb(255,165,19,20),
		
		Color.argb(255,255,255,255), Color.argb(255,151,147,144),Color.argb(255,0,0,0), 
		Color.argb(255,139,101,65),Color.argb(255,156,87,9), Color.argb(255,70,32,11),
		Color.argb(255,45,59,4), Color.argb(255,27,73,86),Color.argb(255,13,1,87),
		
		Color.argb(255,132,44,144), Color.argb(255,135,47,209),Color.argb(255,203,45,183), 
		Color.argb(255,177,144,199),Color.argb(255,114,157,191), Color.argb(255,54,141,160),
		Color.argb(255,0,178,234), Color.argb(255,0,230,230),Color.argb(255,45,198,151),
		
		Color.argb(255,251,247,156), Color.argb(255,253,206,0),Color.argb(255,201,167,60), 
		Color.argb(255,208,149,47),Color.argb(255,189,255,0), Color.argb(255,144,195,0),
		Color.argb(255,54,138,0), Color.argb(255,88,184,123),Color.argb(255,157,173,111)
	};

	/**
	 * 根据索引获得color中对应的颜色
	 * @param index 颜色索引
	 * @return color数组中对应索引的颜色值
	 */
	public static int getColor(int index) {
		if (index >= 0 && index < color.length) {
			return color[index];
		} else {
			return 0;
		}
	}
}
