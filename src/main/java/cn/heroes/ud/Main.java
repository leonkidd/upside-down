package cn.heroes.ud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;

public class Main implements HttpRequestHandler {

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

			DefaultBHttpServerConnection inconn = new DefaultBHttpServerConnection(
					8 * 1024);
			inconn.bind(client_socket);

			HttpProcessor httpproc = HttpProcessorBuilder.create().build();

			HttpCoreContext context = HttpCoreContext.create();
//			HttpContext context = new BasicHttpContext(null);

			// Bind connection objects to the execution context
			//context.setAttribute(HTTP_IN_CONN, inconn);
			//context.setAttribute(HTTP_OUT_CONN, outconn);

			// Set up incoming request handler
			UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
			reqistry.register("*", new Main());

			// Set up the HTTP service
			HttpService httpService = new HttpService(httpproc, reqistry);
			httpService.handleRequest(inconn, context);
			client_socket.close();
		}
		proxy_socket.close();
	}

	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {
		// Send request
		String uri = request.getRequestLine().getUri();
		HttpHost target = new HttpHost(uri);
		CloseableHttpResponse target_response = HttpClients.createDefault().execute(target, request);
		System.out.println(target_response);
	}

}
