package com.jonah.code.java.random.persontracker.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.Executors;

public class PersonTrackerServer {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PERSON_TRACKER_PORT", "8080"));
    private static final String MONGO_URI = System.getenv().getOrDefault("PERSON_TRACKER_MONGO_URI", "mongodb://localhost:27017");
    private static final String MONGO_DB = System.getenv().getOrDefault("PERSON_TRACKER_MONGO_DB", "persontracker");

    public static void main(String[] args) throws IOException {
        MongoPersonRepository repository = new MongoPersonRepository(MONGO_URI, MONGO_DB);
        Runtime.getRuntime().addShutdownHook(new Thread(repository::close));

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/people", exchange -> handlePeople(exchange, repository));
        server.createContext("/api/marriages", exchange -> handleMarriage(exchange, repository));
        server.createContext("/api/divorces", exchange -> handleDivorce(exchange, repository));
        server.createContext("/", PersonTrackerServer::handleStatic);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Person Tracker server listening on http://localhost:" + PORT);
        System.out.println("MongoDB: " + MONGO_URI + " / database: " + MONGO_DB);
    }

    private static void handlePeople(HttpExchange exchange, MongoPersonRepository repository) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange)) {
            return;
        }

        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 200, repository.findAll());
            return;
        }

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                TrackerPerson person = readJson(exchange, TrackerPerson.class);
                String validationError = validatePerson(person, repository);
                if (validationError != null) {
                    sendError(exchange, 400, validationError);
                    return;
                }

                if (person.getId() == null || person.getId().isBlank()) {
                    person.setId(new TrackerPerson().getId());
                }
                person.setName(person.getName().trim());
                person.setAddress(person.getAddress().trim());
                person.setPhone(normalizePhone(person.getPhone()));
                person.setSingle(true);
                person.setMarried(false);
                person.setDivorced(false);
                person.setSpouseId(null);
                person.setYrDivorced(null);
                if (person.getHistory().isEmpty()) {
                    person.getHistory().add("Created record on " + timestamp());
                }

                repository.save(person);
                sendJson(exchange, 201, person);
                return;
            } catch (IOException error) {
                sendError(exchange, 400, "Invalid JSON body.");
                return;
            }
        }

        if ("DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            repository.deleteAll();
            sendJson(exchange, 200, new MessageResponse("All people removed."));
            return;
        }

        sendError(exchange, 405, "Method not allowed.");
    }

    private static void handleMarriage(HttpExchange exchange, MongoPersonRepository repository) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange)) {
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed.");
            return;
        }

        try {
            MarriageRequest request = readJson(exchange, MarriageRequest.class);
            if (request.getPerson1Id() == null || request.getPerson2Id() == null) {
                sendError(exchange, 400, "Both people are required.");
                return;
            }
            if (request.getPerson1Id().equals(request.getPerson2Id())) {
                sendError(exchange, 400, "A person cannot marry themselves.");
                return;
            }

            Optional<TrackerPerson> person1Opt = repository.findById(request.getPerson1Id());
            Optional<TrackerPerson> person2Opt = repository.findById(request.getPerson2Id());
            if (person1Opt.isEmpty() || person2Opt.isEmpty()) {
                sendError(exchange, 404, "One of the selected people could not be found.");
                return;
            }

            TrackerPerson person1 = person1Opt.get();
            TrackerPerson person2 = person2Opt.get();
            if (person1.isMarried() || person2.isMarried()) {
                sendError(exchange, 400, "One of those people is already marked as married.");
                return;
            }

            person1.setMarried(true);
            person2.setMarried(true);
            person1.setDivorced(false);
            person2.setDivorced(false);
            person1.setSingle(false);
            person2.setSingle(false);
            person1.setSpouseId(person2.getId());
            person2.setSpouseId(person1.getId());
            person1.setYrDivorced(null);
            person2.setYrDivorced(null);
            person1.getHistory().add("Married " + person2.getName() + " on " + timestamp());
            person2.getHistory().add("Married " + person1.getName() + " on " + timestamp());

            repository.save(person1);
            repository.save(person2);
            sendJson(exchange, 200, new MessageResponse(person1.getName() + " and " + person2.getName() + " are now marked as married."));
        } catch (IOException error) {
            sendError(exchange, 400, "Invalid JSON body.");
        }
    }

    private static void handleDivorce(HttpExchange exchange, MongoPersonRepository repository) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange)) {
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed.");
            return;
        }

        try {
            DivorceRequest request = readJson(exchange, DivorceRequest.class);
            if (request.getPerson1Id() == null || request.getPerson2Id() == null || request.getYear() == null || request.getYear().isBlank()) {
                sendError(exchange, 400, "Two people and a divorce year are required.");
                return;
            }
            if (request.getPerson1Id().equals(request.getPerson2Id())) {
                sendError(exchange, 400, "Choose two different people.");
                return;
            }

            Optional<TrackerPerson> person1Opt = repository.findById(request.getPerson1Id());
            Optional<TrackerPerson> person2Opt = repository.findById(request.getPerson2Id());
            if (person1Opt.isEmpty() || person2Opt.isEmpty()) {
                sendError(exchange, 404, "One of the selected people could not be found.");
                return;
            }

            TrackerPerson person1 = person1Opt.get();
            TrackerPerson person2 = person2Opt.get();
            boolean linkedMarriage = request.getPerson2Id().equals(person1.getSpouseId())
                    && request.getPerson1Id().equals(person2.getSpouseId());
            if (!linkedMarriage) {
                sendError(exchange, 400, "Those two people are not currently linked as spouses.");
                return;
            }

            String year = request.getYear().trim();
            person1.setDivorced(true);
            person2.setDivorced(true);
            person1.setMarried(false);
            person2.setMarried(false);
            person1.setSingle(true);
            person2.setSingle(true);
            person1.setYrDivorced(year);
            person2.setYrDivorced(year);
            person1.getHistory().add("Divorced " + person2.getName() + " in " + year);
            person2.getHistory().add("Divorced " + person1.getName() + " in " + year);
            person1.setSpouseId(null);
            person2.setSpouseId(null);

            repository.save(person1);
            repository.save(person2);
            sendJson(exchange, 200, new MessageResponse(person1.getName() + " and " + person2.getName() + " are now marked as divorced."));
        } catch (IOException error) {
            sendError(exchange, 400, "Invalid JSON body.");
        }
    }

    private static void handleStatic(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);
        if (handleOptions(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed.");
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        Path base = Path.of("html").toAbsolutePath().normalize();
        Path file = "/".equals(requestPath)
                ? base.resolve("index.html")
                : base.resolve(requestPath.substring(1)).normalize();

        if (!file.startsWith(base) || !Files.exists(file) || Files.isDirectory(file)) {
            sendError(exchange, 404, "Not found.");
            return;
        }

        String contentType = file.toString().endsWith(".html") ? "text/html; charset=utf-8" : "text/plain; charset=utf-8";
        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static <T> T readJson(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return MAPPER.readValue(inputStream, type);
        } catch (JsonProcessingException error) {
            throw new IOException("Invalid JSON body.", error);
        }
    }

    private static String validatePerson(TrackerPerson person, MongoPersonRepository repository) {
        if (person == null) {
            return "Request body is required.";
        }
        if (person.getName() == null || person.getName().trim().isEmpty()) {
            return "Name is required.";
        }
        if (person.getAddress() == null || person.getAddress().trim().isEmpty()) {
            return "Address is required.";
        }
        String phone = normalizePhone(person.getPhone());
        if (!phone.matches("\\d{10}")) {
            return "Phone number must be exactly 10 digits.";
        }
        if ("555".equals(phone.substring(3, 6))) {
            return "Phone number cannot use a 555 exchange.";
        }
        if (repository.existsByName(person.getName())) {
            return "That person already exists in the tracker.";
        }
        return null;
    }

    private static String normalizePhone(String input) {
        return input == null ? "" : input.replaceAll("\\D", "");
    }

    private static String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJson(exchange, statusCode, new MessageResponse(message));
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static final class MessageResponse {
        private final String message;

        private MessageResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
