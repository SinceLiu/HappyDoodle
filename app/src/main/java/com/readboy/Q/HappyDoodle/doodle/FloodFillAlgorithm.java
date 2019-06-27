package com.readboy.Q.HappyDoodle.doodle;

import android.graphics.Bitmap;

public class FloodFillAlgorithm 
{

	private Bitmap inputImage;
	private int[] inPixels;
	private int width;
	private int height;
	
	// 	stack data structure
	private int maxStackSize = 500; // will be increased as needed
	private int[] xstack = new int[maxStackSize];
	private int[] ystack = new int[maxStackSize];
	private int stackSize;

	public FloodFillAlgorithm(Bitmap rawImage) {
		this.inputImage = rawImage;
		width = rawImage.getWidth();
        height = rawImage.getHeight();
        inPixels = new int[width*height];
        //getRGB(rawImage, 0, 0, width, height, inPixels );
	}

	public Bitmap getInputImage() {
		return inputImage;
	}

	public void setInputImage(Bitmap inputImage) {
		this.inputImage = inputImage;
	}
	
	public int getColor(int x, int y)
	{
		//int index = y * width + x;
		//return inPixels[index];
		return inputImage.getPixel(x, y);
	}
	
	public void setColor(int x, int y, int newColor)
	{
		//int index = y * width + x;
		//inPixels[index] = newColor;
		inputImage.setPixel(x, y, newColor);
	}
	
	/*public void updateResult()
	{
		setRGB( inputImage, 0, 0, width, height, inPixels );
	}*/
	
