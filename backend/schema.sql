create table users (
    id          varchar(36) not null,
    email       varchar(255) not null,
    `name`        varchar(255) not null,
    `password`    varchar(255) not null,
    username    varchar(255) not null,
    dob         datetime not null,
    is_host      boolean,
    primary key (id)
);

create table locations (
    id          varchar(36) not null,
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
    id          varchar(36) not null,
    host_id      varchar(36) not null,
    location_id  varchar(36),
    event_name   varchar(255) not null,
    event_date        datetime not null,
    event_description text,
    seat_availability int,
/*    has_seats    boolean not null,*/
    primary key (id),
    foreign key (host_id) references users(id),
    foreign key (location_id) references locations(id)
);

create table admins (
    id          varchar(36) not null,
    event_id     varchar(36) not null,
    user_id      varchar(36) not null,
    primary key (id),
    foreign key (event_id) references `events` (id),
    foreign key (user_id) references users(id)
);

create table `groups` (
    id          varchar(36) not null,
    leader_id    varchar(36) not null,
    size        int not null,
    time_created timestamp,
    ticket_available int,
    primary key (id),
    foreign key (leader_id) references users(id)
);

create table seating_plan (
    id          varchar(36) not null,
    event_id     varchar(36) not null,
    location_id  varchar(36) not null,
    section     varchar(255) not null,
    available_seats int not null,
    primary key (id)
);

create table tickets (
    id          varchar(36) not null,
    user_id      varchar(36) not null,
    event_id     varchar(36) not null,
    section_id    varchar(36) not null,
    seat_no      int,
    reserved   boolean,
    primary key (id),
    foreign key (user_id) references users(id),
    foreign key (event_id) references `events`(id),
    foreign key (section_id) references seating_plan(id)
);

create table categories (
    id          varchar(36) not null,
    event_id     varchar(36) not null,
    category    varchar(255) not null,
    primary key (id),
    foreign key (event_id) references `events`(id)
);

create table tags (
    id          varchar(36) not null,
    event_id     varchar(36) not null,
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
    id varchar(36) not null,
    event_id varchar(36) not null,
    parent_id varchar(36),
    author_id varchar(36) not null,
    comment_text text not null,
    comment_time timestamp not null,

    primary key (id),
    foreign key (event_id) references `events`(id),
    foreign key (parent_id) references event_comments(id),
    foreign key (author_id) references users(id)
);

create table reactions (
    id varchar(36) not null,
    comment_id varchar(36) not null,
    author_id varchar(36) not null,
    react_time timestamp not null,
    react_type char(255) not null,

    primary key (id),
    foreign key (comment_id) references event_comments(id),
    foreign key (author_id) references users(id)
);

create table group_users (
    id varchar(36) not null,
    group_id varchar(36) not null,
    user_id varchar(36) not null,

    primary key (id),
    foreign key (group_id) references `groups`(id),
    foreign key (user_id) references users(id)
);

create table TestTable
(
    id    varchar(36),
    name  varchar(1024) null,
    email varchar(1024) null,
    constraint TestTable_pk
        primary key (id)
);

