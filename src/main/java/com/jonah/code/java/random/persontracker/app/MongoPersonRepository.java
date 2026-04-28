package com.jonah.code.java.random.persontracker.app;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoPersonRepository implements AutoCloseable {
    private final MongoClient client;
    private final MongoCollection<Document> peopleCollection;

    public MongoPersonRepository(String connectionString, String databaseName) {
        this.client = MongoClients.create(connectionString);
        MongoDatabase database = client.getDatabase(databaseName);
        this.peopleCollection = database.getCollection("people");
        this.peopleCollection.createIndex(Indexes.ascending("nameLower"), new IndexOptions().unique(true));
    }

    public List<TrackerPerson> findAll() {
        List<TrackerPerson> people = new ArrayList<>();
        FindIterable<Document> docs = peopleCollection.find().sort(new Document("name", 1));
        for (Document doc : docs) {
            people.add(fromDocument(doc));
        }
        return people;
    }

    public Optional<TrackerPerson> findById(String id) {
        Document doc = peopleCollection.find(Filters.eq("_id", id)).first();
        return doc == null ? Optional.empty() : Optional.of(fromDocument(doc));
    }

    public boolean existsByName(String name) {
        return peopleCollection.find(Filters.eq("nameLower", name.trim().toLowerCase())).first() != null;
    }

    public TrackerPerson save(TrackerPerson person) {
        peopleCollection.replaceOne(
                Filters.eq("_id", person.getId()),
                toDocument(person),
                new ReplaceOptions().upsert(true)
        );
        return person;
    }

    public void deleteAll() {
        peopleCollection.deleteMany(new Document());
    }

    private Document toDocument(TrackerPerson person) {
        return new Document("_id", person.getId())
                .append("name", person.getName())
                .append("nameLower", person.getName() == null ? "" : person.getName().trim().toLowerCase())
                .append("address", person.getAddress())
                .append("phone", person.getPhone())
                .append("gender", person.getGender())
                .append("age", person.getAge())
                .append("married", person.isMarried())
                .append("divorced", person.isDivorced())
                .append("single", person.isSingle())
                .append("spouseId", person.getSpouseId())
                .append("yrDivorced", person.getYrDivorced())
                .append("history", person.getHistory());
    }

    @SuppressWarnings("unchecked")
    private TrackerPerson fromDocument(Document doc) {
        TrackerPerson person = new TrackerPerson();
        person.setId(doc.getString("_id"));
        person.setName(doc.getString("name"));
        person.setAddress(doc.getString("address"));
        person.setPhone(doc.getString("phone"));
        person.setGender(doc.getString("gender"));
        person.setAge(doc.getInteger("age"));
        person.setMarried(Boolean.TRUE.equals(doc.getBoolean("married")));
        person.setDivorced(Boolean.TRUE.equals(doc.getBoolean("divorced")));
        person.setSingle(!Boolean.FALSE.equals(doc.getBoolean("single")));
        person.setSpouseId(doc.getString("spouseId"));
        person.setYrDivorced(doc.getString("yrDivorced"));
        Object history = doc.get("history");
        if (history instanceof List<?>) {
            List<String> historyList = new ArrayList<>();
            for (Object item : (List<Object>) history) {
                historyList.add(String.valueOf(item));
            }
            person.setHistory(historyList);
        }
        return person;
    }

    @Override
    public void close() {
        client.close();
    }
}
