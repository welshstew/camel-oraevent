# Oracle XE setup on fedora

http://www.oracle.com/technetwork/database/enterprise-edition/downloads/index-092322.html
http://www.oracle.com/technetwork/database/enterprise-edition/downloads/oracle12c-linux-12201-3608234.html
download client and database

http://docs.oracle.com/database/122/LADBI/toc.htm


sudo mkdir -p /opt/u01/app/swinches
https://localhost:5500/em

# add to bash profile
export ORACLE_HOME=/opt/u01/app/swinches/product/12.2.0/dbhome_1
export ORACLE_SID=ORCL;
export ORACLE_HOME_LISTNER=/opt/u01/app/swinches/product/12.2.0/dbhome_1

# start the database

```
[swinches@localhost bin]$ ./sqlplus / as sysdba

SQL*Plus: Release 12.2.0.1.0 Production on Mon Sep 11 11:02:47 2017

Copyright (c) 1982, 2016, Oracle.  All rights reserved.

Connected to an idle instance.

SQL> startup
ORACLE instance started.

Total System Global Area 6140461056 bytes
Fixed Size		    8806296 bytes
Variable Size		 1191182440 bytes
Database Buffers	 4932501504 bytes
Redo Buffers		    7970816 bytes
Database mounted.
Database opened.
```

/opt/u01/app/swinches/product/12.2.0/dbhome_1/bin/dbstart /opt/u01/app/swinches/product/12.2.0/dbhome_1


# start oracle connections

cmctl


ORACLE_HOME_LISTNER is not SET, unable to auto-start Oracle Net Listener


# Create a new user for flex
alter session set "_ORACLE_SCRIPT"=true;
CREATE USER flex IDENTIFIED BY "Password1!";

GRANT CONNECT,RESOURCE,DBA TO flex;
GRANT CREATE SESSION GRANT ANY PRIVILEGE TO flex;
GRANT UNLIMITED TABLESPACE TO flex;


GRANT SELECT,UPDATE,INSERT ON <TABLE NAME> TO <USER NAME>;

https://docs.oracle.com/cd/E11882_01/java.112/e16548/dbchgnf.htm#JJDBC28815[]