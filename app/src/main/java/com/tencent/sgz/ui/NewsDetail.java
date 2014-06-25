package com.tencent.sgz.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.tencent.sgz.AppConfig;
import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppDataProvider;
import com.tencent.sgz.AppException;
import com.tencent.sgz.R;
import com.tencent.sgz.adapter.ListViewCommentAdapter;
import com.tencent.sgz.bean.Comment;
import com.tencent.sgz.bean.CommentList;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.bean.Notice;
import com.tencent.sgz.bean.Result;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.entity.Article;
import com.tencent.sgz.widget.BadgeView;
import com.tencent.sgz.widget.PullToRefreshListView;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import in.xsin.weibo.Helper;
import in.xsin.widget.ProgressWebView;

/**
 * 新闻详情
 * 
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
public class NewsDetail extends BaseActivity implements IWeiboHandler.Response  {

	private FrameLayout mHeader;
	private LinearLayout mFooter;
	private ImageView mFavorite;
	private ImageView mRefresh;
	private TextView mHeadTitle;
	private ProgressBar mProgressbar;
	private ViewSwitcher mViewSwitcher;

	private BadgeView bv_comment;

	private ImageView mCommentList;
	private ImageView mShare;
    private ImageView mHeart;


	private ProgressWebView mWebView;
	private Handler mHandler;
	private News newsDetail;
	private int newsId;

	private final static int VIEWSWITCH_TYPE_DETAIL = 0x001;
	private final static int VIEWSWITCH_TYPE_COMMENTS = 0x002;

	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	private final static int DATA_LOAD_FAIL = 0x003;

	private PullToRefreshListView mLvComment;
	private ListViewCommentAdapter lvCommentAdapter;
	private List<Comment> lvCommentData = new ArrayList<Comment>();
	private View lvComment_footer;
	private TextView lvComment_foot_more;
	private ProgressBar lvComment_foot_progress;
	private Handler mCommentHandler;
	private int lvSumData;

	private int curId;
	private int curCatalog;
	private int curLvDataState;
	private int curLvPosition;// 当前listview选中的item位置

	private ProgressDialog mProgress;
	private String tempCommentKey = AppConfig.TEMP_COMMENT;

	private int _catalog;
	private int _id;
	private long _uid;
	private String _content;
	private int _isPostToMyZone;

    private String customTitle;
    private boolean hideFootbar;

	private GestureDetector gd;
	private boolean isFullScreen;

    private PopupWindow pwShareMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_detail);

        //TODO:放到异步线程中

		this.initView();
		this.initData();

		// 加载评论视图&数据
		this.initCommentView();
		this.initCommentData();

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

		if (newsId > 0)
			tempCommentKey = AppConfig.TEMP_COMMENT + "_"
					+ CommentList.CATALOG_NEWS + "_" + newsId;

		mHeader = (FrameLayout) findViewById(R.id.news_detail_header);
		mFooter = (LinearLayout) findViewById(R.id.news_detail_footer);
		//mHome = (ImageView) findViewById(R.id.news_detail_home);
		mRefresh = (ImageView) findViewById(R.id.news_detail_refresh);
		mHeadTitle = (TextView) findViewById(R.id.news_detail_head_title);
		mProgressbar = (ProgressBar) findViewById(R.id.news_detail_head_progress);
		mViewSwitcher = (ViewSwitcher) findViewById(R.id.news_detail_viewswitcher);

		mCommentList = (ImageView) findViewById(R.id.news_detail_footbar_commentlist);
		mShare = (ImageView) findViewById(R.id.news_detail_footbar_share);
		mFavorite = (ImageView) findViewById(R.id.news_detail_footbar_favorite);
        mHeart = (ImageView) findViewById(R.id.news_detail_footbar_heart);

		mWebView = (ProgressWebView) findViewById(R.id.news_detail_webview);

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

		bv_comment = new BadgeView(this, mCommentList);
		bv_comment.setBackgroundResource(R.drawable.widget_count_bg2);
		bv_comment.setIncludeFontPadding(false);
		bv_comment.setGravity(Gravity.CENTER);
		bv_comment.setTextSize(8f);
		bv_comment.setTextColor(Color.WHITE);

        if(!customTitle.equals("")){
            mHeadTitle.setText(customTitle);
        }
        if(hideFootbar){
            mFooter.setVisibility(View.GONE);
        }

	}

	// 初始化控件数据
	private void initData() {
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					headButtonSwitch(DATA_LOAD_COMPLETE);

					// 是否收藏
                    /*
					if (newsDetail.getFavorite() == 1)
						mFavorite
								.setImageResource(R.drawable.fbar_favon_bg);
					else
						mFavorite
								.setImageResource(R.drawable.fbar_fav_bg);
				    */
                    if(appContext.getData().hasFavItem(newsDetail.getMd5())){
                        mFavorite
                                .setImageResource(R.drawable.fbar_favon_bg);
                    }else{
                        mFavorite
                                .setImageResource(R.drawable.fbar_fav_bg);
                    }

					// 显示评论数
					if (newsDetail.getCommentCount() > 0) {
						bv_comment.setText(newsDetail.getCommentCount() + "");
						bv_comment.show();
					} else {
						bv_comment.setText("");
						bv_comment.hide();
					}
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

					// 发送通知广播
					if (msg.obj != null) {
						UIHelper.sendBroadCast(NewsDetail.this,
								(Notice) msg.obj);
					}
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

		new Thread() {
			public void run() {
				Message msg = new Message();
                /*
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
				*/
                msg.what = 1;
                msg.obj = null;
				mHandler.sendMessage(msg);
			}
		}.start();
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
			mHeadTitle.setText(R.string.comment_list_head_title);
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
			mRefresh.setVisibility(View.GONE);
			break;
		case DATA_LOAD_COMPLETE:
            mWebView.setVisibility(View.VISIBLE);
			mProgressbar.setVisibility(View.GONE);
			mRefresh.setVisibility(View.VISIBLE);
			break;
		case DATA_LOAD_FAIL:
            mWebView.setVisibility(View.GONE);
			mProgressbar.setVisibility(View.GONE);
			mRefresh.setVisibility(View.VISIBLE);
			break;
		}
	}

	private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			//hideEditor(v);
			initData(newsId, true);
			loadLvCommentData(curId, curCatalog, 0, mCommentHandler,
					UIHelper.LISTVIEW_ACTION_REFRESH);
		}
	};

	private View.OnClickListener shareClickListener = new View.OnClickListener() {
		public void onClick(View v) {
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

	private View.OnClickListener favoriteClickListener = new View.OnClickListener() {
		public void onClick(View v) {

            if (newsDetail == null||newsDetail.getMd5()==null||newsDetail.getMd5().equals("")) {
                return;
            }

            final long uid = appContext.getLoginUid();

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
                        UIHelper.ToastMessage(appContext,"收藏成功！");
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

            AppDataProvider.toggleFavArticle(appContext, item, uid, onDataGot);

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

	// 初始化视图控件
	private void initCommentView() {
		lvComment_footer = getLayoutInflater().inflate(
				R.layout.listview_footer, null);
		lvComment_foot_more = (TextView) lvComment_footer
				.findViewById(R.id.listview_foot_more);
		lvComment_foot_progress = (ProgressBar) lvComment_footer
				.findViewById(R.id.listview_foot_progress);

		lvCommentAdapter = new ListViewCommentAdapter(this, lvCommentData,
				R.layout.comment_listitem);
		mLvComment = (PullToRefreshListView) findViewById(R.id.comment_list_listview);

		mLvComment.addFooterView(lvComment_footer);// 添加底部视图 必须在setAdapter前
		mLvComment.setAdapter(lvCommentAdapter);
		mLvComment
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// 点击头部、底部栏无效
						if (position == 0 || view == lvComment_footer)
							return;

						Comment com = null;
						// 判断是否是TextView
						if (view instanceof TextView) {
							com = (Comment) view.getTag();
						} else {
							ImageView img = (ImageView) view
									.findViewById(R.id.comment_listitem_userface);
							com = (Comment) img.getTag();
						}
						if (com == null)
							return;

						// 跳转--回复评论界面
						UIHelper.showCommentReply(NewsDetail.this, curId,
								curCatalog, com.getId(), com.getAuthorId(),
								com.getAuthor(), com.getContent());
					}
				});
		mLvComment.setOnScrollListener(new AbsListView.OnScrollListener() {
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mLvComment.onScrollStateChanged(view, scrollState);

				// 数据为空--不用继续下面代码了
				if (lvCommentData.size() == 0)
					return;

				// 判断是否滚动到底部
				boolean scrollEnd = false;
				try {
					if (view.getPositionForView(lvComment_footer) == view
							.getLastVisiblePosition())
						scrollEnd = true;
				} catch (Exception e) {
					scrollEnd = false;
				}

				if (scrollEnd && curLvDataState == UIHelper.LISTVIEW_DATA_MORE) {
					mLvComment.setTag(UIHelper.LISTVIEW_DATA_LOADING);
					lvComment_foot_more.setText(R.string.load_ing);
					lvComment_foot_progress.setVisibility(View.VISIBLE);
					// 当前pageIndex
					int pageIndex = lvSumData / 20;
					loadLvCommentData(curId, curCatalog, pageIndex,
							mCommentHandler, UIHelper.LISTVIEW_ACTION_SCROLL);
				}
			}

			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				mLvComment.onScroll(view, firstVisibleItem, visibleItemCount,
						totalItemCount);
			}
		});
		mLvComment
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						// 点击头部、底部栏无效
						if (position == 0 || view == lvComment_footer)
							return false;

						Comment _com = null;
						// 判断是否是TextView
						if (view instanceof TextView) {
							_com = (Comment) view.getTag();
						} else {
							ImageView img = (ImageView) view
									.findViewById(R.id.comment_listitem_userface);
							_com = (Comment) img.getTag();
						}
						if (_com == null)
							return false;

						final Comment com = _com;

						curLvPosition = lvCommentData.indexOf(com);

						final AppContext ac = (AppContext) getApplication();
						// 操作--回复 & 删除
						long uid = ac.getLoginUid();
						// 判断该评论是否是当前登录用户发表的：true--有删除操作 false--没有删除操作
						if (uid == com.getAuthorId()) {
							final Handler handler = new Handler() {
								public void handleMessage(Message msg) {
									if (msg.what == 1) {
										Result res = (Result) msg.obj;
										if (res.OK()) {
											lvSumData--;
											bv_comment.setText(lvSumData + "");
											bv_comment.show();
											lvCommentData.remove(com);
											lvCommentAdapter
													.notifyDataSetChanged();
										}
										UIHelper.ToastMessage(NewsDetail.this,
												res.getErrorMessage());
									} else {
										((AppException) msg.obj)
												.makeToast(NewsDetail.this);
									}
								}
							};
							final Thread thread = new Thread() {
								public void run() {
									Message msg = new Message();
									try {
										Result res = ac.delComment(curId,
												curCatalog, com.getId(),
												com.getAuthorId());
										msg.what = 1;
										msg.obj = res;
									} catch (AppException e) {
										e.printStackTrace();
										msg.what = -1;
										msg.obj = e;
									}
									handler.sendMessage(msg);
								}
							};
							UIHelper.showCommentOptionDialog(NewsDetail.this,
									curId, curCatalog, com, thread);
						} else {
							UIHelper.showCommentOptionDialog(NewsDetail.this,
									curId, curCatalog, com, null);
						}
						return true;
					}
				});
		mLvComment
				.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
					public void onRefresh() {
						loadLvCommentData(curId, curCatalog, 0,
								mCommentHandler,
								UIHelper.LISTVIEW_ACTION_REFRESH);
					}
				});
	}

	// 初始化评论数据
	private void initCommentData() {
		curId = newsId;
		curCatalog = CommentList.CATALOG_NEWS;

		mCommentHandler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what >= 0) {
					CommentList list = (CommentList) msg.obj;
					Notice notice = list.getNotice();
					// 处理listview数据
					switch (msg.arg1) {
					case UIHelper.LISTVIEW_ACTION_INIT:
					case UIHelper.LISTVIEW_ACTION_REFRESH:
						lvSumData = msg.what;
						lvCommentData.clear();// 先清除原有数据
						lvCommentData.addAll(list.getCommentlist());
						break;
					case UIHelper.LISTVIEW_ACTION_SCROLL:
						lvSumData += msg.what;
						if (lvCommentData.size() > 0) {
							for (Comment com1 : list.getCommentlist()) {
								boolean b = false;
								for (Comment com2 : lvCommentData) {
									if (com1.getId() == com2.getId()
											&& com1.getAuthorId() == com2
													.getAuthorId()) {
										b = true;
										break;
									}
								}
								if (!b)
									lvCommentData.add(com1);
							}
						} else {
							lvCommentData.addAll(list.getCommentlist());
						}
						break;
					}

					// 评论数更新
					if (newsDetail != null
							&& lvCommentData.size() > newsDetail
									.getCommentCount()) {
						newsDetail.setCommentCount(lvCommentData.size());
						bv_comment.setText(lvCommentData.size() + "");
						bv_comment.show();
					}

					if (msg.what < 20) {
						curLvDataState = UIHelper.LISTVIEW_DATA_FULL;
						lvCommentAdapter.notifyDataSetChanged();
						lvComment_foot_more.setText(R.string.load_full);
					} else if (msg.what == 20) {
						curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
						lvCommentAdapter.notifyDataSetChanged();
						lvComment_foot_more.setText(R.string.load_more);
					}
					// 发送通知广播
					if (notice != null) {
						UIHelper.sendBroadCast(NewsDetail.this, notice);
					}
				} else if (msg.what == -1) {
					// 有异常--显示加载出错 & 弹出错误消息
					curLvDataState = UIHelper.LISTVIEW_DATA_MORE;
					lvComment_foot_more.setText(R.string.load_error);
					((AppException) msg.obj).makeToast(NewsDetail.this);
				}
				if (lvCommentData.size() == 0) {
					curLvDataState = UIHelper.LISTVIEW_DATA_EMPTY;
					lvComment_foot_more.setText(R.string.load_empty);
				}
				lvComment_foot_progress.setVisibility(View.GONE);
				if (msg.arg1 == UIHelper.LISTVIEW_ACTION_REFRESH) {
					mLvComment
							.onRefreshComplete(getString(R.string.pull_to_refresh_update)
									+ new Date().toLocaleString());
					mLvComment.setSelection(0);
				}
			}
		};
		this.loadLvCommentData(curId, curCatalog, 0, mCommentHandler,
				UIHelper.LISTVIEW_ACTION_INIT);
	}

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

		if (requestCode == UIHelper.REQUEST_CODE_FOR_RESULT) {
			Comment comm = (Comment) data
					.getSerializableExtra("COMMENT_SERIALIZABLE");
			lvCommentData.add(0, comm);
			lvCommentAdapter.notifyDataSetChanged();
			mLvComment.setSelection(0);
			// 显示评论数
			int count = newsDetail.getCommentCount() + 1;
			newsDetail.setCommentCount(count);
			bv_comment.setText(count + "");
			bv_comment.show();
		} else if (requestCode == UIHelper.REQUEST_CODE_FOR_REPLY) {
			Comment comm = (Comment) data
					.getSerializableExtra("COMMENT_SERIALIZABLE");
			lvCommentData.set(curLvPosition, comm);
			lvCommentAdapter.notifyDataSetChanged();
		}
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
     * @see {@link Activity#onNewIntent}
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
     * @see {@link IWeiboShareAPI#handleWeiboRequest}
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
