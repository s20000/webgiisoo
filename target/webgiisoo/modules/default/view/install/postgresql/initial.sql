#drop table if exists tblconfig;
create table tblconfig
(
	name varchar(50) not null,
	description varchar(255),
	s varchar(8192),
	i int default 0,
	l bigint default 0,
	d decimal(16,6) default 0
);
create unique index tblconfig_index_name on tblconfig(name);
insert into tblconfig(name, s) values('db.version', '1.1');

#drop table if exists dual;
create table dual
(
	x varchar(1)
);
insert into dual values('x');

create table access_log
(
	id varchar(20),
	ip varchar(50),
	method varchar(10),
	agent varchar(1024),
	url varchar(255),
	sid varchar(20),
	header varchar(4096),
	query varchar(4096),
	created bigint default 0,
	cost bigint default 0,
	handler varchar(255),
	module varchar(50)
);
create index access_log_index_id on access_log(id);
create index access_log_index_created on access_log(created);

#drop table if exists tbluser;
create table tbluser
(
	id int,
	name varchar(50),
	nickname varchar(255),
	email varchar(100),
	password varchar(255),
	locked int default 0,
	lockexpired bigint default 0,
	logintimes int default 0,
	lastlogintime bigint,
	lastattemptime bigint,
	lastloginip varchar(20),
	expired bigint,
	sid varchar(20),
	ip varchar(20),
	created bigint,
	rank int default 0,
	workspace varchar(200),
	address varchar(255),
	company varchar(255),
	title varchar(255),
	phone varchar(50),
	photo varchar(255),
	total bigint default -1,
	free bigint default 0,
	spi varchar(50),
	description varchar(1024),
	special varchar(1024),
	department varchar(100),
	remote int default 0,
	certid varchar(20),
	lastfailtime bigint default 0,
	lastfailip varchar(30),
	failtimes int default 0,
	deleted int default 0,
	city varchar(255),
	district varchar(255),
	role varchar(255),
	updated bigint default 0
);
create unique index tbluser_index_id on tbluser(id);
create index tbluser_index_name on tbluser(name);
create index tbluser_index_certid on tbluser(certid);
create index tbluser_index_deleted on tbluser(deleted);
create index tbluser_index_locked on tbluser(locked);

insert into tbluser(id, name, certid, nickname, password, created) values(0, 'admin', '123456', 'admin', 'albqdj2cd1aun', extract(epoch from current_timestamp) * 1000::bigint);

create table tbluserlock
(
	uid int,
	created bigint,
	sid varchar(50),
	host varchar(50),
	useragent varchar(1024)
);
create index tbluserlock_index_uid on tbluserlock(uid);
create index tbluserlock_index_created on tbluserlock(created);
create index tbluserlock_index_host on tbluserlock(host);
create index tbluserlock_index_sid on tbluserlock(sid);

#drop table if exists tbloplog;
create table tbloplog
(
	id varchar(20),
	created bigint,
	system varchar(50),
	module varchar(50),
	uid int,
	type int default 0,
	op varchar(255),
	brief varchar(1024),
	message varchar(8192),
	result varchar(255),
	memo varchar(255),
	ip varchar(50)
);
create index tbloplog_index_id on tbloplog(id);
create index tbloplog_index_created on tbloplog(created);
create index tbloplog_index_system on tbloplog(system);
create index tbloplog_index_module on tbloplog(module);
create index tbloplog_index_uid on tbloplog(uid);
create index tbloplog_index_type on tbloplog(type);

create table tbloplog_cate
(
	type varchar(20),
	cate varchar(255),
	name varchar(255),
	updated bigint default 0
);
create index tbloplog_cate_index_type on tbloplog_cate(type);
create index tbloplog_cate_index_cate on tbloplog_cate(cate);
create index tbloplog_cate_index_name on tbloplog_cate(name);
create index tbloplog_cate_index_updated on tbloplog_cate(updated);

