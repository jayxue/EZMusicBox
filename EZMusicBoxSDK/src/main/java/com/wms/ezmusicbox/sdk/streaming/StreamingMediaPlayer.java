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

package com.wms.ezmusicbox.sdk.streaming;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wms.ezmusicbox.sdk.R;
import com.wms.ezmusicbox.sdk.util.DialogUtil;
import com.wms.ezmusicbox.sdk.util.FileUtil;
import com.wms.ezmusicbox.sdk.util.StringUtil;

/**
 * Acknowledgement: this class was originally created by Daniele Palombo for 105RadioAlarm. Link:
 * 	 https://github.com/DanielePalombo/105RadioAlarm/blob/master/src/org/me/radio105alarm/StreamingMediaPlayer.java
 *
 * It was modified according to purpose of this SDK.
 */

/**
 * MediaPlayer does not yet support streaming from external URLs, so this class provides a pseudo-streaming
 * function by downloading content incrementally and playing as soon as sufficient media data is downloaded
 * into temporary storage.
 */
public class StreamingMediaPlayer {

	// Assume 96kbps*10secs/8bits per byte
    private static final int INTIAL_KB_BUFFER =  96 * 10 / 8;

    private Context context;
    
    // Text view for the media to stream
	private TextView textViewMedia;
	// Image view for the media to stream
	private ImageView imageViewMedia;
	// Button for playing/pausing media
	private ImageButton playButton;
	// Progress bar for streaming process
	private ProgressBar	progressBar;
	
	private ProgressDialog progressDialog;
	
	private String mediaName;
	
	private int mediaSizeInKB, mediaDurationInSeconds, totalKBRead;
	
	// Handler to call view updates on the main UI thread
	private Handler handler;

	private MediaPlayer mediaPlayer;
	
	private File mediaFile; 
	
	private boolean isDownloadingNewFile, isInterrupted;
	
	private int counter;

	public StreamingMediaPlayer(Context context, TextView textViewMedia, ImageView imageViewMedia, ImageButton playButton, ProgressBar progressBar) {
 		this.context = context;
		this.textViewMedia = textViewMedia;
	    this.imageViewMedia = imageViewMedia;
		this.playButton = playButton;
		this.progressBar = progressBar;
		handler = new Handler();
	}
	
    /**  
     * Progressively download the media to a temporary location and update the MediaPlayer as new content becomes available.
     * A progress dialog will be displayed to block user operations if the content is not play-able yet. The dialog will be
     * dismissed when the downloaded content becomes play-able.
     */  
    public void startStreaming(final String mediaUrl, int mediaSizeInKB, int mediaDurationInSeconds, String mediaName, final ProgressDialog progressDialog) throws IOException {    	
    	this.mediaSizeInKB = mediaSizeInKB;
    	this.mediaDurationInSeconds = mediaDurationInSeconds;
    	this.mediaName = mediaName;
    	this.progressDialog = progressDialog;
    			
		Runnable r = new Runnable() {   
	        public void run() {   
	            try {   
	        		downloadMediaIncrement(mediaUrl);
	            } 
	            catch (IOException e) {
	            	if(progressDialog.isShowing()) {
	            		progressDialog.dismiss();
	            	}
	        		Runnable updater = new Runnable() { 
	        			public void run() {
					        isDownloadingNewFile = false;
	        				DialogUtil.showExceptionAlertDialog(context, context.getString(R.string.retrievalFailedTitle), "Unable to initialize the MediaPlayer for " + mediaUrl);
	        	        }
	        	    };
	        	    handler.post(updater);
	            }
	        }   
	    };   
	    new Thread(r).start();
    }
	
