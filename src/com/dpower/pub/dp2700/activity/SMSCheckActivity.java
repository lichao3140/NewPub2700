package com.dpower.pub.dp2700.activity;

import java.io.File;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ConstConf;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * 信息查看
 */
public class SMSCheckActivity extends BaseActivity {

	private WebView mWebView;
	private ImageView mImageView;
	private ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms_check);
		init();
	}

	private void init() {
		findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		String resName = null;
		if (getIntent().getExtras() != null) {
			resName = getIntent().getExtras().getString("resName");
		}
		mImageView = (ImageView) findViewById(R.id.image_view);
		mWebView = (WebView) findViewById(R.id.web_view);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mProgressBar.setVisibility(View.VISIBLE);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				mProgressBar.setVisibility(View.GONE);
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				mProgressBar.setVisibility(View.GONE);
				super.onReceivedError(view, errorCode, description, failingUrl);
			}
		});
		if (resName.contains("jpg")) {
			Bitmap bitmap = BitmapFactory.decodeFile(ConstConf.MESSAGE_PATH + File.separator + resName);
			mImageView.setVisibility(View.VISIBLE);
			mWebView.setVisibility(View.GONE);
			mImageView.setImageBitmap(bitmap);
		} else {
			mImageView.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);
			mWebView.loadUrl("file://" + ConstConf.MESSAGE_PATH + File.separator + resName);
		}
	}

	@Override
	protected void onResume() {
		findViewById(R.id.root_view).setBackground(SPreferences.getInstance().getWallpaper());
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		if (mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			super.onBackPressed();
		}
	}
}