#drop table if exists tblrole;
create table tblrole
(
	id int,
	name varchar(100),
	memo varchar(255),
	updated bigint default 0
);
create unique index tblrole_index_id on tblrole(id);
insert into tblrole(id, name) values(0, 'admin');

#drop table if exists tbluserrole;
create table tbluserrole
(
	uid int,
	rid int,
	created bigint
);
create index tbluserrole_index_uid on tbluserrole(uid);
create unique index tbluserrole_index_uid_rid on tbluserrole(uid, rid);
insert into tbluserrole(uid, rid) values(0, 0);

#drop table if exists tblaccess;
create table tblaccess
(
	id varchar(255),
	name varchar(255)
);
create unique index tblaccess_index_name on tblaccess(name);
create index tblaccess_index_id on tblaccess(id);
insert into tblaccess(name) values('access.admin');

#drop table if exists tblroleaccess;
create table tblroleaccess
(
	id varchar(20),
	rid int,
	name varchar(255)
);
create index tblroleaccess_index_id on tblroleaccess(id);
create index tblroleaccess_index_rid on tblroleaccess(rid);
insert into tblroleaccess(rid, name) values(0, 'access.admin');

#drop table if exists tblmenu;
create table tblmenu
(
	id serial,
	parent int,
	name varchar(50),
	url varchar(255),
	classes varchar(100),
	click varchar(255),
	content varchar(4096),
	tag varchar(20),
	access varchar(1024),
	childs int default 0,
	seq int default 1000,
	tip varchar(255)
);
create index tblmenu_index_id on tblmenu(id);
create index tblmenu_index_parent on tblmenu(parent);
create index tblmenu_index_name on tblmenu(name);
create index tblmenu_index_tag on tblmenu(tag);
create index tblmenu_index_seq on tblmenu(seq);

#drop table if exists tblfolder;
create table tblfolder
(
	id serial,
	parent int,
	name varchar(100),
	tag varchar(50),
	title varchar(255),
	hot varchar(255),
	recommend varchar(255),
	content varchar(4096),
	seq int default 1000,
	access varchar(50),
	created bigint
);
create unique index tblfolder_index_id on tblfolder(id);
create index tblfolder_index_parent on tblfolder(parent);
create index tblfolder_index_tag on tblfolder(tag);

#drop table if exists tblconn;
create table tblconn
(
  	clientid varchar(20),
  	phone varchar(20),
  	alias varchar(100),
  	password varchar(128),
  	photo varchar(100),
  	locked int default 0,
  	pubkey varchar(2048),
  	ip varchar(50),
  	capability int default 0,
  	created bigint,
  	login int,
  	logined bigint,
  	updated bigint,
  	address varchar(255),
  	uid int default 0,
  	sent bigint default 0,
  	received bigint default 0
);
create unique index tblconn_index_clientid on tblconn(clientid);
create index tblconn_index_phone on tblconn(phone);
create index tblconn_index_alias on tblconn(alias);
create index tblconn_index_uid on tblconn(uid);
create index tblconn_index_password on tblconn(password);

#drop table if exists tblurlmapping;
create table tblurlmapping
(
	url varchar(100),
	dest varchar(100),
	seq int default 999
);
create unique index tblurlmapping_index_dest on tblurlmapping(dest);
create index tblurlmapping_index_seq on tblurlmapping(seq);

#drop table if exists tblinbox;
create table tblinbox
(
	id varchar(20),
	uid int,
	created bigint,
	clazz varchar(255),
	refer varchar(255),
	flag int,
	attempt int,
	updated bigint,
	state int
);
create unique index tblinbox_index_id on tblinbox(id);
create index tblinbox_index_uid on tblinbox(uid);
create index tblinbox_index_flag on tblinbox(flag);
create index tblinbox_index_attempt on tblinbox(attempt);
create index tblinbox_index_created on tblinbox(created);
create index tblinbox_index_state on tblinbox(state);

