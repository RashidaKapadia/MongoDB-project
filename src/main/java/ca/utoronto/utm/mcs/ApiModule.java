package ca.utoronto.utm.mcs;

import javax.inject.Singleton;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dagger.Module;
import dagger.Provides;


@Module
public class ApiModule {

    @Provides @Singleton MongoClient provideMongoClient() {
        
        MongoClient mongoClient = MongoClients.create();

    	return mongoClient;
    }
    
}
