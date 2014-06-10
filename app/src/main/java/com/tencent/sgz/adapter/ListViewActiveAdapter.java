package com.tencent.sgz.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.tencent.sgz.R;
import com.tencent.sgz.bean.Active;
import com.tencent.sgz.bean.News;
import com.tencent.sgz.bean.Tweet;
import com.tencent.sgz.bean.Active.ObjectReply;
import com.tencent.sgz.common.BitmapManager;
import com.tencent.sgz.common.StringUtils;
import com.tencent.sgz.common.UIHelper;
import com.tencent.sgz.widget.GridViewForScrollView;
import com.tencent.sgz.widget.LinkView;
import com.tencent.sgz.widget.LinkView.OnLinkClickListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 用户动态Adapter类
 * 
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-4-21
 */
public class ListViewActiveAdapter extends MyBaseAdapter implements AdapterView.OnItemClickListener {
	private Context context;// 运行上下文
	private List<Active> listItems;// 数据集合
	private LayoutInflater listContainer;// 视图容器
	private int itemViewResource;// 自定义项视图源
	private BitmapManager bmpManager;
	private boolean faceClickEnable;

	private final static String AT_HOST_PRE = "http://t.qq.com";
	private final static String MAIN_HOST = "http://www.3gz.qq.com";

    private int                         firstItemViewResource = -1;//自定义第一个元素的视图资源
    private int                         lastItemViewResource = -1;//最后一个元素的视图资源
    static View firstItemView;
    static View lastItemView;
	
	static class ListItemView { // 自定义控件集合
		public ImageView userface;
		public TextView username;
		public TextView date;
		public LinkView content;
		public TextView reply;
		public TextView commentCount;
		public TextView client;
		public ImageView redirect;
		public ImageView image;
	}

	/**
	 * 实例化Adapter
	 * 
	 * @param context
	 * @param data
	 * @param resource
	 */
	public ListViewActiveAdapter(Context context, List<Active> data,
			int resource) {
		this(context, data, resource, true);
	}

	/**
	 * 实例化Adapter
	 * 
	 * @param context
	 * @param data
	 * @param resource
	 * @param faceClickEnable
	 */
	public ListViewActiveAdapter(Context context, List<Active> data,
			int resource, boolean faceClickEnable) {
        this(context,data,resource,-1,-1,faceClickEnable);
	}

    /**
     * 实例化Adapter
     *
     * @param context
     * @param data
     * @param resource
     * @param faceClickEnable
     */
    public ListViewActiveAdapter(Context context, List<Active> data,int resource,int firstItemViewResource,int lastItemViewResource, boolean faceClickEnable) {
        this.context = context;
        this.listContainer = LayoutInflater.from(context); // 创建视图容器并设置上下文
        this.itemViewResource = resource;
        this.listItems = data;
        this.bmpManager = new BitmapManager(BitmapFactory.decodeResource(
                context.getResources(), R.drawable.widget_dface_loading));
        this.faceClickEnable = faceClickEnable;
        this.firstItemViewResource = firstItemViewResource;
        this.lastItemViewResource = lastItemViewResource;
    }

