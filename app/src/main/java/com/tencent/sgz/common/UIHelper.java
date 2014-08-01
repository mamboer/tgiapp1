package com.tencent.sgz.common;

import greendroid.widget.MyQuickAction;
import greendroid.widget.QuickAction;
import in.xsin.common.MTAHelper;
import in.xsin.weibo.Helper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.sgz.AppConfig;
import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppException;
import com.tencent.sgz.AppManager;
import com.tencent.sgz.R;
import com.tencent.sgz.activity.BaseActivity;
import com.tencent.sgz.activity.MainActivity;
import com.tencent.sgz.activity.XGNoticeActivity;
import com.tencent.sgz.activity.XGNoticeDetailActivity;
import com.tencent.sgz.adapter.GridViewFaceAdapter;
import com.tencent.sgz.api.ApiClient;
import com.tencent.sgz.bean.AccessInfo;
import com.tencent.sgz.bean.Active;
import com.tencent.sgz.bean.Comment;
import com.tencent.sgz.bean.CommentList;
import com.tencent.sgz.bean.Messages;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.bean.Notice;
import com.tencent.sgz.bean.Post;
import com.tencent.sgz.bean.Report;
import com.tencent.sgz.bean.Result;
import com.tencent.sgz.bean.Tweet;
import com.tencent.sgz.bean.URLs;
import com.tencent.sgz.entity.XGNotification;
import com.tencent.sgz.ui.*;
import com.tencent.sgz.widget.LinkView;
import com.tencent.sgz.widget.PathChooseDialog;
import com.tencent.sgz.widget.LinkView.MyURLSpan;
import com.tencent.sgz.widget.PathChooseDialog.ChooseCompleteListener;
import com.tencent.sgz.widget.ScreenShotView;
import com.tencent.sgz.widget.ScreenShotView.OnScreenShotListener;
import com.tencent.stat.StatService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * 
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
public class UIHelper {
    private final static String TAG = "UIHelper";

    public final static int LISTVIEW_ACTION_INIT = 0x01;
    public final static int LISTVIEW_ACTION_REFRESH = 0x02;
    public final static int LISTVIEW_ACTION_SCROLL = 0x03;
    public final static int LISTVIEW_ACTION_CHANGE_CATALOG = 0x04;

    public final static int LISTVIEW_DATA_MORE = 0x01;
    public final static int LISTVIEW_DATA_LOADING = 0x02;
    public final static int LISTVIEW_DATA_FULL = 0x03;
    public final static int LISTVIEW_DATA_EMPTY = 0x04;

    public final static int LISTVIEW_DATATYPE_NEWS = 0x01;
    public final static int LISTVIEW_DATATYPE_BLOG = 0x02;
    public final static int LISTVIEW_DATATYPE_POST = 0x03;
    public final static int LISTVIEW_DATATYPE_TWEET = 0x04;
    public final static int LISTVIEW_DATATYPE_ACTIVE = 0x05;
    public final static int LISTVIEW_DATATYPE_MESSAGE = 0x06;
    public final static int LISTVIEW_DATATYPE_COMMENT = 0x07;

    public final static int REQUEST_CODE_FOR_RESULT = 0x01;
    public final static int REQUEST_CODE_FOR_REPLY = 0x02;

    /** 表情图片匹配 */
    private static Pattern facePattern = Pattern
            .compile("\\[{1}([0-9]\\d*)\\]{1}");

    /** 全局web样式 */
    // 链接样式文件，代码块高亮的处理
    public final static String linkCss = "<script type=\"text/javascript\" src=\"file:///android_asset/shCore.js\"></script>"
            + "<script type=\"text/javascript\" src=\"file:///android_asset/brush.js\"></script>"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shThemeDefault.css\">"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/shCore.css\">"
            + "<script type=\"text/javascript\">SyntaxHighlighter.all();</script>";
    public final static String WEB_STYLE = linkCss + "<style>* {font-size:14px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
            + "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
            + "pre {font-size:9pt;line-height:12pt;font-family:Courier New,Arial;border:1px solid #ddd;border-left:5px solid #6CE26C;background:#f6f6f6;padding:5px;overflow: auto;} "
            + "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";
    /**
     * 显示首页
     *
     * @param activity
     */
    public static void showHome(Activity activity) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    /**
     * 显示登录页面
     *
     * @param context
     */
    public static void showLoginDialog(Context context) {
        Intent intent = new Intent(context, LoginDialog.class);
        if (context instanceof MainActivity)
            intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_MAIN);
        else if (context instanceof Setting)
            intent.putExtra("LOGINTYPE", LoginDialog.LOGIN_SETTING);
        else
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 显示登录页面
     *
     * @param context
     */
    public static void showLoginPage(Context context) {

    }

    /**
     * 显示新闻详情
     *
     * @param context
     * @param newsId
     */
    public static void showNewsDetail(Context context, int newsId) {
        Intent intent = new Intent(context, NewsDetail.class);
        intent.putExtra("news_id", newsId);
        context.startActivity(intent);
    }

