// Example script to output Elastic Social personal user data stored in MongoDB.
//
// This is a script for Mongo Shell. It needs to be started with a connection to the
// Elastic Social models database. The name of the user must be passed to the script
// as variable userName.
//
// When authentication is enabled for MongoDB, the corresponding credentials must be passed as username (-u) and
// password (-p) together with the authenticationDatabase "admin".
//
// For example, to output data of user "paul" for the tenant "corporate" stored in
// a locally running MongoDB, invoke the script as follows:
//
// mongo localhost:27017/blueprint_corporate_models -u [mongodb_user] -p [mongodb_password] --authenticationDatabase admin
// --quiet --eval "var userName='paul'" dump-es-user-data.js
//
// See also section "Administration and Operation | Administration | Stored Personal Data"
// in the Elastic Social manual.

// check that a userName has been set
if (typeof userName === 'undefined') {
  print('userName not set. This script must be called with Mongo Shell, MongoDB credentials and the global variable ' +
          '\'userName\', for example:\n mongo localhost:27017/blueprint_corporate_models -u [mongodb_user] -p [mongodb_password] ' +
          '--authenticationDatabase admin --quiet --eval "var userName=\'paul\'" dump-es-user-data.js');
  quit(1);
}

let dbName = db.getName();
let dbNameArgs = dbName.split('_');

// check that we're connected to the models database
if (dbNameArgs.length !== 3 || dbNameArgs[2] !== 'models') {
  print(`Script must be called with Mongo Shell connected to Elastic Social models database but it is connected to 
         ${dbName}. The database name should be [prefix]_[tenant]_models (for example: blueprint_corporate_models)`);
  quit(1);
}

let prefix = dbNameArgs[0];
let tenant = dbNameArgs[1];
counterDb = db.getSiblingDB(`${prefix}_${tenant}_counters`);

// try and find the user
let user = db.users.findOne({ "name" : userName});

if (!user) {
  print("User does not exist: " + userName);
  quit(1);
}

print(`Stored Personal Data of User: '${userName}'`);

let userId = user._id;
delete user._id;
delete user._version;
delete user.passwordHash;

let imageId;
if (user.image) {
  imageId = user.image.id;
  delete user.image;
}

let attachments = [];
let addAttachment = function(a) { attachments.push(a.id) };

print('\n### User Profile');
printData(user);

print('### Internal User Notes');
findUserData(db.notes, "user", userId, printData);

print('### Ratings');
findUserData(db.ratings, "author", userId, printData);

print('### Likes');
findUserData(db.likes, "author", userId, printData);

print('### Reviews');
findCommentsAndComplaints(db.reviews, userId, printData, addAttachment);

print('### Shares');
findUserData(db.shares, "author", userId, printData);

print('### Complaints (reported by the user)');
findUserData(db.complaints, "author", userId, printData);

print('### Complaints (reported about the user)');
findUserData(db.complaints, "target", userId, printData);

print('### Comments');
findCommentsAndComplaints(db.comments, userId, printData, addAttachment);

print('### Counters');
findCounter(counterDb.counters, userId, "user:number_of_logins", function(v) { print(`Number of Logins: ${v}`) });
findCounter(counterDb.counters, userId, "comments:approvedComments", function(v) { print(`Approved Comments: ${v}`) });
findCounter(counterDb.counters, userId, "comments:rejectedComments", function(v) { print(`Rejected Comments: ${v}`) });
findCounter(counterDb.counters, userId, "reviews:approvedReviews", function(v) { print(`Approved Reviews: ${v}`) });
findCounter(counterDb.counters, userId, "reviews:rejectedReviews", function(v) { print(`Rejected Reviews: ${v}`) });

print('\n### Binary Data');
if (imageId) {
  print('The user has uploaded a profile image. Use the following command to fetch it:');
  print(`  mongofiles --host [hostname] --port [port] -u [mongodb_user] -p [mongodb_password] --authenticationDatabase admin 
  -d ${prefix}_${tenant}_blobs get_id 'ObjectId("${imageId}")'`);
}
// remove duplicate attachment ids
attachments = attachments.filter(function(value, index, self) { return self.indexOf(value) === index; });
if (attachments.length > 0) {
  print('The user has uploaded attachments for comments or reviews. Use the following commands to fetch them:');
  attachments.forEach(function(a) {
      print(`  mongofiles --host [hostname] --port [port] -u [mongodb_user] -p [mongodb_password] --authenticationDatabase admin 
      -d ${prefix}_${tenant}_blobs get_id 'ObjectId("${a}")'`);
  });
}

function findCommentsAndComplaints(collection, userId, commentFunction, attachmentFunction) {
  collection.find({"author.id": userId, "author.collection": "users"}, { "_version": 0, "author": 0})
          .forEach(function(x) {
            let id = x._id;
            delete x._id;

            x.attachments && x.attachments.forEach(attachmentFunction);
            x._lastVersion && x._lastVersion.attachments && x._lastVersion.attachments.forEach(attachmentFunction);

            x.complaints = [];
            db.complaints.find({"target.id": id, "target.collection": collection.getName()},
                    {"_id": 0, "_version": 0, "target": 0, "category": 0})
                    .forEach(function(c) { x.complaints.push(c) });

            commentFunction(x);
          })
}

function findUserData(collection, keyPrefix, userId, fun) {
  let query = {};
  query[keyPrefix + ".id"] = userId;
  query[keyPrefix + ".collection"] = "users";
  let fields = {
    "_id": 0,
    "_version": 0
  };
  fields[keyPrefix] = 0;
  collection.find(query, fields).forEach(fun);
}

function findCounter(collection, userId, name, fun) {
  let counter = collection.findOne({"name": name, "target.id": userId, "target.collection": "users"});
  (counter && counter.value) ? fun(counter.value.valueOf()) : fun(0);
}

function printData(x) {
  printjson(x);
  print();
}
