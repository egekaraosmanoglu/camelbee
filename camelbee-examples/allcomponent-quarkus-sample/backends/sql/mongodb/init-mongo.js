db.createUser(
  {
    user: "mongouser",
    pwd: "password",
    roles: [
      {
        role: "readWrite",
        db: "camelbee"
      }
    ]
  }
);
db.createCollection("musicians-in", { capped: true, size: 100000 } );
db.createCollection("musicians-out", { capped: true, size: 100000 } );