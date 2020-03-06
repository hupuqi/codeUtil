package com.mc.nozzle.view;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.mc.core.api.ApiRequest;
import com.mc.core.api.ApiRequestMethod;
import com.mc.nozzle.api.ApiResponseBody;
@ApiRequest.ApiClass(mark = 3, title = "经典电影")
@RequestMapping(value = "api/movie/", method = RequestMethod.POST)
@Controller
public class ApiMovieView {
	@ApiRequestMethod(mark = 1, name = "搜索电影", queue = 1, jump = "http://www.ink10000.com/movie/index.htm")
	@ResponseBody
	@RequestMapping("search")
	public ApiResponseBody search(HttpServletRequest request, HttpServletResponse response, @RequestBody ApiMovieBody.Search body) {
		ApiResponseBody res = new ApiResponseBody(body);
		try {
			System.out.println("输出请求参数：");
			System.out.println(body.order);
			System.out.println(body.score);
			System.out.println(body.SHARE$NOW);
		} catch (Exception e) {
			res.setState(e);
		}
		return res;
	}

}
