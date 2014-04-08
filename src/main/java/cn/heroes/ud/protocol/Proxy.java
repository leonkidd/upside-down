package cn.heroes.ud.protocol;

import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.RequestConnControl;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestExpectContinue;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

import cn.heroes.ud.protocol.ElementalReverseProxy.ProxyHandler;

public class Proxy {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(8888);
		Socket client_socket = null;
		while ((client_socket = ss.accept()) != null) {
			final int bufsize = 8 * 1024;
			DefaultBHttpServerConnection inconn = new DefaultBHttpServerConnection(
					bufsize);
			System.out.println("Incoming connection from "
					+ client_socket.getInetAddress());
			inconn.bind(client_socket);

			// Set up HTTP protocol processor for incoming connections
			final HttpProcessor inhttpproc = new ImmutableHttpProcessor(
					new HttpRequestInterceptor[] { new RequestContent(),
							new RequestTargetHost(), new RequestConnControl(),
							new RequestUserAgent("Test/1.1"),
							new RequestExpectContinue(true) });

			// Set up HTTP protocol processor for outgoing connections
			final HttpProcessor outhttpproc = new ImmutableHttpProcessor(
					new HttpResponseInterceptor[] { new ResponseDate(),
							new ResponseServer("Test/1.1"),
							new ResponseContent(), new ResponseConnControl() });

			// Set up outgoing request executor
			final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

			// Set up incoming request handler
			final UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
			reqistry.register("*", new ProxyHandler(this.target, outhttpproc,
					httpexecutor));

			// Set up the HTTP service
			HttpService httpService = new HttpService(inhttpproc, reqistry);
		}
	}

}
