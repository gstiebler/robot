package com.integratingstuff.android.webserver;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class WebServerService extends Service {

	private WebServer server = null;
	
	Handler _handler;
	
	public WebServerService(Handler handler) {
		_handler = handler;
	}

	@Override
	public void onCreate() {
		Log.i("HTTPSERVICE", "Creating and starting httpService");
		super.onCreate();
		server = new WebServer(this, _handler, "Hello world");
		server.startServer();
	}

	@Override
	public void onDestroy() {
		Log.i("HTTPSERVICE", "Destroying httpService");
		server.stopServer();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}