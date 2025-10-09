#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE keycloak;
    CREATE USER keycloak_user WITH ENCRYPTED PASSWORD 'hGpHM3C8bdH3pz';
    GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak_user;

    \connect keycloak;
    CREATE SCHEMA IF NOT EXISTS keycloak AUTHORIZATION keycloak_user;
    ALTER SCHEMA keycloak OWNER TO keycloak_user;
    ALTER DATABASE keycloak SET search_path TO keycloak;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE DATABASE waitque;
    CREATE USER waitque_api WITH ENCRYPTED PASSWORD 'zDsCDGN96h7J';
    GRANT ALL PRIVILEGES ON DATABASE waitque TO waitque_api;

    CREATE SCHEMA IF NOT EXISTS waitque;
    ALTER ROLE waitque_api SET search_path TO waitque;
EOSQL