CREATE TABLE users (
userID varchar(15), 
password varchar(15),
player tinyint default 0,
loggedin tinyint default 0,
playerOrder tinyint default 0,
primary key(userID));


#for the most part, this is pretty useless, just shows supported area types/IDs
CREATE TABLE Terrain (
AreaType varchar(20),
terr_ID tinyint,
terr_description varchar(50),
primary key(AreaType));

#don't really need this table...but at least it shows the supported unitIDs
CREATE TABLE UnitType (
UnitID int, 
UnitName varchar(20),
primary key(UnitID),
unique(UnitName));


Create Table AdminMap(
GridID int,
AreaType varchar(20),
Owner varchar(15) default NULL,
primary key (GridID),
foreign key(Owner) 
	references users (userID),
foreign key(AreaType) 
	references Terrain (AreaType));    

    
Create Table BasicMap(
GridID int,
AreaType varchar(20),
Owner varchar(15) default NULL,
primary key (GridID),
foreign key(Owner) 
	references users (userID),
foreign key(AreaType) 
	references Terrain (AreaType));
    

#I don't use gridID as a foreign key here because it may be placed in a number of maps
#If I make multiple maps, I'll have to roll this into them or make multiple unitMaps too
Create Table UnitMap(
GridID int,
UnitID int,
health double,
moved tinyint default 0,
attacked tinyint default 0,
userID varchar(15),
primary key (GridID),
foreign key(UnitID)
	references UnitType (UnitID),
foreign key(userID) 
	references users (userID),
foreign key(UnitID) 
	references UnitType (UnitID));


#basic values      
insert INTO users values('admin', '1234', 0, 0, 0),
			 ('Username', 'Password', 0, 0, 0);


insert INTO Terrain values('desert', 1, 'The sand dunes stretch for miles'),
			  ('forest', 2, 'land covered in trees and bushes'),
			  ('meadow', 3, 'a flat grassland with flowers'),
			  ('mountain', 4, 'The tallest thing in the world'),
			  ('town_friendly', 5, 'an urban community'),
			  ('town_friendly_start', 5, 'an urban community'),
			  ('town_hostile', 6, 'an urban community'),
			  ('town_hostile_start', 6, 'an urban community'),
			  ('town_neutral', 7, 'an urban community'),
			  ('water', 8, 'a body of water you cannot cross'),
			  ('impassable_mountain', 9, 'mountains which no man has ever climbed');


#Yea...This needs to be made better...
insert Into BasicMap (GridID, AreaType)
		      values(0,'town_friendly_start'),
		            (1,'town_neutral'),
		            (2,'meadow'),
		            (3,'town_neutral'),
		            (4,'water'),
		            (5,'town_neutral'),
		            (6,'town_neutral'),
		            (7,'town_friendly'),
		            (8,'impassable_mountain'),
		            (9,'town_neutral'),
		            (10,'town_neutral'),
		            (11,'water'),
		            (12,'meadow'),
		            (13,'impassable_mountain'),
		            (14,'forest'),
		            (15,'desert'),
		            (16,'impassable_mountain'),
		            (17,'impassable_mountain'),
		            (18,'town_neutral'),
		            (19,'mountain'),
		            (20,'water'),
		            (21,'impassable_mountain'),
		            (22,'water'),
		            (23,'desert'),
		            (24,'mountain'),
		            (25,'town_neutral'),
		            (26,'forest'),
		            (27,'town_friendly'),
		            (28,'meadow'),
		            (29,'water'),
		            (30,'forest'),
		            (31,'impassable_mountain'),
		            (32,'town_neutral'),
		            (33,'water'),
		            (34,'water'),
		            (35,'town_friendly'),
		            (36,'meadow'),
		            (37,'water'),
		            (38,'desert'),
		            (39,'mountain'),
		            (40,'mountain'),
		            (41,'town_friendly'),
		            (42,'mountain'),
		            (43,'town_neutral'),
		            (44,'impassable_mountain'),
		            (45,'town_neutral'),
		            (46,'water'),
		            (47,'town_neutral'),
		            (48,'desert'),
		            (49,'meadow'),
		            (50,'town_neutral'),
		            (51,'impassable_mountain'),
		            (52,'town_friendly'),
		            (53,'impassable_mountain'),
		            (54,'water'),
		            (55,'forest'),
		            (56,'town_neutral'),
		            (57,'impassable_mountain'),
		            (58,'meadow'),
		            (59,'water'),
		            (60,'impassable_mountain'),
		            (61,'water'),
		            (62,'desert'),
		            (63,'town_neutral'),
		            (64,'water'),
		            (65,'desert'),
		            (66,'town_neutral'),
		            (67,'meadow'),
		            (68,'impassable_mountain'),
		            (69,'meadow'),
		            (70,'forest'),
		            (71,'mountain'),
		            (72,'mountain'),
		            (73,'town_hostile'),
		            (74,'town_neutral'),
		            (75,'water'),
		            (76,'town_neutral'),
		            (77,'meadow'),
		            (78,'mountain'),
		            (79,'impassable_mountain'),
		            (80,'desert'),
		            (81,'town_neutral'),
		            (82,'town_hostile'),
		            (83,'town_hostile'),
		            (84,'town_neutral'),
		            (85,'meadow'),
		            (86,'water'),
		            (87,'meadow'),
		            (88,'town_hostile'),
		            (89,'water'),
		            (90,'mountain'),
		            (91,'mountain'),
		            (92,'mountain'),
		            (93,'town_hostile'),
		            (94,'town_neutral'),
		            (95,'meadow'),
		            (96,'town_neutral'),
		            (97,'town_neutral'),
		            (98,'town_neutral'),
		            (99,'town_hostile');

update BasicMap set Owner = 'friendly' where AreaType like 'town_friendly%';
update BasicMap set Owner = 'hostile' where AreaType like 'town_hostile%';
update BasicMap set Owner = 'neutral' where AreaType = 'town_neutral';

insert into UnitType values(1,'archer'),
		           (2,'calvary'),
		           (3,'swordman'),
		           (4,'spearman'),
		           (5,'general');