#drop table if exists tblrepo;
create table tblrepo
(
	uid int,
	id varchar(20),
	folder varchar(255),
	name varchar(255),
	total bigint,
	pos bigint,
	created bigint,
	flag int,
	tag varchar(20),
	expired bigint,
	memo varchar(1024)
);
create unique index tblrepo_index_id on tblrepo(id);
create index tblrepo_index_uid on tblrepo(uid);
create index tblrepo_index_name on tblrepo(name);
create index tblrepo_index_folder on tblrepo(folder);
create index tblrepo_index_tag on tblrepo(tag);
create index tblrepo_index_expired on tblrepo(expired);

#drop table if exists tblfuzzy;
create table tblfuzzy
(
	word varchar(50),
	content varchar(4098)
);
create unique index tblfuzzy_index_word on tblfuzzy(word);

#drop table if exists tblpendindex;
create table tblpendindex
(
	clazz varchar(255),
	id varchar(255),
	created bigint
);
create index tblpendindex_index_clazz on tblpendindex(clazz);
create index tblpendindex_index_id on tblpendindex(id);
create index tblpendindex_index_created on tblpendindex(created);

#drop table if exists tblarticle;
create table tblarticle
(
	id serial,
	name varchar(100),
	refer int,
	title varchar(255),
	fid int default -1,
	tag varchar(255),
	locked int default 0,
	created bigint,
	uid int,
	replies int default 0,
	content varchar(32768)
);
create unique index tblarticle_index_id on tblarticle(id);
create index tblarticle_index_name on tblarticle(name);
create index tblarticle_index_refer on tblarticle(refer);
create index tblarticle_index_fid on tblarticle(fid);
create index tblarticle_index_tag on tblarticle(tag);
create index tblarticle_index_uid on tblarticle(uid);

#drop table if exists tblfeedback;
create table tblfeedback
(
	id serial not null,
	content varchar(1024),
	ip varchar(50),
	created bigint
);
create unique index tblfeedback_index_id on tblfeedback(id);
create index tblfeedback_index_created on tblfeedback(created);

#drop table if exists tbldict;
create table tbldict(
	id varchar(20),
	parent varchar(20),
	name varchar(50),
	display varchar(50),
	clazz varchar(20),
	count int default 0,
	seq int default 0,
	memo varchar(100),
	created bigint,
	updated bigint
);
create unique index tbldict_index_id on tbldict(id);
create index tbldict_index_parent on tbldict(parent);
create index tbldict_index_parent_name on tbldict(parent, name);
create index tbldict_index_clazz on tbldict(clazz);
create index tbldict_index_seq on tbldict(seq);
insert into tbldict(id, name) values('root', '字典数据库');

create table tblnamemapping
(
	type varchar(50),
	name varchar(255),
	value varchar(255)
);
create index tblnamemapping_index_type on tblnamemapping(type);
create index tblnamemapping_index_name on tblnamemapping(name);
create index tblnamemapping_index_value on tblnamemapping(value);

#drop table if exists tblcart;
create table tblcart
(
	id varchar(20),
	uid int,
	exported bigint,
	state int,
	destclazz varchar(255) default '',
	destination varchar(255) default '',
	count int default 0,
	no varchar(20),
	pubkey varchar(4096),
	memo varchar(255),
	repo varchar(255),
	length bigint default 0
);
create unique index tblcart_index_id on tblcart(id);
create index tblcart_index_uid on tblcart(uid);
create index tblcart_index_exported on tblcart(exported);
create index tblcart_index_no on tblcart(no);
create index tblcart_index_destclazz on tblcart(destclazz);
create index tblcart_index_destination on tblcart(destination);

#drop table if exists tblcartitem;
create table tblcartitem
(
	cartid varchar(20),
	id varchar(20),
	clazz varchar(100),
	refer varchar(1024),
	created bigint,
	name varchar(255),
	description varchar(4096),
	count int default 0,
	state int default 0,
	memo varchar(255)
);
create index tblcartitem_index_cartid on tblcartitem(cartid);
create index tblcartitem_index_id on tblcartitem(id);
create index tblcartitem_index_clazz on tblcartitem(clazz);
create index tblcartitem_index_created on tblcartitem(created);

