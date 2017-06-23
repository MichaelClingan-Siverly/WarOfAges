CREATE TABLE users (
userID varchar(15), 
password varchar(15),
player tinyint default 0,
loggedin tinyint default 0,
playerOrder tinyint default 0,
primary key(userID));


CREATE TABLE Terrain (
AreaType varchar(20),
terr_ID tinyint,
terr_description varchar(50),
primary key(AreaType));


//don't really need this table...
CREATE TABLE UnitType (
UnitID int, 
Strength int, 
Health double,
Defense int, 
movement int,
UnitName varchar(20),
primary key(UnitID));


Create Table AdminMap(
GridID int,
AreaType varchar(20),
Owner varchar(15) default '',
primary key (GridID),
foreign key(Owner) 
	references Terrain (userID),
foreign key(AreaType) 
	references Terrain (AreaType));    

    
Create Table BasicMap(
GridID int,
AreaType varchar(20),
Owner varchar(15) default '',
primary key (GridID),
foreign key(Owner) 
	references Terrain (userID),
foreign key(AreaType) 
	references Terrain (AreaType));
    

//I don't use gridID as a foreign key here because it may be placed in a number of maps
//If I make multiple maps, I'll have to roll this into them or make multiple unitMaps too
Create Table UnitMap(
GridID int,
UnitID int,
health double,
tinyint moved default 0,
tinyint attacked default 0,
userID varchar(15),
primary key (GridID),
foreigh key(UnitID)
	references UnitType (UnitID),
foreign key(userID) 
	references users (userID),
foreign key(UnitID) 
	references UnitType (UnitID));


//basic values
insert INTO users values ('admin', 'admin', '1234', 0, 0, 0);
insert INTO users values ('Username', 'user', 'Password', 0, 0, 0);


insert INTO Terrain values('desert', 1, 'The sand dunes stretch for miles');
insert INTO Terrain values('forest', 2, 'land covered in trees and bushes');
insert INTO Terrain values('meadow', 3, 'a flat grassland with flowers');
insert INTO Terrain values('mountain', 4, 'The tallest thing in the world');
insert INTO Terrain values('town_friendly', 5, 'an urban community');
insert INTO Terrain values('town_hostile', 6, 'an urban community');
insert INTO Terrain values('town_neutral', 7, 'an urban community');
insert INTO Terrain values('water', 8, 'a body of water you cannot cross');
insert INTO Terrain values('impassable_mountain' 9, 'mountains which no man has ever climbed');


//Yea...This needs to be made better...
insert INTO BasicMap values (0,'town_friendly');
insert INTO BasicMap values (1,'desert');
insert INTO BasicMap values (2,'desert');
insert INTO BasicMap values (3,'desert');
insert INTO BasicMap values (4,'desert');
insert INTO BasicMap values (5,'desert');
insert INTO BasicMap values (6,'desert');
insert INTO BasicMap values (7,'desert');
insert INTO BasicMap values (8,'desert');
insert INTO BasicMap values (9,'desert');
insert INTO BasicMap values (10,'desert');
insert INTO BasicMap values (11,'desert');
insert INTO BasicMap values (12,'desert');
insert INTO BasicMap values (13,'desert');
insert INTO BasicMap values (14,'desert');
insert INTO BasicMap values (15,'desert');
insert INTO BasicMap values (16,'desert');
insert INTO BasicMap values (17,'desert');
insert INTO BasicMap values (18,'desert');
insert INTO BasicMap values (19,'desert');
insert INTO BasicMap values (20,'desert');
insert INTO BasicMap values (21,'desert');
insert INTO BasicMap values (22,'desert');
insert INTO BasicMap values (23,'desert');
insert INTO BasicMap values (24,'desert');
insert INTO BasicMap values (25,'desert');
insert INTO BasicMap values (26,'desert');
insert INTO BasicMap values (27,'desert');
insert INTO BasicMap values (28,'desert');
insert INTO BasicMap values (29,'desert');
insert INTO BasicMap values (30,'desert');
insert INTO BasicMap values (31,'desert');
insert INTO BasicMap values (32,'desert');
insert INTO BasicMap values (33,'desert');
insert INTO BasicMap values (34,'desert');
insert INTO BasicMap values (35,'desert');
insert INTO BasicMap values (36,'desert');
insert INTO BasicMap values (37,'desert');
insert INTO BasicMap values (38,'desert');
insert INTO BasicMap values (39,'desert');
insert INTO BasicMap values (40,'desert');
insert INTO BasicMap values (41,'desert');
insert INTO BasicMap values (42,'desert');
insert INTO BasicMap values (43,'desert');
insert INTO BasicMap values (44,'desert');
insert INTO BasicMap values (45,'desert');
insert INTO BasicMap values (46,'desert');
insert INTO BasicMap values (47,'desert');
insert INTO BasicMap values (48,'desert');
insert INTO BasicMap values (49,'desert');
insert INTO BasicMap values (50,'desert');
insert INTO BasicMap values (51,'desert');
insert INTO BasicMap values (52,'desert');
insert INTO BasicMap values (53,'desert');
insert INTO BasicMap values (54,'desert');
insert INTO BasicMap values (55,'desert');
insert INTO BasicMap values (56,'desert');
insert INTO BasicMap values (57,'desert');
insert INTO BasicMap values (58,'desert');
insert INTO BasicMap values (59,'desert');
insert INTO BasicMap values (60,'desert');
insert INTO BasicMap values (61,'desert');
insert INTO BasicMap values (62,'desert');
insert INTO BasicMap values (63,'desert');
insert INTO BasicMap values (64,'desert');
insert INTO BasicMap values (65,'desert');
insert INTO BasicMap values (66,'desert');
insert INTO BasicMap values (67,'desert');
insert INTO BasicMap values (68,'desert');
insert INTO BasicMap values (69,'desert');
insert INTO BasicMap values (70,'desert');
insert INTO BasicMap values (71,'desert');
insert INTO BasicMap values (72,'desert');
insert INTO BasicMap values (73,'desert');
insert INTO BasicMap values (74,'desert');
insert INTO BasicMap values (75,'desert');
insert INTO BasicMap values (76,'desert');
insert INTO BasicMap values (77,'desert');
insert INTO BasicMap values (78,'desert');
insert INTO BasicMap values (79,'desert');
insert INTO BasicMap values (80,'desert');
insert INTO BasicMap values (81,'desert');
insert INTO BasicMap values (82,'desert');
insert INTO BasicMap values (83,'desert');
insert INTO BasicMap values (84,'desert');
insert INTO BasicMap values (85,'desert');
insert INTO BasicMap values (86,'desert');
insert INTO BasicMap values (87,'desert');
insert INTO BasicMap values (88,'desert');
insert INTO BasicMap values (89,'desert');
insert INTO BasicMap values (90,'desert');
insert INTO BasicMap values (91,'desert');
insert INTO BasicMap values (92,'desert');
insert INTO BasicMap values (93,'desert');
insert INTO BasicMap values (94,'desert');
insert INTO BasicMap values (95,'desert');
insert INTO BasicMap values (96,'desert');
insert INTO BasicMap values (97,'desert');
insert INTO BasicMap values (98,'desert');
insert INTO BasicMap values (99,'town_hostile');

insert into UnitType values(1,3,300.0,3,1,'archer');
insert into UnitType values(2,5,900.0,2,3,'calvary');
insert into UnitType values(3,4,600.0,4,2,'swordman');
insert into UnitType values(4,6,450.0,3,2,'spearman');
insert into UnitType values(5,8,2000.0,5,2,'general');