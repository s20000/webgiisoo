alter table tbluser add department varchar(100);
alter table tblconn drop uid;
alter table tblconn add password varchar(128);
alter table tblconn add uid int default 0;
alter table tbluser add remote int default 0;
alter table tbluser add logintimes int default 0;
alter table tbluser add certid varchar(20);
update tbluser set remote=0 where remote is null;
alter table tbluser add lastfailtime bigint default 0;
alter table tbluser add lastfailip varchar(30);
alter table tbluser add failtimes int default 0;
alter table tbluser add certid varchar(20);
alter table tblrole add memo varchar(255);
alter table tblrepo add memo varchar(1024);
alter table tbluser add phone varchar(50);
alter table tbluser add lockexpired bigint default 0;
alter table tbluser add deleted int default 0;
alter table tbluser add city varchar(255);
alter table tbluser add district varchar(255);
alter table tblconn add sent bigint default 0;
alter table tblconn add received bigint default 0;
alter table tbluser add updated bigint default 0;
alter table tbluser add role varchar(255);
