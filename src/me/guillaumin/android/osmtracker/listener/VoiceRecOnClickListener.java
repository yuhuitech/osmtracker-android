package me.guillaumin.android.osmtracker.listener;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import me.guillaumin.android.osmtracker.R;
import me.guillaumin.android.osmtracker.activity.OSMTracker;
import me.guillaumin.android.osmtracker.activity.TrackLogger;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * Manages voice recording.
 * 
 * @author Nicolas Guillaumin
 * 
 */
public class VoiceRecOnClickListener implements OnClickListener, OnInfoListener {

	private final static String TAG = VoiceRecOnClickListener.class.getSimpleName();
	
	/**
	 * Dialog shown while recording
	 */
	ProgressDialog progressDialog;
	
	/**
	 * Parent activity
	 */
	TrackLogger activity;
	
	public VoiceRecOnClickListener(TrackLogger a) {
		activity = a;
	}

	@Override
	public void onClick(View v) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
		String duration = prefs.getString(OSMTracker.PREF_VOICEREC_DURATION, "2");
		boolean playAfter = prefs.getBoolean(OSMTracker.PREF_VOICEREC_PLAYAFTER, false);
		
		// Get a new audio filename
		File audioFile = activity.getGpsLogger().getDataHelper().getNewAudioFile();
	
		// Show a progress dialog while recording
		progressDialog = new ProgressDialog(v.getContext());
		progressDialog.setTitle(v.getResources().getString(R.string.tracklogger_voicerec_title));
		progressDialog.setMessage(v.getResources().getString(R.string.tracklogger_voicerec_text).replaceAll("\\{0\\}", duration));
		progressDialog.show();

		MediaRecorder mediaRecorder = new MediaRecorder();
		
		try {
			// MediaRecorder configuration
			
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
			mediaRecorder.setMaxDuration(Integer.parseInt(duration) * 1000);
			mediaRecorder.setOnInfoListener(this);
		
			Log.d(TAG, "Starting voice rec");
			mediaRecorder.prepare();
			mediaRecorder.start();
						
		} catch (Exception ioe) {
			Log.w("Voice recording has failed", ioe);
			try {
				mediaRecorder.stop();
			} catch (Exception e) {
				Log.w(TAG, "Recording has failed, and MediaPlayer.stop() too");
			} finally {
				mediaRecorder.reset();
				mediaRecorder.release();
			}
			progressDialog.dismiss();
			Toast.makeText(v.getContext(), v.getResources().getString(R.string.error_voicerec_failed), Toast.LENGTH_SHORT).show();
		}
		
		activity.getGpsLogger().trackWayPoint(v.getResources().getString(R.string.wpt_voicerec), audioFile.getName());
	}

	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
			// No need to stop, MediaRecorder has been stopped by system
			mr.reset();
			mr.release();
			
			// Dismiss dialog
			Log.d(TAG, "Dismissing record dialog");
			progressDialog.dismiss();
		}
		
	}
	
}