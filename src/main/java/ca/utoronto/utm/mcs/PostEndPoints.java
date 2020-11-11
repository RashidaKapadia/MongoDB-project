package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;

import javax.inject.Inject;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.BasicDBObject;
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
    @Inject
    public PostEndPoints(MongoClient db) {
        this.db = db;
    }

    public MongoClient getDb() {
        return this.db;
    }

    public void setDb(MongoClient db) {
        this.db = db;
    }

    @Override
    public void handle(HttpExchange r) throws IOException {

        // get db
        MongoDatabase database = db.getDatabase("csc301a2");
        MongoCollection<Document> collection = database.getCollection("posts");

        if (r.getRequestMethod().equals("PUT")) {
            try {
                putPost(r, collection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (r.getRequestMethod().equals("GET")) {
            try {
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

    public void putPost(HttpExchange r, MongoCollection<Document> collection) {
        try {
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
            } else {
                r.sendResponseHeaders(400, -1);
                return;
            }
            // tags
            if (deserialized.has("tags")) {
                JSONArray getTags = deserialized.getJSONArray("tags");
                tags = jsonArrayToArrayList(getTags);
            } else {
                r.sendResponseHeaders(400, -1);
                return;
            }

            // Create Mongodb Document
            Document post = new Document("title", title).append("author", author).append("content", content)
                    .append("tags", tags);

            collection.insertOne(post);

            ObjectId id = (ObjectId) post.get("_id");
            String response = "{\"_id\": \"" + id.toString() + "\"}";

            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getPost(HttpExchange r, MongoCollection<Document> collection) {
        try {
            String _id = "";
            String title = "";
            String response = "";

            // Convert Body to JSON Object
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

            if (_id == "") {
                MongoCursor<Document> cursor = collection.find(regex("title", "" + title + "")).sort(new BasicDBObject("title",1)).iterator();
                response = "[";
                while (cursor.hasNext()) {
                    response = response + cursor.next().toJson() + " , ";
                }
                cursor.close();

                if (response.length() > 3) {
                    response = response.substring(0, response.length() - 3);
                    response = response + "]";
                } else {
                    r.sendResponseHeaders(404, -1);
                    return;
                }
            } else {
                if (_id.length() == 24) {
                    MongoCursor<Document> cursor = collection.find(eq("_id", new ObjectId(_id))).iterator();
                    if (cursor.hasNext()) {
                        response = cursor.next().toJson();
                    } else {
                        r.sendResponseHeaders(404, -1);
                        return;
                    }
                } else {
                    r.sendResponseHeaders(404, -1);
                    return;
                }
            }

            byte[] bs = response.getBytes("UTF-8");
            r.sendResponseHeaders(200, bs.length);
            OutputStream os = r.getResponseBody();
            os.write(bs);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void deletePost(HttpExchange r, MongoCollection<Document> collection) {
        try {

            String _id = "";

            // Convert Body to JSON Object
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

            if (_id.length() == 24) {
                DeleteResult myDoc = collection.deleteOne(eq("_id", new ObjectId(_id)));
                if (myDoc.getDeletedCount() == 0) {
                    r.sendResponseHeaders(404, -1);
                    return;
                }
                r.sendResponseHeaders(200, 0);
                OutputStream os = r.getResponseBody();
                os.write("".getBytes());
                os.close();

            } else {
                r.sendResponseHeaders(404, -1);
                return;
            }
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