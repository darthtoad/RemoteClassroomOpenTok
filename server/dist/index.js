'use strict';

var _constants = require('./constants');

var OpenTok = require('opentok');
var express = require('express');

var port = 3000;
var opentok = new OpenTok(_constants.API_KEY, _constants.API_SECRET);
var app = express();
var token = void 0;

app.get('/session', function (req, res) {
    opentok.createSession(function (err, session) {
        if (err) {
            return console.log(err);
        } else {
            res.send(session);
        }
    });
});

app.get('/token/:id', function (req, res) {
    var sessionId = req.params.id;
    token = opentok.generateToken(sessionId);
    res.send(token);
});

var requestHandler = function requestHandler(request, response) {
    console.log(request.url);
    response.end('Hello Node.js Server!');
};

app.listen(port, function (err) {
    if (err) {
        return console.log('something bad happened', err);
    }

    console.log('server is listening on ' + port);
});