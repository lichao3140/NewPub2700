package com.dpower.pub.dp2700.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.dpower.pub.dp2700.R;
import com.dpower.pub.dp2700.adapter.MusicAdapter;
import com.dpower.pub.dp2700.tools.MediaPlayerTools;
import com.dpower.pub.dp2700.tools.MediaPlayerTools.OnEndListener;
import com.dpower.pub.dp2700.tools.MediaPlayerTools.OnProgressListener;
import com.dpower.pub.dp2700.tools.MyToast;
import com.dpower.pub.dp2700.tools.SPreferences;
import com.dpower.util.ConstConf;
import com.dpower.util.MyLog;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @author ZhengZhiying
 * @Funtion1 娱乐天地/音乐欣赏
 * @Funtion2 设置/铃声设置
 */
public class MusicActivity extends BaseFragmentActivity implements OnClickListener {
	private static final String TAG = "FunMusicActivity";

	/** 控制文件大小10M */
	private final long MAX_SIZE = 10 * 1024 * 1024;
	private final String EXTSD_PATH = "mnt/extsd/ring/";
	private final String STORAGE_PATH = ConstConf.RING_PATH;
	private final String SYSTEM_PATH = "system/media/backup/ring/";
	private ImageButton mButtonPlay;
	private ListView mMusicList;
	private MusicAdapter mAdapter;
	private List<File> mMusicDatas;
	private MediaPlayerTools mPlayerTools;
	private AudioManager mAudioManager;
	private Button mButtonConfirm;
	private File mCurrentFile;
	private int mCurrent;
	private SeekBar mVolumeSeekBar;
	private ProgressBar mMusicProgressBar;
	private int mSeekBarProgress;
	private int mMaxVolume;
	private boolean mIsLoad = true;
	private boolean mIsSetRing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fun_music);
		init();
	}

	private void init() {
		mPlayerTools = new MediaPlayerTools(false);
		findViewById(R.id.btn_back).setOnClickListener(this);
		findViewById(R.id.btn_previous).setOnClickListener(this);
		findViewById(R.id.btn_next).setOnClickListener(this);
		mMusicProgressBar = (ProgressBar) findViewById(R.id.music_progress_bar);
		mButtonPlay = (ImageButton) findViewById(R.id.btn_play);
		mButtonPlay.setOnClickListener(this);
		setPlayerImage(mPlayerTools.isPlaying);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mVolumeSeekBar = (SeekBar) findViewById(R.id.seek_bar);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		mVolumeSeekBar.setMax(mMaxVolume);
		mSeekBarProgress = mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		mVolumeSeekBar.setProgress(mSeekBarProgress);
		mVolumeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM, mSeekBarProgress,
						AudioManager.FLAG_PLAY_SOUND);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mSeekBarProgress = progress;
			}
		});

		mMusicList = (ListView) findViewById(R.id.music_list);
		mButtonConfirm = (Button) findViewById(R.id.btn_confirm);
		mMusicDatas = createFileList();
		if (mMusicDatas != null && mMusicDatas.size() > 0) {
			mCurrentFile = mMusicDatas.get(0);
			mCurrent = 0;
		}
		mAdapter = new MusicAdapter(this);
		mAdapter.setMusicFiles(mMusicDatas);
		mMusicList.setAdapter(mAdapter);
		setListViewListener();
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if (!TextUtils.isEmpty(bundle.getString("FunMusicActivity"))) {
				mButtonConfirm.setVisibility(View.VISIBLE);
				mButtonConfirm.setOnClickListener(this);
				((TextView) findViewById(R.id.tv_title)).setText(R.string.text_ring_set);
			} else {
				mButtonConfirm.setVisibility(View.GONE);
			}
		} else {
			mButtonConfirm.setVisibility(View.GONE);
		}
		mPlayerTools.setOnEndListener(new OnEndListener() {

			@Override
			public void onCompletion() {
				setPlayerImage(mPlayerTools.isPlaying);
				mMusicProgressBar.setProgress(mMusicProgressBar.getMax());
			}
		});
		mPlayerTools.setOnProgressListener(new OnProgressListener() {

			@Override
			public void onProgress(int currentPosition) {
				mMusicProgressBar.setProgress(currentPosition);
			}
		});
	}

	private void setPlayerImage(boolean isPlay) {
		if (isPlay) {
			mButtonPlay.setImageResource(R.drawable.ic_music_pause);
		} else {
			mButtonPlay.setImageResource(R.drawable.ic_music_play);
		}
	}

	private List<File> createFileList() {
		List<File> sdCard = scanFile(STORAGE_PATH);
		List<File> system = scanFile(SYSTEM_PATH);
		List<File> extCard = scanFile(EXTSD_PATH);
		sdCard.addAll(system);
		sdCard.addAll(extCard);
		return sdCard;
	}

	private List<File> scanFile(String path) {
		File file = new File(path);
		List<File> files = new ArrayList<File>();
		if (!file.exists()) {
			return files;
		}
		File[] subFile = file.listFiles();
		if (subFile == null) {
			return files;
		}
		for (int i = 0; i < subFile.length; i++) {
			if (!subFile[i].isDirectory()) {
				String name = subFile[i].getName();
				if (name.endsWith(".mp3") || name.endsWith(".mid") || name.endsWith(".ogg") || name.endsWith(".wav")) {
					if (subFile[i].length() < MAX_SIZE) {
						files.add(subFile[i]);
					} else {
						MyLog.print(MyLog.ERROR, TAG, "文件过大：" + subFile[i].length());
					}
				}
			} else {
				List<File> temp = scanFile(subFile[i].getAbsolutePath());
				files.addAll(temp);
			}
		}
		return files;
	}

	private void setListViewListener() {
		mMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mCurrentFile = mMusicDatas.get(position);
				mCurrent = position;
				mIsLoad = false;
				MyLog.print(TAG, mCurrentFile.getPath());
				mPlayerTools.playMusic(mCurrentFile.getPath());
				mAdapter.setClickIndex(position);
				mIsSetRing = true;
				setPlayerImage(mPlayerTools.isPlaying);
				mMusicProgressBar.setMax(mPlayerTools.getDuration());
			}
		});
	}

	@Override
	protected void onResume() {
		findViewById(R.id.root_view).setBackground(SPreferences.getInstance().getWallpaper());
		super.onResume();
	}

	@Override
	protected void onPause() {
		mPlayerTools.release();
		setPlayerImage(mPlayerTools.isPlaying);
		mMusicProgressBar.setProgress(0);
		mIsLoad = true;
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_previous:
			if (mCurrent != 0) {
				mCurrent--;
				mCurrentFile = mMusicDatas.get(mCurrent);
				mPlayerTools.playMusic(mCurrentFile.getPath());
				mAdapter.setClickIndex(mCurrent);
				setPlayerImage(mPlayerTools.isPlaying);
				mMusicProgressBar.setMax(mPlayerTools.getDuration());
			}
			break;
		case R.id.btn_play:
			if (mIsLoad && mCurrentFile != null) {
				mIsLoad = false;
				mPlayerTools.playMusic(mCurrentFile.getPath());
				mAdapter.setClickIndex(mCurrent);
				mIsSetRing = true;
				mMusicProgressBar.setMax(mPlayerTools.getDuration());
				MyLog.print(TAG, "Button Load");
			} else if (mPlayerTools.isPlaying) {
				mPlayerTools.pauseMusic();
			} else {
				mPlayerTools.continueMusic();
			}
			setPlayerImage(mPlayerTools.isPlaying);
			break;
		case R.id.btn_next:
			if (mCurrent != (mMusicDatas.size() - 1)) {
				mIsLoad = false;
				mCurrent++;
				mCurrentFile = mMusicDatas.get(mCurrent);
				mPlayerTools.playMusic(mCurrentFile.getPath());
				mAdapter.setClickIndex(mCurrent);
				mIsSetRing = true;
				setPlayerImage(mPlayerTools.isPlaying);
				mMusicProgressBar.setMax(mPlayerTools.getDuration());
			}
			break;
		case R.id.btn_confirm:
			// 保存铃声
			if (mIsSetRing) {
				SPreferences.getInstance().setRingAbsolutePath(mCurrentFile.getAbsolutePath());
				MyToast.show(R.string.save_success);
			}
			finish();
			break;
		default:
			break;
		}
	}
}
