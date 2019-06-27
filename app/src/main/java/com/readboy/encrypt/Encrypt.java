package com.readboy.encrypt;

public class Encrypt {
	
	static{
		System.loadLibrary("rbEncrypt");
	}
	
	public native static byte[] nativeEndec(byte[] data, int length);
	
	public native static int macCheck();
}