    /**
     * 显示新闻详情
     *
     * @param context
     * @param news
     */
    public static void showNewsDetailByInstance(Context context, News news) {
        showNewsDetailByInstance(context,news,"",false);
    }

    /**
     * 显示新闻详情
     *
     * @param context
     * @param news
     */
    public static void showNewsDetailByInstance(Context context, News news,String title,boolean hideFootbar) {
        Intent intent = new Intent(context, NewsDetail.class);

        Bundle bundle = new Bundle();
        bundle.putInt("news_id",news.getId());
        bundle.putString("title",title);
        bundle.putBoolean("hideFootbar",hideFootbar);
        bundle.putSerializable("news",news);

        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    /**
     * 新闻超链接点击跳转
     *
     * @param context
     * @param news
     */
    public static void showNewsRedirect(Context context, News news) {
        String url = news.getUrl();
        /*
        // url为空-旧方法
        if (StringUtils.isEmpty(url)) {
            int newsId = news.getId();
            int newsType = news.getNewType().type;
            String objId = news.getNewType().attachment;
            switch (newsType) {
            case News.NEWSTYPE_NEWS:
                showNewsDetail(context, newsId);
                break;
            case News.NEWSTYPE_SOFTWARE:
                showSoftwareDetail(context, objId);
                break;
            case News.NEWSTYPE_POST:
                showQuestionDetail(context, StringUtils.toInt(objId));
                break;
            case News.NEWSTYPE_BLOG:
                showBlogDetail(context, StringUtils.toInt(objId));
                break;
            }
        } else {
            showUrlRedirect(context, url);
        }
        */
        if(StringUtils.isEmpty(url)){
            ToastMessage(context,"无法打开新闻，链接为空！！");
            return;
        }
        showNewsDetailByInstance(context,news);
    }

    /**
     * 调用系统安装了的应用分享
     *
     * @param context
     * @param title
     * @param url
     */
    public static void showShareMore(Activity context, final String title,
            final String url) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享：" + title);
        intent.putExtra(Intent.EXTRA_TEXT, title + " " + url);
        context.startActivity(Intent.createChooser(intent, "选择分享"));
    }

    private static View shareDialogView;
    private static PopupWindow pwShareMenu;
    private static int shareDialogViewHeight;


