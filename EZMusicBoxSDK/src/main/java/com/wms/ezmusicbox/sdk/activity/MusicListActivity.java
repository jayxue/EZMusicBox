/*
 * Copyright 2016 Waterloo Mobile Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wms.ezmusicbox.sdk.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wms.ezmusicbox.sdk.R;
import com.wms.ezmusicbox.sdk.adapter.MusicArrayAdapter;
import com.wms.ezmusicbox.sdk.entity.Music;
import com.wms.ezmusicbox.sdk.listener.ImageButtonSrcSelector;
import com.wms.ezmusicbox.sdk.streaming.StreamingMediaPlayer;
import com.wms.ezmusicbox.sdk.type.MusicPlayMode;
import com.wms.ezmusicbox.sdk.util.ActivityUtil;
import com.wms.ezmusicbox.sdk.util.DialogUtil;
import com.wms.ezmusicbox.sdk.util.FileUtil;
import com.wms.ezmusicbox.sdk.util.MusicUtil;
import com.wms.ezmusicbox.sdk.util.SharedPreferenceUtil;
import com.wms.ezmusicbox.sdk.util.StringUtil;
import com.wms.ezmusicbox.sdk.view.AnimView;

public class MusicListActivity extends AppCompatActivity {

	private ListView listViewMusic;
	private ProgressDialog progressDialogForStartingDownload;
	private TextView textViewMusicToPlay;
	private ImageButton imageButtonPlayMusic;
	private ProgressBar progressBar;
	private TextView textViewMusicItem = null;
	
	private StreamingMediaPlayer audioStreamer = null;
	private MediaPlayer mediaPlayer;

	private boolean isPlaying;

	private List<Music> musicList;
	private MusicArrayAdapter adapter;

	private int musicPlayMode = MusicPlayMode.SEQUENTIAL_LOOPING;
	private int playingMusicIndex = -1;
	private long playingMediaDuration;

	private View lastClickedItem;

	private AnimView animView = null;

	SubMenu subMenu = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.music_list);
        
        musicList = MusicUtil.loadMustList(this);
        
        textViewMusicToPlay = (TextView) findViewById(R.id.textViewMusicToPlay);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
         
        listViewMusic = (ListView) findViewById(R.id.listViewMusic);
        listViewMusic.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {		

				if(audioStreamer != null) {
					if(audioStreamer.isDownloadingNewFile()) {
						Toast.makeText(MusicListActivity.this, getString(R.string.waitForMusicDownloading), Toast.LENGTH_LONG).show();
						return;
					}
					else {
						MediaPlayer autoStreamerPlayer = audioStreamer.getMediaPlayer();
						if(autoStreamerPlayer != null) {
							try {
								if (autoStreamerPlayer.isPlaying()) {
									autoStreamerPlayer.stop();
								}
								autoStreamerPlayer.reset();
								autoStreamerPlayer.release();
							}
							catch (IllegalStateException e) {

							}
						}
					}
				}

				if(lastClickedItem != null) {
					textViewMusicItem = (TextView) lastClickedItem.findViewById(R.id.textViewMusicItem);
					textViewMusicItem.setTextColor(Color.BLACK);
				}

				lastClickedItem = view;
				textViewMusicItem = (TextView) view.findViewById(R.id.textViewMusicItem);
				textViewMusicItem.setTextColor(Color.GRAY);

				Music music = musicList.get(position);
				File downloadingMediaFile = new File(FileUtil.getAppExternalStoragePath(MusicListActivity.this), StringUtil.getFileNameFromUrl(music.getUrl()));
				if (downloadingMediaFile.exists()) {
					playDownloadedMusicByIndex(position);
				}
				else {
					// Stop playing current music
					resetMediaPlayer();

					// Display the progress dialog before downloading starts
					imageButtonPlayMusic.setEnabled(true);
					progressDialogForStartingDownload = DialogUtil.showWaitingProgressDialog(MusicListActivity.this, ProgressDialog.STYLE_SPINNER, getString(R.string.downloadingMusic), false);
					startStreamingAudio(music.getUrl(), music.getSize(), music.getDuration(), music.getName(), progressDialogForStartingDownload, (ImageView) view.findViewById(R.id.imageViewMusicIcon));
					Toast.makeText(MusicListActivity.this, getString(R.string.downloadingMusic), Toast.LENGTH_LONG).show();
				}
			}
			
		});

	    imageButtonPlayMusic = (ImageButton) findViewById(R.id.imageButtonPlayMusic);
        imageButtonPlayMusic.setEnabled(false);
	    imageButtonPlayMusic.setOnTouchListener(new ImageButtonSrcSelector());
        imageButtonPlayMusic.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(mediaPlayer != null) {
					if(mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
						imageButtonPlayMusic.setImageResource(R.drawable.button_play);
					}
					else {					
						mediaPlayer.start();
						imageButtonPlayMusic.setImageResource(R.drawable.button_pause);
						startPlayProgressUpdater(mediaPlayer, playingMediaDuration);
					}
				}

				if(audioStreamer != null) {
					if(!audioStreamer.isDownloadingNewFile()) {
						try {
							if (audioStreamer.getMediaPlayer().isPlaying()) {
								audioStreamer.getMediaPlayer().pause();
								imageButtonPlayMusic.setImageResource(R.drawable.button_play);
							}
							else {
								audioStreamer.getMediaPlayer().start();
								audioStreamer.startPlayProgressUpdater();
								imageButtonPlayMusic.setImageResource(R.drawable.button_pause);
							}
						}
						catch(IllegalStateException e) {

						}
					}
				}
				isPlaying = !isPlaying;
        }});

	    playAnimation();

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		adapter = new MusicArrayAdapter(MusicListActivity.this, R.layout.music_list_item, R.id.textViewMusicItem, musicList);
		listViewMusic.setAdapter(adapter);

	    // Display app icon
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setLogo(R.mipmap.ic_launcher);
	    actionBar.setDisplayUseLogoEnabled(true);
	    actionBar.setDisplayShowHomeEnabled(true);

	    installShortCutIfNeeded();
    }
    
    @Override
    public void onBackPressed() {
		if (audioStreamer != null) {
			if(audioStreamer.isDownloadingNewFile()) {
				Toast.makeText(MusicListActivity.this, getString(R.string.waitForMusicDownloading), Toast.LENGTH_LONG).show();
				return;
			}
			else  {
				if(audioStreamer.getMediaPlayer() != null) {
					try {
						audioStreamer.getMediaPlayer().stop();
						audioStreamer.getMediaPlayer().reset();
						audioStreamer.getMediaPlayer().release();
					}
					catch (IllegalStateException e) {

					}
				}
			}
		}

		try {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
		}
		catch (IllegalStateException e) {

		}
		finally {
			super.onBackPressed();
		}
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		subMenu = menu.addSubMenu("Menu");
		subMenu.add(getString(R.string.action_stopAnim));
		subMenu.add(getString(R.string.action_changePlayMode));
		subMenu.add(getString(R.string.action_about)).setIcon(R.drawable.about_app);

		MenuItem subMenuItem = subMenu.getItem();
		subMenuItem.setIcon(R.drawable.menu_icon);
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals(getString(R.string.action_about))) {
			ActivityUtil.goToActivity(this, AboutAppActivity.class);
			return true;
		}
		else if(item.getTitle().equals(getString(R.string.action_stopAnim))) {
			animView.setVisibility(View.GONE);
			Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.fadeout_slow);
			animView.startAnimation(myAnim);
			return true;
		}
		else if(item.getTitle().equals(getString(R.string.action_changePlayMode))) {
			Toast.makeText(this, "Not implemented yet", Toast.LENGTH_LONG).show();
		}
		return super.onOptionsItemSelected(item);
	}

    private void startStreamingAudio(String musicURL, int mediaLengthInKb, int mediaLengthInSeconds, String musicName, ProgressDialog progressDialogForStartingDownload, ImageView imageViewMusic) {
    	try { 
    		if (audioStreamer != null) {
    			audioStreamer.interrupt();
    		}
    		audioStreamer = new StreamingMediaPlayer(this, textViewMusicToPlay, imageViewMusic, imageButtonPlayMusic, progressBar);
    		audioStreamer.startStreaming(musicURL, mediaLengthInKb, mediaLengthInSeconds, musicName, progressDialogForStartingDownload);
    	}
	    catch (IOException e) {
	    	Log.e(getClass().getName(), "Error starting to stream audio: ", e);
    	}
    }

	private void resetMediaPlayer() {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer.reset();
			mediaPlayer.release();
			mediaPlayer = null;
		}
	}

	public MediaPlayer createMediaPlayer() {
		MediaPlayer mPlayer = new MediaPlayer();
		mPlayer.setLooping(true);
		mPlayer.setOnErrorListener(
			new MediaPlayer.OnErrorListener() {
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e(getClass().getName(), "Error in MediaPlayer: (" + what + ") with extra (" + extra + ")");
					return false;
				}
			}
		);

		return mPlayer;
	}

    private void playDownloadedMusicByIndex(int indexOfMusicToPlay) {
    	Music music = musicList.get(indexOfMusicToPlay);
	    String url = music.getUrl();
		int duration = music.getDuration();
	    int minutes = duration / 60;
	    int seconds = duration - minutes * 60;
	    playingMediaDuration = duration;

	    resetMediaPlayer();
	    playingMusicIndex = indexOfMusicToPlay;

	    // Update music name to play
	    textViewMusicToPlay.setText(MusicListActivity.this.getString(R.string.playing) + " " + music.getName() + "    " + minutes + ":" + seconds);

	    // Create media player from downloaded file
	    mediaPlayer = createMediaPlayer();
	    File musicFile = new File(FileUtil.getAppExternalStoragePath(this), StringUtil.getFileNameFromUrl(url));
	    if(musicFile.exists()) {
		    try {
			    //  It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
			    //  Also I have seen errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
			    //  setDataSource().  So unless otherwise noted, we use a FileDescriptor here.
			    FileInputStream fis = new FileInputStream(musicFile);
			    mediaPlayer.setDataSource(fis.getFD());
			    mediaPlayer.prepare();
		    }
		    catch (IOException e) {

		    }
	    }
	    else {
		    Toast.makeText(this, getString(R.string.musicNotAvailable), Toast.LENGTH_LONG).show();
	    }

	    mediaPlayer.setLooping(false);
	    mediaPlayer.setOnCompletionListener(new RepeatableMediaPlayerOnCompletionListener());
	    mediaPlayer.start();
	    startPlayProgressUpdater(mediaPlayer, duration);
	    imageButtonPlayMusic.setEnabled(true);
	    imageButtonPlayMusic.setImageResource(R.drawable.button_pause);
    }

	private final Handler handler = new Handler();
	private void startPlayProgressUpdater(final MediaPlayer mediaPlayer, final long mediaLengthInSeconds) {
		try {
			float progress = 1.0f;
			if(mediaPlayer != null) {
				progress = (((float) mediaPlayer.getCurrentPosition() / 1000) / mediaLengthInSeconds);
			}
			progressBar.setProgress((int)(progress*100));
			if((int)(progress * 100) == 100) {
				imageButtonPlayMusic.setImageResource(R.drawable.button_stop);
			}
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				Runnable notification = new Runnable() {
					public void run() {
						startPlayProgressUpdater(mediaPlayer, mediaLengthInSeconds);
					}
				};
				handler.postDelayed(notification, 1000);
			}
		}
		catch(IllegalStateException e) {

		}
	}

	private class RepeatableMediaPlayerOnCompletionListener implements MediaPlayer.OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mp) {
			if(textViewMusicItem != null) {
				textViewMusicItem.setTextColor(Color.BLACK);
			}
			
			if(musicPlayMode == MusicPlayMode.SINGLE_SONG) {
				return;
			}

			if(musicPlayMode == MusicPlayMode.SINGLE_LOOPING) {
				mediaPlayer.start();
			}
			else if(musicPlayMode == MusicPlayMode.SEQUENTIAL_LOOPING) {
				setTextViewMusicToPlayColor(playingMusicIndex, Color.BLACK);
				if(playingMusicIndex < musicList.size() - 1) {
					playingMusicIndex++;
				}
				else {
					playingMusicIndex = 0;
				}
				setTextViewMusicToPlayColor(playingMusicIndex, Color.GRAY);
			}		
			else {	// musicPlayMode is MusicPlayMode.RANDOM_LOOPING
				Random random = new Random();
				playingMusicIndex = random.nextInt(musicList.size());
			}
			playDownloadedMusicByIndex(playingMusicIndex);
		}
    	
    }

	private void playAnimation() {
		// Show animation
		animView = new AnimView(this);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		params.leftMargin = 0;
		RelativeLayout baseRelativeLayout = (RelativeLayout) findViewById(R.id.baseRelativeLayout);
		baseRelativeLayout.addView(animView, params);

		Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.fadein_slow);
		animView.startAnimation(myAnim);
	}

	private void setTextViewMusicToPlayColor(int pos, int color) {
		LinearLayout musicItemView = getMusicItemViewByPosition(pos);
		int count = musicItemView.getChildCount();
		for(int i = 0; i < count; i++) {
			View v = musicItemView.getChildAt(i);
			if(v instanceof  TextView) {
				((TextView) v).setTextColor(color);
			}
		}
	}

	private LinearLayout getMusicItemViewByPosition(int pos) {
		final int firstListItemPosition = listViewMusic.getFirstVisiblePosition();
		final int lastListItemPosition = firstListItemPosition + listViewMusic.getChildCount() - 1;

		if (pos < firstListItemPosition || pos > lastListItemPosition ) {
			return (LinearLayout) listViewMusic.getAdapter().getView(pos, null, listViewMusic);
		}
		else {
			final int childIndex = pos - firstListItemPosition;
			return (LinearLayout) listViewMusic.getChildAt(childIndex);
		}
	}

	public int getPlayingMusicIndex() {
		return playingMusicIndex;
	}

	private void installShortCutIfNeeded() {
		String firstTimeUse = SharedPreferenceUtil.getPreferenceItemByName(this, SharedPreferenceUtil.firstTimeUse);
		if(firstTimeUse.equals("")) {
			firstTimeUse = "true";
		}

		if(firstTimeUse.equals("true")) { // Only create shortcut on the first time use
			// Handle shortcut
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
				if (ifShortcutCreated() == false) {  // No duplicated shortcut
					addShortCut();
				}
			}
		}

		SharedPreferenceUtil.savePreferenceItemByName(this, SharedPreferenceUtil.firstTimeUse, "false");	// It would never be first time use later
	}

	private boolean ifShortcutCreated() {
		boolean shortcutCreated = false;

		final ContentResolver cr = getContentResolver();

		String AUTHORITY = "";

		if(android.os.Build.VERSION.SDK_INT < 8){
			AUTHORITY = "com.android.launcher.settings";
		}
		else{
			AUTHORITY = "com.android.launcher2.settings";
		}

		Cursor c = null;
		Uri CONTENT_URI = null;
		try {
			CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");
			c = cr.query(CONTENT_URI,
					new String[] { "title", "iconResource" }, "title=?",
					new String[] { getString(R.string.app_name) }, null);
		}
		catch(Exception e) {

		}
		if (c != null && c.getCount() > 0) {
			shortcutCreated = true;
		}
		return shortcutCreated;
	}

	private void addShortCut() {
		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name));
		Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		shortcut.putExtra("duplicate", false);

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setClass(this, MusicListActivity.class);

		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
		sendBroadcast(shortcut);
	}
}
