package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.Arrays;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import dagger.Module;
import dagger.Provides;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Module
public class DaggerModule {

    // private static 
    static int port = 8080;
	
    @Provides public MongoClient provideMongoClient() {
        
        MongoClient mongoClient = MongoClients.create();

    	return mongoClient;
    }

    @Provides public HttpServer provideHttpServer(){

        HttpServer server = null;

        try{
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        }
        catch (IOException e){
            e.printStackTrace();
        }        

        return server;
    }
}
