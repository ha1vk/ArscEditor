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

package zhao.arsceditor.Translate;

import java.util.List;

import com.memetix.mst.language.Language;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Toast;
import zhao.arsceditor.MainActivity;
import zhao.arsceditor.R;

@SuppressWarnings("deprecation")
public class DoTranslate implements OnCheckedChangeListener {
	// 上下文
	private Context mContext;
	// 跳过已翻译的内容的CheckBox控件
	private CheckBox skip_already_translate;
	// 源语言，翻译为，翻译商选项的Spinner控件
	private Spinner src_type, translate_to, translator;
	// 能够翻译的语言的集合
	private String[] languages;
	// 必应的语言集合
	private Language languages_bing[] = { Language.AUTO_DETECT, Language.CHINESE_SIMPLIFIED, Language.ENGLISH,
			Language.JAPANESE, Language.KOREAN, Language.FRENCH, Language.THAI, Language.RUSSIAN, Language.GERMAN,
			Language.GREEK, Language.ITALIAN, Language.SPANISH, Language.PORTUGUESE, Language.ARABIC };
	// 跳过已翻译的内容的标志，默认为关闭
	private boolean skip_translate = false;
	// 用来储存需要翻译的内容
	private List<String> source_list;
	// 用来储存翻译后的内容
	private List<String> target_list;
	// 是否翻译全部,默认是
	private boolean translate_all = true;
	// 需要翻译的条目的位置
	private int position;

	// 构造函数
	public DoTranslate(List<String> source_list, List<String> target_list, boolean translate_all, Context context) {
		// 获取需要翻译的内容的集合
		this.source_list = source_list;
		// 获取目标集合
		this.target_list = target_list;
		this.translate_all = translate_all;
		// 承接上下文
		mContext = context;
		// 获取语言选项的数组
		languages = context.getResources().getStringArray(R.array.language_short);
	}

	// 初始化
	@SuppressLint("InflateParams")
	public void init(int _position) {
		// 获取需要翻译的条目所在位置
		position = _position;

		LayoutInflater factory = LayoutInflater.from(mContext);
		// 得到自定义对话框
		View DialogView = factory.inflate(R.layout.translate, null);
		// 创建对话框
		new AlertDialog.Builder(mContext).setView(DialogView)// 设置自定义对话框的样式
				.setTitle(R.string.translate) // 设置进度条对话框的标题
				.setNegativeButton(R.string.translate, new DialogInterface.OnClickListener() // 设置按钮，并监听
				{
					@SuppressWarnings("unchecked")
					@Override
					public void onClick(DialogInterface p1, int p2) {
						// 开启一个翻译线程
						new translate_task().execute(source_list, target_list);
					}
				}).create()// 创建
				.show(); // 显示对话框

		// 找到显示源语言的Spinner控件
		src_type = (Spinner) DialogView.findViewById(R.id.src_type);
		// 找到显示目标语言的Spinner控件
		translate_to = (Spinner) DialogView.findViewById(R.id.translate_to);
		// 找到翻译商的选项的Spinner控件
		translator = (Spinner) DialogView.findViewById(R.id.translator);
		// 找到显示跳过选项的CheckBox控件
		skip_already_translate = (CheckBox) DialogView.findViewById(R.id.skip_already_translate);

		// 源语言默认自动识别
		src_type.setSelection(0);
		// 翻译为默认选择中文
		translate_to.setSelection(1);
		// 默认选择百度翻译
		translator.setSelection(0);
		// 如果是翻译单个条目，需要隐藏“跳过已翻译的内容”的选项，否则需要显示该选项
		skip_already_translate.setVisibility(translate_all ? View.VISIBLE : View.GONE);
		skip_already_translate.setOnCheckedChangeListener(this);
	}

	// CheckBox是否被勾选的事件处理
	@Override
	public void onCheckedChanged(CompoundButton p1, boolean p2) {
		// 获取是否需要跳过已翻译的内容的选择
		skip_translate = p2;
	}

	// 翻译线程
	class translate_task extends AsyncTask<List<String>, Void, String> {
		// 翻译进度
		private String progress;
		// 进度条
		private ProgressDialog pdlg;

