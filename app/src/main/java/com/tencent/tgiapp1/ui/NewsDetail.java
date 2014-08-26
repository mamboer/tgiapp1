package com.tencent.tgiapp1.ui;

import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.tencent.tgiapp1.AppConfig;
import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.AppException;
import com.tencent.tgiapp1.R;
import com.tencent.tgiapp1.activity.BaseActivity;
import com.tencent.tgiapp1.bean.CommentList;
import com.tencent.tgiapp1.bean.News;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.AppData;
import com.tencent.tgiapp1.entity.Article;
import com.tencent.tgiapp1.entity.UserRemindArticleList;
import com.tencent.tgiapp1.widget.BadgeView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import in.xsin.common.MTAHelper;
import in.xsin.weibo.Helper;
import in.xsin.widget.ProgressWebView;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * 新闻详情
 * 
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
@ContentView(R.layout.news_detail)
public class NewsDetail extends BaseActivity implements IWeiboHandler.Response  {

    private static String TAG = NewsDetail.class.getName();

    private FrameLayout mHeader;
    private LinearLayout mFooter;
    private ImageView mFavorite;
    private ImageView mRefresh;
    private TextView mHeadTitle;
    private ProgressBar mProgressbar;
    private ViewSwitcher mViewSwitcher;

    //private BadgeView bv_comment;

    private ImageView mCommentList;
    private ImageView mShare;
    private ImageView mHeart;
    private ImageView mStartGame;

    @InjectView(R.id.news_detail_reminder) ImageView mReminder;


    private ProgressWebView mWebView;
    private Handler mHandler;
    private News newsDetail;
    private int newsId;

    private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
    private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

    private final static int DATA_LOAD_ING = 0x001;
    private final static int DATA_LOAD_COMPLETE = 0x002;
    private final static int DATA_LOAD_FAIL = 0x003;

    private int lvSumData;

    private int curId;
    private int curCatalog;
    private int curLvDataState;
    private int curLvPosition;// 当前listview选中的item位置

    private ProgressDialog mProgress;
    private String tempCommentKey = AppConfig.TEMP_COMMENT;

    private int _catalog;
    private int _id;
    private String _uid;
    private String _content;
    private int _isPostToMyZone;

    private String customTitle;
    private boolean hideFootbar;

    private GestureDetector gd;
    private boolean isFullScreen;
    private boolean allowFullscreen = true;

    private PopupWindow pwShareMenu;

    @Override
    public void init(){

    }

    @Override
    public void refresh(Object ...param){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _uid = appContext.getLoginOpenId();

        //TODO:放到异步线程中

        this.initView();
        this.initData();

        // 注册双击全屏事件
        this.regOnDoubleEvent();

        //微博分享注册
        in.xsin.weibo.Helper.attach(this);

        // 当 Activity 被重新初始化时（该 Activity 处于后台时，可能会由于内存不足被杀掉了），
        // 需要调用 {@link IWeiboShareAPI#handleWeiboResponse} 来接收微博客户端返回的数据。
        // 执行成功，返回 true，并调用 {@link IWeiboHandler.Response#onResponse}；
        // 失败返回 false，不调用上述回调
        if (savedInstanceState != null) {
            Helper.handleWeiboResponse(getIntent(), this);
        }
    }

    // 初始化视图控件
    @SuppressLint("SetJavaScriptEnabled")
    private void initView() {

        Intent intent = getIntent();

        newsId = intent.getIntExtra("news_id", 0);
        newsDetail  = (News)intent.getSerializableExtra("news");
        customTitle =intent.getStringExtra("title");
        hideFootbar = intent.getBooleanExtra("hideFootbar",false);
        allowFullscreen = intent.getBooleanExtra("allowFullscreen",true);

        setAllowFullScreen(allowFullscreen);


        if (newsId > 0)
            tempCommentKey = AppConfig.TEMP_COMMENT + "_"
                    + CommentList.CATALOG_NEWS + "_" + newsId;

        mHeader = (FrameLayout) findViewById(R.id.news_detail_header);
        mFooter = (LinearLayout) findViewById(R.id.news_detail_footer);
        //mHome = (ImageView) findViewById(R.id.news_detail_home);
        mRefresh = (ImageView) findViewById(R.id.news_detail_footbar_refresh);
        mHeadTitle = (TextView) findViewById(R.id.news_detail_head_title);
        mProgressbar = (ProgressBar) findViewById(R.id.news_detail_head_progress);
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.news_detail_viewswitcher);

