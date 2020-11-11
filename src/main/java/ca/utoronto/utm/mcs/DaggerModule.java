package ca.utoronto.utm.mcs;

import java.io.IOException;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import dagger.Module;
import dagger.Provides;

@Module
public class DaggerModule {

    static int port = 8080;

    @Provides
    public HttpServer provideHttpServer() {

        HttpServer server = null;

        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return server;
    }
}
