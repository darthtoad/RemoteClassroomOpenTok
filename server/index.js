const OpenTok = require('opentok');
const express = require('express');
const cors = require('cors');
require('dotenv').config();

const API_KEY = process.env.API_KEY;
const API_SECRET = process.env.API_SECRET;
const PASSWORD = process.env.PASSWORD;
const firebaseConfig = {
    apiKey: process.env.FIREBASE_API_KEY,
    authDomain: process.env.FIREBASE_AUTH_DOMAIN,
    databaseURL: process.env.FIREBASE_DATABASE_URL,
    projectId: process.env.FIREBASE_PROJECT_ID,
    storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
    messagingSenderId: process.env.FIREBASE_MESSAGING_SENDER_ID
};

const port = 3000;
const opentok = new OpenTok(API_KEY, API_SECRET);
const app = express();
app.use(cors());
let token;

app.get('/session', function(req, res) {
    opentok.createSession(function(err, session) {
        if (err) {
            return console.log(err);
        } else {
            res.send(session);
        }
    });      
})

app.get('/:password', function(req, res) {
    if (req.params.password === PASSWORD) {
        res.send({message: "Password Correct"});
    } else {
        resn.send({message: "Incorrect Password"});
    }
})

app.get('/token/:id', function(req, res) {
    const sessionId = req.params.id;
    token = opentok.generateToken(sessionId);
    res.send({token: token});
})

app.get('/firebase/:password', function(req, res) {
    if (req.params.password === PASSWORD) {
        res.send(firebaseConfig);
    } else {
        res.send({error: "Wrong Password"})
    }
})

app.get('/OpenTok/:password', function(req, res) {
    if (req.params.password === PASSWORD) {
        res.send({
            "API_KEY": API_KEY,
            "API_SECRET": API_SECRET
        });
    } else {
        res.send({error: "Wrong Password"})
    }
})

const requestHandler = (request, response) => {
  console.log(request.url);
  response.end('Hello Node.js Server!');
}

app.listen(port, (err) => {
  if (err) {
    return console.log('something bad happened', err);
  }

  console.log(`server is listening on ${port}`);
})