        mCommentList = (ImageView) findViewById(R.id.news_detail_footbar_commentlist);
        mShare = (ImageView) findViewById(R.id.news_detail_footbar_share);
        mFavorite = (ImageView) findViewById(R.id.news_detail_footbar_favorite);
        mHeart = (ImageView) findViewById(R.id.news_detail_footbar_heart);
        mStartGame = (ImageView) findViewById(R.id.news_detail_footbar_game);

        mWebView = (ProgressWebView) findViewById(R.id.news_detail_webview);

        mWebView.setWebViewClient(UIHelper.getNewsDetailWebViewClient());

        WebSettings webSettings = mWebView.getSettings();

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        //webSettings.setDefaultFontSize(15);
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadsImagesAutomatically(true);

        UIHelper.addWebImageShow(this, mWebView);

        //mHome.setOnClickListener(homeClickListener);
        mFavorite.setOnClickListener(favoriteClickListener);
        mRefresh.setOnClickListener(refreshClickListener);
        mShare.setOnClickListener(shareClickListener);
        mHeart.setOnClickListener(heartClickListener);
        mCommentList.setOnClickListener(commentlistClickListener);

        mStartGame.setOnClickListener(startGameClickListener);

        mReminder.setOnClickListener(reminderClickListener);

        /*
        bv_comment = new BadgeView(this, mCommentList);
        bv_comment.setBackgroundResource(R.drawable.widget_count_bg2);
        bv_comment.setIncludeFontPadding(false);
        bv_comment.setGravity(Gravity.CENTER);
        bv_comment.setTextSize(8f);
        bv_comment.setTextColor(Color.WHITE);
        */

        if(!customTitle.equals("")){
            mHeadTitle.setText(customTitle);
        }
        if(hideFootbar){
            mFooter.setVisibility(View.GONE);
        }

