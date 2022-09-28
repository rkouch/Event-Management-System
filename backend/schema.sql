create table users (
    id          int not null,
    email       varchar(255) not null,
    `name`        varchar(255) not null,
    `password`    varchar(255) not null,
    username    varchar(255) not null,
    dob         datetime not null,
    is_host      boolean,
    primary key (id)
);

create table location (
    id          int not null,
    street_no    int,
    unit_no      varchar(255),
    street_name  varchar(255),
    postcode    varchar(255) not null,
    state       char(255) not null,
    country     char(255) not null,
    longitude   varchar(255) not null,
    latitude    varchar(255) not null,
    primary key (id)
);

create table `events` (
    id          int not null,
    host_id      int not null,
    location_id  int,
    event_name   varchar(255) not null,
    event_date        datetime not null,
    event_description text,
    seat_availability int,
    has_seats    boolean not null,
    primary key (id),
    foreign key (host_id) references users(id),
    foreign key (location_id) references location(id)
);

create table admins (
    id          int not null,
    event_id     int not null,
    user_id      int not null,
    primary key (id),
    foreign key (event_id) references `events` (id),
    foreign key (user_id) references users(id)
);

create table `groups` (
    id          int not null,
    leader_id    int not null,
    size        int not null,
    time_created timestamp,
    ticket_available int,
    primary key (id),
    foreign key (leader_id) references users(id)
);

create table seating_plan (
    id          int not null,
    event_id     int not null,
    location_id  int not null,
    section     varchar(255) not null,
    available_seats int not null,
    primary key (id)
);

create table tickets (
    id          int not null,
    user_id      int not null,
    event_id     int not null,
    section_id    int not null,
    seat_no      int,
    reserved   boolean,
    primary key (id),
    foreign key (user_id) references users(id),
    foreign key (event_id) references `events`(id),
    foreign key (section_id) references seating_plan(id)
);

create table categories (
    id          int not null,
    event_id     int not null,
    category    varchar(255) not null,
    primary key (id),
    foreign key (event_id) references `events`(id)
);

create table tags (
    id          int not null,
    event_id     int not null,
    tags        varchar(255) not null,
    primary key (id),
    foreign key (event_id) references `events`(id)
);

/*create table reviews (
     id          int not null,
     event_id    int not null,
     author_id   int not null,
     description text not null,
     primary key (id),
     foreign key (event_id) references events(id)
);

create table replies (
    id int not null,
    review_id int not null,
    author_id  int not null,
    comment_text text not null,
    primary key (id),
    foreign key (review_id) references reviews(id),
    foreign key  (author_id) references users(id)
);*/

create table event_comments (
    id int not null,
    event_id int not null,
    parent_id int,
    author_id int not null,
    comment_text text not null,
    comment_time timestamp not null,

    primary key (id),
    foreign key (event_id) references `events`(id),
    foreign key (parent_id) references event_comments(id),
    foreign key (author_id) references users(id)
);

create table reactions (
    id int not null,
    comment_id int not null,
    author_id int not null,
    react_time timestamp not null,
    react_type char(255) not null,

    primary key (id),
    foreign key (comment_id) references event_comments(id),
    foreign key (author_id) references users(id)
);
create table TestTable
(
    id    int auto_increment,
    name  varchar(1024) null,
    email varchar(1024) null,
    constraint TestTable_pk
        primary key (id)
);

