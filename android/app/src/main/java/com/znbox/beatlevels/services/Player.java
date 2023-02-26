package com.znbox.beatlevels.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.MediaMetadata;
import com.google.android.exoplayer2.audio.AudioProcessor;
import com.google.android.exoplayer2.audio.AudioSink;
import com.google.android.exoplayer2.audio.DefaultAudioSink;
import com.google.android.exoplayer2.audio.TeeAudioProcessor;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.znbox.beatlevels.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class Player extends Service {

	private static ExoPlayer exoPlayer = null;
	private static int CURRENT_INDEX = 0;
	private static int[] player_queue = null;
	private static Handler volume_handler = new Handler();
	private static Runnable volume_runner = null;

	public Player() {

	}

	@Override
	public void onCreate() {
		super.onCreate();

		if(exoPlayer == null) {
			DefaultRenderersFactory renderersFactory = new DefaultRenderersFactory(this) {
				@Nullable
				@Override
				protected AudioSink buildAudioSink(Context context, boolean enableFloatOutput, boolean enableAudioTrackPlaybackParams, boolean enableOffload) {
					DefaultAudioSink.Builder builder = new DefaultAudioSink.Builder();
					builder.setAudioProcessors(new AudioProcessor[]{
							new TeeAudioProcessor(new TeeAudioProcessor.AudioBufferSink() {
								@Override
								public void flush(int sampleRateHz, int channelCount, @C.PcmEncoding int encoding) {
									//Log.w("DEBUG", "PCM configuration: sampleRateHz=" + sampleRateHz + ", channelCount=" + channelCount + ", encoding=" + encoding);
								}

								@Override
								public void handleBuffer(ByteBuffer buffer) {

								}
							})
					});
					return builder.build();
				}
			};
			exoPlayer = new ExoPlayer.Builder(getApplicationContext(), renderersFactory).build();
			exoPlayer.setVolume(1);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

        try {

	        Log.d("Where: ", "Begin");
            if(exoPlayer == null) {
                return START_NOT_STICKY;
            }
	        Log.d("Where: ", "After begin");

            final String ACTION = intent.getAction();

            switch (ACTION) {
                case "SET_MEDIA": {
	                Log.d("Where: ", "Start");
	                final String media_info_string = intent.getStringExtra("json");
	                final JSONObject media_info = new JSONObject(media_info_string);

	                final String media_id = media_info.getString("id");
	                final boolean media_local = media_info.getBoolean("local");
	                final long media_duration = media_info.getLong("duration");
	                final String media_uri = media_info.getString("uri");
	                final String media_title = media_info.getString("title");
	                final String media_artist = media_info.getString("artist");
	                final String media_artwork_uri = media_info.getString("artwork_uri");
					final JSONArray media_queue = media_info.getJSONArray("queue");

	                player_queue = new int[media_queue.length()];
	                for (int i = 0; i < media_queue.length(); i ++) {
		                player_queue[i] = media_queue.getInt(i);
	                }
	                Log.d("Where: ", "Here");

                    DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(getApplicationContext());
                    MediaItem mediaItem = null;

					if(media_local) {
						/* If local file */
						File media_file = new File(media_uri);
						MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
						metadataRetriever.setDataSource(media_file.getAbsolutePath());
						byte[] img = metadataRetriever.getEmbeddedPicture();
						if (img == null) {
							@SuppressLint("UseCompatLoadingForDrawables") Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.default_note)).getBitmap();
							ByteArrayOutputStream stream = new ByteArrayOutputStream();
							bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
							img = stream.toByteArray();
						}
						Bundle bundle = new Bundle();
						bundle.putLong("duration", media_duration);
						bundle.putByteArray("img", img);

						MediaMetadata.Builder builder = new MediaMetadata.Builder();
						builder.setTitle(media_title);
						builder.setArtist(media_artist);
						builder.setArtworkData(img, MediaMetadata.PICTURE_TYPE_FILE_ICON_OTHER);
						builder.setExtras(bundle);

						MediaMetadata mediaMetadata = builder.build();

						mediaItem = new MediaItem.Builder()
								.setMediaMetadata(mediaMetadata)
								.setUri(media_file.getAbsolutePath())
								.setMediaId(media_id)
								.build();
					} else {
						/* Remote files */
						@SuppressLint("UseCompatLoadingForDrawables") Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.default_note)).getBitmap();
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byte[] img = stream.toByteArray();

						MediaMetadata.Builder builder = new MediaMetadata.Builder();
						builder.setTitle(media_title);
						builder.setArtist(media_artist);
						builder.setArtworkData(img, MediaMetadata.PICTURE_TYPE_FILE_ICON_OTHER);

						Bundle bundle = new Bundle();
						bundle.putLong("duration", media_duration);
						bundle.putByteArray("img", img);
						builder.setExtras(bundle);

						MediaMetadata mediaMetadata = builder.build();
						mediaItem = new MediaItem.Builder()
								.setMediaMetadata(mediaMetadata)
								.setUri(media_uri)
								.setMediaId(media_id)
								.build();
					}
                    MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem);
	                CURRENT_INDEX = 0;
                    exoPlayer.setMediaSource(mediaSource);
					exoPlayer.prepare();
                    break;
                }
	            case "PLAY": {
					if(exoPlayer != null) {
						exoPlayer.play();
						volume_runner = new Runnable() {
							float time = 0;
							@Override
							public void run() {
								time += 0.1;
								if(time > 1) {
									volume_handler.removeCallbacks(volume_runner);
								} else {
									exoPlayer.setVolume(time);
									volume_handler.postDelayed(volume_runner, 500);
								}
							}
						};
						volume_handler.postDelayed(volume_runner, 0);
					}
					break;
	            }
	            case "PAUSE": {
		            if(exoPlayer != null) {
			            volume_runner = new Runnable() {
				            float time = 0;
				            @Override
				            public void run() {
					            time -= 0.1;
					            if(time < 0) {
						            volume_handler.removeCallbacks(volume_runner);
					            } else {
						            exoPlayer.setVolume(time);
						            volume_handler.postDelayed(volume_runner, 100);
					            }
				            }
			            };
			            volume_handler.postDelayed(volume_runner, 0);
			            exoPlayer.pause();
		            }
					break;
	            }
	            case "STOP": {
		            if(exoPlayer != null) {
			            this.onDestroy();
		            }
					break;
	            }
                default: {

                    break;
                }
            }
        } catch (Exception ex) {
            if (ex.getMessage() != null) {
                Log.d("onStartCommand ERROR: ", ex.getMessage());
            }
        }
        return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (exoPlayer == null) {
			exoPlayer.stop();
			exoPlayer.release();
			exoPlayer = null;
		}
		super.onDestroy();
	}
}