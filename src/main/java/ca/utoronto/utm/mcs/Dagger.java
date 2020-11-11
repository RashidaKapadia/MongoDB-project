package ca.utoronto.utm.mcs;

import javax.inject.Inject;

import com.sun.net.httpserver.HttpServer;

public class Dagger {

	private HttpServer server;

	@Inject
	public Dagger(HttpServer server) {
		this.server = server;
	}

	public HttpServer getServer() {
		return this.server;
	}

	public void setServer(HttpServer server) {
		this.server = server;
	}
}