        //添加提醒功能
        this.assertReminder();

    }
    //业务逻辑：如果该新闻有结束时间，并且结束时间大于当前时间，则允许添加到提醒中去
    private boolean assertReminder(){
        if(null==newsDetail||StringUtils.isEmpty(newsDetail.getMd5())) return false;

        String endTime = newsDetail.getEndAt();

        if(StringUtils.isEmpty(endTime)|| ( !StringUtils.isLargerThanToday(endTime) )) return false;

        return true;

    }

    // 初始化控件数据
    private void initData() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    headButtonSwitch(DATA_LOAD_COMPLETE);

                    AppData appData = appContext.getData();

                    // 是否收藏
                    /*
                    if (newsDetail.getFavorite() == 1)
                        mFavorite
                                .setImageResource(R.drawable.fbar_favon_bg);
                    else
                        mFavorite
                                .setImageResource(R.drawable.fbar_fav_bg);
                    */
                    if(appData.hasFavItem(newsDetail.getMd5())){
                        mFavorite
                                .setImageResource(R.drawable.fbar_favon_bg);
                    }else{
                        mFavorite
                                .setImageResource(R.drawable.fbar_fav_bg);
                    }

                    //是否提醒
                    if(appData.hasRemindItem(newsDetail.getMd5())){
                        mReminder
                                .setImageResource(R.drawable.fbar_clock_bg2);
                    }else{
                        mReminder
                                .setImageResource(R.drawable.fbar_clock_bg1);
                    }

                    // 显示评论数
                    /*
                    if (newsDetail.getCommentCount() > 0) {
                        bv_comment.setText(newsDetail.getCommentCount() + "");
                        bv_comment.show();
                    } else {
                        bv_comment.setText("");
                        bv_comment.hide();
                    }
                    */
                    /*
                    String body = UIHelper.WEB_STYLE + newsDetail.getBody();
                    // 读取用户设置：是否加载文章图片--默认有wifi下始终加载图片
                    boolean isLoadImage;
                    AppContext ac = (AppContext) getApplication();
                    if (AppContext.NETTYPE_WIFI == ac.getNetworkType()) {
                        isLoadImage = true;
                    } else {
                        isLoadImage = ac.isLoadImage();
                    }
                    if (isLoadImage) {
                        // 过滤掉 img标签的width,height属性
                        body = body.replaceAll(
                                "(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
                        body = body.replaceAll(
                                "(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

                        // 添加点击图片放大支持
                        body = body.replaceAll("(<img[^>]+src=\")(\\S+)\"",
                                "$1$2\" onClick=\"javascript:mWebViewImageListener.onImageClick('$2')\"");

                    } else {
                        // 过滤掉 img标签
                        body = body.replaceAll("<\\s*img\\s+([^>]*)\\s*>", "");
                    }

                    // 更多关于***软件的信息
                    String softwareName = newsDetail.getSoftwareName();
                    String softwareLink = newsDetail.getSoftwareLink();
                    if (!StringUtils.isEmpty(softwareName)
                            && !StringUtils.isEmpty(softwareLink))
                        body += String
                                .format("<div id='tencent_software' style='margin-top:8px;color:#FF0000;font-weight:bold'>更多关于:&nbsp;<a href='%s'>%s</a>&nbsp;的详细信息</div>",
                                        softwareLink, softwareName);

                    // 相关新闻
                    if (newsDetail.getRelatives().size() > 0) {
                        String strRelative = "";
                        for (Relative relative : newsDetail.getRelatives()) {
                            strRelative += String
                                    .format("<a href='%s' style='text-decoration:none'>%s</a><p/>",
                                            relative.url, relative.title);
                        }
                        body += String.format(
                                "<p/><hr/><b>相关资讯</b><div><p/>%s</div>",
                                strRelative);
                    }

                    body += "<div style='margin-bottom: 80px'/>";

                    System.out.println(body);

                    mWebView.loadDataWithBaseURL(null, body, "text/html",
                            "utf-8", null);
                    mWebView.setWebViewClient(UIHelper.getWebViewClient());
                    */

                    mWebView.loadUrl(newsDetail.getUrl());
                    mWebView.setWebViewClient(UIHelper.getWebViewClient());

                } else if (msg.what == 0) {
                    headButtonSwitch(DATA_LOAD_FAIL);

                    UIHelper.ToastMessage(NewsDetail.this,
                            R.string.msg_load_is_null);
                } else if (msg.what == -1 && msg.obj != null) {
                    headButtonSwitch(DATA_LOAD_FAIL);

                    ((AppException) msg.obj).makeToast(NewsDetail.this);
                }
            }
        };

        initData(newsId, false);
    }

    private void initData(final int news_id, final boolean isRefresh) {
        headButtonSwitch(DATA_LOAD_ING);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = null;
        mHandler.sendMessage(msg);
        /*

        new Thread() {
            public void run() {
                Message msg = new Message();

                try {
                    newsDetail = ((AppContext) getApplication()).getNews(
                            news_id, isRefresh);
                    msg.what = (newsDetail != null && newsDetail.getId() > 0) ? 1
                            : 0;
                    msg.obj = (newsDetail != null) ? newsDetail.getNotice()
                            : null;// 通知信息

                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }

                msg.what = 1;
                msg.obj = null;
                mHandler.sendMessage(msg);
            }
        }.start();
        */
    }

    /**
     * 底部栏切换
     *
     * @param type
     */
    private void viewSwitch(int type) {
        switch (type) {
        case VIEWSWITCH_TYPE_DETAIL:
            //mDetail.setEnabled(false);
            mCommentList.setEnabled(true);
            mHeadTitle.setText(R.string.news_detail_head_title);
            mViewSwitcher.setDisplayedChild(0);
            break;
        case VIEWSWITCH_TYPE_COMMENTS:
            //mDetail.setEnabled(true);
            mCommentList.setEnabled(false);
            //mHeadTitle.setText(R.string.comment_list_head_title);
            mViewSwitcher.setDisplayedChild(1);
            break;
        }
    }

    /**
     * 头部按钮展示
     *
     * @param type
     */
    private void headButtonSwitch(int type) {
        switch (type) {
        case DATA_LOAD_ING:
            mWebView.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.VISIBLE);
            //mRefresh.setVisibility(View.GONE);
            break;
        case DATA_LOAD_COMPLETE:
            mWebView.setVisibility(View.VISIBLE);
            mProgressbar.setVisibility(View.GONE);
            //mRefresh.setVisibility(View.VISIBLE);
            break;
        case DATA_LOAD_FAIL:
            mWebView.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.GONE);
            //mRefresh.setVisibility(View.VISIBLE);
            break;
        }
    }

    private View.OnClickListener reminderClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            MTAHelper.trackClick(NewsDetail.this,TAG,"reminderClickListener");


            if (!assertReminder()) {
                UIHelper.ToastMessage(appContext,"当前新闻或活动已经过期，不能设置提醒！");
                return;
            }

            final Handler onDataGot = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    super.handleMessage(msg);
                    Bundle data = msg.getData();
                    int errCode = data.getInt("errCode");
                    String errMsg = data.getString("errMsg");
                    boolean isRemoved = data.getBoolean("isRemoved");


                    if(errMsg!=null){
                        UIHelper.ToastMessage(appContext,errMsg);
                        return;
                    }

                    if(isRemoved){
                        UIHelper.ToastMessage(appContext,"已取消提醒！");
                        mReminder.setImageResource(R.drawable.fbar_clock_bg1);
                    }else{
                        UIHelper.ToastMessage(appContext,"添加提醒成功！");
                        mReminder.setImageResource(R.drawable.fbar_clock_bg2);
                    }

                }
            };

            final Article item = new Article();

            item.setCateName(newsDetail.getCateName());
            item.setUrl(newsDetail.getUrl());
            item.setDesc(newsDetail.getDesc());
            item.setTitle(newsDetail.getTitle());
            item.setCover(newsDetail.getFace());
            item.setEvtEndAt(newsDetail.getEndAt());
            item.setEvtStartAt(newsDetail.getStartAt());

            UserRemindArticleList.toggleRemindArticle(appContext, item, _uid, onDataGot);

        }
    };

    private View.OnClickListener refreshClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            MTAHelper.trackClick(NewsDetail.this,TAG,"refreshClickListener");
            //hideEditor(v);
            initData(newsId, true);
        }
    };

    private View.OnClickListener shareClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            MTAHelper.trackClick(NewsDetail.this,TAG,"shareClickListener");
            if (newsDetail == null) {
                UIHelper.ToastMessage(v.getContext(),
                        R.string.msg_read_detail_fail);
                return;
            }
            // 分享到
            /*
            UIHelper.showShareDialog(NewsDetail.this, newsDetail.getTitle(),
                    newsDetail.getUrl());
                    */
            View anchor = findViewById(R.id.news_detail_footer);
            UIHelper.showShareDialog1(NewsDetail.this,anchor, newsDetail.getTitle(),
                    newsDetail.getUrl(),newsDetail.getFace());
        }
    };

    private View.OnClickListener detailClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (newsId == 0) {
                return;
            }
            // 切换到详情
            viewSwitch(VIEWSWITCH_TYPE_DETAIL);
        }
    };

    private View.OnClickListener commentlistClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            /*
            if (newsId == 0) {
                return;
            }
            // 切换到评论
            viewSwitch(VIEWSWITCH_TYPE_COMMENTS);
            */
            UIHelper.ToastMessage(NewsDetail.this,"功能未实现，评论接口待开发实现中ing");
        }
    };
    private View.OnClickListener heartClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            UIHelper.ToastMessage(NewsDetail.this,"功能未实现，点赞接口待开发实现中ing");
        }
    };

    private View.OnClickListener startGameClickListener = new View.OnClickListener(){
        public void onClick(View v) {

            String pName = res.getString(R.string.app_package);
            UIHelper.launchApp(NewsDetail.this,TAG,pName);

        }
    };

    private View.OnClickListener favoriteClickListener = new View.OnClickListener() {
        public void onClick(View v) {

            MTAHelper.trackClick(NewsDetail.this,TAG,"favoriteClickListener");

            if (newsDetail == null||StringUtils.isEmpty(newsDetail.getMd5())) {
                return;
            }

            final Handler onDataGot = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    super.handleMessage(msg);
                    Bundle data = msg.getData();
                    int errCode = data.getInt("errCode");
                    String errMsg = data.getString("errMsg");
                    boolean isRemoved = data.getBoolean("isRemoved");


                    if(errMsg!=null){
                        UIHelper.ToastMessage(appContext,errMsg);
                        return;
                    }

                    if(isRemoved){
                        UIHelper.ToastMessage(appContext,"已取消收藏！");
                        mFavorite.setImageResource(R.drawable.fbar_fav_bg);
                    }else{
                        UIHelper.ToastMessage(appContext,"添加收藏成功！");
                        mFavorite.setImageResource(R.drawable.fbar_favon_bg);
                    }

                }
            };

            final Article item = new Article();

            item.setCateName(newsDetail.getCateName());
            item.setUrl(newsDetail.getUrl());
            item.setDesc(newsDetail.getDesc());
            item.setTitle(newsDetail.getTitle());
            item.setCover(newsDetail.getFace());

            AppDataProvider.toggleFavArticle(appContext, item, _uid, onDataGot);

            /*
            if (newsId == 0 || newsDetail == null) {
                return;
            }

            final AppContext ac = (AppContext) getApplication();
            if (!ac.isLogin()) {
                //UIHelper.showLoginDialog(NewsDetail.this);
                UIHelper.showLoginPage(NewsDetail.this);
                return;
            }
            final long uid = ac.getLoginUid();

            final Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        Result res = (Result) msg.obj;
                        if (res.OK()) {
                            if (newsDetail.getFavorite() == 1) {
                                newsDetail.setFavorite(0);
                                mFavorite
                                        .setImageResource(R.drawable.widget_bar_favorite);
                            } else {
                                newsDetail.setFavorite(1);
                                mFavorite
                                        .setImageResource(R.drawable.widget_bar_favorite2);
                            }
                            // 重新保存缓存
                            ac.saveObject(newsDetail, newsDetail.getCacheKey());
                        }
                        UIHelper.ToastMessage(NewsDetail.this,
                                res.getErrorMessage());
                    } else {
                        ((AppException) msg.obj).makeToast(NewsDetail.this);
                    }
                }
            };
            new Thread() {
                public void run() {
                    Message msg = new Message();
                    Result res = null;
                    try {
                        if (newsDetail.getFavorite() == 1) {
                            res = ac.delFavorite(uid, newsId,
                                    FavoriteList.TYPE_NEWS);
                        } else {
                            res = ac.addFavorite(uid, newsId,
                                    FavoriteList.TYPE_NEWS);
                        }
                        msg.what = 1;
                        msg.obj = res;
                    } catch (AppException e) {
                        e.printStackTrace();
                        msg.what = -1;
                        msg.obj = e;
                    }
                    handler.sendMessage(msg);
                }
            }.start();
            */
        }
    };

    /**
     * 线程加载评论数据
     *
     * @param id
     *            当前文章id
     * @param catalog
     *            分类
     * @param pageIndex
     *            当前页数
     * @param handler
     *            处理器
     * @param action
     *            动作标识
     */
    private void loadLvCommentData(final int id, final int catalog,
            final int pageIndex, final Handler handler, final int action) {
        new Thread() {
            public void run() {
                Message msg = new Message();
                boolean isRefresh = false;
                if (action == UIHelper.LISTVIEW_ACTION_REFRESH
                        || action == UIHelper.LISTVIEW_ACTION_SCROLL)
                    isRefresh = true;
                try {
                    CommentList commentlist = ((AppContext) getApplication())
                            .getCommentList(catalog, id, pageIndex, isRefresh);
                    msg.what = commentlist.getPageSize();
                    msg.obj = commentlist;
                } catch (AppException e) {
                    e.printStackTrace();
                    msg.what = -1;
                    msg.obj = e;
                }
                msg.arg1 = action;// 告知handler当前action
                handler.sendMessage(msg);
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        super.onActivityResult(requestCode,resultCode,data);

        Helper.onActivityResult(requestCode,resultCode,data);
        

        if (resultCode != RESULT_OK)
            return;
        if (data == null)
            return;

        viewSwitch(VIEWSWITCH_TYPE_COMMENTS);// 跳到评论列表
    }

    /**
     * 注册双击全屏事件
     */
    private void regOnDoubleEvent() {
        gd = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        isFullScreen = !isFullScreen;
                        if (!isFullScreen) {
                            WindowManager.LayoutParams params = getWindow()
                                    .getAttributes();
                            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            getWindow().setAttributes(params);
                            getWindow()
                                    .clearFlags(
                                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                            mHeader.setVisibility(View.VISIBLE);
                            mFooter.setVisibility(View.VISIBLE);
                        } else {
                            WindowManager.LayoutParams params = getWindow()
                                    .getAttributes();
                            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                            getWindow().setAttributes(params);
                            getWindow()
                                    .addFlags(
                                            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                            mHeader.setVisibility(View.GONE);
                            mFooter.setVisibility(View.GONE);
                        }
                        return true;
                    }
                });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isAllowFullScreen()) {
            gd.onTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        Helper.handleWeiboResponse(intent,this);
    }

    /**
     * 接收微客户端博请求的数据。
     * 当微博客户端唤起当前应用并进行分享时，该方法被调用。
     *
     * @param baseResp 微博请求数据对象
     */
    @Override
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
            case WBConstants.ErrorCode.ERR_OK:
                UIHelper.ToastMessage(NewsDetail.this,R.string.Weibo_Share_Success);
                break;
            case WBConstants.ErrorCode.ERR_CANCEL:
                UIHelper.ToastMessage(NewsDetail.this,R.string.Weibo_Share_Cancel);
                break;
            case WBConstants.ErrorCode.ERR_FAIL:
                UIHelper.ToastMessage(NewsDetail.this,getString(R.string.Weibo_Share_Error)+":"+baseResp.errMsg);
                break;
        }
    }

}
