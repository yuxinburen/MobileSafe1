package com.davie.mobilesafe.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Xml;

/**
 * ���ŵı��ݺͻ�ԭ ������
 */
public class SmsUtils {
	public interface BackUpCallBack{
		/**
		 * ���ŵ���ǰ���õķ���
		 */
		public void beforeSmsBackup(int total);
		/**
		 * ���ű����е��õķ��� 
		 * @param progress ��ǰ���ݵĽ��ȡ�
		 */
		public void onSmsBackup(int progress);
	}
	/**
	 * ���ŵı���
	 * @param context ������
	 * @param od �������Ի���
	 * @throws Exception 
	 */
	public static void backupSms(Context context,BackUpCallBack backupCallback) throws Exception{
		ContentResolver resolver = context.getContentResolver();
		File file = new File(Environment.getExternalStorageDirectory(),"backuo.xml");
		FileOutputStream fos = new FileOutputStream(file);
		//���û��Ķ���һ��һ��������,����һ���ĸ�ʽд���ļ���
		XmlSerializer serializer = Xml.newSerializer();//��ȡxml�ļ���������(ϵ����)
		//��ʼ��������
		serializer.setOutput(fos,"utf-8");
		serializer.startDocument("utf-8",true);//true �Ƿ����
		serializer.startTag(null,"smss");
		Uri uri = Uri.parse("content://sms/");
		Cursor cursor = resolver.query(uri,new String[]{"body","address","type","date"}, null, null, null);
		//��ʼ���ݵ�ʱ��,���ý����������ֵ
		backupCallback.beforeSmsBackup(cursor.getCount());
		int progress = 0;
		while (cursor.moveToNext()) {
			Thread.sleep(500);
			String body = cursor.getString(0);
			String address = cursor.getString(1);
			String type = cursor.getString(2);
			String date = cursor.getString(3);
			
			serializer.startTag(null,"sms");
			
			serializer.startTag(null,"body");
			serializer.text(body);
			serializer.endTag(null,"body");
			
			serializer.startTag(null,"address");
			serializer.text(address);
			serializer.endTag(null,"address");
			
			serializer.startTag(null,"type");
			serializer.text(type);
			serializer.endTag(null,"type");
			
			serializer.startTag(null,"date");
			serializer.text(date);
			serializer.endTag(null,"date");
			
			serializer.endTag(null,"sms");
			//���ݹ�����,���ӽ���
			progress++;
			backupCallback.onSmsBackup(progress);
		}
		serializer.endTag(null,"smss");
		serializer.endDocument();
		fos.close();
	}
	/**
	 * ���ŵĻ�ԭ
	 * @throws Exception 
	 */
	public static void restoreSms(Context context,boolean flag) throws Exception{
		Uri uri = Uri.parse("content://sms/");
		if(flag){ //true���������Ķ���
			context.getContentResolver().delete(uri,null,null);
		}
		XmlPullParser pull = Xml.newPullParser();
		File file = new File(Environment.getExternalStorageDirectory(),"backuo.xml");
		FileInputStream fis = new FileInputStream(file);
		pull.setInput(fis,"utf-8");
		int eventType = pull.getEventType();
		String body = null;
		String date = null;
		String type = null;
		String address = null;
		ContentValues values = null;
		while(eventType != XmlPullParser.END_DOCUMENT){
			String tagName = pull.getName();
			switch (eventType) {
				case XmlPullParser.START_TAG: //����ǿ�ʼ��ǩ
					if("body".equals(tagName)){
						body = pull.nextText();
					}else if("date".equals(tagName)){
						date = pull.nextText();
					}else if("type".equals(tagName)){
						type = pull.nextText();
					}else if("address".equals(tagName)){
						address = pull.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					if("sms".equals(tagName)){
						values = new ContentValues();
						values.put("body",body);
						values.put("date",date);
						values.put("type",type);
						values.put("address",address);
						context.getContentResolver().insert(uri, values);
					}
					break;
			}
			eventType = pull.next();
		}
		fis.close();
	}
}





















