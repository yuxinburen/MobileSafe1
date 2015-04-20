package com.davie.mobilesafe;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.renderscript.Program;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

/**
 * User: davie
 * Date: 15-4-20
 */
public class HomeActivity extends Activity {
    protected static final String TAG = "HomeActivity";
    private GridView list_home;
    private MyAdapter adapter;
    private SharedPreferences sp;
    private static String[] names = {
            "手机防盗","通讯卫士","软件管理",
            "进程管理","流量统计","手机杀毒",
            "缓存清理","高级工具","设置中心"
    };

    private static int[] ids = {
            R.drawable.safe, R.drawable.callmsgsafe, R.drawable.app,
            R.drawable.taskmanager, R.drawable.netmanager, R.drawable.trojan,
            R.drawable.sysoptimize, R.drawable.atools, R.drawable.settings
    };
    private EditText et_setup_pwd;
    private EditText et_setup_confirm;
    private Button cancel;
    private Button ok;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sp = getSharedPreferences("config",MODE_PRIVATE);
        list_home = (GridView) findViewById(R.id.list_home);
        adapter = new MyAdapter();
        list_home.setAdapter(adapter);
        list_home.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent;
                switch (position) {
                    case 0://进入手机防盗
                        showLostFindDialog();
                        break;
                    case 1://加入黑名单拦截页面

                        break;
                    case 2://进入软件管理界面

                        break;
                    case 3://进入进程管理

                        break;
                    case 7://进入高级工具

                        break;
                    case 8://进入设置中心

                        break;
                    default:
                        break;
                }
            }
        });
    }


    private void showLostFindDialog() {
        //判断是否设置过密码
        if(isSetupPwd()){
            //已经设置过密码 弹出的是输入对话框
            showEnterDialog();
        }else{
            //没有设置密码,弹出设置密码框
            showSetupPwdDialog();
        }
    }

    private AlertDialog alertDialog;
    /**
     * 设置密码对话框
     */
    private void showSetupPwdDialog() {
        AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        //自定义一个布局文件
        View view = View.inflate(this, R.layout.dialog_setup_password,null);
        et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
        et_setup_confirm = (EditText) view.findViewById(R.id.et_setup_confirm);
        ok = (Button) view.findViewById(R.id.ok);
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //把这个对话框取消
                alertDialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //取出密码
                String password = et_setup_pwd.getText().toString().trim();
                String password_confirm = et_setup_confirm.getText().toString().trim();

                if(TextUtils.isEmpty(password)||TextUtils.isEmpty(password_confirm)){
                    Toast.makeText(HomeActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                    return ;
                }
                //判断密码和确认密码是否一致
                if(password.equals(password_confirm)){
                    //一致的话,保存数据,对话框取消掉，进入防盗页面
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putString("password",password);
                    ed.commit();

                    alertDialog.dismiss();

                    //进入手机防盗页面

                }else{
                    Toast.makeText(HomeActivity.this,"密码不一致",Toast.LENGTH_SHORT).show();
                    et_setup_pwd.setText("");
                    return ;
                }
            }
        });
        alertDialog = builder.create();
        alertDialog.setView(view,0,0,0,0);
        alertDialog.show();
    }

    /**
     * 输入密码对话框
     */
    private void showEnterDialog() {
        AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        //自定义一个布局文件
        View view = View.inflate(this, R.layout.dialog_enter_password,null);
        et_setup_pwd = (EditText) view.findViewById(R.id.et_setup_pwd);
        ok = (Button) view.findViewById(R.id.ok);
        cancel = (Button) view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //把这个对话框取消
                alertDialog.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //取出密码
                String password = et_setup_pwd.getText().toString().trim();
                String spPassword = sp.getString("password", "");
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(HomeActivity.this,"密码为空",Toast.LENGTH_SHORT).show();
                    return ;
                }

                //判断密码和确认密码是否一致
                if(password.equals(spPassword)){
                    //一致的话,对话框取消掉，进入防盗页面
                    alertDialog.dismiss();

                    //进入手机防盗页面
                }else {
                    Toast.makeText(HomeActivity.this,"密码不正确",Toast.LENGTH_SHORT).show();
                    return ;
                }
            }
        });
        alertDialog = builder.create();
        alertDialog.setView(view,0,0,0,0);
        alertDialog.show();
    }

    /**
     * 判断是否设置过密码
     * @return
     */
    private boolean isSetupPwd(){
        String password = sp.getString("password",null);
//        if(TextUtils.isEmpty(password)){
//            return false;
//        }else {
//            return true;
//        }
        return !TextUtils.isEmpty(password);
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return names.length;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(HomeActivity.this,R.layout.list_item_home,null);
            ImageView iv_item = (ImageView) view.findViewById(R.id.iv_item);
            TextView tv_info = (TextView) view.findViewById(R.id.tv_item);
            tv_info.setText(names[position]);
            iv_item.setImageResource(ids[position]);
            return view;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
}