#!/usr/bin/env bash

set -eu

## Exact2sheets db
psql -c "CREATE ROLE exact2sheetsuser WITH PASSWORD 'password' CREATEDB LOGIN;"
psql --command="GRANT exact2sheetsuser TO postgres;"
psql -c "CREATE DATABASE exact2sheets WITH OWNER = exact2sheetsuser TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';"

psql --dbname="exact2sheets" --command="GRANT ALL PRIVILEGES ON DATABASE exact2sheets TO exact2sheetsuser;";
psql --dbname="exact2sheets" --command="GRANT USAGE ON SCHEMA public TO exact2sheetsuser;"
