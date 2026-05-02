# Person Tracker

This repository now includes a cloud-ready setup:

- GitHub Pages for the frontend
- MongoDB Atlas for shared data
- Render for the hosted API

## Architecture

- GitHub Pages serves the static frontend
- Render runs the Java app and serves `/api/*`
- MongoDB stores the shared people data
- every computer sees the same records when it uses the same API base URL

## Deploy the frontend to GitHub Pages

1. Push the repo to GitHub.
2. In the repository settings, open `Pages`.
3. Set the source to `Deploy from a branch`.
4. Choose the `main` branch and the `/ (root)` folder.
5. Save.

## Deploy the API to Render

Render officially supports managing services with `render.yaml` Blueprints, including Docker-based web services and `healthCheckPath`, so this repo includes a ready-made [render.yaml](c:/Users/jonah/code/java/random/persontracker/render.yaml). Source: Render Blueprint docs and YAML reference.

1. Create a MongoDB Atlas cluster and database user.
2. Copy your Atlas Java driver connection string from Atlas.
   MongoDB’s official docs say to get this from `Connect` -> `Drivers`, and to use the Atlas connection string for your application.
3. In Atlas Network Access, allow connections from your hosted API.
   For the simplest public setup, allow the Render service to connect per Atlas network access rules.
4. In Render, create a new Blueprint from this repo.
5. When prompted, set:
   `PERSON_TRACKER_MONGO_URI` = your Atlas connection string
   `PERSON_TRACKER_ALLOWED_ORIGIN` = your exact GitHub Pages origin, such as `https://YOUR-USER.github.io`
6. Deploy the Blueprint.
7. Copy the Render API URL, such as `https://persontracker-api.onrender.com`.

## Connect the frontend to the cloud API

You have two ways to point the GitHub Pages app at the hosted backend:

1. Quick way:
   Open the Pages site, paste the Render URL into the `API base URL` field, and save it.

2. Repo-configured way:
   Edit [html/config.js](c:/Users/jonah/code/java/random/persontracker/html/config.js) and set:

```js
window.PERSON_TRACKER_CONFIG = {
    apiBaseUrl: "https://your-render-service.onrender.com"
};
```

Then push again so every browser uses the same backend automatically.

## What ships

- `index.html` redirects GitHub Pages visitors to the app
- `html/index.html` contains the GitHub Pages frontend
- `html/config.js` lets you hardcode the hosted API URL for all clients
- `src/main/java/.../PersonTrackerServer.java` exposes the shared API
- `Dockerfile` packages the Java API for cloud hosting
- `render.yaml` defines the Render web service
- MongoDB holds the shared data

## Important note

I checked MongoDB’s official docs first: Atlas App Services / Data API / HTTPS Endpoints are deprecated or end-of-life, so this repo does not use that path. The supported setup here is MongoDB Atlas plus a separately hosted API.
