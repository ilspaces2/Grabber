CREATE TABLE company
(
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);

insert into company (id,name) values (1,'Philips');
insert into company (id,name) values (2,'Samsung');
insert into company (id,name) values (3,'IBM');
insert into company (id,name) values (4,'Apple');
insert into company (id,name) values (5,'Asus');

insert into person (id,name,company_id) values (1,'Anna',1);
insert into person (id,name,company_id) values (2,'Ivan',1);
insert into person (id,name,company_id) values (3,'Petr',2);
insert into person (id,name,company_id) values (4,'Victor',4);
insert into person (id,name,company_id) values (5,'Ilya',3);
insert into person (id,name,company_id) values (6,'Maria',3);
insert into person (id,name,company_id) values (7,'Anton',5);
insert into person (id,name,company_id) values (8,'Alex',5);
insert into person (id,name,company_id) values (9,'Max',1);
insert into person (id,name,company_id) values (10,'Victoria',3);

/*1*/
select p.name p_name, c.name c_name 
from person p join company c
on p.company_id=c.id where c.id!=5;

/*2*/
with tmp as (
select c.name, count(p.company_id) count_person
from person p join company c
on p.company_id=c.id
group by c.name)
select * from tmp where tmp.count_person = (select max(count_person) from tmp);
