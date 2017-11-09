create table t_email_verification_token (id int8 not null, expiry_date timestamp, origin_url varchar(255), request_link varchar(255), token varchar(255), verified boolean, project_user_id int8 not null, primary key (id));
alter table t_email_verification_token add constraint uk_email_verification_token_project_user_id unique (project_user_id);
alter table t_email_verification_token add constraint fk_email_verification_token foreign key (project_user_id) references t_project_user;
create sequence seq_email_verification_token start 1 increment 50;