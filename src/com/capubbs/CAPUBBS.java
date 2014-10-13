package com.capubbs;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.lang.reflect.*;
import java.net.*;

import com.ndktools.javamd5.*;

import android.net.*;
import android.os.*;
import android.os.Process;
import android.provider.MediaStore;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.sax.*;
import android.text.Html.*;
import android.text.*;
import android.text.format.Time;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.*;
import android.widget.AdapterView.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.xml.sax.*;


public class CAPUBBS extends Activity {	
	static final String updateTime="2014-09-23";
	ArrayList<String> list;
	class Contents {
		private String author,time,ftext;
		private Spanned text;
		private String floor="",serial,page,id,lzl="";
		private String fid="",title="";
		private boolean istop=false,islocked=false,isextr=false;
		String getPage() {return page;}
		String getAuthor() {return author;}
		String getTime() {return time;}
		Spanned getText() {return text;}
		String getFloor() {return floor;}
		String getSerial() {return serial;}
		String getId() {return id;}
		String getlzl() {return lzl;}
		String getfid() {return fid;}
		String getTitle() {return title;}
		boolean gettop() {return istop;}
		boolean getextr() {return isextr;}
		boolean getlock() {return islocked;}
		void setAuthor(String au) {author=au;}
		void setTime(String tm) {time=tm;}
		void setText(String tx) {ftext=tx;}
		void setFloor(String fl) {floor=fl;}
		void setSerial(String sl) {serial=sl;}
		void setPage(String pg) {page=pg;}
		void setId(String i) {id=i;}
		void setlzl(String i) {lzl=i;}
		void setfid(String i) {fid=i;}
		void setTitle(String i) {title=i;}
		void settop(boolean top) {istop=top;}
		void setextr(boolean extr) {isextr=extr;}
		void setlock(boolean lock) {islocked=lock;}
		
		void trans() {
			
			if (loadingtype!=LOADING_SHOW) {
				text=SpannedString.valueOf(ftext);
				return;
			}
			
			String tmp=ftext;
			if (istop || isextr || islocked) tmp+="&nbsp;&nbsp;";

			if (istop) tmp+="&nbsp;<img src=\"-top-\" />";
			if (isextr) tmp+="&nbsp;<img src=\"-extr-\" />";
			if (islocked) tmp+="&nbsp;<img src=\"-lock-\" />";
			
			text=Html.fromHtml(tmp, new Html.ImageGetter() {
				
				@Override
				public Drawable getDrawable(String arg0) {
					arg0=arg0.trim();
					Drawable auto=getResources().getDrawable(R.drawable.picture);
					Drawable top=getResources().getDrawable(R.drawable.top);
					Drawable extr=getResources().getDrawable(R.drawable.extr);
					Drawable lock=getResources().getDrawable(R.drawable.lock);
					auto.setBounds(0, 0, 25, 25);
					top.setBounds(0, 0, 25, 25);
					extr.setBounds(0, 0, 25, 25);
					lock.setBounds(0, 0, 25, 25);
					if (arg0.equals("-top-"))
						return top;
					else if (arg0.equals("-extr-")) return extr;
					else if (arg0.equals("-lock-")) return lock;
					
					if (!showImage)
						return auto;
					if (!arg0.startsWith("http") && !arg0.startsWith("ftp"))
						arg0="http://www.chexie.net"+arg0;
					Drawable drawable=null;
					URL url;
					try {
						url=new URL(arg0);
						if (!isRunning) {
							return null;
						}
						drawable = Drawable.createFromStream(url.openStream(), "");
						if (drawable!=null)
						{
							int height=drawable.getIntrinsicHeight();
							int width=drawable.getIntrinsicWidth();
							height=(int)(2.5*height);width=(int)(2.5*width);
							if (width>listWidth) {
								height=height*listWidth/width;
								width=listWidth;
							}
							drawable.setBounds(0, 0, width, height);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (drawable==null) drawable=auto;
					return drawable;
				}
			}, new MyTagHandler());
		}
	}
	class JumpUrl extends ClickableSpan {
		private String url="";
		int bid=-1;
		int tid=-1;
		int page=1;
		
		JumpUrl(String turl) {url=turl;}
		public void onClick(View e) {
			
			boolean autoopen=false;
			if (url.equals("#")) return;
			if (!url.startsWith("http://www.chexie.net/bbs/main")
					&& !url.startsWith("http://www.chexie.net/bbs/content")
					&& !url.startsWith("http://www.chexie.net/cgi-bin/bbs.pl?")) {
				autoopen=true;
			}
			else {
				getbidtid(url);
				if (bid<1 || bid==8 || (bid>9 && bid!=28)) autoopen=true;
			}
			
			if (autoopen) {
				new AlertDialog.Builder(CAPUBBS.this).setTitle("是否打开网页？").
				setMessage("你点开了一个网页，是否使用系统自带浏览器打开？")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						Uri uri=Uri.parse(url);
						Intent intent=new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				}).setNegativeButton("取消", null).setCancelable(true).show();
			}
			else {
				if (bid==9) bid=8;
				else if (bid==28) bid=9;
				if (tid!=-1)
					showText(bid, tid+"", page, "");
				else showText(bid, "", page, "");
			}
			
		}
		
		void getbidtid(String str) {
			int pos=str.lastIndexOf("?");
			int anchor=str.lastIndexOf("#");
			String queryString="";
			if (anchor<=pos+1)
				queryString=str.substring(pos+1);
			else queryString=str.substring(pos+1, anchor-pos-1);
			if (queryString=="") return;
			String[] strings=queryString.split("&");
			for (String string : strings) {
				String[] paramStrings=string.split("=");
				paramStrings[0].trim();
				paramStrings[1].trim();
				if (paramStrings[0].equals("bid") || paramStrings[0].equals("b")) {
					bid=getNum(paramStrings[1]);
				}
				else if (paramStrings[0].equals("tid")) {
					tid=getNum(paramStrings[1]);
				}
				else if (paramStrings[0].equals("p")) {
					page=getNum(paramStrings[1]);
					if (page==-1) page=1;
				}
				else if (paramStrings[0].equals("id")) {
					bid=getid(paramStrings[1]);
				}
				else if (paramStrings[0].equals("see")) {
					tid=gettid(paramStrings[1]);
				}
			}
		}
		
		int getNum(String t) {
			if (t==null) return -1;
			if (t=="") return -1;
			int x;
			try {
				x=Integer.parseInt(t);
			} catch (NumberFormatException e) {
				return -1;
				// TODO: handle exception
			}
			if (x<=0) return -1;
			return x;
		}
		
		int getid(String str) {
			if (str==null || str=="")  return -1;
			String[] strings={"","act","capu","bike","water","acad","asso","skill","book",
					"race","info","null","bull","news"};
			for (int i = 1; i < strings.length; i++)
				if (strings[i]==str) return i;
			if (str=="play") return 16;
			if (str=="tour") return 20;
			if (str=="web") return 28;
			if (str=="test") return 30;
			if (str=="jing") return 31;
			return -1;
		}
		
		int gettid(String str) {
			if (str==null || str.length()!=4) return -1;
			char[] x=new char[4];
			int num=0;
			for (int i=0;i<=3;i++)
			{
				num=num*26;
				x[i]=str.charAt(i);
				if (x[i]<'a' || x[i]>'z') return -1;
				num+=x[i]-'a';
			}
			return num+1;
		}
	}
	class MyTagHandler implements TagHandler {
		boolean first=true;
		String parent=null;
		int index=1;
		@Override
		public void handleTag(boolean opening,String tag,Editable output,XMLReader xmlReader) {
			if (tag.equals("ul")) parent="ul";
			else if (tag.equals("ol")) parent="ol";
			if (tag.equals("li")) {
				if(parent.equals("ul")){
					if(first) {
						output.append("\n\t ▪");
						first=false;
					}
					else first=true;
				}
				else {
					if (first) {
						output.append("\n\t "+index+". ");
						first=false;
						index++;
					}
					else first=true;
				}
			}
		}
	}
	String[] serialmap;
	String[] floormap;
	Spanned[] textmap;
	String[] authormap;
	String[] fidmap;
	String[] titlemap;
	int[] lzlmap;
	boolean[] topmap;
	boolean[] extrmap;
	boolean[] lockmap;
	List<Map<String, Object>> maps=new ArrayList<Map<String, Object>>();
	Map<String, ConnectWeb> listMap=new HashMap<String, ConnectWeb>();
	int id;String serial;int page;String title;
	int oldid, oldpage;
	String oldserial, oldtitle;
	
	String username,password;
	String token="";
	boolean login,requesting=false;
	int fontsize,version;
	boolean deling,replying,lzling;
	int registering,mainpaging;
	int searching;
	Object locked=new Object();
	SharedPreferences datas;
	SharedPreferences.Editor editor;
	String hints,quotetext,seq,text;
	ConnectWeb connectWeb=new ConnectWeb(),imageConnectWeb;
	boolean autoregister,firsttime=false,showImage=false;
	boolean autologin=false;
	boolean issetting;
	boolean ismainpage,saved;
	long thisStatistics,totalStatistics;
	int uid,selectid;
	int listWidth;
	String tmptitle,tmpserial;
	String lzlContextMenuHintString="";
	ArrayAdapter<String> adapter;
	String tid,tsee;
	Spanned ttext;
	String author="";
	boolean isRunning=false;
	int fid,lzlid;
	int loadingtype,showId,showPage,savedpage=1,replys;
	String delSerial,delFloor,showSerial,showTitle,imgurl="";
	String newUpdateTime="",updateText="",updateURL="";
	int threadoffset,postoffset,searchoffset;
	int lastSearchType;
	boolean istop,isextr,islock;
	double exittime=0;
	ActionBar actionBar;
	Switch imageSwitch;
	Dialog lzlDialog;
	RequestingTask requestingTask;
	private static final int LOADING_NONE = 0;
	private static final int LOADING_MAIN = 1;
	private static final int LOADING_REGISTER = 2;
	private static final int LOADING_LOGIN = 3;
	private static final int LOADING_DELETE = 4;
	private static final int LOADING_SHOW = 5;
	private static final int LOADING_POST = 6;
	private static final int LOADING_UPLOAD_IMAGE = 7;
	private static final int LOADING_LZL_SHOW = 8;
	private static final int LOADING_LZL_POST = 9;
	private static final int LOADING_LZL_DELETE = 10;
	private static final int LOADING_SEARCH_THREAD = 11;
	private static final int LOADING_SEARCH_POST = 12;
	private static final int LOADING_ACTION = 13;
	
