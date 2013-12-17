package com.integratingstuff.android.webserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class ImageCommandHandler implements HttpRequestHandler {

	Handler _handler;
	DataHolder _dataHolder;

	public ImageCommandHandler(Context context, Handler handler,
			DataHolder dataHolder) {
		_handler = handler;
		_dataHolder = dataHolder;
	}

	@Override
	public void handle(final HttpRequest request, HttpResponse response,
			HttpContext httpContext) throws HttpException, IOException {

		Message completeMessage = _handler.obtainMessage(1, "IMAGE");
		completeMessage.sendToTarget();

		HttpEntity entity = null;
		try {
			synchronized (_dataHolder) {
				_dataHolder.wait();
			}
			entity = new InputStreamEntity(new ByteArrayInputStream(
					_dataHolder.data), _dataHolder.data.length);
		} catch (InterruptedException e) {

			entity = new EntityTemplate(new ContentProducer() {
				public void writeTo(final OutputStream outstream)
						throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(
							outstream, "UTF-8");

					writer.write("teste imagem jpg");
					writer.flush();

				}
			});

			e.printStackTrace();
		}

		response.setHeader("Content-Type", "image/jpeg");
		response.setEntity(entity);
	}

}
