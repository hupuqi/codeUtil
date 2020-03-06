package com.mc.nozzle.view;
import java.util.Arrays;
import java.util.LinkedHashMap;
import com.mc.core.api.ApiRequest;
public class ApiMovieBody {
	public static class Search extends ApiRequest {
		@ApiRequest.ApiField(name = "豆瓣评分", kagi = "score")
		public String score;
		@ApiRequest.ApiField(name = "视频尺寸", kagi = "size")
		public String size;
		@ApiRequest.ApiField(name = "排序方式", kagi = "order")
		public String order;
		@ApiRequest.ApiField(name = "搜索词语")
		public String word;
		public LinkedHashMap<String, LinkedHashMap<?, String>> legal() {
			return new LinkedHashMap<String, LinkedHashMap<?, String>>() {
				private static final long serialVersionUID = 1L;
				{
					super.put("score", new LinkedHashMap<String, String>() {
						private static final long serialVersionUID = 1L;
						{
							super.put("0-69", "0 - 6.9");
							super.put("70-79", "7.0 - 7.9");
							super.put("80-89", "8.0 - 8.9");
							super.put("90-100", "9.0 - 10.0");
						}
					});
					super.put("size", new LinkedHashMap<String, String>() {
						private static final long serialVersionUID = 1L;
						{
							super.put("1280-720", "1280 × 720px");
							super.put("1024-576", "1024 × 576px");
							super.put("960-720", "960 × 720px");
						}
					});
					super.put("order", new LinkedHashMap<String, String>() {
						private static final long serialVersionUID = 1L;
						{
							super.put("0-1", "按上映日期升序");
							super.put("1-0", "按豆瓣评分降序");
							super.put("1-1", "按豆瓣评分升序");
							super.put("2-0", "按更新时间降序");
							super.put("2-1", "按更新时间升序");
							super.put("3-0", "按评论总数降序");
							super.put("3-1", "按评论总数升序");
							super.put("4-0", "按看过人数降序");
							super.put("4-1", "按看过人数升序");
						}
					});
				}
			};
		}
		public LinkedHashMap<String, String> field() {
			return new LinkedHashMap<String, String>() {
				private static final long serialVersionUID = 1L;
				{
					super.put("id", "电影编号");
					super.put("name", "中文名称");
				}
			};
		}
		public LinkedHashMap<String, String> order() {
			return null;
		}
		public ApiLeafer leaf() {
			return ApiLeafer.MARKER_LEAFER;
		}
		public LinkedHashMap<String, Object> body() {
			return new LinkedHashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					super.put("datas", Arrays.asList(new LinkedHashMap<String, Object>() {
						private static final long serialVersionUID = 1L;
						{
							super.putAll(field());
							super.put("catena", new LinkedHashMap<String, Object>() {
								private static final long serialVersionUID = 1L;
								{
									super.put("id", "系列编号");
									super.put("name", "系列名称");
								}
							});
						}
					}));
				}
			};
		}
	}
}
