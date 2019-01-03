/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package com.zhijiaiot.app.signalr4j.http.java;

import com.zhijiaiot.app.signalr4j.LogLevel;
import com.zhijiaiot.app.signalr4j.Logger;
import com.zhijiaiot.app.signalr4j.Platform;
import com.zhijiaiot.app.signalr4j.http.HttpConnection;
import com.zhijiaiot.app.signalr4j.http.HttpConnectionFuture;
import com.zhijiaiot.app.signalr4j.http.HttpConnectionFuture.ResponseCallback;
import com.zhijiaiot.app.signalr4j.http.Request;

/**
 * Java HttpConnection implementation, based on HttpURLConnection and threads
 * async operations
 */
public class JavaHttpConnection implements HttpConnection {

	/**
	 * User agent header name
	 */
	private static final String USER_AGENT_HEADER = "User-Agent";

	private Logger mLogger;

	/**
	 * Initializes the JavaHttpConnection
	 * 
	 * @param logger
	 *            logger to log activity
	 */
	public JavaHttpConnection(Logger logger) {
		mLogger = logger;
	}

	@Override
	public HttpConnectionFuture execute(final Request request, final ResponseCallback callback) {

		if (request.getHeaderField(USER_AGENT_HEADER) == null) {
			request.addHeader(USER_AGENT_HEADER, Platform.getUserAgent());
		}

		mLogger.log("Create new thread for HTTP Connection", LogLevel.Verbose);

		HttpConnectionFuture future = new HttpConnectionFuture();

		final NetworkRunnable target = new NetworkRunnable(mLogger, request, future, callback);
		final NetworkThread networkThread = new NetworkThread(target) {
			@Override
			void releaseAndStop() {
				try {
					target.closeStreamAndConnection();
				} catch (Throwable error) {
				}
			}
		};

		future.onCancelled(new Runnable() {

			@Override
			public void run() {
				networkThread.releaseAndStop();
			}
		});

		networkThread.start();

		return future;
	}
}
