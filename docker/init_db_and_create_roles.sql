CREATE DATABASE user_db;
CREATE DATABASE product_db;
CREATE DATABASE notification_db;

CREATE ROLE role_user WITH LOGIN PASSWORD 'user_db';
CREATE ROLE role_product WITH LOGIN PASSWORD 'product_db';
CREATE ROLE role_notification WITH LOGIN PASSWORD 'notification_db';

GRANT ALL PRIVILEGES ON DATABASE user_db TO role_user;
GRANT ALL PRIVILEGES ON DATABASE product_db TO role_product;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO role_notification;

\c user_db

CREATE SCHEMA IF NOT EXISTS public;
GRANT CREATE, USAGE ON SCHEMA public TO role_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO role_user;

\c product_db

CREATE SCHEMA IF NOT EXISTS public;
GRANT CREATE, USAGE ON SCHEMA public TO role_product;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO role_product;

\c notification_db

CREATE SCHEMA IF NOT EXISTS public;
GRANT CREATE, USAGE ON SCHEMA public TO role_notification;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO role_notification;