	/**
	 * it is very low calculation speed and cause the stack overflow issue when fill 
	 * some big area and irregular shape. performance is very bad.
	 * 
	 * @param x
	 * @param y
	 * @param newColor
	 * @param oldColor
	 */
	public void floodFill4(int x, int y, int newColor, int oldColor)
	{
	    if(x >= 0 && x < width && y >= 0 && y < height 
	    		&& getColor(x, y) == oldColor && getColor(x, y) != newColor) 
	    { 
	    	setColor(x, y, newColor); //set color before starting recursion
	        floodFill4(x + 1, y,     newColor, oldColor);
	        floodFill4(x - 1, y,     newColor, oldColor);
	        floodFill4(x,     y + 1, newColor, oldColor);
	        floodFill4(x,     y - 1, newColor, oldColor);
	    }   
	}
	/**
	 * 
	 * @param x
	 * @param y
	 * @param newColor
	 * @param oldColor
	 */
	public void floodFill8(int x, int y, int newColor, int oldColor)
	{
	    if(x >= 0 && x < width && y >= 0 && y < height && 
	    		getColor(x, y) == oldColor && getColor(x, y) != newColor) 
	    { 
	    	setColor(x, y, newColor); //set color before starting recursion
	        floodFill8(x + 1, y,     newColor, oldColor);
	        floodFill8(x - 1, y,     newColor, oldColor);
	        floodFill8(x,     y + 1, newColor, oldColor);
	        floodFill8(x,     y - 1, newColor, oldColor);
	        floodFill8(x + 1, y + 1, newColor, oldColor);
	        floodFill8(x - 1, y - 1, newColor, oldColor);
	        floodFill8(x - 1, y + 1, newColor, oldColor);
	        floodFill8(x + 1, y - 1, newColor, oldColor);
	    }   
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param newColor
	 * @param oldColor
	 */
	public void floodFillScanLine(int x, int y, int newColor, int oldColor)
	{
		if(oldColor == newColor) return;
	    if(getColor(x, y) != oldColor) return;
	      
	    int y1;
	    
	    //draw current scanline from start position to the top
	    y1 = y;
	    while(y1 < height && getColor(x, y1) == oldColor)
	    {
	    	setColor(x, y1, newColor);
	        y1++;
	    }    
	    
	    //draw current scanline from start position to the bottom
	    y1 = y - 1;
	    while(y1 >= 0 && getColor(x, y1) == oldColor)
	    {
	    	setColor(x, y1, newColor);
	        y1--;
	    }
	    
	    //test for new scanlines to the left
	    y1 = y;
	    while(y1 < height && getColor(x, y1) == newColor)
	    {
	        if(x > 0 && getColor(x - 1, y1) == oldColor) 
	        {
	        	floodFillScanLine(x - 1, y1, newColor, oldColor);
	        } 
	        y1++;
	    }
	    y1 = y - 1;
	    while(y1 >= 0 && getColor(x, y1) == newColor)
	    {
	        if(x > 0 && getColor(x - 1, y1) == oldColor) 
	        {
	        	floodFillScanLine(x - 1, y1, newColor, oldColor);
	        }
	        y1--;
	    } 
	    
	    //test for new scanlines to the right 
	    y1 = y;
	    while(y1 < height && getColor(x, y1) == newColor)
	    {
	        if(x < width - 1 && getColor(x + 1, y1) == oldColor) 
	        {           
	        	floodFillScanLine(x + 1, y1, newColor, oldColor);
	        } 
	        y1++;
	    }
	    y1 = y - 1;
	    while(y1 >= 0 && getColor(x, y1) == newColor)
	    {
	        if(x < width - 1 && getColor(x + 1, y1) == oldColor) 
	        {
	        	floodFillScanLine(x + 1, y1, newColor, oldColor);
	        }
	        y1--;
	    }
	}
	
	/**
	 * 泛滥+扫描线填充算法
	 * @param x 填充点x坐标
	 * @param y 填充点y坐标
	 * @param newColor 欲填充颜色
	 * @param oldColor 填充点当前颜色
	 * @return 有封闭区域填充成功返回true，返回false这里表示无封闭区域（图片边界不做封闭区域算）
	 */
	public boolean floodFillScanLineWithStack(int x, int y, int newColor, int oldColor)
	{
		if(oldColor == newColor) {
			System.out.println("do nothing !!!, filled area!!");
			return false;
		}
	    emptyStack();
	    
	    int x1,y1; 
	    boolean spanLeft, spanRight;
	    push(x, y);
	    
	    while(true)
	    {    
	    	x = popx();
	    	if(x == -1) return true;
	    	y = popy();
	        y1 = y;
	        while(y1 >= 0 && getColor(x, y1) == oldColor) y1--; // go to line top/bottom
	        
	        if(y1 == -1 || y1 == height) return false;//如果超过图片边界，则表示没有封闭区域，直接返回不填色
	        x1 = x;
	        while(x1 >= 0 && getColor(x1, y) == oldColor) x1--; // go to line left/right
	        
	        if(x1 == 0 || x1 == width-1) return false;//如果超过图片边界，则表示没有封闭区域，直接返回不填色
	        
	        y1++; // start from line starting point pixel
	        spanLeft = spanRight = false;
	        while(y1 < height && getColor(x, y1) == oldColor)//每次填充一条竖线
	        {
	        	if(y1 == height-1) return false;//如果超过图片边界，则表示没有封闭区域，直接返回不填色
	        	
	        	setColor(x, y1, newColor);
	            if(!spanLeft && x > 0 && getColor(x - 1, y1) == oldColor)// just keep left line once in the stack
	            {
	                push(x - 1, y1);
	                spanLeft = true;
	            }
	            else if(spanLeft && x > 0 && getColor(x - 1, y1) != oldColor)
	            {
	                spanLeft = false;
	            }
	            if(!spanRight && x < width - 1 && getColor(x + 1, y1) == oldColor) // just keep right line once in the stack
	            {
	                push(x + 1, y1);
	                spanRight = true;
	            }
	            else if(spanRight && x < width - 1 && getColor(x + 1, y1) != oldColor)
	            {
	                spanRight = false;
	            } 
	            y1++;
	        }
	    }
		
	}
	
	private void emptyStack() {
		while(popx() != - 1) {
			popy();
		}
		stackSize = 0;
	}

	final void push(int x, int y) {
		stackSize++;
		if (stackSize==maxStackSize) {
			int[] newXStack = new int[maxStackSize*2];
			int[] newYStack = new int[maxStackSize*2];
			System.arraycopy(xstack, 0, newXStack, 0, maxStackSize);
			System.arraycopy(ystack, 0, newYStack, 0, maxStackSize);
			xstack = newXStack;
			ystack = newYStack;
			maxStackSize *= 2;
		}
		xstack[stackSize-1] = x;
		ystack[stackSize-1] = y;
	}
	
	final int popx() {
		if (stackSize==0)
			return -1;
		else
            return xstack[stackSize-1];
	}

	final int popy() {
        int value = ystack[stackSize-1];
        stackSize--;
        return value;
	}


}
