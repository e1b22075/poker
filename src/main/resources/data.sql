INSERT INTO users (userName) VALUES ('おくだ');
INSERT INTO users (userName) VALUES ('ひらお');
INSERT INTO users (userName) VALUES ('よしたに');
INSERT INTO users (userName) VALUES ('まつうら');
INSERT INTO users (userName) VALUES ('CPU');


INSERT INTO cards (num,cardtype,isActive) VALUES (1,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (2,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (3,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (4,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (5,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (6,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (7,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (8,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (9,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (10,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (11,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (12,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (13,'heart',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (1,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (2,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (3,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (4,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (5,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (6,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (7,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (8,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (9,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (10,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (11,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (12,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (13,'spade',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (1,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (2,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (3,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (4,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (5,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (6,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (7,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (8,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (9,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (10,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (11,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (12,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (13,'dia',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (1,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (2,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (3,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (4,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (5,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (6,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (7,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (8,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (9,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (10,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (11,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (12,'clover',false);
INSERT INTO cards (num,cardtype,isActive) VALUES (13,'clover',false);


INSERT INTO hand (userid,hand1id,hand2id,hand3id,hand4id,hand5id,coin,roleid,rolenum,isActive) VALUES (1,2,3,4,5,6,7,8,9,false);


INSERT INTO match(user1id,user2id,user1coin,user2coin,bet,isActive) VALUES
(1,2,5,5,1,false);

INSERT INTO room (roomName,user1id,user1Status,user2id,user2Status) VALUES ('テストの部屋',1,false,NULL,false);
INSERT INTO room (roomName,user1id,user1Status,user2id,user2Status) VALUES ('テストの部屋2',NULL,false,NULL,false);


INSERT INTO role (roleName) VALUES ('ロイヤルストレートフラッシュ');
INSERT INTO role (roleName) VALUES ('ストレート・フラッシュ');
INSERT INTO role (roleName) VALUES ('フォア・カード');
INSERT INTO role (roleName) VALUES ('フルハウス');
INSERT INTO role (roleName) VALUES ('フラッシュ');
INSERT INTO role (roleName) VALUES ('ストレート');
INSERT INTO role (roleName) VALUES ('スリーカード');
INSERT INTO role (roleName) VALUES ('ツウ・ペア');
INSERT INTO role (roleName) VALUES ('ワン・ペア');
