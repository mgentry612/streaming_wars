create table demographic (
    short_name text primary key,
    long_name text,
    num_accounts integer
);

create table studio (
    short_name text primary key,
    long_name text
);

create table event (
     id serial primary key,
     name text,
     year_produced integer,
     license_fee integer,
     studio text references studio(short_name),
     event_type text,
     duration integer,
     UNIQUE (name, year_produced)
);

create table streaming_service (
    short_name text primary key,
    long_name text,
    subscription_price decimal
);

create table sss_transaction(
    id serial primary key,
    streaming_service text references streaming_service(short_name),
    event_id integer references event(id),
    month timestamp without time zone,
    transaction_amount decimal
);

create table ssd_transaction(
    id serial primary key,
    streaming_service text references streaming_service(short_name),
    event_id integer references event(id),
    month timestamp without time zone,
    transaction_amount decimal,
    demographic_percentage integer,
    demographic text references demographic(short_name)
);

create table offering(
    id serial primary key,
    sss_transaction_id integer references sss_transaction(id),
    offerings_order_counter integer,
    viewing_price integer
);

create table user_info(
    id serial primary key,
    user_name text,
    user_pw text,
    roles text
);

create table audit_log(
    id serial primary key,
    action_type text,
    action_content text,
    username text,
    created_date text
);