#drop table if exists tblkeypair;
create table tblkeypair
(
	created bigint,
	memo varchar(255),
	length int,
	pubkey varchar(2048),
	prikey varchar(2048)
);
create unique index tblkeypair_index_created on tblkeypair(created);

create table tblalert
(
	tag varchar(50),
	number varchar(20),
	id varchar(20),
	content varchar(255),
	state int default 0,
	sent bigint default 0
);
create unique index tblalert_index_tag_number on tblalert(tag, number);
create index tblalert_index_id on tblalert(id);
create index tblalert_index_state on tblalert(state);
create index tblalert_index_sent on tblalert(sent);

create table tblupgradelog
(
	created bigint,
    modules varchar(1024),
	_release varchar(20),
	build varchar(50),
	url varchar(255),
	host varchar(255),
	remote varchar(255),
	flag int  default 0
);
create index tblupgradelog_index_created on tblupgradelog(created);
create index tblupgradelog_index_flag on tblupgradelog(flag);

create table tblmessage
(
	id varchar(20),
	uid int,
	from_uid int,
	clazz varchar(50),
	subject varchar(255),
	body varchar(4096),
	created bigint,
	flag varchar(10) default 'new'
);
create index tblmessage_index_id on tblmessage(id);
create index tblmessage_index_uid on tblmessage(uid);
create index tblmessage_index_clazz on tblmessage(clazz);
create index tblmessage_index_flag on tblmessage(flag);

create table tblapp
(
	appid varchar(50) not null,
	_key varchar(128) not null,
	memo varchar(255),
	locked int default 0,
	company varchar(255),
	contact varchar(50),
	phone varchar(20),
	email varchar(100),
	lastlogin bigint,
	created bigint,
	logout varchar(255)
);
create unique index tblapp_index_appid on tblapp(appid);

create table tbltaskstat
(
	date int,
	parent varchar(255),
	name varchar(255),
	access varchar(50),
	count decimal(10, 2),
	updated bigint
);
create index tbltaskstat_index_date on tbltaskstat(date);
create index tbltaskstat_index_parent on tbltaskstat(parent);
create index tbltaskstat_index_name on tbltaskstat(name);
create index tbltaskstat_index_access on tbltaskstat(access);
create index tbltaskstat_index_updated on tbltaskstat(updated);
create unique index tbltaskstat_index_date_access on tbltaskstat(date, parent, name, access);

create table space
(
	uid int,
	total bigint,
	free bigint,
	used bigint,
	usage decimal(5, 2)
);
create index space_index_uid on space(uid);

create table tblload
(
	name varchar(255),
	node varchar(255),
	count int default 0,
	updated bigint default 0
);
create unique index tblload_index_name_node on tblload(name, node);
create index tblload_index_name on tblload(name);
create index tblload_index_count on tblload(count);
create index tblload_index_updated on tblload(updated);

create table tblstat
(
	id varchar(20),
	date bigint,
	module varchar(255),
	f0 varchar(255),
	f1 varchar(255),
	f2 varchar(255),
	f3 varchar(255),
	f4 varchar(255),
	uid int,
	count decimal(20, 2),
	updated bigint
);
create index tblstat_index_id on tblstat(id);
create index tblstat_index_date on tblstat(date);
create index tblstat_index_module on tblstat(module);
create index tblstat_index_f0 on tblstat(f0);
create index tblstat_index_f1 on tblstat(f1);
create index tblstat_index_f2 on tblstat(f2);
create index tblstat_index_f3 on tblstat(f3);
create index tblstat_index_f4 on tblstat(f4);
create index tblstat_index_uid on tblstat(uid);
create index tblstat_index_updated on tblstat(updated);
