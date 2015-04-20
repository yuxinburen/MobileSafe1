package com.davie.mobilesafe;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.davie.mobilesafe.utils.StreamTools;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

public class SplashActivity extends Activity {
	protected static final String TAG = "SplashActivity";
	protected static final int ENTER_HOME = 0;
	protected static final int SHOW_UPDATE_DIALOG = 1;
	protected static final int URL_ERROR = 2;
	protected static final int NETWORK_ERROR = 3;
	protected static final int JSON_ERROR = 4;
	private TextView tv_splash_version;
	private String version,description,apkurl;
	private TextView tv_update_info;
	private SharedPreferences sp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		sp = getSharedPreferences("config",MODE_PRIVATE);
		tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);
		tv_splash_version.setText("版本号: "+getVersionName());
		tv_update_info = (TextView) findViewById(R.id.tv_update_info);

		AlphaAnimation aa = new AlphaAnimation(0.2f,1.0f);
		aa.setDuration(500);
		findViewById(R.id.rl_root_splash).startAnimation(aa);
	}


	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SHOW_UPDATE_DIALOG: //显示升级的对话框
					Log.i(TAG,"弹出更新对话框");
					showUpdateDialog();
					break;
				case ENTER_HOME: //进入主页面
					enterHome();
					break;
				case URL_ERROR: //URL错误
					enterHome();
					Toast.makeText(getApplicationContext(),"URL",Toast.LENGTH_SHORT).show();
					break;
				case NETWORK_ERROR: //network
					Toast.makeText(getApplicationContext(),"网络错误", Toast.LENGTH_SHORT).show();
					enterHome();
					break;
				case JSON_ERROR: //JSON��������
					Toast.makeText(SplashActivity.this,"JSON错误",Toast.LENGTH_SHORT).show();
					enterHome();
					break;
			}
		}
	};

	/**
	 * 显示更新对话框
	 */
	private void showUpdateDialog() {
		Builder builder = new Builder(this);
		builder.setTitle("版本更新");
		builder.setCancelable(false);//点击其他位置无法消失,只能点击对话框����������
		//监听取消的事件
		builder.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				//进入主页面
				enterHome();
				dialog.dismiss();
			}
		});
		builder.setMessage(description);
		builder.setPositiveButton("立即更新",new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//下载APK，并且替换安装
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					//如果sdcard存在
					//afnal
					FinalHttp finalhttp = new FinalHttp();
					finalhttp.download(apkurl,
							Environment.getExternalStorageDirectory().getAbsolutePath()+"/mobilesafe2.0.apk", 
							new AjaxCallBack<File>() {
								@Override
								public void onFailure(Throwable t, int errorNo,
										String strMsg) {
									t.printStackTrace();
									Toast.makeText(getApplicationContext(),"下载失败", Toast.LENGTH_SHORT).show();
									super.onFailure(t, errorNo, strMsg);
								}
								@Override
								public void onLoading(long count, long current) {
									super.onLoading(count, current);
									//当前下载百分比
									tv_update_info.setVisibility(View.VISIBLE);
									int progress = (int) (current * 100/count);
									tv_update_info.setText("下载进度: "+progress+"%");
								}
								@Override
								public void onSuccess(File t) {
									super.onSuccess(t);
									installAPK(t);
								}
								//安装APK
								private void installAPK(File t) {
									Intent intent = new Intent();
									intent.setAction("android.intent.action.VIEW");
									intent.addCategory("android.intent.category.DEFAULT");
									intent.setDataAndType(
											Uri.fromFile(t),
											"application/vnd.android.package-archive");
									startActivity(intent);
								}
								
							});
				}else{
					Toast.makeText(getApplicationContext(),"没有sdcard,请安装上在试", Toast.LENGTH_SHORT).show();
				}
			}
		});
		builder.setNegativeButton("下次再说",new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				enterHome();
			}
		});
		builder.show();
	}
	private void enterHome() {
		Intent intent = new Intent(this,HomeActivity.class);
		startActivity(intent);
		//关闭当前Activity页面
		finish();
	};

	//检查是否有新版本,如果有就升级
	 private void checkUpdate() {
		new Thread(){
			@Override
			public void run() {
				Message msg = Message.obtain();
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(getString(R.string.serverurl));
					//联网��
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					//设置请求方法�����󷽷�
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(4000);
					int code = conn.getResponseCode();
					if(code == 200){
						//联网成功
						InputStream is = conn.getInputStream();
						//把流转换成字符串String
						String result = StreamTools.readFromStream(is);
						Log.i(TAG,"联网成功"+result);
						//JSON����
						JSONObject obj = new JSONObject(result);
						version = (String) obj.get("version");
						description = (String) obj.get("description");
						apkurl = (String) obj.get("path");
						//校验是否有新版本
						if(getVersionName().equals(version)){
							//版本一致.没有新版本,进入主页面
							msg.what = ENTER_HOME;
						}else{
							//有新版本,弹出一升级对话框
							msg.what = SHOW_UPDATE_DIALOG;
						}
					}
				} catch (MalformedURLException e) {
					msg.what = URL_ERROR;
					e.printStackTrace();
				}catch (IOException e) {
					msg.what = NETWORK_ERROR;
					e.printStackTrace();
				} catch (JSONException e) {
					msg.what = JSON_ERROR;
					e.printStackTrace();
				}finally{
					long endTime = System.currentTimeMillis();//结束的时间�
					long dTime = endTime - startTime;//算出花费的时间
					if(dTime < 2000){
						try {
							Thread.sleep(2000-dTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					handler.sendMessage(msg);
				}
			}
		}.start();
	}

	// 得到应用程序的版本名称��
	private String getVersionName(){
		//用来管理手机的APK
		PackageManager pm = getPackageManager();
		//得到指定APK的功能清单文件
		try {
			PackageInfo info = pm.getPackageInfo(getPackageName(),0);
			return info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

}





