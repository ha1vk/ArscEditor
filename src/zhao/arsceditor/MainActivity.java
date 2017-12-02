/**
 *  Copyright 2015 ZhaoHai <2801045898@qq.com>
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author zhaohai
 * @time 2015.10
 * ARSC编辑器主界面
 * */
package zhao.arsceditor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import zhao.arsceditor.ResDecoder.ARSCCallBack;
import zhao.arsceditor.ResDecoder.data.ResTable;
import zhao.arsceditor.Translate.DoTranslate;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnItemLongClickListener {

	// 存储字符串的集合
	public List<String> txtOriginal = new ArrayList<String>();
	// 存储修改后的字符串的集合
	public List<String> txtTranslated = new ArrayList<String>();
	// 存储字符串在资源中对应的键
	public List<String> txtTranslated_Key = new ArrayList<String>();
	// 列表控件
	public ListView stringListView;
	// 数据处理器
	public stringListAdapter mAdapter;
	// 存储资源Configs的集合
	public static List<String> Configs;
	// 存储资源种类的集合
	public static List<String> Types;
	// 存储资源的集合
	private List<ContentValues> RESOURCES = new ArrayList<ContentValues>();
	// 显示资源种类的文本控件
	private TextView textCategory;
	// 显示资源Config的文本框
	private TextView textConfig;
	// 用于在列表顶部显示信息的TextView控件
	private TextView info;
	// 翻译按钮
	private ImageView btnTranslate;
	// 搜索按钮
	private ImageView btnSearch;
	// 保存按钮
	private ImageView btnSave;
	// ARSC解析器
	private AndrolibResources mAndRes;
	// 字符串是否修改
	public boolean isChanged = false;
	// 资源类型
	private int ResType;
	// 资源类型常量
	public static final int ARSC = 0, AXML = 1, DEX = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置主界面布局文件
		setContentView(R.layout.string_list);
		// 初始化列表控件
		stringListView = (ListView) findViewById(R.id.list_res_string);
		// 初始化显示资源类型的文本框
		textCategory = (TextView) findViewById(R.id.textCategory);
		// 初始化显示资源Config的文本框
		textConfig = (TextView) findViewById(R.id.textConfig);
		// 初始化翻译按钮
		btnTranslate = (ImageView) findViewById(R.id.btnTranslate);
		// 初始化搜索按钮
		btnSearch = (ImageView) findViewById(R.id.btnSearch);
		// 初始化保存按钮
		btnSave = (ImageView) findViewById(R.id.btnSave);
		// 获取用来显示信息的文本框
		info = (TextView) findViewById(R.id.info);
		// 为显示资源类型的文本框设置点击事件的监听器
		textCategory.setOnClickListener(MyOnClickListener);
		// 为显示资源Config的文本框设置点击事件的监听器
		textConfig.setOnClickListener(MyOnClickListener);
		// 为显示资源类型的文本框设置文本内容改变的监听器
		textCategory.addTextChangedListener(textWatcher);
		// 为显示资源Config的文本框设置文本内容改变的监听器
		textConfig.addTextChangedListener(textWatcher);
		// 为翻译按钮设置点击事件监听器
		btnTranslate.setOnClickListener(MyOnClickListener);
		// 为搜索按钮设置点击事件监听器
		btnSearch.setOnClickListener(MyOnClickListener);
		// 为保存按钮设置点击事件监听器
		btnSave.setOnClickListener(MyOnClickListener);
		// 初始化数据适配器
		mAdapter = new stringListAdapter(this);
		// 为列表控件设置数据适配器
		stringListView.setAdapter(mAdapter);
		// 为列表控件设置长按事件监听器
		stringListView.setOnItemLongClickListener(this);
		try {
			open("/sdcard/resources.arsc");
		} catch (IOException e) {
			showMessage(this, e.toString()).show();
		}
	}

	// 列表项目长按事件处理
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {

		if (textCategory.getText().toString().equals("id")) {
			return false;
		}
		// 弹出一个对话框，显示一个翻译选项
		new AlertDialog.Builder(this).setItems(R.array.translate, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// 初始化翻译
				DoTranslate translateTask = new DoTranslate(txtOriginal, txtTranslated, false, MainActivity.this);
				// 开启翻译
				translateTask.init(arg2);
			}
		}).create().show();

		return true;
	}

	// 一些控件的点击事件监听器
	private OnClickListener MyOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			// 点击了翻译按钮
			case R.id.btnTranslate:
				if (textCategory.getText().toString().equals("id")) {
					Toast.makeText(MainActivity.this, R.string.can_not_edit, Toast.LENGTH_LONG).show();
					return;
				}
				// 初始化翻译
				DoTranslate translate = new DoTranslate(txtOriginal, txtTranslated, true, MainActivity.this);
				// 开启翻译·
				translate.init(0);
				break;
			// 点击了搜索按钮
			case R.id.btnSearch:
				// 初始化搜索
				SearchString searchTask = new SearchString(MainActivity.this, txtOriginal);
				// 开启搜索
				searchTask.search();
				break;
			// 点击了保存按钮
			case R.id.btnSave:
				// 创建一个线程用来保存资源文件
				SaveFileTask saveTask = new SaveFileTask();
				// 执行该线程
				saveTask.execute("/sdcard/1.arsc");
				break;
			// 点击了资源类型的文本框
			case R.id.textCategory:
				// 弹出一个对话框，列出所有的资源类型
				new AlertDialog.Builder(MainActivity.this).setTitle("")
						.setItems((String[]) Types.toArray(new String[Types.size()]),
								new DialogInterface.OnClickListener() {
									// 对话框上的条目点击的事件监听器
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										// TODO Auto-generated method stub
										textCategory.setText(Types.get(arg1));
									}
								})
						.create().show();
				break;
			// 点击了资源Config的文本框
			case R.id.textConfig:
				// 弹出一个对话框，列出所有的资源Config
				new AlertDialog.Builder(MainActivity.this).setTitle("")
						.setItems((String[]) Configs.toArray(new String[Configs.size()]),
								new DialogInterface.OnClickListener() {
									// 对话框上的条目点击的事件监听器
									@Override
									public void onClick(DialogInterface arg0, int arg1) {
										// TODO Auto-generated method stub
										textConfig.setText(Configs.get(arg1));
									}
								})
						.create().show();
				break;
			}
		}
	};

	private void open(String resFile) throws IOException {
		if (resFile.endsWith(".arsc")) {
			open(new FileInputStream(resFile), ARSC);
		} else if (resFile.endsWith(".xml")) {
			open(new FileInputStream(resFile), AXML);
		} else if (resFile.endsWith(".dex")) {
			open(new FileInputStream(resFile), DEX);
		} else {
			throw new IOException("Unsupported FileType");
		}
	}

	private void open(InputStream resInputStream, int resType) {
		// 初始化一个线程用来解析资源文件
		AsyncTask<InputStream, Integer, String> task = new ParseTask();
		try {
			// 开启该线程
			task.execute(resInputStream);
			ResType = resType;
		} catch (OutOfMemoryError e) {
			showMessage(this, getString(R.string.out_of_memory)).show();
		}
		// 初始化一个线程用来获取解析后的资源
		AsyncTask<String, Integer, String> getTask = new GetTask();
		// 开启该线程
		getTask.execute(textCategory.getText().toString(), textConfig.getText().toString());
	}

	/**
	 * 文本框内容改变的事件监听器
	 * 
	 * @author zhaohai
	 */
	private TextWatcher textWatcher = new TextWatcher() {

		// 文本改变后的事件处理
		@Override
		public void afterTextChanged(Editable s) {
			// 初始化一个线程用来获取资源
			AsyncTask<String, Integer, String> task = new GetTask();
			// 开启该线程
			task.execute(textCategory.getText().toString(), textConfig.getText().toString());
		}

		// 文本改变之前的事件处理
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
			Log.d("TAG", "beforeTextChanged--------------->");
		}

		// 文本改变的事件处理
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

	};

	/**
	 * 一个用来获取解析后的资源的线程
	 * 
	 * @author zhaohai
	 */
	class GetTask extends AsyncTask<String, Integer, String> {
		// 进度条
		private ProgressDialog dlg;

		// 耗时任务开始前执行的任务
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// 初始化进度条
			dlg = new ProgressDialog(MainActivity.this);
			// 设置标题
			dlg.setTitle(R.string.parsing);
			// 设置按返回进度条不消失
			dlg.setCancelable(false);
			// 显示进度条
			dlg.show();

			// 如果储存Config的列表未初始化
			if (Configs == null) {
				// 初始化Config列表
				Configs = new ArrayList<String>();
			}

			// 检查是否发生改变
			for (String str : txtTranslated) {
				if (!str.equals(""))
					isChanged = true;
				break;
			}

			if (isChanged) {
				// 排序整理修改后的内容，以方便一一写入
				for (int i = 0; i < txtOriginal.size(); i++)
					mAndRes.mARSCDecoder.mTableStrings.sortStringBlock(txtOriginal.get(i), txtTranslated.get(i));
			}

			// 清除几个列表中的元素
			txtOriginal.clear();
			txtTranslated.clear();
			txtTranslated_Key.clear();
			Configs.clear();
		}

		// 执行耗时任务
		@Override
		protected String doInBackground(String... params) {
			switch (ResType) {

			case ARSC:
				for (ContentValues resource : RESOURCES) {
					// 获取资源的键
					String NAME = (String) resource.get(MyObj.NAME);
					// 获取资源的值
					String VALUE = (String) resource.get(MyObj.VALUE);
					// 获取资源类型
					String TYPE = (String) resource.get(MyObj.TYPE);
					// 获取资源分支
					String CONFIG = (String) resource.get(MyObj.CONFIG);

					// 如果资源的Config开头存在-符号，并且Config列表中不存在该资源的Config元素，并且资源种类是params[0]的值
					if (CONFIG.startsWith("-") && !Configs.contains(CONFIG.substring(1)) && TYPE.equals(params[0]))
						// 向Config列表中添加元素
						Configs.add(CONFIG.substring(1));
					// 如果资源的Config开头不存在-符号，并且Config列表中不存在该资源的Config元素，并且资源种类是params[0]的值
					else if (!CONFIG.startsWith("-") && !Configs.contains(CONFIG) && TYPE.equals(params[0]))
						Configs.add(CONFIG);

					// 如果资源的Config开头存在-符号，并且Config列表中存在该资源的Config元素，并且Config是params[1]的值
					if (TYPE.equals(params[0]) && CONFIG.startsWith("-") && CONFIG.substring(1).equals(params[1])) {
						// 向储存字符串的列表中添加字符串成员
						txtOriginal.add(VALUE);
						// 向储存修改后的字符串的列表中添加空成员
						txtTranslated.add("");
						// 向储存资源的键的列表添加键
						txtTranslated_Key.add(NAME);
						// 如果资源的Config开头不存在-符号，并且Config列表中存在该资源的Config元素，并且Config是params[1]的值
					} else if (TYPE.equals(params[0]) && !CONFIG.startsWith("-") && CONFIG.equals(params[1])) {
						// 向储存字符串的列表中添加字符串成员
						txtOriginal.add(VALUE);
						// 向储存修改后的字符串的列表中添加空成员
						txtTranslated.add("");
						// 向储存资源的键的列表添加键
						txtTranslated_Key.add(NAME);
					}
				}
				break;
			case AXML:
				try {
					mAndRes.mAXMLDecoder.getStrings(txtOriginal);
					for (int i = 0; i < txtOriginal.size(); i++) {
						// 向储存修改后的字符串的列表中添加空成员
						txtTranslated.add("");
						// 向储存资源的键添加空成员
						txtTranslated_Key.add("");
					}
				} catch (CharacterCodingException e) {
					return e.toString();
				}
				break;
			case DEX:

				break;
			}
			// 返回一个成功的标志
			return getString(R.string.success);
		}

		// 耗时任务执行完毕后的事件处理
		@Override
		protected void onPostExecute(String result) {
			// 隐藏进度条
			dlg.dismiss();
			// 如果收到的返回值不是成功的标志
			if (!result.equals(getString(R.string.success))) {
				// 则显示错误详情
				showMessage(MainActivity.this, result).show();
				return;
			} else if (result.equals(getString(R.string.success)) && txtOriginal.size() == 0) // 如果收到成功的标志，并且字符串列表没有成员，说明资源列表中不存在这样的成员
			{
				if (Configs.size() != 0)
					// 那么就获取默认的资源
					textConfig.setText(
							Configs.contains("[DEFAULT]") ? Configs.get(Configs.indexOf("[DEFAULT]")) : Configs.get(0));
			}
			// 对Config列表进行排序
			Collections.sort(Configs);
			//
			// Collections.sort(txtOriginal);
			// 通知数据适配器更新数据
			mAdapter.notifyDataSetInvalidated();
		}

	}

	// 一个储存键的类
	class MyObj {
		public final static String NAME = "name";
		public final static String VALUE = "value";
		public final static String TYPE = "type";
		public final static String CONFIG = "config";
	}

	/**
	 * @author zhaohai 一个用来解析ARSC的线程
	 */
	class ParseTask extends AsyncTask<InputStream, Integer, String> {
		// 资源回调接口
		private ARSCCallBack callback;
		// 进度条
		private ProgressDialog dlg;
		// 创建values值对象
		private ContentValues values = null;

		// 耗时任务开始前执行的任务
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// 初始化ARSC解析器
			mAndRes = new AndrolibResources(MainActivity.this);
			// 初始化进度条
			dlg = new ProgressDialog(MainActivity.this);
			// 设置进度条标题
			dlg.setTitle(R.string.parsing);
			// 设置按进度条外部进度条不消失
			dlg.setCancelable(false);
			// 显示进度条
			dlg.show();

			// 如果储存资源类型的列表未初始化
			if (Types == null) {
				// 初始化储存资源类型的列表
				Types = new ArrayList<String>();
			}

			// 实现资源回调接口
			callback = new ARSCCallBack() {
				// 进度值
				int i = 0;

				@Override
				public void back(String config, String type, String key, String value) {
					// 这里是为了出去一些不能编辑的字符串
					if (type != null) {
						// 初始化键值映射
						values = new ContentValues();
						// 向映射中添加资源的键
						values.put(MyObj.NAME, key);
						// 向映射中添加资源的值
						values.put(MyObj.VALUE, value);
						// 向映射中添加资源的种类
						values.put(MyObj.TYPE, type);
						// 向映射中添加资源的Config
						values.put(MyObj.CONFIG, config);
						// 向资源中添加该映射
						RESOURCES.add(values);
					}
					// 如果资源种类集合中不存在该种类
					if (!Types.contains(type))
						// 向其中添加该种类
						Types.add(type);
					// 进度值加1
					i++;
					// 更新进度条
					publishProgress(i);
				}
			};
		}

		// 获取ARSC文件的ResTable的方法
		public ResTable getResTable(InputStream ARSCStream) throws IOException {
			return mAndRes.getResTable(ARSCStream);
		}

		// 执行耗时任务
		@Override
		protected String doInBackground(InputStream... params) {
			try {
				switch (ResType) {
				case ARSC:
					// 解析ARSC
					mAndRes.decodeARSC(getResTable(params[0]), callback);
					break;
				case AXML:
					// 解析ARSC
					mAndRes.decodeAXML(params[0]);
					break;
				}
			} catch (Exception e) {
				return e.toString();
			}
			return getString(R.string.success);
		}

		// 更新ui界面
		@Override
		protected void onProgressUpdate(Integer... values) {
			dlg.setMessage(String.valueOf(values[0]));
		}

		// 耗时任务执行完毕后的事件处理
		@Override
		protected void onPostExecute(String result) {
			// 隐藏进度条
			dlg.dismiss();
			// 如果返回的结果不是成功
			if (!result.equals(getString(R.string.success))) {
				// 显示错误信息
				showMessage(MainActivity.this, result).show();
				return;
			}
			// 对资源种类列表排序
			Collections.sort(Types);
		}

	}

	/**
	 * @author zhaohai 一个用来保存资源文件的线程
	 */
	class SaveFileTask extends AsyncTask<String, String, String> {
		// 进度条
		private ProgressDialog dlg;

		// 耗时任务开始前执行的任务
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// 初始化进度条
			dlg = new ProgressDialog(MainActivity.this);
			// 设置进度条标题
			dlg.setTitle(R.string.saving);
			// 设置按进度条外部进度条不消失
			dlg.setCancelable(false);
			// 显示进度条
			dlg.show();
		}

		// 执行耗时任务
		@Override
		protected String doInBackground(String... params) {
			try {
				switch (ResType) {
				case ARSC:
					// 创建新文件输出流
					FileOutputStream fo1 = new FileOutputStream(params[0]);
					mAndRes.mARSCDecoder.write(fo1, txtOriginal, txtTranslated);
					fo1.close();
					break;
				case AXML:
					// 创建新文件输出流
					FileOutputStream fo2 = new FileOutputStream(params[0]);
					mAndRes.mAXMLDecoder.write(txtOriginal, txtTranslated, fo2);
					fo2.close();
					break;
				case DEX:
					break;
				}
			} catch (IOException e) {
				return e.toString();
			} catch (OutOfMemoryError e) {
				return getString(R.string.out_of_memory);
			}
			return getString(R.string.success);
		}

		// 耗时任务执行完毕后的事件处理
		@Override
		protected void onPostExecute(String result) {
			// 隐藏进度条
			dlg.dismiss();
			// 如果返回的结果不是成功
			if (!result.equals(getString(R.string.success))) {
				// 显示错误信息
				showMessage(MainActivity.this, result).show();
				return;
			}
			// 还原是否改变的标志
			isChanged = false;
		}

	}

	// 数据适配器
	public class stringListAdapter extends BaseAdapter {

		// 上下文
		private Context mContext;
		// 显示原来的字符串内容的控件
		private TextView txtOriginalView;
		// 用于修改的文本框控件
		private EditText txtTranslatedView;

		// 构造函数
		public stringListAdapter(Context context) {
			super();
			// 获取上下文
			this.mContext = context;
		}

		// 获取数据成员个数
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return txtOriginal.size();
		}

		// 获取指定条目的内容
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		// 获取指定条目的文字
		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		// 文本框点击是事件监听器
		OnTouchListener touch = new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_UP)
					Toast.makeText(MainActivity.this, R.string.can_not_edit, Toast.LENGTH_LONG).show();
				return false;
			}
		};

		// 获取View
		@SuppressLint({ "ViewHolder", "InflateParams" })
		@Override
		public View getView(final int position, View view, ViewGroup arg2) {

			// 文本框内容改变的事件监听器
			TextWatcher textWatcher = new TextWatcher() {

				// 文本改变后的事件处理
				@Override
				public void afterTextChanged(Editable s) {

				}

				// 文本改变之前的事件处理
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					// TODO Auto-generated method stub
					Log.d("TAG", "beforeTextChanged--------------->");
				}

				// 文本改变的事件处理
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// 从集合中移除该条目对应的空白内容
					txtTranslated.remove(position);
					// 向当前位置添加新的内容，以此实现文本的更新
					txtTranslated.add(position, s.toString());
				}

			};

			// 创建view对象
			view = LayoutInflater.from(mContext).inflate(R.layout.res_string_item, null);
			// 获取显示原来的字符串的控件
			txtOriginalView = (TextView) view.findViewById(R.id.txtOriginal);
			// 获取用来修改的文本框
			txtTranslatedView = (EditText) view.findViewById(R.id.txtTranslated);

			// 如果选中了style资源，则显示没有可供编辑的字符串的提示
			if (textCategory.getText().toString().equals("style")) {
				// 隐藏列表
				stringListView.setVisibility(View.INVISIBLE);
				// 显示一个控件用来提示
				info.setVisibility(View.VISIBLE);
				// 显示文字
				info.setText(R.string.no_strings_for_editing);
				return view;
			} else {
				// 隐藏显示信息的控件
				info.setVisibility(View.GONE);
				// 显示列表
				stringListView.setVisibility(View.VISIBLE);
			}
			// 如果选中了id资源，则设置文本框不可编辑，因为id不能随意编辑
			if (textCategory.getText().toString().equals("id")) {
				txtTranslatedView.setFocusable(false);
				txtTranslatedView.setOnTouchListener(touch);
			}
			// 显示原来的字符串
			txtOriginalView.setText(txtOriginal.get(position));
			// 显示修改后的字符串
			txtTranslatedView.setText(txtTranslated.get(position));
			// 为文本框设置底层的显示内容
			txtTranslatedView.setHint(txtTranslated_Key.get(position));
			// 为文本框设置内容改变的监听器
			txtTranslatedView.addTextChangedListener(textWatcher);
			return view;
		}
	}

	// 显示信息的方法
	public static AlertDialog.Builder showMessage(Context activity, String message) {
		return new AlertDialog.Builder(activity).setMessage(message).setNegativeButton(R.string.ok, null)
				.setCancelable(false).setTitle(R.string.error);
	}
}