	public void init() {
		list=new ArrayList<String>();
		list.add("车协工作区");
		list.add("行者足音");
		list.add("车友宝典");
		list.add("纯净水");
		list.add("考察与社会");
		list.add("五湖四海");
		list.add("一技之长");
		list.add("竞赛竞技");
		list.add("网站维护");
		adapter=new ArrayAdapter<String>(this, 
				android.R.layout.simple_expandable_list_item_1, list);
		id=0;serial="";page=1;title="CAPUBBS";saved=false;
		replying=false;registering=0;deling=false;lzling=false;
		searching=0;mainpaging=1;
		login=false;
		loadingtype=LOADING_NONE;
		username=new String("");
		password=new String("");
		ismainpage=false;
		autoregister=datas.getBoolean("auto", true);
		fontsize=datas.getInt("Font", 0);
		if (autoregister || firsttime)
		{
			username=datas.getString("Username", "");
			password=datas.getString("Password", "");
		}
		else
		{
			editor.putString("Username", "");
			editor.putString("Password", "");
			editor.commit();
		}
		invalidateOptionsMenu();

		setContentView(R.layout.activity_capubbs);
		if (!firsttime)
		{
			showImage=false;
			ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
			if (networkInfo!=null && networkInfo.isAvailable()) {
				if (networkInfo.getType()==ConnectivityManager.TYPE_WIFI) {
					showImage=true;
					Toast.makeText(this, "自动开启图片显示", Toast.LENGTH_SHORT).show();
				}
				else Toast.makeText(this, "图片显示已关闭", Toast.LENGTH_SHORT).show();
			}
		}
		
		if (!username.equals("")) {
			//Toast.makeText(this, "您已经登录了id: "+username, Toast.LENGTH_SHORT).show();
			((EditText)findViewById(R.id.username)).setText(username);
			((EditText)findViewById(R.id.password)).setText(password);
		}
		
		actionBar.setTitle("CAPUBBS");
		connectWeb=new ConnectWeb();
		loadingtype=LOADING_MAIN;
		showId=0;showSerial="mainpage";showPage=1;
		if (firsttime && findCache())
		{
			connectWeb.status=true;
			login=true;
			finishRequest();
		}
		else
		{
			sendRequest("正在获取首页，请稍后....");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getOverflowMenu();
		actionBar=getActionBar();
		actionBar.show();
		actionBar.setHomeButtonEnabled(true);
		datas=getPreferences(0);
		editor=datas.edit();
		thisStatistics=datas.getLong("thisSta", 0);
 		totalStatistics=datas.getLong("totalSta", 0);
		uid=Process.myUid();
		long txBytes=TrafficStats.getUidRxBytes(uid)+TrafficStats.getUidTxBytes(uid);
		
		if (txBytes==TrafficStats.UNSUPPORTED) {
			thisStatistics=0;
			editor.putLong("thisSta", thisStatistics);
			editor.commit();
		}
		setContentView(R.layout.activity_capubbs);
		DisplayMetrics displayMetrics=new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		listWidth=displayMetrics.widthPixels;
		init();
	}
	
	public void unregister() {
		Toast.makeText(this, "注销成功", Toast.LENGTH_SHORT).show();
		editor.putString("Username", "");
		editor.putString("Password", "");
		editor.commit();
		token="";
		init();
	}
	
	public void mainPage(View view) {
		mainpaging=0;
		ismainpage=true;
		invalidateOptionsMenu();
		//setTitle("CAPUBBS");
		actionBar.setTitle("CAPUBBS");
		replying=false;registering=0;deling=false;
		lzling=false;
		id=0;serial="";page=1;
		ListView listView=new ListView(this);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					showText((int)arg3+1,"",1,title);
					return;
			}
		});
		setContentView(listView);
	}
	
	public void register(View view) {
		invalidateOptionsMenu();
		actionBar.setTitle("注册新用户");
		registering=1;
		setContentView(R.layout.register_page);
		TextView textView=(TextView)findViewById(R.id.regisuserhint);
		textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		textView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showText(2,"6205",1,"");
			}
		});
	}
	
	public void regisPost(View view) {
		EditText userText=(EditText)findViewById(R.id.regisuser);
		EditText passText=(EditText)findViewById(R.id.regispsd);
		EditText pass2Text=(EditText)findViewById(R.id.regispsd2);
		EditText codeText=(EditText)findViewById(R.id.code);
		EditText qqText=(EditText)findViewById(R.id.qq);
		EditText emailText=(EditText)findViewById(R.id.email);
		EditText fromText=(EditText)findViewById(R.id.from);
		EditText introText=(EditText)findViewById(R.id.intro);
		EditText sigText=(EditText)findViewById(R.id.sig);
		String user=userText.getText().toString();
		String pass=passText.getText().toString();
		String pass2=pass2Text.getText().toString();
		String code=codeText.getText().toString();
		String qq=qqText.getText().toString();
		String email=emailText.getText().toString();
		String from=fromText.getText().toString();
		String sig=sigText.getText().toString();
		String intro=introText.getText().toString();
		if (user.equals("") || user.indexOf("'")!=-1 || user.indexOf(" ")!=-1) {
			Toast.makeText(this, "用户名为空或有非法字符", Toast.LENGTH_SHORT).show();
			return;
		}
		if (pass.equals("")) {
			Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (!pass.equals(pass2)) {
			Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
			return;
		}
		if (code.length()!=8) {
			Toast.makeText(this, "无效的注册码", Toast.LENGTH_SHORT).show();
			return;
		}
		if (user.length()>15) {
			Toast.makeText(this, "用户名过长", Toast.LENGTH_SHORT).show();
			return;
		}
		RadioButton maleButton=(RadioButton)findViewById(R.id.male);
		RadioButton femaleButton=(RadioButton)findViewById(R.id.female);
		String sex;
		if (maleButton.isChecked()) sex="男";
		else if (femaleButton.isChecked()) sex="女";
		else {
			Toast.makeText(this, "性别不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		connectWeb=new ConnectWeb(user, pass, code, sex, qq, email, from, intro, sig);
		loadingtype=LOADING_REGISTER;
		sendRequest("注册中，请稍后......");
	}
	
	public void login(View view) {
		EditText user=(EditText)findViewById(R.id.username);
		EditText pass=(EditText)findViewById(R.id.password);
		String tempusername=user.getText().toString();
		String temppassword=pass.getText().toString();
		if (tempusername.equals("")) {
			Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
			return;
		}
		if (temppassword.equals("")) {
			Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
			return;
		}
		connectWeb=new ConnectWeb(tempusername, temppassword);
		loadingtype=LOADING_LOGIN;
		sendRequest("登录中，请稍后......");
	}
	
	public void del(String _serial,String _floor) {
		deling=true;
		delSerial=_serial;
		delFloor=_floor;
		connectWeb=new ConnectWeb(id,_serial,_floor);
		loadingtype=LOADING_DELETE;
		sendRequest("删除中，请稍后......");
	}
	
	class RequestingTask extends AsyncTask<ConnectWeb, Integer, Boolean> {
		ConnectWeb oldConnectWeb;
		ProgressDialog progressDialog;
		public RequestingTask(Context context,String string) {
			progressDialog=new ProgressDialog(CAPUBBS.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle("提示");
			progressDialog.setMessage(string);
			progressDialog.setIndeterminate(false);
			progressDialog.setCancelable(false);
			progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
				
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN)
						isRunning=false;
					return false;
				}
			});
		}
		protected void onPreExecute() {
			progressDialog.show();
			requesting=true;
		}
		protected Boolean doInBackground(ConnectWeb... params) {
			oldConnectWeb=new ConnectWeb(params[0]);
			hints="";
			params[0].sendRequest(this);
			return true;
		}
		protected void onPostExecute(Boolean boolean1) {
			progressDialog.dismiss();
			requesting=false;
			if (loadingtype!=LOADING_UPLOAD_IMAGE && !isRunning)
				connectWeb=oldConnectWeb;
			CAPUBBS.this.finishRequest();
		}
	}
	
	public void sendRequest(String msg) {
		login=false;
		requestingTask=new RequestingTask(this, msg);
		requestingTask.execute(connectWeb);
	}
	
	public void	finishRequest() {
		deling=false;
		if (loadingtype==LOADING_UPLOAD_IMAGE) {
			boolean anss=imageConnectWeb.status&&login;
			loadingtype=LOADING_NONE;
			if (!anss) {
				new AlertDialog.Builder(this).setTitle("图片上传失败！")
				.setMessage(hints).setCancelable(true).setPositiveButton("确认", null)
				.show();
			}
			else imageSendFinished();
			return;
		}
		
		boolean ans=connectWeb.status&&login;
		if (!ans)
		{
			autologin=false;
			loadingtype=LOADING_NONE;
			Toast.makeText(this, hints, Toast.LENGTH_SHORT).show();
			return;
		}
		invalidateOptionsMenu();
		if (loadingtype==LOADING_NONE) return;
		if (loadingtype==LOADING_MAIN) {
			loadingtype=LOADING_NONE;
			listMap.put(showId+showSerial+showPage, connectWeb);
			LinearLayout linearLayout=(LinearLayout)findViewById(R.id.mainpage);
			ListIterator<Contents> iterator=connectWeb.uList.listIterator();
			int xid=0;
			serialmap=new String[40];
			floormap=new String[40];
			while (iterator.hasNext()) {
				Contents contents=iterator.next();
				if (contents.ftext==null) continue;
				ttext=contents.getText();
				String bid=contents.getId();
				String tid=contents.getSerial();
				boolean haslink=true;
				if (bid=="") haslink=false;
				final TextView textView=new TextView(this);
				textView.setText(ttext);
				textView.setTextSize(18);
				textView.setPadding(0, 20, 0, 0);
				if (haslink) {
					int realid=Integer.parseInt(bid);
					if (realid>=1 && realid<=7) bid=""+realid;
					else if (realid==9) bid="8";
					else if (realid==28) bid="9";
					else haslink=false;
				}
				textView.setClickable(haslink);
				textView.setTextColor(Color.BLACK);
				textView.setId(xid);
				xid++;
				if (haslink) {
					floormap[xid-1]=bid;
					serialmap[xid-1]=tid;
					textView.setTextColor(Color.BLUE);
					textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
					textView.setOnClickListener(new View.OnClickListener() {
					
						@Override
						public void onClick(View v) {
							int x=textView.getId();
							showText(Integer.parseInt(floormap[x]), serialmap[x], 1, "");
						}
					});
				}
				linearLayout.addView(textView);
			}
			if (!newUpdateTime.equals("") && !newUpdateTime.equals(updateTime)) {
				String msg="软件存在更新 "+newUpdateTime+"\n（当前版本 "+updateTime+" ）\n\n";
				if (updateText.length()!=0) {
					msg=msg+"更新说明："+updateText+"\n\n";
				}
				msg+="点击立刻下载！";
				new AlertDialog.Builder(this).setTitle("提示更新").setMessage(msg)
				.setPositiveButton("下载", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (!"".equals(updateURL)) {
							Uri uri=Uri.parse(updateURL);
							Intent intent=new Intent(Intent.ACTION_VIEW, uri);
							startActivity(intent);
						}
						else {
							new AlertDialog.Builder(CAPUBBS.this).setTitle("错误")
							.setMessage("下载出错\n\n请进入论坛查看有关更新的最新信息")
							.setPositiveButton("关闭", null).show();
						}
					}
				})
				.setNegativeButton("关闭", null).show();
			}
			else {
				if (!firsttime && token=="" && autoregister && 
						!username.equals("") && !password.equals("")) {
					connectWeb=new ConnectWeb(username,password);
					autologin=true;
					loadingtype=LOADING_LOGIN;
					sendRequest("登录中，请稍后......");
				}
			}
			firsttime=true;
			return;
		}
		if (loadingtype==LOADING_REGISTER) {
			registering=1;
			loadingtype=LOADING_NONE;
			Toast.makeText(this, "注册成功，请于电脑端修改更多信息", Toast.LENGTH_SHORT).show();
			username=((EditText)findViewById(R.id.regisuser)).getText().toString();
			password=((EditText)findViewById(R.id.regispsd)).getText().toString();
			editor.putString("Username", username);
			editor.putString("Password", password);
			editor.commit();
			mainPage(null);
			return;
		}
		if (loadingtype==LOADING_LOGIN) {
			loadingtype=LOADING_NONE;
			Toast.makeText(this, hints, Toast.LENGTH_SHORT).show();
			username=((EditText)findViewById(R.id.username)).getText().toString();
			password=((EditText)findViewById(R.id.password)).getText().toString();
			editor.putString("Username", username);
			editor.putString("Password", password);
			editor.commit();
			if (!autologin)
				mainPage(null);
			else autologin=false;
			return;
		}
		if (loadingtype==LOADING_DELETE) {
			loadingtype=LOADING_NONE;
			Toast.makeText(this, hints, Toast.LENGTH_SHORT).show();
			if (delFloor.equals("") || (delFloor.equals("0") && replys==0)) showText(id, "", 1, "");
			else showText(id, delSerial, 1, title);
			return;
		}
		if (loadingtype==LOADING_ACTION) {
			loadingtype=LOADING_NONE;
			Toast.makeText(this, hints, Toast.LENGTH_SHORT).show();
			showText(id, "", 1, "");
			return;
		}
		if (loadingtype==LOADING_SHOW) {
			loadingtype=LOADING_NONE;
			listMap.put(showId+showSerial+showPage, connectWeb);
			issetting=false;saved=false;
			ismainpage=false;lzling=false;
			if (searching==1) searching=2;
			if (registering==1) registering=2;
			if (mainpaging==1) mainpaging=2;
			deling=false;replying=false;
			if (connectWeb.replys!="")
				replys=Integer.parseInt(connectWeb.replys);
			else replys=0;
			final ListView listView=new ListView(this);
			maps=new ArrayList<Map<String,Object>>();
			id=showId;serial=showSerial;page=showPage;
			if (id==0) title="CAPUBBS";
			else if (serial=="") title=list.get(id-1);
			else if (!showTitle.equals("")) title=showTitle;
			else title=tmptitle;
			tmptitle="";
			actionBar.setTitle("("+page+"/"+connectWeb.pages+") "+title);
			Map<String, Object> map=new HashMap<String, Object>();
			ListIterator<Contents> iterator=connectWeb.uList.listIterator();
			int i=0;
			serialmap=new String[40];
			floormap=new String[40];
			textmap=new Spanned[40];
			authormap=new String[40];
			fidmap=new String[40];
			lzlmap=new int[40];
			topmap=new boolean[40];
			extrmap=new boolean[40];
			lockmap=new boolean[40];
			while (iterator.hasNext()) {
				Contents contents=iterator.next();
				map=new HashMap<String, Object>();
				if (contents.ftext==null) continue;
				map.put("text", contents.getText());
				map.put("floor", contents.getFloor());
				map.put("author", contents.getAuthor());
				map.put("time", contents.getTime());
				map.put("lzl", "["+contents.getlzl()+"]");
				maps.add(map);
				serialmap[i]=contents.getSerial();
				textmap[i]=contents.getText();
				authormap[i]=contents.getAuthor();
				topmap[i]=contents.gettop();
				extrmap[i]=contents.getextr();
				lockmap[i]=contents.getlock();
				if (contents.getFloor()!="") {
					floormap[i]=contents.getFloor();
					fidmap[i]=contents.getfid();
					lzlmap[i]=Integer.parseInt(contents.getlzl());
				}
				i++;
			}
			SimpleAdapter adapter;
			if (!serial.equals(""))
			{
				adapter=new SimpleAdapter(this, maps,
					R.layout.show_page_0,
					new String[] {"text","floor","author","time","lzl"},
					new int[] {R.id.text,R.id.floor,R.id.author,R.id.time,R.id.lzl});
				if (fontsize==1) {
					adapter=new SimpleAdapter(this, maps,
							R.layout.show_page_1,
					new String[] {"text","floor","author","time","lzl"},
					new int[] {R.id.text,R.id.floor,R.id.author,R.id.time,R.id.lzl});
				}
			}
			else {
				adapter=new SimpleAdapter(this, maps,
						R.layout.show_page_2,
						new String[] {"text","author","time"},
						new int[] {R.id.text,R.id.author,R.id.time});
					if (fontsize==1) {
						adapter=new SimpleAdapter(this, maps,
								R.layout.show_page_3,
						new String[] {"text","author","time"},
						new int[] {R.id.text,R.id.author,R.id.time});
					}
			}
			adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
				
				@Override
				public boolean setViewValue(View arg0, Object arg1, String arg2) {
					if (arg1 instanceof Spanned && arg0 instanceof TextView) {
						Spanned text=(Spanned)arg1;
						int end=text.length();
						Spannable spannable=(Spannable)text;
						URLSpan[] urlSpans=spannable.getSpans(0, end, URLSpan.class);
						SpannableStringBuilder spannableStringBuilder=new SpannableStringBuilder(text);
						for (URLSpan urlSpan : urlSpans) {
							JumpUrl jumpUrl=new JumpUrl(urlSpan.getURL());
							spannableStringBuilder.setSpan(jumpUrl, 
									spannable.getSpanStart(urlSpan), spannable.getSpanEnd(urlSpan),
									Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						}
						((TextView)arg0).setText((Spanned)spannableStringBuilder);
					}
					else
						((TextView)arg0).setText((String)arg1);
					return true;
				}
			});
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						if (!serial.equals("")) {
							if (floormap[(int)arg3]=="") return;
							fid=Integer.parseInt(fidmap[(int)arg3]);
							seq=floormap[(int)arg3];
							int lzlnum=lzlmap[(int)arg3];
							if (lzlnum!=0) showlzl(fid, Integer.parseInt(seq));
							else {
								text=quotetext=textmap[(int)arg3].toString();
								if (quotetext.length()>50) quotetext=quotetext.substring(0, 50)+"...";
								quotetext="[quote="+authormap[(int)arg3]+"]"+quotetext+"[/quote]\n";
								lzlContextMenuHintString="回复楼中楼";
								listView.showContextMenu();
							}
							return;
						}
						showText(id,serialmap[(int)arg3],1,"");
				}
			});
			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					if (!serial.equals(""))
						fid=Integer.parseInt(fidmap[(int)arg3]);
					text=quotetext=textmap[(int)arg3].toString();
					seq=floormap[(int)arg3];
					if (serial.equals("")) {
						tmpserial=serialmap[(int)arg3];
						istop=topmap[(int)arg3];
						isextr=extrmap[(int)arg3];
						islock=lockmap[(int)arg3];
					}
					if (quotetext.length()>50) quotetext=quotetext.substring(0, 50)+"...";
					quotetext="[quote="+authormap[(int)arg3]+"]"+quotetext+"[/quote]\n";
					lzlContextMenuHintString="查看楼中楼";
					return false;
				}
			});
			listView.setOnScrollListener(new AbsListView.OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView arg0, int arg1) {
					// TODO Auto-generated method stub
					if (arg1==AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
						int val=listView.getFirstVisiblePosition();
						if (serial.equals(""))
							threadoffset=val;
						else postoffset=val;
					}
				}
				
				@Override
				public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub
				}
			});
			this.registerForContextMenu(listView);
			setContentView(listView);
			if (serial.equals("")) {
				savedpage=page;
				listView.setSelection(threadoffset);
			}
			else listView.setSelection(postoffset);
			return;
		}
		if (loadingtype==LOADING_POST) {
			loadingtype=LOADING_NONE;
			Toast.makeText(this, hints, Toast.LENGTH_SHORT).show();
			showText(id, serial, page, "");
			return;
		}
		if (loadingtype==LOADING_LZL_SHOW) {
			loadingtype=LOADING_NONE;
			lzling=true;
			final ListView listView=new ListView(this);
			maps=new ArrayList<Map<String,Object>>();
			Map<String, Object> map=new HashMap<String, Object>();
			ListIterator<Contents> iterator=connectWeb.uList.listIterator();
			int i=0;
			serialmap=new String[400];
			floormap=new String[400];
			textmap=new Spanned[400];
			authormap=new String[400];
			fidmap=new String[400];
			while (iterator.hasNext()) {
				Contents contents=iterator.next();
				map=new HashMap<String, Object>();
				if (contents.ftext==null) continue;
				map.put("text", contents.getText());
				map.put("author", contents.getAuthor());
				map.put("time", contents.getTime());
				maps.add(map);
				textmap[i]=contents.getText();
				authormap[i]=contents.getAuthor();
				floormap[i]=contents.getId();
				fidmap[i]=contents.getfid();
				i++;
			}
			actionBar.setTitle("查看 "+seq+" 楼的楼中楼（共 "+i+" 条）");
			SimpleAdapter adapter;
			adapter=new SimpleAdapter(this, maps,
				R.layout.lzl_page_0,
				new String[] {"text","floor","author","time","lzl"},
				new int[] {R.id.text,R.id.floor,R.id.author,R.id.time,R.id.lzl});
			if (fontsize==1) {
				adapter=new SimpleAdapter(this, maps,
						R.layout.lzl_page_1,
				new String[] {"text","floor","author","time","lzl"},
				new int[] {R.id.text,R.id.floor,R.id.author,R.id.time,R.id.lzl});
			}
			adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
				
				@Override
				public boolean setViewValue(View arg0, Object arg1, String arg2) {
					if (arg1 instanceof Spanned && arg0 instanceof TextView)
						((TextView)arg0).setText((Spanned)arg1);
					else
						((TextView)arg0).setText((String)arg1);
					return true;
				}
			});
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						lzlid=Integer.parseInt(floormap[(int)arg3]);
						author=authormap[(int)arg3];
						text=textmap[(int)arg3].toString();
						listView.showContextMenu();
				}
			});
			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					lzlid=Integer.parseInt(floormap[(int)arg3]);
					author=authormap[(int)arg3];
					text=textmap[(int)arg3].toString();
					return false;
				}
			});
			this.registerForContextMenu(listView);
			setContentView(listView);
			if (serial.equals("")) {
				savedpage=page;
			}
			return;			
		}
		if (loadingtype==LOADING_LZL_POST) {
			loadingtype=LOADING_NONE;
			Toast.makeText(this, hints, Toast.LENGTH_SHORT).show();
			lzlDialog.dismiss();
			author="";
			showlzl(fid, Integer.parseInt(seq));
			return;
		}
		if (loadingtype==LOADING_LZL_DELETE) {
			loadingtype=LOADING_NONE;
			Toast.makeText(this, hints, Toast.LENGTH_SHORT).show();
			showlzl(fid, Integer.parseInt(seq));
			return;
		}
		if (loadingtype==LOADING_SEARCH_THREAD || loadingtype==LOADING_SEARCH_POST) {
			lastSearchType=(loadingtype==LOADING_SEARCH_THREAD)?0:1;
			loadingtype=LOADING_NONE;
			searching=1;saved=false;
			listMap.put("0search1", connectWeb);
			actionBar.setTitle("搜索结果（只显示前 100 条）");
			final ListView listView=new ListView(this);
			maps=new ArrayList<Map<String,Object>>();
			ListIterator<Contents> iterator=connectWeb.uList.listIterator();
			Map<String, Object> map=new HashMap<String, Object>();
			int i=0;
			serialmap=new String[100];
			floormap=new String[100];
			textmap=new Spanned[100];
			authormap=new String[100];
			fidmap=new String[100];
			titlemap=new String[100];
			while (iterator.hasNext()) {
				Contents contents=iterator.next();
				map=new HashMap<String, Object>();
				if (contents.ftext==null) continue;
				map.put("text", contents.getText());
				map.put("author", contents.getAuthor());
				map.put("time", contents.getTime());
				map.put("floor", contents.getFloor()+"楼");
				map.put("title", contents.getTitle());
				maps.add(map);
				textmap[i]=contents.getText();
				authormap[i]=contents.getAuthor();
				floormap[i]=contents.getFloor();
				fidmap[i]=contents.getId();
				serialmap[i]=contents.getSerial();
				titlemap[i]=contents.getTitle();
				i++;
			}
			SimpleAdapter adapter;
			if (lastSearchType==0) {
				adapter=new SimpleAdapter(this, maps,
					R.layout.show_page_2,
					new String[] {"text","author","time"},
					new int[] {R.id.text,R.id.author,R.id.time});
				if (fontsize==1) {
					adapter=new SimpleAdapter(this, maps,
							R.layout.show_page_3,
							new String[] {"text","author","time"},
							new int[] {R.id.text,R.id.author,R.id.time});
				}
			}
			else {
				adapter=new SimpleAdapter(this, maps,
						R.layout.search_post_0,
						new String[] {"title","text","floor","author","time"},
						new int[] {R.id.title,R.id.text,R.id.floor,R.id.author,R.id.time});
				if (fontsize==1) {
						adapter=new SimpleAdapter(this, maps,
								R.layout.search_post_1,
								new String[] {"title","text","floor","author","time"},
								new int[] {R.id.title,R.id.text,R.id.floor,R.id.author,R.id.time});
				}
			}
			adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
				
				@Override
				public boolean setViewValue(View arg0, Object arg1, String arg2) {
					if (arg1 instanceof Spanned && arg0 instanceof TextView)
						((TextView)arg0).setText((Spanned)arg1);
					else
						((TextView)arg0).setText((String)arg1);
					return true;
				}
			});
			listView.setAdapter(adapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						int page=1;
						int _bid=Integer.parseInt(fidmap[(int)arg3]);
						String _serial=serialmap[(int)arg3];
						
						if (lastSearchType==1) {
							String _floor=floormap[(int)arg3];
							int floor=Integer.parseInt(_floor);
							page=(floor-1)/12+1;
							int start=(page-1)*12+1;
							postoffset=floor-start;
						}
						
						showText(_bid,_serial,page,"");
				}
			});
			listView.setOnScrollListener(new AbsListView.OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView arg0, int arg1) {
					// TODO Auto-generated method stub
					if (arg1==AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
						int val=listView.getFirstVisiblePosition();
						searchoffset=val;
					}
				}
				
				@Override
				public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
					// TODO Auto-generated method stub
				}
			});
			this.registerForContextMenu(listView);
			setContentView(listView);
			listView.setSelection(searchoffset);
			return;
		}
	}
	
	public class ConnectWeb {
		List<Contents> uList=new ArrayList<Contents>();
		Contents contents=new Contents();
		boolean status;
		boolean loading=true;
		boolean hasnextpage;
		String pages="",replys="";
		String bid="",tid="",p="",fid="",id="",pid="";
		String user="",pwd="",code="",title="",text="";
		String sex="",qq="",email="",from="",intro="",sig="";
		String ask="",method="";
		String begintime="",endtime="";
		byte[] bt=null;
		String site="http://www.chexie.net/api/client.php";
		
		ConnectWeb(ConnectWeb cp) {
			uList=new ArrayList<Contents>(cp.uList);
			status=cp.status;
			loading=cp.loading;
			hasnextpage=cp.hasnextpage;
			pages=cp.pages;
			replys=cp.replys;			
		}
		
		ConnectWeb(int _fid) {
			ask="lzl";
			method="show";
			fid=""+_fid;
		}
		
		ConnectWeb(int _fid,String _text) {
			ask="lzl";
			method="post";
			fid=""+_fid;
			text=_text;
		}
		ConnectWeb(int _fid,int _lzlid) {
			ask="lzl";
			method="delete";
			fid=""+_fid;
			id=""+_lzlid;
		}
		
		ConnectWeb() {
			ask="main";
		}
		
		ConnectWeb(byte[] tt) {
			ask="image";
			bt=tt;
		}
		
		ConnectWeb(int _id,String _see,String page) {
			if (_id<=7) bid=Integer.toString(_id);
			else if (_id==8) bid="9";
			else bid="28";
			tid=_see;
			if (!deling) {
				p=page;
				ask="show";
			}
			else {
				ask="delete";
				pid=page;
			}
		}
		
		ConnectWeb(int _id,String _see,String page,String type) {
			if (_id<=7) bid=Integer.toString(_id);
			else if (_id==8) bid="9";
			else bid="28";
			tid=_see;
			pid="";
			ask="action";
			method=type;
		}
		
		ConnectWeb(String user,String psd,String code,String sex,String qq,String email,
				String from,String intro,String sig) {
			this.user=user;this.qq=qq;this.email=email;
			this.pwd=psd;this.from=from;this.intro=intro;
			this.sex=sex;this.sig=sig;this.code=code;
			ask="register";
		}
		
		ConnectWeb(String keyword,String type,String board,String begintime,
				String endtime,String author) {
			ask="search";
			user=author;
			bid=board;
			text=keyword;
			method=type;
			this.begintime=begintime;
			this.endtime=endtime;
		}
		
		ConnectWeb(String user,String pwd) {
			this.user=user;this.pwd=pwd;
			ask="login";
		}
		
		ConnectWeb(int _id,String serial,String title,String text,String floor,String sig) {
			if (_id<=7) bid=Integer.toString(_id);
			else if (_id==8) bid="9";
			else bid="28";
			ask="post";
			tid=serial;
			pid=floor;
			this.title=title;
			this.text=text;
			this.sig=sig;
		}
		
		public void sendRequest(RequestingTask requestingTask) {
			isRunning=true;
			editor.commit();
			status=false;
			try {
				Mademd5 mademd5=new Mademd5();
				HttpParams httpParams=new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParams, 4000);
				HttpConnectionParams.setSoTimeout(httpParams, 13000);
				HttpClient httpClient=new DefaultHttpClient(httpParams);
				HttpPost httpPost=new HttpPost(site);
				List<NameValuePair> paramsList=new ArrayList<NameValuePair>();
				paramsList.add(new BasicNameValuePair("ask",ask));
				paramsList.add(new BasicNameValuePair("method", method));
				paramsList.add(new BasicNameValuePair("token", token));
				paramsList.add(new BasicNameValuePair("username",user));
				if (pwd!="")
					paramsList.add(new BasicNameValuePair("password",mademd5.toMd5(pwd)));
				paramsList.add(new BasicNameValuePair("bid",bid));
				paramsList.add(new BasicNameValuePair("tid",tid));
				paramsList.add(new BasicNameValuePair("pid",pid));
				paramsList.add(new BasicNameValuePair("fid", fid));
				paramsList.add(new BasicNameValuePair("id", id));
				paramsList.add(new BasicNameValuePair("p",p));
				paramsList.add(new BasicNameValuePair("code",code));
				paramsList.add(new BasicNameValuePair("sex",sex));
				paramsList.add(new BasicNameValuePair("mail",email));
				paramsList.add(new BasicNameValuePair("qq",qq));
				paramsList.add(new BasicNameValuePair("from",from));
				paramsList.add(new BasicNameValuePair("intro",intro));
				paramsList.add(new BasicNameValuePair("sig",sig));
				paramsList.add(new BasicNameValuePair("title",title));
				paramsList.add(new BasicNameValuePair("text",text));
				paramsList.add(new BasicNameValuePair("type", method));
				paramsList.add(new BasicNameValuePair("starttime", begintime));
				paramsList.add(new BasicNameValuePair("endtime", endtime));
				paramsList.add(new BasicNameValuePair("os", "android"));
				if (loadingtype==LOADING_LOGIN || loadingtype==LOADING_REGISTER) {
					paramsList.add(new BasicNameValuePair("device", android.os.Build.MODEL));
					paramsList.add(new BasicNameValuePair("version", android.os.Build.VERSION.RELEASE));
				}
				if (loadingtype==LOADING_UPLOAD_IMAGE)
				{
					String imageString=Base64.encodeToString(bt, Base64.DEFAULT);
					if (imageString.length()>1024*1024) {hints="图片过大";loading=false;return;}
					paramsList.add(new BasicNameValuePair("image", imageString));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(paramsList, "utf-8"));
				user="";pwd="";
				HttpResponse httpResponse=httpClient.execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode()==200)	{
					if (!isRunning) {
						hints="请求已取消";
						loading=false;
						return;
					}
					BufferedReader bf=new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
					String string="";
					status=true;
					String line=bf.readLine();
					while (line!=null) {
						string=string+line+"\n";
						line=bf.readLine();
					}
					getContent(string);
				}
				else hints="服务器请求失败 (HTTP: "+httpResponse.getStatusLine().getStatusCode()+")";
			}
			catch (IOException e) {
				e.printStackTrace();
				hints="连接超时，请稍后重试";
			}
			finally {
				loading=false;
			}
		}
		
		public List<Contents> getContent(String str) {
			try {
				uList=new ArrayList<Contents>();
				RootElement root=new RootElement("capu");
				Element item=root.getChild("info");
				item.getChild("code").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						if (arg0.equals("-1")) login=true;
						else if (arg0.equals("0")) {
							login=true;
							if (loadingtype!=LOADING_LOGIN)
							{
								if (loadingtype==LOADING_DELETE || loadingtype==LOADING_LZL_DELETE)
									hints="删除成功";
								else if (loadingtype==LOADING_ACTION) hints="操作成功";
								else if (loadingtype==LOADING_LZL_POST) hints="回复成功";
								else if (!seq.equals("")) hints="编辑成功";
								else if (serial.equals("")) hints="发表成功";
								else hints="回复成功";
							}
							else
								hints="登录成功";
						}
						else if (hints=="") {
							if (arg0.equals("-25"))
								hints="超时，请重新登录 (errorcode: -1)";
							else if (arg0.equals("1"))
								hints="密码错误 (errorcode: 1)";
							else if (arg0.equals("2"))
								hints="用户不存在 (errorcode: 2)";
							else if (arg0.equals("3"))
								hints="您已被封禁 (errorcode: 3)";
							else if (arg0.equals("4"))
								hints="两次发表时间差不能少于15s (errorcode: 4)";
							else if (arg0.equals("5"))
								hints="文章已被锁定 (errorcode: 5)";
							else if (arg0.equals("6"))
							{
								if (loadingtype==LOADING_UPLOAD_IMAGE) hints="上传失败 (errorcode: 6)";
								else if (deling) hints="删除失败 (errorcode: 6)";
								else if (lzling) hints="回复失败 (errorcode: 6)";
								else if (!seq.equals("")) hints="编辑失败 (errorcode: 6)";
								else if (!serial.equals("")) hints="回复失败 (errorcode: 6)";
								else hints="发表失败 (errorcode: 6)";
							}
							else if (arg0.equals("7"))
								hints="只能编辑自己的帖子 (errorcode: 7)";
							else if (arg0.equals("8"))
								hints="用户名有非法字符 (errorcode: 8)";
							else if (arg0.equals("9"))
								hints="用户名已存在 (errorcode: 9)";
							else if (arg0.equals("10"))
								hints="权限不够 (errorcode: 10)";
							else if (arg0.equals("11"))
								hints="请直接删除主题 (errorcode: 11)";
						}
					}
				});
				item.getChild("msg").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						if (arg0!=null && arg0!="")
							hints=arg0;
					}
				});
				item.getChild("token").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						token=arg0;
					}
				});
				item.getChild("top").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						if (arg0.equals("1")) contents.settop(true);
					}
				});
				item.getChild("lock").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						if (arg0.equals("1")) contents.setlock(true);
					}
				});
				item.getChild("extr").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						if (arg0.equals("1")) contents.setextr(true);
					}
				});
				item.getChild("nextpage").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						if (arg0.equals("true")) hasnextpage=true;
						else hasnextpage=false;
					}
				});
				item.getChild("pages").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						pages=arg0;
					}
				});
				item.getChild("updatetext").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						updateText=arg0;
					}
				});
				item.getChild("updateurl").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						updateURL=arg0;
					}
				});
				item.getChild("replys").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						replys=arg0;
					}
				});
				item.getChild("updatetime").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						newUpdateTime=arg0;
					}
				});
				item.getChild("floor").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						contents.setFloor(arg0);
					}
				});
				item.getChild("lzl").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						contents.setlzl(arg0);
					}
				});
				item.getChild("fid").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						contents.setfid(arg0);
					}
				});
				item.getChild("author").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String string) {
						contents.setAuthor(string);
					}
				});
				item.getChild("time").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						contents.setTime(arg0);
					}
				});
				item.getChild("text").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						contents.setText(arg0);
					}
				});
				item.getChild("page").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						contents.setPage(arg0);
					}
				});
				item.getChild("tid").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						contents.setSerial(arg0);
					}
				});
				item.getChild("imgurl").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String arg0) {
						imgurl=arg0;
					}
				});
				item.getChild("title").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String body) {
						tmptitle=body;
						contents.setTitle(body);
					}
				});
				item.getChild("bid").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String body) {
						contents.setId(body);
					}
				});
				item.getChild("id").setEndTextElementListener(new EndTextElementListener() {
					
					@Override
					public void end(String body) {
						contents.setId(body);
					}
				});
				item.setEndElementListener(new EndElementListener() {
					
					@Override
					public void end() {
						if (!isRunning) {
							hints="请求已取消";
							loading=false;
							return;
						}
						if (contents.ftext!=null) contents.trans();
						uList.add(contents);
						contents=new Contents();
					}
				});
				Xml.parse(new ByteArrayInputStream(str.getBytes()), Xml.Encoding.UTF_8, root.getContentHandler());
			} catch (Exception e) {
				e.printStackTrace();
				hints="帖子不存在或内容错误 (errorcode: 6)";
			}
			return uList;
		}
	}
	
	public boolean findCache() {
		ConnectWeb tempConnectWeb=listMap.get(showId+showSerial+showPage);
		if (tempConnectWeb!=null) {
			connectWeb=tempConnectWeb;
			return true;
		}
		return false;
	}
	
	public void jumppage() {
		final Dialog dialog=new Dialog(this);
		dialog.setContentView(R.layout.jump_page);
		dialog.setTitle("直接跳转到=>");
		dialog.setCancelable(true);
		dialog.show();
		Spinner spinner=(Spinner)dialog.findViewById(R.id.pageselect);
		dialog.show();
		List<String> lst=new ArrayList<String>();
		lst.add("1");
		String string="当前第"+page+"页";
		if (!connectWeb.pages.equals("")) {
			string+="，共"+connectWeb.pages+"页";
			int j=Integer.parseInt(connectWeb.pages);
			for (int l=2;l<=j;l++)
				lst.add(l+"");
		}
		ArrayAdapter<String> adapter2=
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lst);
		spinner.setAdapter(adapter2);
		selectid=1;
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onNothingSelected(AdapterView<?> arg0) {
				selectid=1;
			}
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				selectid=arg2+1;
			}
		});
		((TextView)dialog.findViewById(R.id.jumpppagehint)).setText(string);
		Button yesbutton=(Button)dialog.findViewById(R.id.jumpp);
		yesbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
				showText(id, serial, selectid, "");
			}
		});
		Button nobutton=(Button)dialog.findViewById(R.id.cancell);
		nobutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}
	
	public void addURL(View view) {
		EditText editText=(EditText)findViewById(R.id.retext);
		final Editable editable=editText.getEditableText();
		final Dialog dialog=new Dialog(this);
		dialog.setTitle("插入超链接");
		dialog.setContentView(R.layout.add_url);
		Button addurl=(Button)dialog.findViewById(R.id.urladdbtn);
		addurl.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				String urrl=((EditText)dialog.findViewById(R.id.urladdress)).getEditableText().toString();
				if ("".equals(urrl)) {
					Toast.makeText(CAPUBBS.this, "请输入要插入的链接地址", Toast.LENGTH_SHORT).show();
					return;
				}
				else if (!urrl.startsWith("http://") && !urrl.startsWith("https://")
						&& !urrl.startsWith("ftp://")) {
					urrl="http://"+urrl;
					Toast.makeText(CAPUBBS.this, "已自动添加 \"http://\"", Toast.LENGTH_SHORT).show();
				}
				String urltext=((EditText)dialog.findViewById(R.id.urltext)).getEditableText().toString();
				if ("".equals(urltext)) editable.append("[url]"+urrl+"[/url]");
				else editable.append("[url="+urltext+"]"+urrl+"[/url]");
				dialog.dismiss();
				Toast.makeText(CAPUBBS.this, "超链接插入成功", Toast.LENGTH_SHORT).show();
			}
		});
		Button cancel=(Button)dialog.findViewById(R.id.urladdcancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.show();
	}
	
	public void addImage(View view) {
		Intent intent=new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, 1);
	}
	
	public void chooseImage(String imagePath) {
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inJustDecodeBounds=true;
		Bitmap bitmap=BitmapFactory.decodeFile(imagePath, options);
		int w=options.outWidth;int h=options.outHeight;
		float hh=800f;float ww=480f;
		int be=1;
		if (w>h && w>ww) be=(int)(w/ww);
		else if (w<h && h>hh) be=(int)(h/hh);
		else if (be<=0) be=1;
		options.inSampleSize=be;
		options.inJustDecodeBounds=false;
		bitmap=BitmapFactory.decodeFile(imagePath, options);
		int d=100;
		ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, d, byteArrayOutputStream);
		while (byteArrayOutputStream.toByteArray().length>102400) {
			byteArrayOutputStream.reset();
			d-=10;
			bitmap.compress(CompressFormat.JPEG, d, byteArrayOutputStream);
		}
		final byte[] bt=byteArrayOutputStream.toByteArray();
		File fl=new File(imagePath);
		final byte[] bts=new byte[(int)fl.length()];
		try {
			FileInputStream fileInputStream=new FileInputStream(imagePath);
			fileInputStream.read(bts);
			fileInputStream.close();
		} catch (IOException e) {}
		final ImageView imageView=new ImageView(this);
		imageView.setImageBitmap(bitmap);
		final String s="确认上传此图片？ ";
		new AlertDialog.Builder(this).setTitle(s)
		.setView(imageView).setPositiveButton("确认上传", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				new AlertDialog.Builder(CAPUBBS.this).setTitle("是否上传压缩图片？")
				.setMessage("原图的大小为："+FormatFileSize(bts.length)+"\n压缩后大小为："+
						FormatFileSize(bt.length)+"\n\n是否上传压缩后的图片？")
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						loadingtype=LOADING_UPLOAD_IMAGE;login=false;
						new RequestingTask(CAPUBBS.this, "图片上传中......")
						.execute(imageConnectWeb=new ConnectWeb(bt));
					}
				}).setNegativeButton("否", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						loadingtype=LOADING_UPLOAD_IMAGE;login=false;
						new RequestingTask(CAPUBBS.this, "图片上传中......")
						.execute(imageConnectWeb=new ConnectWeb(bts));
					}
				}).setCancelable(true).show();
			}
		})
		.setCancelable(true).setNegativeButton("重新选择", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				addImage(null);
			}
		}).setNeutralButton("放弃上传", null).show();
	}
	
	public void imageSendFinished() {
		loadingtype=LOADING_NONE;
		EditText editText=(EditText)findViewById(R.id.retext);
		Editable editable=editText.getEditableText();
		editable.append("[img]"+imgurl+"[/img]");
		new AlertDialog.Builder(this).setTitle("图片上传成功！")
		.setCancelable(false).setMessage("图片地址为："+imgurl+
				"\n\n图片已自动插入，请不要动 \"[img]\" 和 \"[/img]\" 之间的内容！\n\n" +
				"是否将图片地址复制到剪切板？").setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						imgurl="";
					}
				})
				.setPositiveButton("复制", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						android.content.ClipboardManager cpManager=(android.content.ClipboardManager)CAPUBBS.this.getSystemService(CLIPBOARD_SERVICE);
						cpManager.setPrimaryClip(ClipData.newPlainText("text", imgurl));
						Toast.makeText(CAPUBBS.this, "图片地址已复制到剪切板", Toast.LENGTH_SHORT).show();
						imgurl="";
					}
				}).show();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode==1 && resultCode==RESULT_OK) {
			Uri uri=data.getData();
			String[] proj={MediaStore.Images.Media.DATA};
			CursorLoader loader=new CursorLoader(this, uri, proj, null, null, null);
			Cursor cursor = loader.loadInBackground();
			int index=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			chooseImage(cursor.getString(index));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void showText(int _id,String _serial,int _page,String _title) {
		connectWeb=new ConnectWeb(_id,_serial,Integer.toString(_page));
		loadingtype=LOADING_SHOW;
		showId=_id;
		showPage=_page;
		if (_serial==null || _serial.length()==0) _serial="";
		showSerial=_serial;
		showTitle=_title;
		if (saved && findCache()) {
			connectWeb.status=true;
			login=true;
			saved=false;
			finishRequest();
		}
		else {
			if (_serial.equals("")) threadoffset=0;
			else 
				if (searching!=1)
					postoffset=0;
			sendRequest("获取数据中，请稍后......");
		}
	}
	
	public void showlzl(int _fid,int _pid) {
		connectWeb=new ConnectWeb(_fid);
		loadingtype=LOADING_LZL_SHOW;
		sendRequest("获取数据中，请稍后......");
	}
	
	public void thread_action(String type) {
		final String realtype=type;
		if (token.equals("")) {
			Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
			return;
		}
		String string="";
		if (type.equals("top")) {
			string="置顶";
			if (istop) string="取消置顶";
		}
		else if (type.equals("extr")) {
			string="加精";
			if (isextr) string="取消精品";
		}
		else if (type.equals("lock")) {
			string="锁定";
			if (islock) string="取消锁帖";
		}
		if (string.equals("")) return;
		
		new AlertDialog.Builder(this).setTitle("确认操作？").setMessage("确认对这个帖子进行"+string+"操作？")
			.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					loadingtype=LOADING_ACTION;
					connectWeb=new ConnectWeb(id, tmpserial, "", realtype);
					sendRequest("正在操作中，请稍后......");
				}
			}).setNegativeButton("取消", null).setCancelable(true).show();
	}
	
	public void addsearchview() {
		final Dialog dialog=new Dialog(this);
		dialog.setContentView(R.layout.add_search);
		dialog.setTitle("搜索");
		dialog.show();
		dialog.setCancelable(true);
		
		final Spinner search_board=(Spinner)dialog.findViewById(R.id.search_board);
		ArrayAdapter<String> adapter2=
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		search_board.setAdapter(adapter2);
		if (oldid==0) oldid=1;
		int nowboard=oldid-1;
		search_board.setSelection(nowboard);
		
		RadioButton typeButton=(RadioButton)dialog.findViewById(R.id.type_thread);
		typeButton.setChecked(true);
		
		final Spinner begintime_year=(Spinner)dialog.findViewById(R.id.search_begintime_year);
		final Spinner begintime_month=(Spinner)dialog.findViewById(R.id.search_begintime_month);
		final Spinner begintime_day=(Spinner)dialog.findViewById(R.id.search_begintime_day);
		final Spinner endtime_year=(Spinner)dialog.findViewById(R.id.search_endtime_year);
		final Spinner endtime_month=(Spinner)dialog.findViewById(R.id.search_endtime_month);
		final Spinner endtime_day=(Spinner)dialog.findViewById(R.id.search_endtime_day);
		List<String> yearlist=new ArrayList<String>();
		List<String> monthlist=new ArrayList<String>();
		List<String> daylist=new ArrayList<String>();
		Time time=new Time("GMT+8");time.setToNow();
		for (int i=2001;i<=time.year;i++) yearlist.add(i+"");
		for (int i=1;i<=12;i++) monthlist.add(i<10?"0"+i:""+i);
		for (int i=1;i<=31;i++) daylist.add(i<10?"0"+i:""+i);
		
		ArrayAdapter<String> yearAdapter=
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, yearlist);
		ArrayAdapter<String> monthAdapter=
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, monthlist);
		ArrayAdapter<String> dayAdapter=
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, daylist);
		begintime_year.setAdapter(yearAdapter);
		endtime_year.setAdapter(yearAdapter);
		begintime_month.setAdapter(monthAdapter);
		endtime_month.setAdapter(monthAdapter);
		begintime_day.setAdapter(dayAdapter);
		endtime_day.setAdapter(dayAdapter);
		
		begintime_year.setSelection(0);
		begintime_month.setSelection(0);
		begintime_day.setSelection(0);
		
		endtime_year.setSelection(time.year-2001);
		endtime_month.setSelection(time.month);
		endtime_day.setSelection(time.monthDay-1);
		
		Button addButton=(Button)dialog.findViewById(R.id.search_confirm_button);
		Button cancelButton=(Button)dialog.findViewById(R.id.search_cancel_button);
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		addButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String keyword=((EditText)dialog.findViewById(R.id.search_key)).getText().toString();
				String author=((EditText)dialog.findViewById(R.id.search_author)).getText().toString();
				String type="thread";
				RadioButton radioButton=(RadioButton)dialog.findViewById(R.id.type_post);
				if (radioButton.isChecked()) type="post";
				
				String board_id="";
				int search_id=search_board.getSelectedItemPosition()+1;
				if (search_id==8) search_id=9;
				else if (search_id==9) search_id=28;
				board_id=""+search_id;
				
				String begintime=
						(String)begintime_year.getSelectedItem()+"-"+(String)begintime_month.getSelectedItem()
						+"-"+(String)begintime_day.getSelectedItem();
				String endtime=
						(String)endtime_year.getSelectedItem()+"-"+(String)endtime_month.getSelectedItem()
						+"-"+(String)endtime_day.getSelectedItem();
				dialog.dismiss();
			
				if (type.equals("thread"))
					loadingtype=LOADING_SEARCH_THREAD;
				else
					loadingtype=LOADING_SEARCH_POST;
				connectWeb=new ConnectWeb(keyword, type, board_id, begintime, endtime, author);
				sendRequest("正在搜索......");
			}
		});
	}
	
	public void addlzlview() {
		if (token=="") {
			Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
			return;
		}
		lzlDialog=new Dialog(this);
		lzlDialog.setContentView(R.layout.add_lzl);
		lzlDialog.setTitle("回复"+seq+"楼的楼中楼");
		lzlDialog.show();
		lzlDialog.setCancelable(true);
		
		EditText editText=(EditText)lzlDialog.findViewById(R.id.lzlpost);
		if (author!="") {
			String content="回复 @"+author+"：";
			editText.setText(content);
			editText.setSelection(content.length());
		}
		
		Button addButton=(Button)lzlDialog.findViewById(R.id.lzladdbtn);
		Button cancelButton=(Button)lzlDialog.findViewById(R.id.lzladdcancel);
		addButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				EditText editText=(EditText)lzlDialog.findViewById(R.id.lzlpost);
				String string=editText.getText().toString();
				addlzlpost(string);
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				lzlDialog.dismiss();
			}
		});
	}
	
	public void addlzlpost(String text) {
		loadingtype=LOADING_LZL_POST;
		connectWeb=new ConnectWeb(fid, text);
		sendRequest("回复中，请稍后...");
	}
	
	public void deletelzl() {
		loadingtype=LOADING_LZL_DELETE;
		connectWeb=new ConnectWeb(fid, lzlid);
		sendRequest("删除中，请稍后...");
	}
	
	public void postView(String retitle,String retext) {
		if (token=="") {
			Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
			return;
		}
		setContentView(R.layout.post_page);
		invalidateOptionsMenu();
		replying=true;deling=false;
		String xString="发表";
		if (!serial.equals("")) xString="回复";
		actionBar.setTitle("\""+username+"\""+xString+"于: "+list.get(id-1));
		EditText titleText=(EditText)findViewById(R.id.retitle);
		EditText textText=(EditText)findViewById(R.id.retext);
		Button button=(Button)findViewById(R.id.submit);
		if (!serial.equals("")) button.setText("回复");
		else button.setText("发表");
		titleText.setText(retitle);
		textText.setText(retext);
	}
	
	public void sendPost(View view) {
		EditText retitle=(EditText)findViewById(R.id.retitle);
		EditText retext=(EditText)findViewById(R.id.retext);
		RadioButton sig1=(RadioButton)findViewById(R.id.sig1);
		RadioButton sig2=(RadioButton)findViewById(R.id.sig2);
		RadioButton sig0=(RadioButton)findViewById(R.id.sig0);
		String temptitle=retitle.getText().toString();
		String temptext=retext.getText().toString();
		String sigString;
		if (temptitle.equals("")) {
			Toast.makeText(this, "标题不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (temptext.equals("")) {
			Toast.makeText(this, "内容不能为空", Toast.LENGTH_SHORT).show();
			return;
		}
		if (temptitle.length()>40) {
			Toast.makeText(this, "标题过长", Toast.LENGTH_SHORT).show();
			return;
		}
		if (temptext.length()>65536) {
			Toast.makeText(this, "内容过长", Toast.LENGTH_SHORT).show();
			return;
		}
		if (sig0.isChecked()) sigString="0";
		else if (sig1.isChecked()) sigString="1";
		else if (sig2.isChecked()) sigString="2";
		else sigString="3";
		connectWeb=new ConnectWeb(id, serial, temptitle, temptext, seq,sigString);
		loadingtype=LOADING_POST;
		sendRequest("发表中，请稍后......");
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (issetting || replying || registering==1) return true;
		if (lzling) {
			menu.add(Menu.NONE, 15, 15, "回复").setIcon(R.drawable.talk).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}
		
		if (searching==1) {
			menu.add(Menu.NONE, 22, 22, "搜索").setIcon(R.drawable.search2).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			return true;
		}
		
		if (id!=0)
		{
			menu.add(Menu.NONE, 0, 0, "上一页").setIcon(R.drawable.pre).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, 1, 1, "下一页").setIcon(R.drawable.next).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add(Menu.NONE, 2, 2, "  刷新").setIcon(R.drawable.refresh);
			if (serial=="") menu.add(Menu.NONE, 3, 3, "  发表").setIcon(R.drawable.post);
			else menu.add(Menu.NONE, 3, 3, "  回复").setIcon(R.drawable.post);
			if (page==1)
				menu.getItem(0).setEnabled(false);
			if (connectWeb.hasnextpage==false)
				menu.getItem(1).setEnabled(false);
		}
		if (id!=0)
			menu.add(Menu.NONE, 4, 4, "  搜索").setIcon(R.drawable.search1);
		menu.add(Menu.NONE, 5, 5, "  跳转").setIcon(R.drawable.jump);
		if (id!=0)
			menu.add(Menu.NONE, 6, 6, "  跳页").setIcon(R.drawable.page);
		if (!serial.equals(""))
		{
			menu.add(Menu.NONE, 7, 7, "  删除").setIcon(R.drawable.del);
		}
		menu.add(Menu.NONE, 8, 8, "  设置").setIcon(R.drawable.setting);
		menu.add(Menu.NONE, 9, 9, "  退出").setIcon(R.drawable.login);
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		setIconEnable(menu, true);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		String tmp="";
		switch (item.getItemId()) {
			case android.R.id.home:
				if (ismainpage)
					init();
				else if (id==0 || issetting || replying)
					about();
				else if (serial=="") mainPage(null);
				else
					new AlertDialog.Builder(CAPUBBS.this).setMessage(actionBar.getTitle())
					.setCancelable(true).show();
				break;
			case 0:
				showText(id, serial, page-1, title);
				break;
			case 1:
				showText(id, serial, page+1, title);
				break;
			case 2:
				showText(id, serial, page, title);
				break;
			case 3:
				if (token=="") {
					Toast.makeText(CAPUBBS.this, "请先登录", Toast.LENGTH_SHORT).show();
					break;
				}
				if (!serial.equals(""))
					tmp="Re: "+title;
				seq="";
				postView(tmp, "");
				break;
			case 4: 
				oldid=id;oldpage=page;oldserial=serial;oldtitle=title;addsearchview();break;
			case 5: jump();
				break;
			case 6:jumppage();break;
			case 7:
				if (token=="") {
					Toast.makeText(CAPUBBS.this, "请先登录", Toast.LENGTH_SHORT).show();
					break;
				}
				new AlertDialog.Builder(this).setTitle("确认删除？").setMessage("您确认要删除这个主题吗？").
				setIcon(R.drawable.del).setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						del(serial, "");
					}
				}).setNegativeButton("取消",null).show();
				break;
			case 8:
				settings();
				break;
			case 9:
				exitActivity();
				break;
			case 15:
				addlzlview();
				break;
			case 22:addsearchview();break; 
		}
		return true;
	}
	
	public void exitActivity() {
		long temp=TrafficStats.getUidRxBytes(uid)+TrafficStats.getUidTxBytes(uid);
		if (temp!=TrafficStats.UNSUPPORTED) {
			totalStatistics+=temp-thisStatistics;
			thisStatistics=temp;
			editor.putLong("thisSta", thisStatistics);
			editor.putLong("totalSta", totalStatistics);
			editor.commit();
		}
		CAPUBBS.this.finish();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu,View v,ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (!lzling && !serial.equals("")) {
			menu.add(Menu.NONE,0,0,lzlContextMenuHintString);
		}
		menu.add(Menu.NONE, 1, 1, "复制");
		if (!lzling && !serial.equals(""))
		{
			menu.add(Menu.NONE, 2, 2, "引用");
			menu.add(Menu.NONE, 3, 3, "编辑");
		}
		if (lzling)
		{
			menu.add(Menu.NONE, 4, 4, "回复");
			if (!token.equals(""))
				menu.add(Menu.NONE, 5, 5, "删除");
		}
		else if (!token.equals(""))
			menu.add(Menu.NONE, 6, 6, "删除");
		if (!token.equals("") && !lzling && serial.equals("")) {
			String string="置顶";
			if (istop) string="取消"+string;
			menu.add(Menu.NONE, 7, 7, string);
			string="加精";
			if (isextr) string="取消精品";
			menu.add(Menu.NONE, 8, 8, string);
			string="锁定";
			if (islock) string="取消锁帖";
			menu.add(Menu.NONE, 9, 9, string);
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case 0:
				if (lzlContextMenuHintString.equals("回复楼中楼"))
					addlzlview();
				else 
					showlzl(fid, Integer.parseInt(seq));
				break;
			case 1:
				android.content.ClipboardManager cpManager=
					(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
				cpManager.setPrimaryClip(ClipData.newPlainText("text", text));
				Toast.makeText(this, "文本已复制到剪切板", Toast.LENGTH_SHORT).show();
				break;
			case 2:
				seq="";
				postView("Re: "+title, quotetext);
				break;
			case 3:
				if (!seq.equals("0")) postView("Re: "+title, text);
				else postView(title, text);
				break;
			case 4:
				addlzlview();
				break;
			case 5:
				new AlertDialog.Builder(this).setTitle("确认删除？").
				setMessage("你确认要删除 "+author+" 发表的楼中楼吗？").setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						deletelzl();
					}
				}).setNegativeButton("取消", null).setCancelable(true).show();
				break;
			case 6:
				String string="您确认要删除这一楼吗？";
				if (serial.equals(""))
				{
					string="您确认要删除这个主题吗？";
					seq="";
				}
				else
					tmpserial=serial;
				new AlertDialog.Builder(this).setTitle("确认删除？").setMessage(string).
				setIcon(R.drawable.del).setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						del(tmpserial, seq);
					}
				}).setNegativeButton("取消", null).show();
				break;
			case 7:
				thread_action("top");
				break;
			case 8:
				thread_action("extr");
				break;
			case 9:
				thread_action("lock");
				break;
		}
		lzlContextMenuHintString="";
		return super.onContextItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
			if (lzling) {
				exittime=0;
				lzling=false;
				saved=true;
				showText(id, serial, page, title);
			}
			else if (searching==2) {
				connectWeb=new ConnectWeb();
				if (lastSearchType==0) loadingtype=LOADING_SEARCH_THREAD;
				else loadingtype=LOADING_SEARCH_POST;
				showId=0;showSerial="search";showPage=1;
				findCache();
				connectWeb.status=true;
				login=true;
				finishRequest();
			}
			else if (searching==1) {
				exittime=0;
				searching=0;
				saved=true;
				threadoffset=0;
				postoffset=0;
				searchoffset=0;
				showText(oldid, oldserial, oldpage, oldtitle);
			}
			else if (issetting) {
				exittime=0;
				issetting=false;
				if (id==0) init();
				else
				{
					saved=true;
					showText(id, serial, page, title);
				}
			}
			else if (registering==1) {
				if (System.currentTimeMillis()-exittime>2000) {
					Toast.makeText(this, "再按一遍放弃注册", Toast.LENGTH_SHORT).show();
					exittime=System.currentTimeMillis();
				}
				else
				{
					exittime=0;
					init();
				}
			}
			else if (registering==2) {
				register(null);
			}
			else if (mainpaging==2) {
				init();
			}
			else if (mainpaging==1) {
				if (System.currentTimeMillis()-exittime>2000) {
					Toast.makeText(this, "再按一遍退出程序", Toast.LENGTH_SHORT).show();
					exittime=System.currentTimeMillis();
				}
				else
					exitActivity();
			}
			else if (replying) {
				if (System.currentTimeMillis()-exittime>2000) {
					Toast.makeText(this, "再按一遍放弃编辑", Toast.LENGTH_SHORT).show();
					exittime=System.currentTimeMillis();
				}
				else
				{
					exittime=0;
					saved=true;
					showText(id, serial, page, title);
				}
			}
			else if (id==0) {
				init();
			}
			else if (serial=="") {
				exittime=0;
				mainPage(null);
			}
			else {
				exittime=0;
				saved=true;
				showText(id, "", savedpage, "");
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void setIconEnable(Menu menu, boolean enable) {
		try {
			Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
			Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
			m.setAccessible(true);
			m.invoke(menu, enable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void settings() {
		invalidateOptionsMenu();
		setContentView(R.layout.setting_page);
		actionBar.setTitle("设置");
		issetting=true;

		Switch autoSwitch=(Switch)findViewById(R.id.autoLogin);
		autoSwitch.setTextOn("开启");autoSwitch.setTextOff("关闭");
		if (autoregister) autoSwitch.setChecked(true);
		else autoSwitch.setChecked(false);
		
		autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg0.isChecked()) {
					autoregister=true;
					editor.putBoolean("auto", true);
					editor.commit();
				}
				else {
					autoregister=false;
					editor.putBoolean("auto", false);
					editor.commit();
				}
			}
		});
		
		Switch fontSwitch=(Switch)findViewById(R.id.fontSize);
		fontSwitch.setTextOn("大号");fontSwitch.setTextOff("小号");
		if (fontsize!=0) fontSwitch.setChecked(true);
		else fontSwitch.setChecked(false);
		
		fontSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg0.isChecked()) {
					fontsize=1;
					editor.putInt("Font", 1);
				}
				else {
					fontsize=0;
					editor.putInt("Font", 0);
				}
				editor.commit();
			}
		});
		
		imageSwitch=(Switch)findViewById(R.id.imageView);
		imageSwitch.setTextOn("开启");imageSwitch.setTextOff("关闭");
		if (showImage) imageSwitch.setChecked(true);
		else imageSwitch.setChecked(false);
		
		imageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg0.isChecked())
					alert();
				else
					showImage=false;
			}
		});
		
		TableRow statisticsRow=(TableRow)findViewById(R.id.sta);
		statisticsRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				statistics();
			}
		});
		
		TableRow showinwebRow=(TableRow)findViewById(R.id.showinweb);
		showinwebRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String url="http://www.chexie.net/bbs/";
				if (id!=0) {
					if (serial!="") {
						url=url+"content/?bid="+id+"&tid="+serial+"&p="+page;
					}
					else url=url+"main/?bid="+id+"&p="+page;
				}
				else url=url+"index/";
				Uri uri=Uri.parse(url);
				Intent intent=new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		TableRow helpRow=(TableRow)findViewById(R.id.help);
		helpRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showText(4, "17637", 1, "");
			}
		});
		
		TableRow aboutRow=(TableRow)findViewById(R.id.about);
		aboutRow.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				about();
			}
		});
		
		TableLayout unregisterRow=(TableLayout)findViewById(R.id.setting_3);
		if (!username.equals("")) {
			unregisterRow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					unregister();
				}
			});
		}
		else
		{
			unregisterRow.setEnabled(false);
			TextView unregisterView=(TextView)findViewById(R.id.unregisText);
			unregisterView.setTextColor(Color.parseColor("#303030"));
		}
	}
	
	void alert() {
		ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
		if (networkInfo!=null && networkInfo.isAvailable()) {
			if (networkInfo.getType()!=ConnectivityManager.TYPE_WIFI) {
				new AlertDialog.Builder(CAPUBBS.this).setTitle("警告！")
				.setMessage("检测到你正在使用数据流量上网。\n你确定要打开图片显示吗？\n\n部分图片可能会非常大，大到超过1M！")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						showImage=true;
						imageSwitch.setChecked(true);
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						imageSwitch.setChecked(false);
						showImage=false;
					}
				}).setCancelable(false).show();
			}
			else
			{
				showImage=true;
				imageSwitch.setChecked(true);
			}
		}
		else {
			new AlertDialog.Builder(this).setTitle("提示！").setMessage("您未开启网络！")
			.setCancelable(true).setPositiveButton("关闭", null).show();
			imageSwitch.setChecked(false);
			showImage=false;
		}
	}
	
	public void about() {
		Time time=new Time("GMT+8");time.setToNow();
		String string="CAPUBBS客户端  ver1.6\n更新时间  "+updateTime+"\n\n开发者：维茨C\n"+
				"邮箱：ckcz123@126.com\n\n"+
				"协会论坛地址 www.chexie.net\n\ncopyright 2014.1-"+
				time.year+"."+(time.month+1);
		new AlertDialog.Builder(this).setTitle("关于本软件")
		.setMessage(string).setPositiveButton("关闭", null).show();
	}
	
	public String FormatFileSize(long fileS) {
		DecimalFormat df=new DecimalFormat("#.00");
		String fileSizeString="";
		if (fileS<1024) fileSizeString=df.format((double)fileS)+"B";
		else if (fileS<1048576) fileSizeString=df.format((double)fileS/1024)+"K";
		else if (fileS<1073741824) fileSizeString=df.format((double)fileS/1048576)+"M";
		else fileSizeString=df.format((double)fileS/1073741824)+"G";
		return fileSizeString;
	}
	
	public void statistics() {
		long temp=TrafficStats.getUidRxBytes(uid)+TrafficStats.getUidTxBytes(uid);
		if (temp==TrafficStats.UNSUPPORTED) {
			Toast.makeText(this, "您的手机不支持流量统计", Toast.LENGTH_SHORT).show();
			return;
		}
		long delta=temp-thisStatistics;
		long total=totalStatistics+delta;
		String s="本次耗费流量为 "+FormatFileSize(delta>=0?delta:0L);
		s+="\n总计耗费流量为 "+FormatFileSize(total>=0?total:0L);
		s+="\n数据仅供参考";
		new AlertDialog.Builder(this).setCancelable(true).setTitle("流量统计")
		.setMessage(s).setPositiveButton("关闭", null).show();
	}
	
	public void jump() {
		final Dialog dialog=new Dialog(this);
		dialog.setContentView(R.layout.jump_dialog);
		dialog.setTitle("直接跳转到=>");
		dialog.setCancelable(true);
		Spinner spinner=(Spinner)dialog.findViewById(R.id.boardselect);
		dialog.show();
		ArrayAdapter<String> adapter2=
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
		spinner.setAdapter(adapter2);
		selectid=1;
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onNothingSelected(AdapterView<?> arg0) {
				selectid=1;
			}
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				selectid=arg2+1;
			}
		});
		Button yesbutton=(Button)dialog.findViewById(R.id.jump);
		yesbutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				EditText editText=(EditText)dialog.findViewById(R.id.seeselect);
				String selectString=editText.getText().toString();
				if (selectString.length()!=0) {
					try {
						int x=Integer.parseInt(selectString);
						if (x<=0) throw new NumberFormatException();
					} catch (NumberFormatException e) {
						Toast.makeText(CAPUBBS.this, "请输入有效的帖子序号", Toast.LENGTH_SHORT).show();
					}
				}				
				if (selectString.length()==0) selectString="";
				dialog.dismiss();
				searching=mainpaging=registering=0;
				showText(selectid, selectString, 1, "");
			}
		});
		Button nobutton=(Button)dialog.findViewById(R.id.cancel);
		nobutton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
	}

	private void getOverflowMenu() {
		 
	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
}
