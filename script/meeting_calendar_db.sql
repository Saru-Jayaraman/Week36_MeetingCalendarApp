create schema if not exists meeting_calendar_db;
use meeting_calendar_db;

create table users(
username varchar(255) not null primary key,
_password varchar(255) not null,
expired tinyint default false,
create_date datetime default now()
);

create table calendars(
id int not null auto_increment primary key,
username varchar(255) not null,
title varchar(255) not null,
create_date datetime default now(),
foreign key (username) references users(username)
);

create table meetings(
id int not null auto_increment primary key,
title varchar(255) not null,
start_time datetime not null,
end_time datetime not null,
_description text,
calendar_id int not null,
create_date datetime default now(),
foreign key (calendar_id) references calendars(id)
);