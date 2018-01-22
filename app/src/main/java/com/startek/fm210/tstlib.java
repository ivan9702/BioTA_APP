package com.startek.fm210;

import android.content.Context;

/**
 * 參考資料:
 * 	reference\doc\20140714-FM220-android-sdk-ProgrammerGuide.pdf
 * 	reference\sample\StartekFM220AndroidDemo.7z
 */
public class tstlib {

	private static boolean initialized = false;

	public tstlib(Context context)
	{
		if(!initialized)
		{
			String packageName = context.getPackageName(); // startek.biotaclient
			SetFPLibraryPath("/data/data/" + packageName + "/lib/");
			InitialSDK();

			initialized = true;
		}
	}

    static
	{
    	System.loadLibrary("startek_jni");
    }

    public native void SetFPLibraryPath(String filepath);
	public native void InitialSDK();
	public native int FP_ConnectCaptureDriver(int number);
	public native void FP_DisconnectCaptureDriver();
	public native int FP_Capture();
	public native int FP_CheckBlank();
	public native void FP_SaveImageBMP(String filepath);
	public native int FP_CreateEnrollHandle();
	public native int FP_GetTemplate(byte[] m1);
	public native int FP_ISOminutiaEnroll(byte[] m1, byte[] m2);
	public native void FP_SaveISOminutia(byte[] m2, String filepath);
	public native void FP_DestroyEnrollHandle();
	public native int FP_LoadISOminutia(byte[] m2, String filepath);
	public native int FP_ISOminutiaMatchEx(byte[] m1, byte[] m2);
	public native int FP_ISOminutiaMatch180Ex(byte[] m1, byte[] m2);
	public native int FP_ISOminutiaMatch360Ex(byte[] m1, byte[] m2);
	public native int Score();
	public native void FP_GetImageBuffer(byte[] bmpBuffer);
	public native int FP_GetImageWidth();
	public native int FP_GetImageHeight();
	public native int FP_LedOff();
	public native int FP_GetNFIQ();
}
