package com.example.networktest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity implements OnClickListener {

	public static final int SHOW_RESPONSE1 = 1;
	public static final int SHOW_RESPONSE2 = 2;

	private Button sendRequest1;
	private Button sendRequest2;

	private TextView responseText;

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_RESPONSE1:
				String response1 = (String) msg.obj;
				responseText.setText(Html.fromHtml(response1));
			case SHOW_RESPONSE2:
				String response2 = (String) msg.obj;
				responseText.setText(response2);
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sendRequest1  = (Button)   findViewById(R.id.send_request1);
		sendRequest2  = (Button)   findViewById(R.id.send_request2);
		
		responseText  = (TextView) findViewById(R.id.response_text);
		
		sendRequest1.setOnClickListener(this);
		sendRequest2.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.send_request1) {
			responseText.setText("");
			sendRequestWithHttpURLConnection();
		}else if (v.getId() == R.id.send_request2) {
			responseText.setText("");
			sendRequestWithHttpClient();
		}
	}

	// 发送HttpClient请求
	private void sendRequestWithHttpClient() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = new DefaultHttpClient();
					// 指定访问的服务器地址是电脑本机
					// 注意：由于是本机测试，所以测试设备和接口必须在同一网段内，否则访问失败
					HttpGet httpGet = new HttpGet("http://192.168.0.162:8082/get_data.json");
					HttpResponse httpResponse = httpClient.execute(httpGet);
					if (httpResponse.getStatusLine().getStatusCode() == 200) {
						// 请求和响应都成功了
						HttpEntity entity = httpResponse.getEntity();
						String response = EntityUtils.toString(entity, "utf-8");
						// 解析JSON
						parseJSONWithGSON(response);
						
						 Message message = new Message();
						 message.what    = SHOW_RESPONSE2;
						 message.obj     = response.toString();
						 handler.sendMessage(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	// 发送HttpURLConnection请求
	private void sendRequestWithHttpURLConnection() {
		// 开启线程来发起网络请求
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL("http://www.baidu.com");
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					connection.setDoInput(true);
					connection.setDoOutput(true);
					InputStream in = connection.getInputStream();
					// 下面对获取到的输入流进行读取
					BufferedReader reader  = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						response.append(line);
					}
					Message message = new Message();
					message.what 	= SHOW_RESPONSE1;
					message.obj 	= response.toString();
					handler.sendMessage(message);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}

	private void parseJSONWithGSON(String jsonData) {
		Gson gson = new Gson();
		List<AppModel> appList = gson.fromJson(jsonData, new TypeToken<List<AppModel>>() {}.getType());
		for (AppModel app : appList) {
			Log.d("andli", "id is " 	 + app.getId());
			Log.d("andli", "name is " 	 + app.getName());
			Log.d("andli", "version is " + app.getVersion());
		}
	}

	private void parseJSONWithJSONObject(String jsonData) {
		try {
			JSONArray jsonArray = new JSONArray(jsonData);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");
				String version = jsonObject.getString("version");
				Log.d("MainActivity", "id is " + id);
				Log.d("MainActivity", "name is " + name);
				Log.d("MainActivity", "version is " + version);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
