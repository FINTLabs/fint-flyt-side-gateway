create table dispatch_context (
    id varchar(255) primary key,
    source_application_integration_id varchar(255) not null,
    source_application_instance_id varchar(255) not null,
    callback_url text not null
);

create table dispatch_receipt (
    id varchar(255) primary key,
    source_application_integration_id varchar(255) not null,
    source_application_instance_id varchar(255) not null,
    callback_url text not null,
    payload text not null
);
