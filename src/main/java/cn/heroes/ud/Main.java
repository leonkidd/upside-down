package cn.heroes.ud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.DefaultBHttpServerConnection;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.apache.http.util.EntityUtils;

public class Main implements HttpRequestHandler {

	public static void main(String[] args) throws Exception {
		ServerSocket proxy_socket = new ServerSocket(8080);
		Socket client_socket = null;
		while ((client_socket = proxy_socket.accept()) != null) {
			Waiter a = new Waiter(client_socket);
			a.run();
		}
		proxy_socket.close();
	}

	private static class Waiter extends Thread {
		private Socket client_socket = null;

		public Waiter(Socket socket) {
			this.client_socket = socket;
		}

		public void run() {
			try {
				DefaultBHttpServerConnection inconn = new DefaultBHttpServerConnection(
						8 * 1024);
				inconn.bind(client_socket);

				HttpProcessor httpproc = HttpProcessorBuilder.create().build();

				// Set up incoming request handler
				UriHttpRequestHandlerMapper reqistry = new UriHttpRequestHandlerMapper();
				reqistry.register("*", new Main());

				HttpCoreContext context = HttpCoreContext.create();

				// Set up the HTTP service
				HttpService httpService = new HttpService(httpproc, reqistry);
				httpService.handleRequest(inconn, context);
			} catch (UnknownHostException e) {
			} catch (ConnectionClosedException e) {
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (client_socket != null)
					try {
						client_socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}

	public void handle(HttpRequest request, HttpResponse response,
			HttpContext context) throws HttpException, IOException {
		// Send request
		Header[] header = request.getHeaders("Host");
		String host = header[0].getValue();
		HttpHost target = new HttpHost(host);
		// System.out.println(host);

		String uri = request.getRequestLine().getUri();

		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse targetResponse = client.execute(target, request);

		response.setStatusLine(targetResponse.getStatusLine());
		response.setHeaders(targetResponse.getAllHeaders());

		HttpEntity entity = targetResponse.getEntity();
		try {
			if (isImage(uri) || entity != null) {
				// TODO do sth fun with png
				System.out.println(uri);
				byte[] bs = EntityUtils.toByteArray(entity);
				bs = Fun.transform(bs);
				// TODO with image
				entity = new ByteArrayEntity(bs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setEntity(entity);
	}

	// private static final String[] imageSubfix = { "bmp", "gif", "ico",
	// "jpeg",
	// "jpg", "png", "tga", "tiff" };
	private static final String[] imageSubfix = { "bmp", "gif", "jpeg", "jpg",
			"png" };

	public static boolean isImage(String uri) {
		String low = uri.toLowerCase();
		for (String subfix : imageSubfix) {
			if (low.endsWith("." + subfix)) {
				return true;
			}
		}
		return false;
	}

}
