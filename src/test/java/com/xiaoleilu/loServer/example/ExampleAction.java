package com.xiaoleilu.loServer.example;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.db.Db;
import cn.hutool.db.Entity;
import cn.hutool.json.JSONUtil;
import com.xiaoleilu.loServer.LoServer;
import com.xiaoleilu.loServer.ServerSetting;
import com.xiaoleilu.loServer.action.Action;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;

import java.sql.SQLException;
import java.util.Date;
import lombok.Data;
/**
 * loServer样例程序<br>
 * Action对象用于处理业务流程，类似于Servlet对象<br>
 * 在启动服务器前必须将path和此Action加入到ServerSetting的ActionMap中<br>
 * 使用ServerSetting.setPort方法设置监听端口，此处设置为8090（如果不设置则使用默认的8090端口）
 * 然后调用LoServer.start()启动服务<br>
 * 在浏览器中访问http://localhost:8090/example?a=b既可在页面上显示response a: b
 * @author Looly
 *
 */

@Data
class AddDistributedGoodsQO {
	// 分销商ID
	private Integer distributorID;

	// 商品spuID
	private Integer productSpuID;

	// 分销状态，默认是已分销
	private  Integer distributionState = 1;
	// 审核者
	private String approver;

	AddDistributedGoodsQO (Integer distributorID, Integer productSpuID
							,Integer distributionState, String approver) {
		this.distributorID = distributorID;
		this.productSpuID = productSpuID;
		this.distributionState = distributionState;
		this.approver = approver;
	}
}

@Data
class Test {
	private Integer id = 123;
	private String name = "lizhanbin";

	Test(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
}
public class ExampleAction implements Action{

	@Override
	public void doAction(Request request, Response response) {
		String a = request.getParam("a");
		response.setContent("response a: " + a);
		// throw new RuntimeException("Test");
		try {
			Db.use().insert(
					Entity.create("user")
							.set("username", "forward")
							.set("password", "123456")
			);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String jsonStr = "{ \"id\":12345}";
		response.setJsonContent(jsonStr);
		Date hutoolJsonDateTimeStart = DateTime.now().toJdkDate();
		cn.hutool.json.JSONObject hutoolJsonObject = JSONUtil.parseObj(jsonStr);
		Date hutoolJsonDateTimeEnd = DateTime.now().toJdkDate();
		System.out.printf("Hutool转化JsonObject耗时：%d毫秒\n", DateUtil.between(hutoolJsonDateTimeStart,hutoolJsonDateTimeEnd, DateUnit.MS));

		System.out.println("--------------JsonObject转String----------------");

		Test t = new Test(8888, "lizhanbin");
		AddDistributedGoodsQO d = new AddDistributedGoodsQO(777, 888, 1, "lizhanbin");
		String hutoolString = JSONUtil.toJsonStr(d);
		response.setJsonContent(hutoolString);

	}

	public static void main(String[] args) {
		ServerSetting.setAction("/example", ExampleAction.class);
		ServerSetting.setRoot("root");
		ServerSetting.setPort(8090);
		LoServer.start();
	}
}