		@Override
		protected void onPreExecute() {
			// 创建一个进度条对话框
			pdlg = new ProgressDialog(mContext);
			// 设置标题
			pdlg.setTitle(R.string.translating);
			// 进度条对话框设置点击外面不消失
			pdlg.setCancelable(false);
			// 设置进度条对话框样式
			pdlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			// 显示进度条对话框
			pdlg.show();
		}

		@Override
		protected String doInBackground(@SuppressWarnings("unchecked") List<String>... parms) {

			// 翻译的结果
			String translatedTexte = null;
			try {
				switch (translator.getSelectedItemPosition()) {
				case 1:
					BingTranslate bingTranslator = new BingTranslate(languages_bing[src_type.getSelectedItemPosition()],
							languages_bing[translate_to.getSelectedItemPosition()]);
					if (translate_all) {
						for (String item : parms[0]) {
							if (!item.equals(""))
								// 获取必应翻译的结果
								translatedTexte = bingTranslator.getTranslateResult(item);
							else
								translatedTexte = item;

							if (skip_translate == true) {
								if (parms[1].get(parms[0].indexOf(item)).equals("")) {
									parms[1].remove(parms[0].indexOf(item));
									parms[1].add(parms[0].indexOf(item),
											translatedTexte.equals("") ? item : translatedTexte);
								}
							} else {
								parms[1].remove(parms[0].indexOf(item));
								parms[1].add(parms[0].indexOf(item),
										translatedTexte.equals("") ? item : translatedTexte);
							}
							progress = String.valueOf(parms[0].indexOf(item));
							publishProgress();
						}
					} else {
						if (!parms[0].get(position).equals(""))
							// 获取谷歌翻译的结果
							translatedTexte = bingTranslator.getTranslateResult(parms[0].get(position));
						else
							translatedTexte = parms[0].get(position);
						parms[1].remove(position);
						parms[1].add(position, translatedTexte.equals("") ? parms[0].get(position) : translatedTexte);
					}
					break;
				case 0:
					BaiduTranslate baiduTranslator = new BaiduTranslate(languages[src_type.getSelectedItemPosition()],
							languages[translate_to.getSelectedItemPosition()]);
					if (translate_all) {
						for (String item : parms[0]) {
							if (!item.equals(""))
								// 获取百度翻译的结果
								translatedTexte = baiduTranslator.getResult(item);
							else
								translatedTexte = item;
							if (skip_translate == true) {
								if (parms[1].get(parms[0].indexOf(item)).equals("")) {
									parms[1].remove(parms[0].indexOf(item));
									parms[1].add(parms[0].indexOf(item),
											translatedTexte.equals("") ? item : translatedTexte);
								}
							} else {
								parms[1].remove(parms[0].indexOf(item));
								parms[1].add(parms[0].indexOf(item),
										translatedTexte.equals("") ? item : translatedTexte);
							}
							progress = String.valueOf(parms[0].indexOf(item));
							publishProgress();
						}
					} else {
						if (!parms[0].get(position).equals(""))
							// 获取百度翻译的结果
							translatedTexte = baiduTranslator.getResult(parms[0].get(position));
						else
							translatedTexte = parms[0].get(position);
						parms[1].remove(position);
						parms[1].add(position, translatedTexte.equals("") ? parms[0].get(position) : translatedTexte);
					}
					break;
				}
				// 字符常量池已发生改变的标志
				((MainActivity) mContext).isChanged = true;
				return mContext.getString(R.string.translate_success); // 返回翻译成功的结果
			} catch (Exception e) {
				for (String item : parms[1]) // 遍历获取listitem_change的内容
				{
					if (item != "") // 如果内容不为空
					{
						((MainActivity) mContext).isChanged = true; // 字符已经改变的标志
						break; // 跳出循环
					}
				}
				return e.toString(); // 返回错误信息
			}

		}

		// 更新UI的方法
		@Override
		protected void onProgressUpdate(Void... p1) {
			pdlg.setMessage(progress + "/" + source_list.size());
		}

		// 线程执行完毕后的任务
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			// 隐藏该进度条对话框
			pdlg.dismiss();
			// 更新字符常量池的显示
			((MainActivity) mContext).mAdapter.notifyDataSetChanged();
			if (result == mContext.getString(R.string.translate_success))
				Toast.makeText(mContext, result, Toast.LENGTH_SHORT).show();
			else
				MainActivity.showMessage(mContext, result).show();// 显示错误信息
		}
	}

}
