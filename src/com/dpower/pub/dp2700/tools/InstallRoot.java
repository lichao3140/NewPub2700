package com.dpower.pub.dp2700.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

public class InstallRoot {
	private static final String TAG = "InstallRoot";

	private static final String APK = "cnaec.apk";
	private static InstallRoot mInstance;
	private Context mContext;

	public static InstallRoot getInstance() {
		if (mInstance == null) {
			mInstance = new InstallRoot();
		}
		return mInstance;
	}

	public void init(Context context) {
		mContext = context;
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean result = checkApkExist("com.fbee.smarthome_fb");
				if (!result) {
					getPackName(APK);
				}
			}
		}).start();
	}

	private boolean checkApkExist(String packageName) {
		if (TextUtils.isEmpty(packageName))
			return false;
		try {
			mContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	private void getPackName(String apk) {
		String path = ConstConf.SD_DIR + apk;
		try {
			File file = new File(path);
			if (!file.exists() || (int) file.length() == 0) {
				FileOperate.copyToSD(mContext, path, apk);
			}
			String archiveFilePath = file.getAbsolutePath();
			installx(archiveFilePath);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	private String installx(String apkAbsolutePath) {
		MyLog.print(TAG, "install : " + apkAbsolutePath);
		String[] args = { "pm", "install", "-r", apkAbsolutePath };
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write("/n".getBytes());
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}
}
