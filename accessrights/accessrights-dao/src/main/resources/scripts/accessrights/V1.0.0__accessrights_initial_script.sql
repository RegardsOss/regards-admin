create table t_access_settings (id int8 not null, mode varchar(255), primary key (id));
create table t_metadata (id int8 not null, key varchar(64), value varchar(255), visibility varchar(255), user_id int8, primary key (id));
create table t_project_user (id int8 not null, email varchar(128), last_connection timestamp, last_update timestamp, licence_accepted boolean, status varchar(30), role_id int8, primary key (id));
create table t_resources_access (id int8 not null, controller_name varchar(32), defaultRole varchar(16), description text, microservice varchar(32), resource varchar(512), verb varchar(10), user_id int8, primary key (id));
create table t_role (id int8 not null, authorized_addresses varchar(255), is_default boolean, is_native boolean, name varchar(255), parent_role_id int8, primary key (id));
create table ta_resource_role (role_id int8 not null, resource_id int8 not null, primary key (role_id, resource_id));
alter table t_metadata add constraint uk_metadata_key_user_id unique (key, user_id);
alter table t_project_user add constraint uk_project_user_email unique (email);
create index idx_role_name on t_role (name);
alter table t_role add constraint uk_role_name unique (name);
create sequence seq_access_settings start 1 increment 50;
create sequence seq_metadata start 1 increment 50;
create sequence seq_project_user start 1 increment 50;
create sequence seq_resources_access start 1 increment 50;
create sequence seq_role start 1 increment 50;
alter table t_metadata add constraint fk_user_metadata foreign key (user_id) references t_project_user;
alter table t_project_user add constraint fk_user_role foreign key (role_id) references t_role;
alter table t_resources_access add constraint fk_user_permissions foreign key (user_id) references t_project_user;
alter table t_role add constraint fk_role_parent_role foreign key (parent_role_id) references t_role;
alter table ta_resource_role add constraint fk_resource_role_resource_id foreign key (resource_id) references t_resources_access;
alter table ta_resource_role add constraint fk_resource_role_role_id foreign key (role_id) references t_role;