	public int getCount() {
        int itemSize = listItems.size();
        if(this.firstItemViewResource>0){
            itemSize+=1;
        }
        if(this.lastItemViewResource>0){
            itemSize+=1;
        }
        return itemSize;
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	/**
	 * ListView Item设置
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
        int dataPosition = this.firstItemViewResource>0?(position-1):position;

        //列表第一项
        //TODO:参考convertView.setTag优化性能
        if(position==0 && this.firstItemViewResource>0){
            if(firstItemView == null){
                firstItemView = listContainer.inflate(this.firstItemViewResource,null);
                //TODO:第一个元素视图的数据绑定
                this.initFirstItemView(firstItemView);
            }
            return firstItemView;
        }

        if ( position == (getCount()-1) && this.lastItemViewResource > 0 ){
            if(lastItemView == null){
                lastItemView = listContainer.inflate(this.lastItemViewResource,null);
                //TODO:最后一个元素视图的数据绑定
            }
            return lastItemView;
        }

		// 自定义视图
		ListItemView listItemView = null;

		if (convertView == null||convertView.getTag()==null) {
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(this.itemViewResource, null);

			listItemView = new ListItemView();
			// 获取控件对象
			listItemView.userface = (ImageView) convertView
					.findViewById(R.id.active_listitem_userface);
			listItemView.username = (TextView) convertView
					.findViewById(R.id.active_listitem_username);
			listItemView.content = (LinkView) convertView
					.findViewById(R.id.active_listitem_content);
			listItemView.date = (TextView) convertView
					.findViewById(R.id.active_listitem_date);
			listItemView.commentCount = (TextView) convertView
					.findViewById(R.id.active_listitem_commentCount);
			listItemView.client = (TextView) convertView
					.findViewById(R.id.active_listitem_client);
			listItemView.reply = (TextView) convertView
					.findViewById(R.id.active_listitem_reply);
			listItemView.redirect = (ImageView) convertView
					.findViewById(R.id.active_listitem_redirect);
			listItemView.image = (ImageView) convertView
					.findViewById(R.id.active_listitem_image);

			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置文字和图片
		Active active = listItems.get(position);
		listItemView.username.setText(UIHelper.parseActiveAction(
				active.getAuthor(), active.getObjectType(),
				active.getObjectCatalog(), active.getObjectTitle()));
		listItemView.username.setTag(active);// 设置隐藏参数(实体类)

		// 把相对路径改成绝对路径
		String message = modifyPath(active.getMessage());
		
		listItemView.content.setLinkText(message);
		listItemView.content.setTag(active);// 设置隐藏参数(实体类)
		listItemView.content.setOnClickListener(linkViewClickListener);
		listItemView.content.setLinkClickListener(linkClickListener);
		
		listItemView.date
				.setText(StringUtils.friendly_time(active.getPubDate()));
		listItemView.commentCount.setText(active.getCommentCount() + "");

		switch (active.getAppClient()) {
		default:
			listItemView.client.setText("");
			break;
		case Active.CLIENT_MOBILE:
			listItemView.client.setText("来自:手机");
			break;
		case Active.CLIENT_ANDROID:
			listItemView.client.setText("来自:Android");
			break;
		case Active.CLIENT_IPHONE:
			listItemView.client.setText("来自:iPhone");
			break;
		case Active.CLIENT_WINDOWS_PHONE:
			listItemView.client.setText("来自:Windows Phone");
			break;
		}
		if (StringUtils.isEmpty(listItemView.client.getText().toString()))
			listItemView.client.setVisibility(View.GONE);
		else
			listItemView.client.setVisibility(View.VISIBLE);

		ObjectReply reply = active.getObjectReply();
		if (reply != null) {
			listItemView.reply.setText(UIHelper.parseActiveReply(
					reply.objectName, reply.objectBody));
			listItemView.reply.setVisibility(TextView.VISIBLE);
		} else {
			listItemView.reply.setText("");
			listItemView.reply.setVisibility(TextView.GONE);
		}

		if (active.getActiveType() == Active.CATALOG_OTHER)
			listItemView.redirect.setVisibility(ImageView.GONE);
		else
			listItemView.redirect.setVisibility(ImageView.VISIBLE);

		String faceURL = active.getFace();
		if (faceURL.endsWith("portrait.gif") || StringUtils.isEmpty(faceURL)) {
			listItemView.userface.setImageResource(R.drawable.widget_dface);
		} else {
			bmpManager.loadBitmap(faceURL, listItemView.userface);
		}
		if (faceClickEnable) {
			listItemView.userface.setOnClickListener(faceClickListener);
		}
		listItemView.userface.setTag(active);

		String imgSmall = active.getTweetimage();
		if (!StringUtils.isEmpty(imgSmall)) {
			bmpManager.loadBitmap(imgSmall, listItemView.image, BitmapFactory
					.decodeResource(context.getResources(),
							R.drawable.image_loading));
			listItemView.image.setVisibility(ImageView.VISIBLE);
		} else {
			listItemView.image.setVisibility(ImageView.GONE);
		}

		return convertView;
	}

	private View.OnClickListener faceClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Active active = (Active) v.getTag();
			UIHelper.showUserCenter(v.getContext(), active.getAuthorId(),
					active.getAuthor());
		}
	};

	/**
	 * 修正一些路径
	 * 
	 * @param message
	 * @return
	 */
	private String modifyPath(String message) {
		message = message.replaceAll("(<a[^>]+href=\")/([\\S]+)\"", "$1"
				+ AT_HOST_PRE + "/$2\"");
		message = message.replaceAll("(<a[^>]+href=\")http://t.qq.com([\\S]+)\"", "$1"+MAIN_HOST+"$2\"");
		return message;
	}
	
	private View.OnClickListener linkViewClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			if(!isLinkViewClick()){
				UIHelper.showActiveRedirect(v.getContext(), (Active)v.getTag());
			}
			setLinkViewClick(false);
		}
	};
	
	private OnLinkClickListener linkClickListener = new OnLinkClickListener() {
		public void onLinkClick() {
			setLinkViewClick(true);
		}
	};


    private GridViewForScrollView appboxToolsGridview;
    private GridViewForScrollView appboxGamesGridview;
    private void initFirstItemView(View view){
        appboxToolsGridview = (GridViewForScrollView)view.findViewById(R.id.appbox_tools);

        ArrayList<HashMap<String,Object>> lst = new ArrayList<HashMap<String,Object>>();

        // 百宝箱数据
        HashMap<String,Object> map = new HashMap<String,Object>();
        map.put("itemImage", R.drawable.ico_cate_intro);
        map.put("itemText", "游戏介绍");
        map.put("action","http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264819.shtml");
        lst.add(map);

        map = new HashMap<String,Object>();
        map.put("itemImage", R.drawable.ico_cate_wujiang);
        map.put("itemText", "武将");
        map.put("action","http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264819.shtml");
        lst.add(map);

        map = new HashMap<String,Object>();
        map.put("itemImage", R.drawable.ico_cate_war);
        map.put("itemText", "乱舞战");
        map.put("action","http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264819.shtml");
        lst.add(map);

        map = new HashMap<String,Object>();
        map.put("itemImage", R.drawable.ico_appbox_wallpaper);
        map.put("itemText", "壁纸");
        map.put("action","http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264819.shtml");
        lst.add(map);

        map = new HashMap<String,Object>();
        map.put("itemImage", R.drawable.ico_appbox_qrcode);
        map.put("itemText", "扫一扫");
        map.put("action","app://UIHelper.showCapture");
        lst.add(map);

        map = new HashMap<String,Object>();
        map.put("itemImage", R.drawable.ico_cate_wujiang);
        map.put("itemText", "阵容");
        map.put("action","http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264819.shtml");
        lst.add(map);

        map = new HashMap<String,Object>();
        map.put("itemImage", R.drawable.ico_cate_gift);
        map.put("itemText", "礼包");
        map.put("action","http://ttxd.qq.com/webplat/info/news_version3/7367/7750/7756/m6160/201406/264819.shtml");
        lst.add(map);

        SimpleAdapter adpter = new SimpleAdapter(context,
                lst,R.layout.appbox_gvitem,
                new String[]{"itemImage","itemText"},
                new int[]{R.id.appbox_tool_icon,R.id.appbox_tool_title});

        appboxToolsGridview.setAdapter(adpter);

        appboxToolsGridview.setOnItemClickListener(new gridView1OnClickListener());

        //游戏数据
        appboxGamesGridview = (GridViewForScrollView)view.findViewById(R.id.appbox_games);

        ArrayList<HashMap<String,Object>> lst1 = new ArrayList<HashMap<String,Object>>();
        HashMap<String,Object> map1 = new HashMap<String,Object>();
        map1.put("itemImage", R.drawable.ico_cate_intro);
        map1.put("itemText", "天天炫斗");
        map1.put("action","https://play.google.com/store/apps/details?id=com.tencent.game.VXDGame");
        lst1.add(map1);

        map1 = new HashMap<String,Object>();
        map1.put("itemImage", R.drawable.ico_cate_intro);
        map1.put("itemText", "天天飞车");
        map1.put("action","https://play.google.com/store/apps/details?id=com.king.game.motorag");
        lst1.add(map1);

        SimpleAdapter adpter1 = new SimpleAdapter(context,
                lst1,R.layout.appbox_gvitem_game,
                new String[]{"itemImage","itemText"},
                new int[]{R.id.appbox_game_icon,R.id.appbox_game_title});

        appboxGamesGridview.setAdapter(adpter1);

        //注册点击事件，方法2
        appboxGamesGridview.setOnItemClickListener(this);


    }

    class gridView1OnClickListener implements AdapterView.OnItemClickListener
    {

        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            // TODO Auto-generated method stub
            Object obj = appboxToolsGridview.getAdapter().getItem(arg2);
            HashMap<String,Object> map  = (HashMap<String,Object>)obj;
            String str = (String) map.get("itemText");
            String action = (String)map.get("action");
            UIHelper.ToastMessage(context,""+str,0);

            if (action.indexOf("http")==0){
                News news = new News();
                news.setUrl(action);
                UIHelper.showNewsDetailByInstance(context,news,str,false);
                return;
            }

            if(action.indexOf("app://")==0){
                action = action.replace("app://","");
                if (action.equals("UIHelper.showCapture")){
                    UIHelper.showCapture(context);
                }
            }

        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id)
    {
        Object obj = appboxGamesGridview.getAdapter().getItem(position);
        HashMap<String,Object> map  = (HashMap<String,Object>)obj;
        String str = (String) map.get("itemText");
        String action = (String)map.get("action");
        UIHelper.ToastMessage(context,""+str,0);

        if (action.indexOf("http")==0){
            News news = new News();
            news.setUrl(action);
            UIHelper.showNewsDetailByInstance(context,news,str,false);
            return;
        }

        if(action.indexOf("app://")==0){
            action = action.replace("app://","");
            if (action.equals("UIHelper.showCapture")){
                UIHelper.showCapture(context);
            }
        }

        //TODO:直接启动appstore
        /*
        if (UIHelper.isPlayStoreInstalled()) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + item.getPackageName())));
        } else {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + item.getPackageName())));
        }
        */

        //TODO:直接打开app
        /*
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(item.getPackageName());
        if (intent != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.cantOpen, Toast.LENGTH_SHORT).show();
        }
        */

    }

}