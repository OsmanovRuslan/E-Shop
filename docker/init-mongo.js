db = db.getSiblingDB('order_db');
db.createUser(
        {
            user: "mongo",
            pwd: "mongo",
            roles: [
                {
                    role: "readWrite",
                    db: "order_db"
                }
            ]
        }
);
db.createCollection('orders');