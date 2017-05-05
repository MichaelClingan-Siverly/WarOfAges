use db309yt05;

CREATE TABLE users (
userID varchar(10), 
nickname varchar(10),
password varchar(15),
player tinyint,
loggedin tinyint,
playerOrder tinyint,
primary key(userID), 
unique(password));

CREATE TABLE Terrain (
AreaType varchar(20), 
terr_description varchar(50), 
primary key(AreaType));

CREATE TABLE UnitType (
UnitID int, 
Strength int, 
Health double,
Defense int, 
movement int,
UnitName varchar(20),
primary key(UnitID));

    
Create Table Map(
GridID int,
AreaType varchar(20),
primary key (GridID),
foreign key(AreaType) 
	references Terrain (AreaType));
    
Create Table UnitMap(
GridID int,
UnitID int,
health double,
userID varchar(10),
primary key (GridID),
foreign key(GridID) 
	references Map (GridID),
foreign key(userID) 
	references users (userID),
foreign key(UnitID) 
	references UnitType (UnitID));
    
Create Table AdminMap(
GridID int,
AreaType varchar(20),
primary key (GridID),
foreign key(GridID) 
	references Map (GridID),
foreign key(AreaType) 
	references Terrain (AreaType));    


insert INTO users values ('admin', 'admin', '1234', 0, 0, 0);

insert INTO users values ('Username', 'user', 'Password', 0, 0, 0);

insert INTO Terrain(AreaType, terr_description)
values ('forest','land covered in trees and bushes');

insert INTO Terrain(AreaType, terr_description)
values ('pond','a body of water you cannot cross');

insert INTO Terrain values('mountain','The tallest thing in the world');

insert INTO Terrain values('meadow','a flat grassland with flowers');

insert INTO Terrain values('town','an urban community');

insert INTO Terrain values('desert','The sand dunes stretch for miles');

insert INTO Map values (0,'town');
insert INTO Map values (1,'desert');
insert INTO Map values (2,'desert');
insert INTO Map values (3,'desert');
insert INTO Map values (4,'desert');
insert INTO Map values (5,'desert');
insert INTO Map values (6,'desert');
insert INTO Map values (7,'desert');
insert INTO Map values (8,'desert');
insert INTO Map values (9,'desert');
insert INTO Map values (10,'desert');
insert INTO Map values (11,'desert');
insert INTO Map values (12,'desert');
insert INTO Map values (13,'desert');
insert INTO Map values (14,'desert');
insert INTO Map values (15,'desert');
insert INTO Map values (16,'desert');
insert INTO Map values (17,'desert');
insert INTO Map values (18,'desert');
insert INTO Map values (19,'desert');
insert INTO Map values (20,'desert');
insert INTO Map values (21,'desert');
insert INTO Map values (22,'desert');
insert INTO Map values (23,'desert');
insert INTO Map values (24,'desert');
insert INTO Map values (25,'town');
insert INTO Map values (26,'desert');
insert INTO Map values (27,'desert');
insert INTO Map values (28,'desert');
insert INTO Map values (29,'desert');
insert INTO Map values (30,'desert');
insert INTO Map values (31,'desert');
insert INTO Map values (32,'desert');
insert INTO Map values (33,'desert');
insert INTO Map values (34,'desert');
insert INTO Map values (35,'desert');
insert INTO Map values (36,'desert');
insert INTO Map values (37,'desert');
insert INTO Map values (38,'desert');
insert INTO Map values (39,'desert');
insert INTO Map values (40,'desert');
insert INTO Map values (41,'desert');
insert INTO Map values (42,'desert');
insert INTO Map values (43,'desert');
insert INTO Map values (44,'desert');
insert INTO Map values (45,'desert');
insert INTO Map values (46,'desert');
insert INTO Map values (47,'desert');
insert INTO Map values (48,'desert');
insert INTO Map values (49,'desert');
insert INTO Map values (50,'desert');
insert INTO Map values (51,'desert');
insert INTO Map values (52,'desert');
insert INTO Map values (53,'desert');
insert INTO Map values (54,'desert');
insert INTO Map values (55,'desert');
insert INTO Map values (56,'desert');
insert INTO Map values (57,'desert');
insert INTO Map values (58,'desert');
insert INTO Map values (59,'desert');
insert INTO Map values (60,'desert');
insert INTO Map values (61,'desert');
insert INTO Map values (62,'desert');
insert INTO Map values (63,'desert');
insert INTO Map values (64,'desert');
insert INTO Map values (65,'desert');
insert INTO Map values (66,'desert');
insert INTO Map values (67,'desert');
insert INTO Map values (68,'desert');
insert INTO Map values (69,'desert');
insert INTO Map values (70,'desert');
insert INTO Map values (71,'desert');
insert INTO Map values (72,'desert');
insert INTO Map values (73,'desert');
insert INTO Map values (74,'desert');
insert INTO Map values (75,'desert');
insert INTO Map values (76,'desert');
insert INTO Map values (77,'desert');
insert INTO Map values (78,'desert');
insert INTO Map values (79,'desert');
insert INTO Map values (80,'desert');
insert INTO Map values (81,'desert');
insert INTO Map values (82,'desert');
insert INTO Map values (83,'desert');
insert INTO Map values (84,'desert');
insert INTO Map values (85,'desert');
insert INTO Map values (86,'desert');
insert INTO Map values (87,'desert');
insert INTO Map values (88,'desert');
insert INTO Map values (89,'desert');
insert INTO Map values (90,'desert');
insert INTO Map values (91,'desert');
insert INTO Map values (92,'desert');
insert INTO Map values (93,'desert');
insert INTO Map values (94,'desert');
insert INTO Map values (95,'desert');
insert INTO Map values (96,'desert');
insert INTO Map values (97,'desert');
insert INTO Map values (98,'desert');
insert INTO Map values (99,'desert');

insert into UnitType values(1,3,300.0,3,1,'archer');
insert into UnitType values(2,5,900.0,2,3,'calvary');
insert into UnitType values(3,4,600.0,4,2,'swordman');
insert into UnitType values(4,6,450.0,3,2,'spearman');
insert into UnitType values(5,8,2000.0,5,2,'general');