    /**
     * 分享到'新浪微博'或'腾讯微博'的对话框
     *
     * @param context
     *            当前Activity
     * @param title
     *            分享的标题
     * @param url
     *            分享的链接
     */
    public static void showShareDialog1(final Activity context,View anchor,
                                       final String title, final String url,final String imgUrl) {

        if (shareDialogView==null){

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //更多菜单 http://blog.csdn.net/mad1989/article/details/9024977
            //获取自定义布局文件pop.xml的视图
            shareDialogView = inflater.inflate(R.layout.news_detail_share,
                    null, false);
            // 创建PopupWindow实例
            pwShareMenu = new PopupWindow(shareDialogView, ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            // 设置动画效果 [R.style.AnimationFade 是自己事先定义好的]
            pwShareMenu.setAnimationStyle(R.style.Animation_FadeInOut);
            pwShareMenu.setFocusable(true);
            pwShareMenu.setTouchable(true);

            //触摸popupwindow外部，可以消失。必须设置背景
            pwShareMenu.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.transparent));
            pwShareMenu.setOutsideTouchable(true);
            pwShareMenu.update();


            // 自定义view添加点击事件
            AppConfig cfgHelper = AppConfig.getAppConfig(context);
            final AccessInfo access = cfgHelper.getAccessInfo();
            ArrayList<View> cateBtnViews = UIHelper.getViewsByTag((ViewGroup)shareDialogView,"catebtn");

            for (View cateBtnView : cateBtnViews) {
                cateBtnView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        //hide the popupwindow
                        pwShareMenu.dismiss();

                        switch (view.getId()){
                            case R.id.snsBtnSinaWeibo:
                                MTAHelper.trackClick(context, TAG, "snsBtnSinaWeibo");
                                Helper.shareWebPage(title,url,imgUrl,null);
                                break;
                            case R.id.snsBtnQQWeibo:
                                MTAHelper.trackClick(context, TAG, "snsBtnQQWeibo");
                                OpenQQHelper.shareToWeibo(context,title,url,imgUrl,"add_t",null);
                                break;
                            case R.id.snsBtnWXPYQ:
                                MTAHelper.trackClick(context, TAG, "snsBtnWXPYQ");
                                // 微信朋友圈
                                WeixinHelper.shareToWXTimeline(context, title, url,imgUrl);
                                break;
                            case R.id.snsBtnWXHY:
                                MTAHelper.trackClick(context, TAG, "snsBtnWXHY");
                                //微信好友
                                WeixinHelper.shareToWXFriends(context, title, url,imgUrl);
                                break;
                            case R.id.snsBtnQQHY:
                                MTAHelper.trackClick(context, TAG, "snsBtnQQHY");
                                //QQ好友
                                Bundle params = new Bundle();
                                params.putString(QQShare.SHARE_TO_QQ_TITLE, title);
                                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
                                params.putString(QQShare.SHARE_TO_QQ_SUMMARY, "分享地址："+url);
                                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                                if(null!=imgUrl && !imgUrl.equals("")){
                                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imgUrl);
                                }else{
                                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, OpenQQHelper.getDefaultPic());
                                }
                                OpenQQHelper.shareToQQ(context,params,null);
                                break;
                            case R.id.snsBtnQZone:
                                MTAHelper.trackClick(context, TAG, "snsBtnQZone");
                                //QQ空间

                                Bundle params1 = new Bundle();
                                params1.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                                params1.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
                                params1.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, "分享地址："+url);
                                params1.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);
                                // 支持传多个imageUrl
                                ArrayList<String> imageUrls = new ArrayList<String>();
                                if(null!=imgUrl && !imgUrl.equals("")){
                                    imageUrls.add(imgUrl);
                                }else{
                                    imageUrls.add(OpenQQHelper.getDefaultPic());
                                }
                                //String imageUrl = "XXX";
                                //params.putString(Tencent.SHARE_TO_QQ_IMAGE_URL, imageUrl);
                                params1.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrls);

                                OpenQQHelper.shareToQZone(context,params1,null);

                                break;
                            case R.id.snsBtnMore:
                                MTAHelper.trackClick(context, TAG, "snsBtnMore");
                                //更多
                                showShareMore(context, title, url);
                                break;
                            case R.id.snsBtnCapture:
                                MTAHelper.trackClick(context, TAG, "snsBtnCapture");
                                //截屏分享
                                addScreenShot(context, new OnScreenShotListener() {

                                    @SuppressLint("NewApi")
                                    public void onComplete(Bitmap bm) {
                                        Intent intent = new Intent(context,ScreenShotShare.class);
                                        intent.putExtra("title", title);
                                        intent.putExtra("url", url);
                                        intent.putExtra("cut_image_tmp_path",ScreenShotView.getTempShareFileName());
                                        try {
                                            ImageUtils.saveImageToSD(context,ScreenShotView.getTempShareFileName(),bm, 100);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        context.startActivity(intent);
                                    }
                                });
                                break;

                            default:
                                ToastMessage(context,"功能正在实现中");
                                break;
                        }
                    }
                });
            }

            // get dialog height
            shareDialogView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            shareDialogViewHeight = shareDialogView.getMeasuredHeight();

        }
        int[] location      = new int[2];

        anchor.getLocationOnScreen(location);



        pwShareMenu.showAtLocation(anchor, Gravity.NO_GRAVITY,location[0],location[1]-shareDialogViewHeight);

    }

    /**
     * 信鸽信息提示
     *
     * @param context
     *            当前Activity
     */
    public static void showXGDetailDialog(final Activity context,
                                        final XGNotification data) {

        Intent intent = new Intent(context, XGNoticeDetailActivity.class);
        intent.putExtra("data",data);
        context.startActivity(intent);

    }

    /**
     * 分享到'新浪微博'或'腾讯微博'的对话框
     *
     * @param context
     *            当前Activity
     * @param title
     *            分享的标题
     * @param url
     *            分享的链接
     */
    public static void showShareDialog(final Activity context,
            final String title, final String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.btn_star);
        builder.setTitle(context.getString(R.string.share));
        builder.setItems(R.array.app_share_items,
                new DialogInterface.OnClickListener() {
                    AppConfig cfgHelper = AppConfig.getAppConfig(context);
                    AccessInfo access = cfgHelper.getAccessInfo();

                    public void onClick(DialogInterface arg0, int arg1) {
                        switch (arg1) {
                        case 0:// 新浪微博
                            // 分享的内容
                            final String shareMessage = title + " " + url;
                            // 初始化微博
                            if (SinaWeiboHelper.isWeiboNull()) {
                                SinaWeiboHelper.initWeibo();
                            }
                            // 判断之前是否登陆过
                            if (access != null) {
                                SinaWeiboHelper.progressDialog = new ProgressDialog(
                                        context);
                                SinaWeiboHelper.progressDialog
                                        .setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                SinaWeiboHelper.progressDialog
                                        .setMessage(context
                                                .getString(R.string.sharing));
                                SinaWeiboHelper.progressDialog
                                        .setCancelable(true);
                                SinaWeiboHelper.progressDialog.show();
                                new Thread() {
                                    public void run() {
                                        SinaWeiboHelper.setAccessToken(
                                                access.getAccessToken(),
                                                access.getAccessSecret(),
                                                access.getExpiresIn());
                                        SinaWeiboHelper.shareMessage(context,
                                                shareMessage);
                                    }
                                }.start();
                            } else {
                                SinaWeiboHelper
                                        .authorize(context, shareMessage);
                            }
                            break;
                        case 1:// 腾讯微博
                            QQWeiboHelper.shareToQQ(context, title, url);
                            break;
                        case 2:// 微信朋友圈
                            WeixinHelper.shareToWXFriends(context, title, url,null);
                            break;
                        case 3:// 截图分享
                            addScreenShot(context, new OnScreenShotListener() {

                                @SuppressLint("NewApi")
                                public void onComplete(Bitmap bm) {
                                    Intent intent = new Intent(context,ScreenShotShare.class);
                                    intent.putExtra("title", title);
                                    intent.putExtra("url", url);
                                    intent.putExtra("cut_image_tmp_path",ScreenShotView.getTempShareFileName());
                                    try {
                                        ImageUtils.saveImageToSD(context,ScreenShotView.getTempShareFileName(),bm, 100);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    context.startActivity(intent);
                                }
                            });
                            break;
                        case 4:// 更多
                            showShareMore(context, title, url);
                            break;
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * 收藏操作选择框
     *
     * @param context
     * @param thread
     */
    public static void showFavoriteOptionDialog(final Activity context,
            final Thread thread) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(R.drawable.ic_dialog_menu);
        builder.setTitle(context.getString(R.string.select));
        builder.setItems(R.array.favorite_options,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        switch (arg1) {
                        case 0:// 删除
                            thread.start();
                            break;
                        }
                    }
                });
        builder.create().show();
    }

    /**
     * 显示图片对话框
     *
     * @param context
     * @param imgUrl
     */
    public static void showImageDialog(Context context, String imgUrl) {
        Intent intent = new Intent(context, ImageDialog.class);
        intent.putExtra("img_url", imgUrl);
        context.startActivity(intent);
    }

    public static void showImageZoomDialog(Context context, String imgUrl) {
        Intent intent = new Intent(context, ImageZoomDialog.class);
        intent.putExtra("img_url", imgUrl);
        context.startActivity(intent);
    }

    /**
     * 显示系统设置界面
     *
     * @param context
     */
    public static void showSetting(Context context) {
        Intent intent = new Intent(context, Setting.class);
        context.startActivity(intent);
    }

    /**
     * 显示搜索界面
     *
     * @param context
     */
    public static void showSearch(Context context) {
        Intent intent = new Intent(context, Search.class);
        context.startActivity(intent);
    }

    /**
     * 显示扫一扫界面
     * @param context
     */
    public static void showCapture(Context context) {
        Intent intent = new Intent(context, Capture.class);
        context.startActivity(intent);
    }

    /**
     * 显示路径选择对话框
     *
     * @param context
     */
    public static void showFilePathDialog(Activity context,
            ChooseCompleteListener listener) {
        new PathChooseDialog(context, listener).show();
    }

    /**
     * 加载显示用户头像
     *
     * @param imgFace
     * @param faceURL
     */
    public static void showUserFace(final ImageView imgFace,
            final String faceURL) {
        showLoadImage(imgFace, faceURL,
                imgFace.getContext().getString(R.string.msg_load_userface_fail));
    }

    /**
     * 加载显示图片
     *
     * @param imgView
     * @param imgURL
     * @param errMsg
     */
    public static void showLoadImage(final ImageView imgView,
            final String imgURL, final String errMsg) {
        // 读取本地图片
        if (StringUtils.isEmpty(imgURL) || imgURL.endsWith("portrait.gif")) {
            Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(),
                    R.drawable.widget_dface);
            imgView.setImageBitmap(bmp);
            return;
        }

        // 是否有缓存图片
        final String filename = FileUtils.getFileName(imgURL);
        // Environment.getExternalStorageDirectory();返回/sdcard
        String filepath = imgView.getContext().getFilesDir() + File.separator
                + filename;
        File file = new File(filepath);
        if (file.exists()) {
            Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);
            imgView.setImageBitmap(bmp);
            return;
        }

        // 从网络获取&写入图片缓存
        String _errMsg = imgView.getContext().getString(
                R.string.msg_load_image_fail);
        if (!StringUtils.isEmpty(errMsg))
            _errMsg = errMsg;
        final String ErrMsg = _errMsg;
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1 && msg.obj != null) {
                    imgView.setImageBitmap((Bitmap) msg.obj);
                    try {
                        // 写图片缓存
                        ImageUtils.saveImage(imgView.getContext(), filename,
                                (Bitmap) msg.obj);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastMessage(imgView.getContext(), ErrMsg);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    Bitmap bmp = ApiClient.getNetBitmap(imgURL);
                    msg.what = 1;
                    msg.obj = bmp;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * url跳转
     *
     * @param context
     * @param url
     */
    public static void showUrlRedirect(Context context, String url) {
        URLs urls = URLs.parseURL(url);
        if (urls != null) {
            showLinkRedirect(context, urls.getObjType(), urls.getObjId(),
                    urls.getObjKey());
        } else {
            openBrowser(context, url);
        }
    }

    public static void showLinkRedirect(Context context, int objType,
            int objId, String objKey) {
        switch (objType) {
        case URLs.URL_OBJ_TYPE_NEWS:
            showNewsDetail(context, objId);
            break;
        case URLs.URL_OBJ_TYPE_OTHER:
            openBrowser(context, objKey);
            break;
        }
    }

    /**
     * 打开浏览器
     *
     * @param context
     * @param url
     */
    public static void openBrowser(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent it = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(it);
        } catch (Exception e) {
            e.printStackTrace();
            ToastMessage(context, "无法浏览此网页", 500);
        }
    }

    /**
     * 获取webviewClient对象
     *
     * @return
     */
    public static WebViewClient getWebViewClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                showUrlRedirect(view.getContext(), url);
                return true;
            }
        };
    }

    /**
     * 获取TextWatcher对象
     *
     * @param context
     * @param temlKey
     * @return
     */
    public static TextWatcher getTextWatcher(final Activity context,
            final String temlKey) {
        return new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // 保存当前EditText正在编辑的内容
                ((AppContext) context.getApplication()).setProperty(temlKey,
                        s.toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        };
    }

    /**
     * 编辑器显示保存的草稿
     *
     * @param context
     * @param editer
     * @param temlKey
     */
    public static void showTempEditContent(Activity context, EditText editer,
            String temlKey) {
        String tempContent = ((AppContext) context.getApplication())
                .getProperty(temlKey);
        if (!StringUtils.isEmpty(tempContent)) {
            SpannableStringBuilder builder = parseFaceByText(context,
                    tempContent);
            editer.setText(builder);
            editer.setSelection(tempContent.length());// 设置光标位置
        }
    }

    /**
     * 将[12]之类的字符串替换为表情
     *
     * @param context
     * @param content
     */
    public static SpannableStringBuilder parseFaceByText(Context context,
            String content) {
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        Matcher matcher = facePattern.matcher(content);
        while (matcher.find()) {
            // 使用正则表达式找出其中的数字
            int position = StringUtils.toInt(matcher.group(1));
            int resId = 0;
            try {
                if (position > 65 && position < 102)
                    position = position - 1;
                else if (position > 102)
                    position = position - 2;
                resId = GridViewFaceAdapter.getImageIds()[position];
                Drawable d = context.getResources().getDrawable(resId);
                d.setBounds(0, 0, 35, 35);// 设置表情图片的显示大小
                ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
                builder.setSpan(span, matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (Exception e) {
            }
        }
        return builder;
    }

    /**
     * 清除文字
     *
     * @param cont
     * @param editer
     */
    public static void showClearWordsDialog(final Context cont,
            final EditText editer, final TextView numwords) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setTitle(R.string.clearwords);
        builder.setPositiveButton(R.string.sure,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 清除文字
                        editer.setText("");
                        numwords.setText("160");
                    }
                });
        builder.setNegativeButton(R.string.cancle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }


    /**
     * 组合动态的动作文本
     *
     * @param objecttype
     * @param objectcatalog
     * @param objecttitle
     * @return
     */
    @SuppressLint("NewApi")
    public static SpannableString parseActiveAction(String author,
            int objecttype, int objectcatalog, String objecttitle) {
        String title = "";
        int start = 0;
        int end = 0;
        if (objecttype == 32 && objectcatalog == 0) {
            title = "加入了开源中国";
        } else if (objecttype == 1 && objectcatalog == 0) {
            title = "添加了开源项目 " + objecttitle;
        } else if (objecttype == 2 && objectcatalog == 1) {
            title = "在讨论区提问：" + objecttitle;
        } else if (objecttype == 2 && objectcatalog == 2) {
            title = "发表了新话题：" + objecttitle;
        } else if (objecttype == 3 && objectcatalog == 0) {
            title = "发表了博客 " + objecttitle;
        } else if (objecttype == 4 && objectcatalog == 0) {
            title = "发表一篇新闻 " + objecttitle;
        } else if (objecttype == 5 && objectcatalog == 0) {
            title = "分享了一段代码 " + objecttitle;
        } else if (objecttype == 6 && objectcatalog == 0) {
            title = "发布了一个职位：" + objecttitle;
        } else if (objecttype == 16 && objectcatalog == 0) {
            title = "在新闻 " + objecttitle + " 发表评论";
        } else if (objecttype == 17 && objectcatalog == 1) {
            title = "回答了问题：" + objecttitle;
        } else if (objecttype == 17 && objectcatalog == 2) {
            title = "回复了话题：" + objecttitle;
        } else if (objecttype == 17 && objectcatalog == 3) {
            title = "在 " + objecttitle + " 对回帖发表评论";
        } else if (objecttype == 18 && objectcatalog == 0) {
            title = "在博客 " + objecttitle + " 发表评论";
        } else if (objecttype == 19 && objectcatalog == 0) {
            title = "在代码 " + objecttitle + " 发表评论";
        } else if (objecttype == 20 && objectcatalog == 0) {
            title = "在职位 " + objecttitle + " 发表评论";
        } else if (objecttype == 101 && objectcatalog == 0) {
            title = "回复了动态：" + objecttitle;
        } else if (objecttype == 100) {
            title = "更新了动态";
        }
        title = author + " " + title;
        SpannableString sp = new SpannableString(title);
        // 设置用户名字体大小、加粗、高亮
        sp.setSpan(new AbsoluteSizeSpan(14, true), 0, author.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
                author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 0,
                author.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // 设置标题字体大小、高亮
        if (!StringUtils.isEmpty(objecttitle)) {
            start = title.indexOf(objecttitle);
            if (objecttitle.length() > 0 && start > 0) {
                end = start + objecttitle.length();
                sp.setSpan(new AbsoluteSizeSpan(14, true), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                sp.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#0e5986")),
                        start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return sp;
    }

    /**
     * 组合动态的回复文本
     *
     * @param name
     * @param body
     * @return
     */
    public static SpannableString parseActiveReply(String name, String body) {
        SpannableString sp = new SpannableString(name + "：" + body);
        // 设置用户名字体加粗、高亮
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
                name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 0,
                name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    /**
     * 组合消息文本
     *
     * @param name
     * @param body
     * @return
     */
    public static void parseMessageSpan(LinkView view, String name,
            String body, String action) {
        Spanned span = null;
        SpannableStringBuilder style = null;
        int start = 0;
        int end = 0;
        String content = null;
        if (StringUtils.isEmpty(action)) {
            content = name + "：" + body;
            span = Html.fromHtml(content);
            view.setText(span);
            end = name.length();
        } else {
            content = action + name + "：" + body;
            span = Html.fromHtml(content);
            view.setText(span);
            start = action.length();
            end = start + name.length();
        }
        view.setMovementMethod(LinkMovementMethod.getInstance());

        Spannable sp = (Spannable) view.getText();
        URLSpan[] urls = span.getSpans(0, sp.length(), URLSpan.class);

        style = new SpannableStringBuilder(view.getText());
        // style.clearSpans();// 这里会清除之前所有的样式
        for (URLSpan url : urls) {
             style.removeSpan(url);// 只需要移除之前的URL样式，再重新设置
             MyURLSpan myURLSpan =  view.new MyURLSpan(url.getURL());
             style.setSpan(myURLSpan, span.getSpanStart(url),
                    span.getSpanEnd(url), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 设置用户名字体加粗、高亮
        style.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start,
                end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        style.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")),
                start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(style);
    }

    /**
     * 组合回复引用文本
     *
     * @param name
     * @param body
     * @return
     */
    public static SpannableString parseQuoteSpan(String name, String body) {
        SpannableString sp = new SpannableString("回复：" + name + "\n" + body);
        // 设置用户名字体加粗、高亮
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 3,
                3 + name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 3,
                3 + name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sp;
    }

    /**
     * 弹出Toast消息
     *
     * @param msg
     */
    public static void ToastMessage(Context cont, String msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, int msg) {
        Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
    }

    public static void ToastMessage(Context cont, String msg, int time) {
        Toast.makeText(cont, msg, time).show();
    }

    /**
     * 点击返回监听事件
     *
     * @param activity
     * @return
     */
    public static View.OnClickListener finish(final Activity activity) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                activity.finish();
            }
        };
    }

    /**
     * 显示关于我们
     *
     * @param context
     */
    public static void showAbout(Context context) {
        Intent intent = new Intent(context, About.class);
        context.startActivity(intent);
    }

    /**
     * 显示关于消息中心
     *
     * @param context
     */
    public static void showMsgCenter(Context context) {
        Intent intent = new Intent(context, MsgCenter.class);
        context.startActivity(intent);
    }

    /**
     * 显示活动提醒
     *
     * @param context
     */
    public static void showEventCenter(Context context,Bundle data) {
        Intent intent = new Intent(context, EventNotice.class);
        if(null!=data){
            intent.putExtras(data);
        }
        context.startActivity(intent);
    }

    /**
     * 显示系统消息
     *
     * @param context
     */
    public static void showXGCenter(Context context,Bundle data) {
        Intent intent = new Intent(context, XGNoticeActivity.class);
        if(null!=data){
            intent.putExtras(data);
        }
        context.startActivity(intent);
    }

    /**
     * 显示用户收藏
     *
     * @param context
     */
    public static void showUserFavor(Context context) {
        Intent intent = new Intent(context, UserFavor.class);
        context.startActivity(intent);
    }

    /**
     * 显示频道列表
     * @param context
     */
    public static void showChannelList(Context context){
        Intent intent = new Intent(context, ChannelList.class);
        context.startActivity(intent);
    }

    /**
     * 显示用户反馈
     *
     * @param context
     */
    public static void showFeedBack(Context context) {
        Intent intent = new Intent(context, FeedBack.class);
        context.startActivity(intent);
    }
    /**
     * 菜单显示登录或登出
     *
     * @param activity
     * @param menu
     */
    public static void showMenuLoginOrLogout(Activity activity, Menu menu) {
        if (((AppContext) activity.getApplication()).isLogin()) {
            menu.findItem(R.id.main_menu_user).setTitle(
                    R.string.main_menu_logout);
            menu.findItem(R.id.main_menu_user).setIcon(
                    R.drawable.ic_menu_logout);
        } else {
            menu.findItem(R.id.main_menu_user).setTitle(
                    R.string.main_menu_login);
            menu.findItem(R.id.main_menu_user)
                    .setIcon(R.drawable.ic_menu_login);
        }
    }

    /**
     * 快捷栏显示登录与登出
     *
     * @param activity
     * @param qa
     */
    public static void showSettingLoginOrLogout(Activity activity,
            QuickAction qa) {
        if (((AppContext) activity.getApplication()).isLogin()) {
            qa.setIcon(MyQuickAction.buildDrawable(activity,
                    R.drawable.ic_menu_logout));
            qa.setTitle(activity.getString(R.string.main_menu_logout));
        } else {
            qa.setIcon(MyQuickAction.buildDrawable(activity,
                    R.drawable.ic_menu_login));
            qa.setTitle(activity.getString(R.string.main_menu_login));
        }
    }

    /**
     * 快捷栏是否显示文章图片
     *
     * @param activity
     * @param qa
     */
    public static void showSettingIsLoadImage(Activity activity, QuickAction qa) {
        if (((AppContext) activity.getApplication()).isLoadImage()) {
            qa.setIcon(MyQuickAction.buildDrawable(activity,
                    R.drawable.ic_menu_picnoshow));
            qa.setTitle(activity.getString(R.string.main_menu_picnoshow));
        } else {
            qa.setIcon(MyQuickAction.buildDrawable(activity,
                    R.drawable.ic_menu_picshow));
            qa.setTitle(activity.getString(R.string.main_menu_picshow));
        }
    }

    /**
     * 用户登录或注销
     *
     * @param activity
     */
    public static void loginOrLogout(Activity activity) {
        AppContext ac = (AppContext) activity.getApplication();
        if (ac.isLogin()) {
            ac.logout();
            ToastMessage(activity, "已退出登录");
        } else {
            showLoginDialog(activity);
        }
    }

    /**
     * 文章是否加载图片显示
     *
     * @param activity
     */
    public static void changeSettingIsLoadImage(Activity activity) {
        AppContext ac = (AppContext) activity.getApplication();
        if (ac.isLoadImage()) {
            ac.setConfigLoadimage(false);
            ToastMessage(activity, "已设置文章不加载图片");
        } else {
            ac.setConfigLoadimage(true);
            ToastMessage(activity, "已设置文章加载图片");
        }
    }

    public static void changeSettingIsLoadImage(Activity activity, boolean b) {
        AppContext ac = (AppContext) activity.getApplication();
        ac.setConfigLoadimage(b);
    }

    /**
     * 清除app缓存
     *
     * @param activity
     */
    public static void clearAppCache(Activity activity) {
        final AppContext ac = (AppContext) activity.getApplication();
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    ToastMessage(ac, "缓存清除成功");
                } else {
                    ToastMessage(ac, "缓存清除失败");
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = new Message();
                try {
                    ac.clearAppCache();
                    msg.what = 1;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = -1;
                }
                handler.sendMessage(msg);
            }
        }.start();
    }

    /**
     * 发送App异常崩溃报告
     *
     * @param cont
     * @param crashReport
     */
    public static void sendAppCrashReport(final Context cont,
            final String crashReport) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.app_error);
        builder.setMessage(R.string.app_error_message);
        builder.setPositiveButton(R.string.submit_report,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 发送异常报告
                        Intent i = new Intent(Intent.ACTION_SEND);
                        // i.setType("text/plain"); //模拟器
                        i.setType("message/rfc822"); // 真机
                        i.putExtra(Intent.EXTRA_EMAIL,
                                new String[] { "badstyle@qq.com" });
                        i.putExtra(Intent.EXTRA_SUBJECT,
                                "赤壁乱舞客户端 - 错误报告");
                        i.putExtra(Intent.EXTRA_TEXT, crashReport);
                        cont.startActivity(Intent.createChooser(i, "发送错误报告"));
                        // 退出
                        AppManager.getAppManager().AppExit(cont);
                    }
                });
        builder.setNegativeButton(R.string.sure,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 退出
                        AppManager.getAppManager().AppExit(cont);
                    }
                });
        builder.show();

    }

    /**
     * 退出程序
     *
     * @param cont
     */
    public static void Exit(final Context cont) {
        AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(R.string.app_menu_surelogout);
        builder.setPositiveButton(R.string.sure,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // 退出
                        AppManager.getAppManager().AppExit(cont);
                    }
                });
        builder.setNegativeButton(R.string.cancle,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    /**
     * 添加截屏功能
     */
    @SuppressLint("NewApi")
    public static void addScreenShot(Activity context,
            OnScreenShotListener mScreenShotListener) {
        BaseActivity cxt = null;
        if (context instanceof BaseActivity) {
            cxt = (BaseActivity) context;
            cxt.setAllowFullScreen(false);
            ScreenShotView screenShot = new ScreenShotView(cxt,
                    mScreenShotListener);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            context.getWindow().addContentView(screenShot, lp);
        }
    }

    /**
     * 添加网页的点击图片展示支持
     */
    @SuppressLint("SetJavaScriptEnabled")
    public static void addWebImageShow(final Context cxt, WebView wv) {
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(new OnWebViewImageListener() {

            @Override
            public void onImageClick(String bigImageUrl) {
                if (bigImageUrl != null)
                    UIHelper.showImageZoomDialog(cxt, bigImageUrl);
            }
        }, "mWebViewImageListener");
    }

    /**
     * 显示cordova webview1
     * @param context
     */
    public static void showCDV1(Context context) {
        Intent intent = new Intent(context, CDVActivity1.class);
        context.startActivity(intent);
    }
    //http://www.androidhub4you.com/2012/12/listview-into-scrollview-in-android.html

    /**
     * disable listview's scroll
     * @param myListView
     */
    public static void disableListViewScrolling(ListView myListView) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            //do nothing return null
            return;
        }
        //set listAdapter in loop for getting final size
        int totalHeight = 0;
        for (int size = 0; size < myListAdapter.getCount(); size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        //setting listview item in adapter
        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
        params.height += 5;//if without this statement,the listview will be a little short
        myListView.setLayoutParams(params);
        // print height of adapter on log
        Log.i("height of listItem:", String.valueOf(totalHeight));
    }

    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag){
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    public static void showCommonWebView(Context context,String url,String title,boolean hideFootbar){
        News news = new News();
        news.setUrl(url);
        showNewsDetailByInstance(context,news,title,hideFootbar);
    }

    public static boolean isPlayStoreInstalled(Context context) {
        Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=dummy"));
        PackageManager manager = context.getPackageManager();
        List<ResolveInfo> list = manager.queryIntentActivities(market, 0);

        return list.size() > 0;
    }

    public static int convertDpToPixel(Context context,float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    /** * 根据手机的分辨率从 dp 的单位 转成为 px(像素) */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /** * 根据手机的分辨率从 px(像素) 的单位 转成为 dp */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 检查是否需要换图片
     * @param view
     */
    public static void checkWelcomeBG(Context context,RelativeLayout view) {
        String path = FileUtils.getAppCache(context, "wellcomeback");
        List<File> files = FileUtils.listPathFiles(path);
        if (!files.isEmpty()) {
            File f = files.get(0);
            long time[] = getTime(f.getName());
            long today = StringUtils.getToday();
            if (today >= time[0] && today <= time[1]) {
                view.setBackgroundDrawable(Drawable.createFromPath(f.getAbsolutePath()));
            }
        }
    }

    /**
     * 启动应用
     * @param context
     * @param appPackageName
     */
    public static void launchApp(Context context,String contextTag,String appPackageName){
        MTAHelper.trackClick(context,contextTag,"startGameClickListener");

        if(StringUtils.isEmpty(appPackageName)) {
            return;
        }

        boolean isInstalled = false;
        // 得到PackageManager对象
        final PackageManager pm = context.getPackageManager();

        // 得到系统 安装的所有程序包的PackageInfo对象
        List<PackageInfo> packs = pm
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (PackageInfo pi : packs) {
            if(pi.packageName.equals(appPackageName)){
                isInstalled = true;
                break;
            }
        }

        if(isInstalled){
            //取到点击的包名
            Intent i = pm.getLaunchIntentForPackage(appPackageName);
            //如果该程序不可启动（像系统自带的包，有很多是没有入口的）会返回NULL
            if (i != null)
                context.startActivity(i);
        }else{
            //TODO:去安卓市场
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                //http://android.myapp.com/myapp/detail.htm?apkName=com.tencent.game.VXDGame
                //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + pName)));
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://android.myapp.com/myapp/detail.htm?apkName=" + appPackageName)));
            }

        }
    }

    /**
     * 分析显示的时间
     * @param time
     * @return
     */
    private static long[] getTime(String time) {
        long res[] = new long[2];
        try {
            time = time.substring(0, time.indexOf("."));
            String t[] = time.split("-");
            res[0] = Long.parseLong(t[0]);
            if (t.length >= 2) {
                res[1] = Long.parseLong(t[1]);
            } else {
                res[1] = Long.parseLong(t[0]);
            }
        } catch (Exception e) {
        }
        return res;
    }

}
