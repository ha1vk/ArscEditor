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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;

public class BaiduTranslate {

	// 需要翻译的内容，源语言，目标语言
	private String str = null, fromString = "auto", toString = "auto";
	// 结果
	private String result = "";

	// 构造函数
	public BaiduTranslate(String fromString, String toString) {
		// 获取源语言
		this.fromString = fromString;
		// 获取目标语言
		this.toString = toString;
	}

	/**
	 * 获取翻译结果 str:需要翻译的内容
	 */
	public String getResult(String str) throws IOException, JSONException {
		// 获取需要翻译的内容
		this.str = str;
		// 开启翻译
		doTranslate();
		// 返回结果
		return result;
	}

	// 翻译的函数
	public void doTranslate() throws IOException, JSONException {

		// 格式化需要翻译的内容为UTF-8编码
		String str_utf = URLEncoder.encode(str, "UTF-8");
		// 百度翻译api
		String str_url = "http://openapi.baidu.com/public/2.0/bmt/translate?client_id=GOr7jiTs5hiQvkHqDNg4KSTV&q="
				+ str_utf + "&from=" + fromString + "&to=" + toString;
		// 将api网址转化成URL
		URL url_word = new URL(str_url);
		// 连接到该URL
		URLConnection connection = (URLConnection) url_word.openConnection();
		// 获取输入流
		InputStream is = connection.getInputStream();
		// 转化成读取流
		InputStreamReader isr = new InputStreamReader(is);
		// 转化成缓冲读取流
		BufferedReader br = new BufferedReader(isr);
		// 每行的内容
		String line;
		// 字符串处理类
		StringBuilder sBuilder = new StringBuilder();
		// 读取每行内容
		while ((line = br.readLine()) != null) {
			// 在字符串末尾追加内容
			sBuilder.append(line);
		}

		/**
		 * 单词解析
		 */

		JSONTokener jtk = new JSONTokener(sBuilder.toString());
		JSONObject jObject = (JSONObject) jtk.nextValue();

		JSONArray jArray = jObject.getJSONArray("trans_result");
		Log.i("TAG", url_word.toString());
		Log.i("TAG", jObject.toString());

		JSONObject sub_jObject_1 = jArray.getJSONObject(0);
		// dst对应的内容就是翻译结果
		result = sub_jObject_1.getString("dst");

		br.close();
		isr.close();
		is.close();
	}
}