    public void startPlayProgressUpdater() {
    	try {
    		float progress = 1.0f;
	    	if(mediaPlayer != null) {
			    progress = (((float) mediaPlayer.getCurrentPosition() / 1000) / mediaDurationInSeconds);
		    }

	    	progressBar.setProgress((int)(progress * 100));
	    	if((int)(progress * 100) == 100) {
	    		playButton.setImageResource(R.drawable.button_stop);
	    	}

			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				Runnable notification = new Runnable() {
			        public void run() {
			        	startPlayProgressUpdater();
					}
			    };
			    handler.postDelayed(notification, 1000);
	    	}
    	}
    	catch(IllegalStateException e) {

    	}
    }
    
    public void interrupt() {
    	playButton.setEnabled(false);
    	isInterrupted = true;
    	validateNotInterrupted();
    	
    	// Downloading a new file?
    	if(isDownloadingNewFile) {
    		// If download is not finished yet?
    		if(progressBar.getSecondaryProgress() < 100) {
    			if(mediaFile != null) {
    				// Delete the partially downloaded file. Next time it will be downloaded again.
    				mediaFile.delete();
    			}
    		}
    		else {
    			playButton.setEnabled(true);
    		}
    	}
    }

    public MediaPlayer getMediaPlayer() {
    	return mediaPlayer;
	}
    
 	public boolean isDownloadingNewFile() {
		return isDownloadingNewFile;
	}
    
    private void downloadMediaIncrement(String mediaUrl) throws IOException {
	    String mediaFileName = FileUtil.getAppExternalStoragePath(context);
	    mediaFile = new File(mediaFileName);
	    if (!mediaFile.exists()) {
		    // Create the folder if it does not exist yet
		    mediaFile.mkdir();
	    }
	    mediaFileName += "/" + StringUtil.getFileNameFromUrl(mediaUrl);
	    mediaFile = new File(mediaFileName);

		isDownloadingNewFile = true;

		URLConnection connection = new URL(mediaUrl).openConnection();			
		connection.connect();   
		InputStream stream = connection.getInputStream();
		if (stream == null) {
			return;
		}
		FileOutputStream out = new FileOutputStream(mediaFile);   
		byte buf[] = new byte[16384];
		int totalBytesRead = 0;
		do {
			int numRead = stream.read(buf);
		    if (numRead <= 0) {
		        break;
		    }
		    out.write(buf, 0, numRead);
		    totalBytesRead += numRead;
		    totalKBRead = totalBytesRead / 1000;		    
		    testMediaBuffer();
		   	fireDataLoadUpdate();
		} 
		while (validateNotInterrupted());   
		
		out.close();
		stream.close();		
		
		if (validateNotInterrupted()) {
		   	fireDataFullyLoaded();
		}
    }  

    private boolean validateNotInterrupted() {
		if (isInterrupted) {
			if (mediaPlayer != null) {
				try {
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
					}
				}
				catch(IllegalStateException e) {

				}
			}
			return false;
		}
		else {
			return true;
		}
    }

    
    /**
     * Test whether we need to transfer buffered data to the MediaPlayer.
     * Interacting with MediaPlayer on non-main UI thread can cause crash so perform this using a Handler.
     */  
    private void  testMediaBuffer() {
	    Runnable updater = new Runnable() {
	        public void run() {
	            if (mediaPlayer == null) {
	            	//  Only create the media player once we have the minimum buffered data
	            	if ( totalKBRead >= INTIAL_KB_BUFFER) {
	            		try {
		            		startMediaPlayer();
	            		}
			            catch (Exception e) {
	            			Log.e(getClass().getName(), "Error copying buffered content.", e);
	            		}
	            	}
	            }
	            else if ( mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000 ){
	            	//  NOTE:  The media player has stopped at the end so transfer any existing buffered data.
	            	//  We test for < 1 second of data because the media player can stop when there is still
	            	//  a few milliseconds of data left to play
	            	transferBufferToMediaPlayer();
	            }
	        }
	    };
	    handler.post(updater);
    }
    
    private void startMediaPlayer() {
        try {   
        	File bufferedFile = new File(FileUtil.getAppExternalStoragePath(context),"playingMedia" + (counter++) + ".dat");
        	
        	// We double buffer the data to avoid potential read/write errors that could happen if the 
        	// download thread attempted to write at the same time the MediaPlayer was trying to read.
        	// For example, we can't guarantee that the MediaPlayer won't open a file for playing and leave it locked while 
        	// the media is playing.  This would permanently deadlock the file download.  To avoid such a deadlock, 
        	// we move the currently loaded data to a temporary buffer file that we start playing while the remaining 
        	// data downloads.  
        	moveFile(mediaFile, bufferedFile);
    		
        	mediaPlayer = createMediaPlayer(bufferedFile);
        	
    		// We have pre-loaded enough content and started the MediaPlayer so update the buttons and progress.
	    	mediaPlayer.start();
	    	startPlayProgressUpdater();        	
			playButton.setEnabled(true);
        }
        catch (IOException e) {
        	Log.e(getClass().getName(), "Error initializing the MediaPlayer: ", e);
        }   
    }
    
    public MediaPlayer createMediaPlayer(File mediaFile) throws IOException {
    	MediaPlayer mPlayer = new MediaPlayer();
    	mPlayer.setOnErrorListener(
			new MediaPlayer.OnErrorListener() {
		        public boolean onError(MediaPlayer mp, int what, int extra) {
		            Log.e(getClass().getName(), "Error in MediaPlayer: (" + what +") with extra (" + extra + ")" );
		            return false;
		        }
	        }
	    );

		// It appears that for security/permission reasons, it is better to pass a FileDescriptor rather than a direct path to the File.
		// Also there are errors such as "PVMFErrNotSupported" and "Prepare failed.: status=0x1" if a file path String is passed to
		// setDataSource(). So unless otherwise noted, we use a FileDescriptor here.
		FileInputStream fis = new FileInputStream(mediaFile);
		mPlayer.setDataSource(fis.getFD());
		fis.close();
		mPlayer.prepare();
		return mPlayer;
    }
    
    /**
     * Transfer buffered data to the MediaPlayer.
     * NOTE: Interacting with a MediaPlayer on a non-main UI thread can cause thread-lock and crashes so 
     * this method should always be called using a Handler.
     */  
    private void transferBufferToMediaPlayer() {
	    try {
	    	// First determine if we need to restart the player after transferring data, e.g. perhaps the user pressed pause
	    	boolean wasPlaying = false;
		    int curPosition = 0;
		    if(mediaPlayer != null) {
			    wasPlaying = mediaPlayer.isPlaying();
			    curPosition = mediaPlayer.getCurrentPosition();
		    }
	    	
	    	// Copy the currently downloaded content to a new buffered File. Store the old File for deleting later.
	    	File oldBufferedFile = new File(FileUtil.getAppExternalStoragePath(context), "playingMedia" + counter + ".dat");
	    	File bufferedFile = new File(FileUtil.getAppExternalStoragePath(context), "playingMedia" + (counter++) + ".dat");

	    	// This may be the last buffered File so ask that it be delete on exit. If it's already deleted, then this won't mean anything. If you want to
	    	// keep and track fully downloaded files for later use, write caching code and please send me a copy.
	    	bufferedFile.deleteOnExit();   
	    	moveFile(mediaFile, bufferedFile);

		    if(mediaPlayer != null) {
			    // Pause the current player now as we are about to create and start a new one. So far (Android v1.5),
			    // this always happens so quickly that the user never realized we've stopped the player and started a new one
			    mediaPlayer.pause();

			    // Create a new MediaPlayer rather than try to re-prepare the prior one.
			    mediaPlayer = createMediaPlayer(bufferedFile);
			    mediaPlayer.seekTo(curPosition);

			    //  Restart if at end of prior buffered content or mediaPlayer was previously playing.
			    //	NOTE: We test for < 1second of data because the media player can stop when there is still
			    //  a few milliseconds of data left to play
			    boolean atEndOfFile = mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition() <= 1000;
			    if (wasPlaying || atEndOfFile) {
				    mediaPlayer.start();
			    }
		    }

	    	// Lastly delete the previously playing buffered File as it's no longer needed.
	    	oldBufferedFile.delete();
	    }
	    catch (Exception e) {
	    	Log.e(getClass().getName(), "Error updating to newly loaded content.", e);            		
		}
    }
    
    private void fireDataLoadUpdate() {
		Runnable updater = new Runnable() {
	        public void run() {
	        	textViewMedia.setText(mediaName + " " + context.getString(R.string.hasRetrieved) + " " + totalKBRead + " KB");
	    		float loadProgress = ((float)totalKBRead / (float)mediaSizeInKB);
	    		progressBar.setSecondaryProgress((int)(loadProgress * 100));
	    		if(loadProgress * 100 > 5) {
	    			if(progressDialog != null && progressDialog.isShowing()) {
	    				progressDialog.dismiss();
	    			}
	    		}
	        }
	    };
	    handler.post(updater);
    }
    
    private void fireDataFullyLoaded() {
		Runnable updater = new Runnable() { 
			public void run() {
   	        	transferBufferToMediaPlayer();
   	        	isDownloadingNewFile = false;	// Change the flag
   	        	progressBar.setSecondaryProgress(100);
	        	textViewMedia.setText(mediaSizeInKB + " KB " + context.getString(R.string.retrieved) + " " + context.getString(R.string.playing) + " " + mediaName);
				playButton.setImageResource(R.drawable.button_pause);
				playButton.setEnabled(true);
				imageViewMedia.setImageResource(R.drawable.music_downloaded);
	        }
	    };
	    handler.post(updater);
    }

    /**
     *  Move the file from oldLocation to newLocation.
     */
	private void moveFile(File oldLocation, File newLocation) throws IOException {
		if (oldLocation.exists()) {
			BufferedInputStream reader = null;
			BufferedOutputStream writer = null;
			try {
				reader = new BufferedInputStream(new FileInputStream(oldLocation));
				writer = new BufferedOutputStream(new FileOutputStream(newLocation, false));
			    byte[]  buff = new byte[8192];
			    int numChars = 0;
			    while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
			    	writer.write(buff, 0, numChars);
			    }
			}
			catch( IOException ex ) {
				throw new IOException("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
			}
			finally {
				try {
					if (reader != null) {
						reader.close();
				    }
					if(writer != null) {
						writer.close();
					}
				}
				catch( IOException ex ){
					Log.e(getClass().getName(),"Error closing files when transferring " + oldLocation.getPath() + " to " + newLocation.getPath()); 
				}
			}
        }
		else {
			throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
        }
	}
}
