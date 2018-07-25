import { API_KEY, API_SECRET } from './constants';

const OpenTok = require('opentok');
const express = require('express');
const cors = require('cors');

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

app.get('/token/:id', function(req, res) {
    const sessionId = req.params.id;
    token = opentok.generateToken(sessionId);
    res.send({token: token});
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