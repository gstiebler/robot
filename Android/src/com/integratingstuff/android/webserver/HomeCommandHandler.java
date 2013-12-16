package com.integratingstuff.android.webserver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class HomeCommandHandler implements HttpRequestHandler {
	
	private Context context = null;
	Handler _handler;
	String _htmlText;

	public HomeCommandHandler(Context context, Handler handler, String html) {
		this.context = context;
		_handler = handler;
		_htmlText = html;
	}

	@Override
	public void handle(final HttpRequest request, HttpResponse response,
	    HttpContext httpContext) throws HttpException, IOException {
		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			public void writeTo(final OutputStream outstream) throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");

				String rl = request.getRequestLine().getUri();
				String[] param = rl.split("\\?");
				
				if( param.length < 2) {
					writer.write(_htmlText);
				} else {
					String paramValue = parseParameter(param[1]);

					Message completeMessage = _handler.obtainMessage(0, paramValue);
					completeMessage.sendToTarget();
					
					writer.write("Ok, got it: " + paramValue);
				}
				
				writer.flush();
			}
		});
		response.setHeader("Content-Type", "text/html");
		response.setEntity(entity);
	}
	
	String parseParameter(String urlParameter) {
		String[] parameter = urlParameter.split("=");
		return parameter[1];
	}

	public Context getContext() {
		return context;
	}
}