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

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class BingTranslate {

	// 源语言
	private Language SRCLANGUAGE = Language.ENGLISH;
	// 目标语言
	private Language TARGETLANGUAGE = Language.CHINESE_SIMPLIFIED;

	// 构造函数
	public BingTranslate(Language from, Language to) {
		// 获取源语言
		SRCLANGUAGE = from;
		// 获取目标语言
		TARGETLANGUAGE = to;
		// 设置翻译api的用户id
		Translate.setClientId("20000227");
		// 设置翻译api的用户密钥
		Translate.setClientSecret("bvgP0SOFq1up2Elv2I8QI1Yuhdb0GZlQ82mS0cDohgM=");
		Translate.setHttpReferrer("https://datamarket.azure.com/developer/applications");
	}

	/** 获取翻译结果 */
	public String getTranslateResult(String str) throws Exception {
		return Translate.execute(str, SRCLANGUAGE, TARGETLANGUAGE);
	}

}
