package com.znbox.beatlevels.modules;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MediaScanner extends ReactContextBaseJavaModule {

	protected static final int FIELD_TYPE_BLOB = 4;
	protected static final int FIELD_TYPE_FLOAT = 2;
	protected static final int FIELD_TYPE_INTEGER = 1;
	protected static final int FIELD_TYPE_NULL = 0;
	protected static final int FIELD_TYPE_STRING = 3;

	public MediaScanner(ReactApplicationContext context) {
		super(context);
	}

	@ReactMethod
	public void find_media(Promise promise) {
		try {
			Cursor cursor = this.getReactApplicationContext().getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					null, null, null, null
			);
			WritableArray array = new WritableNativeArray();
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				WritableMap writableMap = new WritableNativeMap();
				for(String column : cursor.getColumnNames()) {
					if(cursor.getType(cursor.getColumnIndexOrThrow(column)) == Cursor.FIELD_TYPE_NULL) {
						writableMap.putNull(column);
						continue;
					}
					if(cursor.getType(cursor.getColumnIndexOrThrow(column)) == Cursor.FIELD_TYPE_STRING) {
						writableMap.putString(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
						continue;
					}
					if(cursor.getType(cursor.getColumnIndexOrThrow(column)) == Cursor.FIELD_TYPE_INTEGER) {
						writableMap.putInt(column, cursor.getInt(cursor.getColumnIndexOrThrow(column)));
						continue;
					}
					if(cursor.getType(cursor.getColumnIndexOrThrow(column)) == Cursor.FIELD_TYPE_FLOAT) {
						writableMap.putDouble(column, cursor.getDouble(cursor.getColumnIndexOrThrow(column)));
						continue;
					}
					if(cursor.getType(cursor.getColumnIndexOrThrow(column)) == Cursor.FIELD_TYPE_BLOB) {
						writableMap.putString(column, Base64.encodeToString(cursor.getBlob(cursor.getColumnIndexOrThrow(column)), Base64.NO_WRAP));
					}
				}
				array.pushMap(writableMap);
				cursor.moveToNext();
			}
			cursor.close();
			WritableMap writableMap = Arguments.createMap();
			writableMap.putArray("media_files", array);
			promise.resolve(writableMap);
		} catch (Exception ex) {
			promise.reject(ex);
			if(ex.getMessage() != null) {
				Toast.makeText(getReactApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	public ArrayList<File> scan_dir_files(String path, ArrayList <File> file_list) {

		File file = new File(path);

		if(file.isDirectory()) {
			File[] list = file.listFiles();
			if(list != null) {
				for(File _file : list) {
					Log.d("File Map", "File: " + _file.getAbsolutePath());
					if(_file.isFile()) {
						//Log.d("File Map", "File: " + _file.getAbsolutePath());
						file_list.add(_file);
					} else {
						//Log.d("Dir Map", "Dirr: " + _file.getAbsolutePath());
						scan_dir_files(_file.getAbsolutePath(), file_list);
					}
				}
			}
		} else {
			file_list.add(file);
		}

		return file_list;
	}

	@ReactMethod
	public void find_files(String dir, Promise promise) {
		try {
			String path = Environment.getExternalStorageDirectory().toString() + "" + dir;
			File file = new File(path);

			WritableArray array = new WritableNativeArray();
			ArrayList <File> files = scan_dir_files(file.getAbsolutePath(), new ArrayList<File>());

			for(File _file : files) {
				WritableMap writableMap = Arguments.createMap();
				writableMap.putString("file_name", _file.getName());
				writableMap.putString("file_path", _file.getPath());
				writableMap.putString("file_absolute_path", _file.getAbsolutePath());
				writableMap.putString("parent_dir", _file.getParent());
				writableMap.putDouble("total_space", _file.getTotalSpace());
				writableMap.putDouble("total_usable_space", _file.getUsableSpace());
				writableMap.putDouble("total_free_space", _file.getFreeSpace());
				writableMap.putBoolean("is_file", _file.isFile());
				writableMap.putBoolean("is_directory", _file.isDirectory());
				array.pushMap(writableMap);
			}

			WritableMap writableMap = Arguments.createMap();
			writableMap.putArray("media_files", array);
			promise.resolve(writableMap);
		} catch (Exception ex) {
			promise.reject(ex);
			if(ex.getMessage() != null) {
				Toast.makeText(getReactApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@ReactMethod
	public void refresh_media_files(String dir, Promise promise) {
		try {
			String path = Environment.getExternalStorageDirectory().toString() + "" + dir;
			MediaScannerConnection.scanFile(
				getReactApplicationContext(),
				new String[]{ path },
				new String[]{ "audio/mp3", "*/*" },
				new MediaScannerConnection.MediaScannerConnectionClient()
				{
					public void onMediaScannerConnected() {

					}
					public void onScanCompleted(String path, Uri uri) {
						promise.resolve(path);
					}
				}
			);
		} catch (Exception ex) {
			promise.reject(ex);
			if(ex.getMessage() != null) {
				Toast.makeText(getReactApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@ReactMethod
	public void get_media_img(String path, int _id, Promise promise) {

			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						File file = new File(path);
						MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
						metadataRetriever.setDataSource(file.getAbsolutePath());

						byte [] img = metadataRetriever.getEmbeddedPicture();

						if(img != null) {

							File dir = new File(getReactApplicationContext().getFilesDir(), "cache");
							if(!dir.exists()) {
								dir.mkdir();
							}
							File _file = new File(dir.getAbsolutePath() + "/" + _id + ".bitmap");
							if(!_file.exists()) {
								Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
								OutputStream os = new BufferedOutputStream(new FileOutputStream(_file));
								bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
								os.close();
							}
							promise.resolve(_file.getAbsolutePath());
						} else {
							promise.resolve(null);
						}
					} catch (Exception ex) {
						promise.reject(ex);
						if(ex.getMessage() != null) {
							Toast.makeText(getReactApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				}
			};
			t.start();
	}

	@ReactMethod
	public void list_dir(String dir, Promise promise) {
		try {
			String path = Environment.getExternalStorageDirectory().toString() + "" + dir;
			File file = new File(path);
			File files[] = file.listFiles();
			WritableArray array = new WritableNativeArray();
			for(File _file : files) {
				WritableMap writableMap = Arguments.createMap();
				writableMap.putString("file_name", _file.getName());
				writableMap.putString("file_path", _file.getPath());
				writableMap.putString("file_absolute_path", _file.getAbsolutePath());
				writableMap.putString("parent_dir", _file.getParent());
				writableMap.putDouble("total_space", _file.getTotalSpace());
				writableMap.putDouble("total_usable_space", _file.getUsableSpace());
				writableMap.putDouble("total_free_space", _file.getFreeSpace());
				writableMap.putBoolean("is_file", _file.isFile());
				writableMap.putBoolean("is_directory", _file.isDirectory());
				array.pushMap(writableMap);
			}
			WritableMap writableMap = Arguments.createMap();
			writableMap.putArray("files", array);
			promise.resolve(writableMap);

		} catch (Exception ex) {
			promise.reject(ex);
			if(ex.getMessage() != null) {
				Toast.makeText(getReactApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public String getName() {
		return "MediaScanner";
	}
}