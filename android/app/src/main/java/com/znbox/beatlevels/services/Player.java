package com.znbox.beatlevels.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

public class Player extends Service {

	private static ExoPlayer exoPlayer = null;
	private static int CURRENT_INDEX = 0;
	private static int[] player_queue = null;
	private static Handler volume_handler = new Handler();
	private static Runnable volume_runner = null;


	/* Notification */
	private static String CHANNEL_ID = "BeatLevels Player";
	private static String CHANNEL_NAME = "BeatLevels Channel";
	private static int NOTIFICATION_ID = 1;
	private static NotificationManagerCompat player_notification_manager = null;
	private static Notification player_notification = null;
	private static MediaSessionCompat mediaSessionCompat = null;

	/* Notification Actions */
	public static final String ACTION_PLAY = "play";
	public static final String ACTION_PAUSE = "pause";
	public static final String ACTION_REWIND = "rewind";
	public static final String ACTION_FAST_FORWARD = "fast_foward";
	public static final String ACTION_NEXT = "next";
	public static final String ACTION_PREVIOUS = "previous";
	public static final String ACTION_STOP = "stop";

	public Player() {

	}

	@SuppressLint("ServiceCast")
	private void create_notification() {
		try {
			NotificationChannelCompat notificationChannelCompat = new NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_NONE)
					.setLightColor(Color.BLUE)
					.setName(CHANNEL_NAME)
					.build();
			byte[] buff = exoPlayer.getMediaMetadata().artworkData;
			Bitmap bitmap = BitmapFactory.decodeByteArray(buff, 0, buff.length);

			if(mediaSessionCompat == null) {
				mediaSessionCompat = new MediaSessionCompat(this, "Player");
				/* Setting playback state here because exoplayer onIsPlayingChanged doesn't fire on set music */
				mediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
						.setState(PlaybackStateCompat.STATE_PLAYING, 0, 1)
						.setActions(PlaybackStateCompat.ACTION_SEEK_TO)
						.build()
				);
			}
			android.media.MediaMetadata mediaMetadata = new android.media.MediaMetadata.Builder()
					.putBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART, bitmap)
					.putString(android.media.MediaMetadata.METADATA_KEY_ARTIST, (String) exoPlayer.getMediaMetadata().artist)
					.putString(android.media.MediaMetadata.METADATA_KEY_ALBUM, (String) exoPlayer.getMediaMetadata().albumTitle)
					.putString(android.media.MediaMetadata.METADATA_KEY_TITLE, (String) exoPlayer.getMediaMetadata().title)
					.putLong(android.media.MediaMetadata.METADATA_KEY_DURATION, exoPlayer.getDuration())
					.build();
			mediaSessionCompat.setMetadata(MediaMetadataCompat.fromMediaMetadata(mediaMetadata));
			mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
				@Override
				public void onPlay() {
					Intent intent = new Intent();
					intent.setAction("PLAY");
					intent.setClass(getApplicationContext(), Player.class);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						startForegroundService(intent);
					}
				}

				@Override
				public void onPause() {
					Intent intent = new Intent();
					intent.setAction("PAUSE");
					intent.setClass(getApplicationContext(), Player.class);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						startForegroundService(intent);
					}
				}

				@Override
				public void onSeekTo(long pos) {
					super.onSeekTo(pos);
					exoPlayer.seekTo(pos);
				}
			});
			mediaSessionCompat.setActive(true);
			androidx.media.app.NotificationCompat.MediaStyle mediaStyle = new androidx.media.app.NotificationCompat.MediaStyle()
					.setShowActionsInCompactView(1, 2, 3)
					.setMediaSession(MediaSessionCompat.Token.fromToken(mediaSessionCompat.getSessionToken().getToken()));

			/* Setting playback actions */
			Intent pauseIntent = new Intent(this, Player.class);
			Bundle bundle_pause = new Bundle();
			pauseIntent.setAction("PAUSE");
			pauseIntent.putExtras(bundle_pause);
			PendingIntent pause_pendingIntent = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				pause_pendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_MUTABLE);
			} else {
				pause_pendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			}
			NotificationCompat.Action pause_action = new NotificationCompat.Action.Builder(R.drawable.round_pause_24, ACTION_PAUSE, pause_pendingIntent).build();

			Intent playIntent = new Intent(this, Player.class);
			Bundle bundle_play = new Bundle();
			playIntent.setAction("PLAY");
			playIntent.putExtras(bundle_play);
			PendingIntent play_pendingIntent = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				play_pendingIntent = PendingIntent.getService(this, 2, playIntent, PendingIntent.FLAG_MUTABLE);
			} else {
				play_pendingIntent = PendingIntent.getService(this, 2, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			}
			NotificationCompat.Action play_action = new NotificationCompat.Action.Builder(R.drawable.round_play_arrow_24, ACTION_PLAY, play_pendingIntent).build();

			Intent stopIntent = new Intent(this, Player.class);
			Bundle bundle_stop = new Bundle();
			stopIntent.setAction("STOP");
			stopIntent.putExtras(bundle_stop);
			PendingIntent stop_pendingIntent = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				stop_pendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_MUTABLE);
			} else {
				stop_pendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			}
			NotificationCompat.Action stop_action = new NotificationCompat.Action.Builder(R.drawable.round_close_24, ACTION_STOP, stop_pendingIntent).build();

			Intent nextIntent = new Intent(this, Player.class);
			stopIntent.setAction("NEXT");
			Bundle bundle_next = new Bundle();
			nextIntent.putExtras(bundle_next);
			PendingIntent next_pendingIntent = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				next_pendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_MUTABLE);
			} else {
				next_pendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			}
			NotificationCompat.Action next_action = new NotificationCompat.Action.Builder(R.drawable.round_skip_next_24, ACTION_NEXT, next_pendingIntent).build();

			Intent previousIntent = new Intent(this, Player.class);
			stopIntent.setAction("PREVIOUS");
			Bundle bundle_previous = new Bundle();
			previousIntent.putExtras(bundle_previous);
			PendingIntent previous_pendingIntent = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				previous_pendingIntent = PendingIntent.getService(this, 4, previousIntent, PendingIntent.FLAG_MUTABLE);
			} else {
				previous_pendingIntent = PendingIntent.getService(this, 4, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			}
			NotificationCompat.Action previous_action = new NotificationCompat.Action.Builder(R.drawable.round_skip_previous_24, ACTION_PREVIOUS, previous_pendingIntent).build();

			Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setSmallIcon(R.drawable.play_circle)
					.setContentTitle(exoPlayer.getMediaMetadata().title)
					.setContentText(exoPlayer.getMediaMetadata().artist)
					.setLargeIcon(bitmap)
					.addAction(previous_action)
					.addAction(exoPlayer.isPlaying() ? pause_action : play_action)
					.addAction(next_action)
					.addAction(stop_action)
					.setStyle(mediaStyle)
					.setColorized(true)
					.setUsesChronometer(true)
					.build();
			player_notification_manager = NotificationManagerCompat.from(getApplicationContext());
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
				/* Notification Manager. Creating notification channel */
				if(player_notification_manager.getNotificationChannelsCompat().size() == 0) {
					player_notification_manager.createNotificationChannel(notificationChannelCompat);
				}
			}
			/* Setting or updating notification */
			if(player_notification == null) {
				startForeground(NOTIFICATION_ID, notification);
				player_notification = notification;
			} else {
				player_notification = notification;
				player_notification_manager.notify(NOTIFICATION_ID, player_notification);
			}
		} catch (Exception ex) {
			if(ex.getMessage() != null) {
				ex.printStackTrace();
				Log.e("Notification error: ", ex.getMessage());
			} else {
				ex.printStackTrace();
				Log.e("Notification error: ", "Empty message");
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();

		/* Player */
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
			exoPlayer.addListener(new com.google.android.exoplayer2.Player.Listener() {
				@Override
				public void onIsPlayingChanged(boolean isPlaying) {
					com.google.android.exoplayer2.Player.Listener.super.onIsPlayingChanged(isPlaying);
					if(mediaSessionCompat != null) {
						mediaSessionCompat.setPlaybackState(
								new PlaybackStateCompat.Builder()
										.setState(
												isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
												exoPlayer.getCurrentPosition(),
												1
										)
										.setActions(PlaybackStateCompat.ACTION_SEEK_TO)
										.build()
						);
					}
					/* Notification */
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
						create_notification();
					}
				}

				@Override
				public void onPositionDiscontinuity(com.google.android.exoplayer2.Player.PositionInfo oldPosition, com.google.android.exoplayer2.Player.PositionInfo newPosition, int reason) {
					com.google.android.exoplayer2.Player.Listener.super.onPositionDiscontinuity(oldPosition, newPosition, reason);
					if(mediaSessionCompat != null) {
						mediaSessionCompat.setPlaybackState(
								new PlaybackStateCompat.Builder()
										.setState(
												exoPlayer.isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
												exoPlayer.getCurrentPosition(),
												1
										)
										.setActions(PlaybackStateCompat.ACTION_SEEK_TO)
										.build()
						);
					}
				}
			});
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
            if(exoPlayer == null) {
                return START_NOT_STICKY;
            }

            final String ACTION = intent.getAction();

            switch (ACTION) {
                case "SET_MEDIA": {
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

						MediaMetadata.Builder builder = new MediaMetadata.Builder();
						builder.setTitle(media_title);
						builder.setArtist(media_artist);
						builder.setArtworkData(img, MediaMetadata.PICTURE_TYPE_FILE_ICON_OTHER);

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
						exoPlayer.setVolume(0);
						exoPlayer.play();
						volume_runner = new Runnable() {
							float time = 0;
							@Override
							public void run() {
								time += 0.025;
								if(time > 1) {
									volume_handler.removeCallbacks(volume_runner);
								} else {
									exoPlayer.setVolume(time);
									volume_handler.postDelayed(volume_runner, 10);
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
				            float time = exoPlayer.getVolume();
				            @Override
				            public void run() {
					            time -= 0.025;
					            if(time < 0) {
						            volume_handler.removeCallbacks(volume_runner);
						            exoPlayer.pause();
					            } else {
						            exoPlayer.setVolume(time);
						            volume_handler.postDelayed(volume_runner, 10);
					            }
				            }
			            };
			            volume_handler.postDelayed(volume_runner, 0);
		            }
					break;
	            }
	            case "STOP": {
		            if(exoPlayer != null) {
			            stopSelf();
		            }
					break;
	            }
                default: {

                    break;
                }
            }
        } catch (Exception ex) {
			ex.printStackTrace();
            if (ex.getMessage() != null) {
                Log.d("onStartCommand ERROR: ", ex.getMessage());
            }
        }
        return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		if (exoPlayer != null) {
			exoPlayer.stop();
			exoPlayer.release();
			exoPlayer = null;
		}
		if(player_notification_manager != null) {
			player_notification_manager.cancelAll();
			player_notification_manager = null;
			player_notification = null;
		}
		if(mediaSessionCompat != null) {
			mediaSessionCompat.release();
			mediaSessionCompat = null;
		}
		super.onDestroy();
	}
}