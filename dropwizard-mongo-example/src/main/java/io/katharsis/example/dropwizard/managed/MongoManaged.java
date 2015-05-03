package io.katharsis.example.dropwizard.managed;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import io.dropwizard.lifecycle.Managed;
import io.katharsis.example.dropwizard.MongoConfiguration;

public class MongoManaged implements Managed {
    private final MongoClient mongo;
    private final DB db;

    public MongoManaged (MongoConfiguration mongoConfig) throws Exception {
        mongo = new MongoClient(mongoConfig.host, mongoConfig.port);
        db = mongo.getDB(mongoConfig.db);
    }

    public Mongo getMongo() {
        return mongo;
    }

    public DB getDb() {
        return db;
    }

    public void start() throws Exception {

    }

    public void stop() throws Exception {
        mongo.close();
    }
}
