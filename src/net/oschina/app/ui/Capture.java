package net.oschina.app.ui;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

import net.oschina.app.AppContext;
import net.oschina.app.AppException;
import net.oschina.app.AppManager;
import net.oschina.app.R;
import net.oschina.app.bean.Barcode;
import net.oschina.app.common.UIHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import com.barcode.camera.CameraManager;
import com.barcode.core.CaptureActivityHandler;
import com.barcode.core.FinishListener;
import com.barcode.core.QRCodeReader;
import com.barcode.core.RGBLuminanceSource;
import com.barcode.core.ViewfinderView;
import com.barcode.executor.ResultHandler;
import com.barcode.core.InactivityTimer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.EncodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.HybridBinarizer;

/**
 * 扫描二维码
 * @author 火蚁（http://my.oschina/LittleDY）
 *
 */
@SuppressWarnings("deprecation")
public final class Capture extends Activity implements SurfaceHolder.Callback {

	private static final String TAG = Capture.class.getSimpleName();
	private AppContext ac;
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private ImageView back;
	private Result lastResult;
	private boolean hasSurface;
	private IntentSource source;
	private Collection<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	//private Button from_gallery;
	private final int from_photo = 010;
	static final int PARSE_BARCODE_SUC = 3035;
	static final int PARSE_BARCODE_FAIL = 3036;
	String photoPath;
	ProgressDialog mProgress;

	enum IntentSource {
		ZXING_LINK, NONE
	}

	Handler barHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PARSE_BARCODE_SUC:
				// 解析二维码
				parseBarCode((String) msg.obj);
				break;
			case PARSE_BARCODE_FAIL:
				//showDialog((String) msg.obj);
				if (mProgress != null && mProgress.isShowing()) {
					mProgress.dismiss();
				}
				new AlertDialog.Builder(Capture.this).setTitle("提示").setMessage("扫描失败！").setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.capture);
		ac = (AppContext)getApplication();
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		cameraManager = new CameraManager(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);
		back = (ImageView) findViewById(R.id.capture_back);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AppManager.getAppManager().finishActivity(Capture.this);
			}
		});
	}
	
	/**
	 * 解析本地的二维
	 * @param path
	 * @return
	 */
	public String parsLocalPic(String path) {
		String parseOk = null;
		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF8");

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小
		// 缩放比
		int be = (int) (options.outHeight / (float) 200);
		if (be <= 0)
			be = 1;
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(path, options);
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		System.out.println(w + "   " + h);
		RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader2 = new QRCodeReader();
		Result result;
		try {
			result = reader2.decode(bitmap1, hints);
			android.util.Log.i("steven", "result:" + result);
			parseOk = result.getText();

		} catch (NotFoundException e) {
			parseOk = null;
		} catch (ChecksumException e) {
			parseOk = null;
		} catch (FormatException e) {
			parseOk = null;
		}
		return parseOk;
	}
	
	/**
	 * 扫描手机中指定图片时的回调处理
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		android.util.Log.i("steven", "data.getData()" + data);
		if (data != null) {
			mProgress = new ProgressDialog(Capture.this);
			mProgress.setMessage("正在扫描...");
			mProgress.setCancelable(false);
			mProgress.show();
			if (requestCode == from_photo) {
				if (resultCode == RESULT_OK) {
					Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
					if (cursor.moveToFirst()) {
						photoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
					}
					cursor.close();
					new Thread(new Runnable() {
						@Override
						public void run() {
							Looper.prepare();
							String result = parsLocalPic(photoPath);
							if (result != null) {
								Message m = Message.obtain();
								m.what = PARSE_BARCODE_SUC;
								m.obj = result;
								barHandler.sendMessage(m);
							} else {
								Message m = Message.obtain();
								m.what = PARSE_BARCODE_FAIL;
								m.obj = "扫描失败";
								barHandler.sendMessage(m);
							}
							Looper.loop();
						}
					}).start();
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		handler = null;
		lastResult = null;
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		inactivityTimer.onResume();
		source = IntentSource.NONE;
		decodeFormats = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		if (mProgress!= null && mProgress.isShowing()) {
			mProgress.dismiss();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		if (mProgress!= null) {
			mProgress.dismiss();
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
				AppManager.getAppManager().finishActivity(Capture.this);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			cameraManager.setTorch(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			cameraManager.setTorch(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// 创建预览
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	private static ParsedResult parseResult(Result rawResult) {
		return ResultParser.parseResult(rawResult);
	}

	// 解析二维码
	public void handleDecode(Result rawResult, Bitmap barcode) {
		inactivityTimer.onActivity();
		lastResult = rawResult;
		ResultHandler resultHandler = new ResultHandler(parseResult(rawResult));

		if (barcode == null) {
			android.util.Log.i("steven", "rawResult.getBarcodeFormat().toString():" + rawResult.getBarcodeFormat().toString());
			android.util.Log.i("steven", "resultHandler.getType().toString():" + resultHandler.getType().toString());
			android.util.Log.i("steven", "resultHandler.getDisplayContents():" + resultHandler.getDisplayContents());
		} else {
			// 对扫到的二维码进行解析
			parseBarCode(resultHandler.getDisplayContents().toString());
		}
	}
	
	// 解析二维码
	private void parseBarCode(String msg) {
		// 手机震动
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(100);
		mProgress = ProgressDialog.show(Capture.this, null, "已扫描，正在处理···",true,true);
		mProgress.setOnDismissListener(new DialogInterface.OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				restartPreviewAfterDelay(1l);
			}
		});
		
		// 判断是否符合基本的json格式
		if (!msg.matches("^\\{.*")) {
			showDialog(msg);
		} else {
			try {
				Barcode barcode = Barcode.parse(msg);
				Log.i(TAG, barcode.toString());
				int type = barcode.getType();
				if (barcode.isRequireLogin()) {
					if (!ac.isLogin()) {
						UIHelper.showLoginDialog(Capture.this);
						return;
					}
				}
				switch (type) {
				case Barcode.SIGN_IN:// 签到
					signin(barcode);
					break;
				default:
					break;
				}
			} catch (AppException e) {
				UIHelper.ToastMessage(this, "json数据解析异常");
				mProgress.dismiss();
			}
		}
	}
	
	/**
	 * 启动签到界面
	 * @param barcode
	 */
	private void signin(Barcode barcode) {
		Intent intent = new Intent(Capture.this, Signin.class);
		Bundle bundle = new Bundle();
		bundle.putSerializable("barcode", barcode);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	/**
	 * 扫描结果对话框
	 * @param msg
	 */
	private void showDialog(final String msg) {
		new AlertDialog.Builder(Capture.this).setTitle("扫描结果").
		setMessage("非oschina提供活动签到二维码\n内容：" + msg).
		setPositiveButton("复制", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mProgress.dismiss();
				dialog.dismiss();
				//获取剪贴板管理服务
				ClipboardManager cm =(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				//将文本数据复制到剪贴板
				cm.setText(msg);
				UIHelper.ToastMessage(Capture.this, "复制成功");
			}
		}).setNegativeButton("返回", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mProgress.dismiss();
				dialog.dismiss();
				if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
					restartPreviewAfterDelay(0L);
				}
			}
		}).show();
	}

	// 初始化照相机，CaptureActivityHandler解码
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats, characterSet, cameraManager);
			}
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}
	
	/**
	 * 初始化照相机失败显示窗口
	 */
	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton("确定", new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}
	
	/**
	 * 重启二维码扫描界面
	 * @param delayMS
	 */
	public void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
}
