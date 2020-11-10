package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Iterator;

import javax.inject.Inject;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class PostEndPoints implements HttpHandler {

    private MongoClient db;

    /* Establish driver of the db */
    public PostEndPoints(MongoClient db) {
        this.db = db;
    }

    @Override
    @Inject
    public void handle(HttpExchange r) throws IOException {

        // Dagger service = DaggerDaggerComponent.builder().build().buildMongoHttp();

        // get db
        MongoDatabase database = db.getDatabase("csc301a2");
        MongoCollection<Document> collection = database.getCollection("posts");

        System.out.println(r.getRequestMethod());

        if (r.getRequestMethod().equals("PUT")) {
            try {
                putPost(r, collection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (r.getRequestMethod().equals("GET")) {
            try {
                System.out.println("before going to getPost");
                getPost(r, collection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (r.getRequestMethod().equals("DELETE")) {
            try {
                deletePost(r, collection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            r.sendResponseHeaders(405, -1);
        }
    }

    public void putPost(HttpExchange r, MongoCollection<Document> collection) throws IOException, JSONException {
        // Convert Body to JSON Object
        String title = "";
        String author = "";
        String content = "";
        ArrayList<String> tags;
        JSONObject deserialized = new JSONObject();
        try {
            String body = Utils.convert(r.getRequestBody());
            deserialized = new JSONObject(body);
        } catch (JSONException e) {
            r.sendResponseHeaders(400, -1);
            return;
        } catch (IOException e) {
            r.sendResponseHeaders(500, -1);
            return;
        }

        // Get Data from JSON
        // title
        if (deserialized.has("title")) {
            title = deserialized.getString("title");
        } else {
            r.sendResponseHeaders(400, -1);
            return;
        }
        // author
        if (deserialized.has("author")) {
            author = deserialized.getString("author");
        } else {
            r.sendResponseHeaders(400, -1);
            return;
        }
        // content
        if (deserialized.has("content")) {
            content = deserialized.getString("content");
            System.out.println(content);
        } else {
            r.sendResponseHeaders(400, -1);
            return;
        }
        // tags
        if (deserialized.has("tags")) {
            JSONArray getTags = deserialized.getJSONArray("tags");
            tags = jsonArrayToArrayList(getTags);
            System.out.println(tags);
            System.out.println(author);
            System.out.println(content);
        } else {
            r.sendResponseHeaders(400, -1);
            return;
        }

        // Create Mongodb Document
        Document post = new Document("title", title).append("author", author).append("content", content).append("tags",
                tags);

        collection.insertOne(post);

        ObjectId id = (ObjectId) post.get("_id");
        String response = "{\"_id\": \"" + id.toString() + "\"}";

        r.sendResponseHeaders(200, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public void deletePost(HttpExchange r, MongoCollection<Document> collection) {
        try {
            System.out.println("reached here");
            // Convert Body to JSON Object
            String title = "";
            String _id = "";
            String response = "";

            JSONObject deserialized = new JSONObject();
            try {
                String body = Utils.convert(r.getRequestBody());
                deserialized = new JSONObject(body);
            } catch (JSONException e) {
                r.sendResponseHeaders(400, -1);
                return;
            } catch (IOException e) {
                r.sendResponseHeaders(500, -1);
                return;
            }

            // Get Data from JSON
            // id
            if (deserialized.has("_id")) {
                _id = deserialized.getString("_id");
            } else {
                r.sendResponseHeaders(400, -1);
                return;
            }

            DeleteResult myDoc = collection.deleteOne(eq("_id", new ObjectId(_id)));
            r.sendResponseHeaders(200, 0);
            OutputStream os = r.getResponseBody();
            os.write("".getBytes());
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPost(HttpExchange r, MongoCollection<Document> collection) {
        try {
            System.out.println("reached here");
            // Convert Body to JSON Object
            String _id = "";
            String response = "";

            JSONObject deserialized = new JSONObject();
            try {
                String body = Utils.convert(r.getRequestBody());
                deserialized = new JSONObject(body);
            } catch (JSONException e) {
                r.sendResponseHeaders(400, -1);
                return;
            } catch (IOException e) {
                r.sendResponseHeaders(500, -1);
                return;
            }

            // Get Data from JSON
            // id
            if (deserialized.has("_id")) {
                _id = deserialized.getString("_id");
            }
            // title
            else if (deserialized.has("title")) {
                title = deserialized.getString("title");
            } else {
                r.sendResponseHeaders(400, -1);
                return;
            }

            // if (title.equals("") && _id.equals("")){
            // return;
            // }

            if (_id == "") {
                MongoCursor<Document> cursor = collection.find(eq("title", title)).iterator();
                try {
                    response = "[";
                    while (cursor.hasNext()) {
                        // System.out.println(cursor.next().toJson());
                        System.out.println(response);

                        response = response + cursor.next().toJson() + " , ";
                    }
                } finally {
                    cursor.close();
                }
                response = response.substring(0, response.length() - 3);
                response = response + "]";
                System.out.println("______________________________________________");
                System.out.println(response);
            } else {
                System.out.println(_id);
                Document myDoc = collection.find(eq("_id", new ObjectId(_id))).first();
                System.out.println(myDoc.toJson());
                response = myDoc.toJson();
                System.out.println(response);
            }

            byte[] bs = response.getBytes("UTF-8");
            r.sendResponseHeaders(200, bs.length);
            OutputStream os = r.getResponseBody();
            os.write(bs);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> jsonArrayToArrayList(JSONArray jArray) {
        try {
            ArrayList<String> listdata = new ArrayList<String>();
            if (jArray != null) {
                for (int i = 0; i < jArray.length(); i++) {
                    listdata.add(jArray.getString(i));
                }
                return listdata;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}