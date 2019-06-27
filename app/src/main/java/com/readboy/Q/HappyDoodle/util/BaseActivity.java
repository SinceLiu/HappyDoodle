package com.readboy.Q.HappyDoodle.util;

import com.loveplusplus.update.UpdateChecker;

import android.app.Activity;
import android.app.ReadboyActivity;
import android.os.Bundle;

public class BaseActivity extends ReadboyActivity{

	@Override
	protected boolean onInit() {
		UpdateChecker.addActivity(this);
		return true;
	}

	
	@Override
	public void onExit(){
    	super.onExit();
    	UpdateChecker.removeActivity(this);
	}
	
}

