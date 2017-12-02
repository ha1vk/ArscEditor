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
package zhao.arsceditor;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.EditText;

//本类实现在字符常量池中搜索字符串
@SuppressWarnings("deprecation")
public class SearchString {
	// 进度条控件
	private ProgressDialog mdialog;
	// 上下文
	private Context mContext;
	// 在某个集合中搜索
	private List<String> listitems;
	// 搜索结果
	private List<String> listresult = new ArrayList<String>();
	// 储存搜索的结果中每一个条目在原来的集合中的位置
	private List<Integer> listposition = new ArrayList<Integer>();

	// 构造函数
	public SearchString(Context context, List<String> listitems) {
		// 获取上下文
		mContext = context;
		this.listitems = listitems;
	}

	// 搜索
	public void search() {
		final EditText name = new EditText(mContext);
		name.setHint(mContext.getString(R.string.search_hint));

		Dialog alertDialog = new AlertDialog.Builder(mContext).setTitle(R.string.search).setView(name)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new AsyncLoader().execute(name.getText().toString()); // 执行线程进行搜索
					}
				}).setNegativeButton(R.string.cancel, null).create();
		alertDialog.show();
	}

	// 线程
	// AsyncTask
	class AsyncLoader extends AsyncTask<String, Void, Void> {
		@Override
		// doInBackground执行前执行
		protected void onPreExecute() {
			// 创建进度条对象
			mdialog = new ProgressDialog(mContext);
			// 设置进度条提示的信息
			mdialog.setMessage(mContext.getString(R.string.searching));
			// 设置点击进度条外，进度条不消失
			mdialog.setCancelable(false);
			// 设置进度条样式为圆形
			mdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// 显示进度条
			mdialog.show();

		}

		// 正式开始搜索
		protected Void doInBackground(String... params) {

			for (String result : listitems) // 遍历获取成员，并筛选
			{
				if (result.indexOf(params[0]) != -1) {
					listresult.add(result);
					listposition.add(listitems.indexOf(result));
				}
			}
			return null;
		}

		// doInBackground结束后执行
		protected void onPostExecute(Void result) {

			mdialog.dismiss();
			new AlertDialog.Builder(mContext).setTitle(R.string.search_result)
					.setItems((String[]) listresult.toArray(new String[listresult.size()]),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// 在原来的列表中选择该条目
									((MainActivity) mContext).stringListView.setSelection(listposition.get(arg1));
								}
							})
					.create().show();

		}
	}

}
