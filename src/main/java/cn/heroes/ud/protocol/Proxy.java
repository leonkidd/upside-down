package cn.heroes.ud.protocol;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.RequestLine;
import org.apache.http.impl.DefaultBHttpClientConnection;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.protocol.HttpRequestHandler;
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

public class Proxy {

	private static final String HTTP_IN_CONN = "http.proxy.in-conn";
	private static final String HTTP_OUT_CONN = "http.proxy.out-conn";
	private static final String HTTP_CONN_KEEPALIVE = "http.proxy.conn-keepalive";

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		ServerSocket proxy_socket = new ServerSocket(8888);
		Socket client_socket = null;
		while ((client_socket = proxy_socket.accept()) != null) {
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

			// Set up incoming request handler
			final UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
			reqistry.register("*", new ProxyHandler());

			// Set up the HTTP service
			HttpService httpService = new HttpService(inhttpproc, reqistry);

			// Set up outgoing HTTP connection
			// final Socket outsocket = new Socket(
			// this.target.getHostName(), this.target.getPort());
			final DefaultBHttpClientConnection outconn = new DefaultBHttpClientConnection(
					bufsize);
			// outconn.bind(outsocket);
			// System.out.println("Outgoing connection to " +
			// outsocket.getInetAddress());

			final HttpContext context = new BasicHttpContext(null);

			// Bind connection objects to the execution context
			context.setAttribute(HTTP_IN_CONN, inconn);
			context.setAttribute(HTTP_OUT_CONN, outconn);

			try {
				while (!Thread.interrupted()) {
					if (!inconn.isOpen()) {
						outconn.close();
						break;
					}

					httpService.handleRequest(inconn, context);

					final Boolean keepalive = (Boolean) context
							.getAttribute(HTTP_CONN_KEEPALIVE);
					if (!Boolean.TRUE.equals(keepalive)) {
						outconn.close();
						inconn.close();
						break;
					}
				}
			} catch (final ConnectionClosedException ex) {
				System.err.println("Client closed connection");
			} catch (final IOException ex) {
				System.err.println("I/O error: " + ex.getMessage());
			} catch (final HttpException ex) {
				System.err.println("Unrecoverable HTTP protocol violation: "
						+ ex.getMessage());
			} finally {
				try {
					inconn.shutdown();
				} catch (final IOException ignore) {
				}
				try {
					outconn.shutdown();
				} catch (final IOException ignore) {
				}
			}
		}
		proxy_socket.close();
	}

	static class ProxyHandler implements HttpRequestHandler {
		private HttpProcessor outhttpproc = null;

		public ProxyHandler() {
			// Set up HTTP protocol processor for outgoing connections
			outhttpproc = new ImmutableHttpProcessor(
					new HttpResponseInterceptor[] { new ResponseDate(),
							new ResponseServer("Test/1.1"),
							new ResponseContent(), new ResponseConnControl() });
		}

		public void handle(HttpRequest request, HttpResponse response,
				HttpContext context) throws HttpException, IOException {

			final HttpClientConnection conn = (HttpClientConnection) context
					.getAttribute(HTTP_OUT_CONN);

			// Set up outgoing request executor
			final HttpRequestExecutor httpexecutor = new HttpRequestExecutor();

			httpexecutor.preProcess(request, outhttpproc, context);
			HttpResponse targetResponse = httpexecutor.execute(request, conn,
					context);
			httpexecutor.postProcess(response, outhttpproc, context);

			System.out.println(targetResponse);
		}

	}

}
