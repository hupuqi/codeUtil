package com.mc.nozzle.api;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.mc.core.api.ApiJournalAfter;
import com.mc.core.api.ApiResponse;
/**
 * 之后切片
 * @author 1138789752@qq.com
 * @since 2019-11-08 14:21:44
 * */
@Component
public class ApiJournalAfterImpl implements ApiJournalAfter {
	private static final ConcurrentLinkedQueue<LinkedHashMap<String, Object>> QUEUE = new ConcurrentLinkedQueue<LinkedHashMap<String, Object>>();
	public void execute(LinkedHashMap<String, Object> data, Object res) {
		try {
			if (res instanceof ApiResponse) {
				String body = String.valueOf(((ApiResponse) res).getEtc());
				if (body != null && body.startsWith("total=")) {
					System.out.println("返回数据条数 = " + Long.parseLong(body.replace("total=", "")));
				}
			}
			//System.out.println(com.mc.core.api.ApiRequest.format(data, String.class, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Scheduled(initialDelay = 90 * 1000L, fixedDelay = 60 * 1000L)
	public void start() {
		try {
			ArrayList<LinkedHashMap<String, Object>> datas = new ArrayList<LinkedHashMap<String, Object>>();
			while (!QUEUE.isEmpty()) {
				datas.add(QUEUE.poll());
				if (datas.size() >= 50) {
					break;
				}
			}
			
			if (datas.size() > 0) {}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}