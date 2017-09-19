OraEvent Component 
=================

This is a component for [Apache Camel](http://camel.apache.org/) which allows
for Consuming Oracle Database Change Notifications

## URI Format

    oraevent:query?datasource=dataSource
    
Where `query` is the sql query passed to the `DCN_QUERY_CHANGE_NOTIFICATION` parameter

## URI Options

These map to options from the [Oracle Documentation](https://docs.oracle.com/cd/E11882_01/java.112/e16548/dbchgnf.htm#JJDBC28815)

| UriParam                | ORACLE_PARAM          | Description  |
| ------------------      | --------------------- | ------------ |
| ignoreDeleteOp          | DCN_IGNORE_DELETEOP   | If set to true, DELETE operations will not generate any database change event. |
| ignoreInsertOp          | DCN_IGNORE_INSERTOP   | If set to true, INSERT operations will not generate any database change event. |
| ignoreUpdateOp          | DCN_IGNORE_UPDATEOP   | If set to true, UPDATE operations will not generate any database change event. |
| notifyChangeLag         | DCN_NOTIFY_CHANGELAG  | Specifies the number of transactions by which the client is willing to lag behind. Note: If this option is set to any value other than 0, then ROWID level granularity of information will not be available in the events, even if the DCN_NOTIFY_ROWIDS option is set to true. |
| notifyRowIds            | DCN_NOTIFY_ROWIDS     | Database change events will include row-level details, such as operation type and ROWID. |
| queryChangeNotification | DCN_QUERY_CHANGE_NOTIFICATION   | Activates query change notification instead of object change notification.  Note: This option is available only when running against an 11.0 database. |
| localHost               | NTF_LOCAL_HOST        | Specifies the IP address of the computer that will receive the notifications from the server. |
| localTcpPort            | NTF_LOCAL_TCP_PORT    | Specifies the TCP port that the driver should use for the listener socket. |
| qosPurgeOnNtfn          | NTF_QOS_PURGE_ON_NTFN | Specifies if the registration should be expunged on the first notification event. |
| qosReliable             | NTF_QOS_RELIABLE      | Specifies whether or not to make the notifications persistent, which comes at a performance cost. |
| ntfTimeout              | DCN_IGNORE_DELETEOP   | Specifies the time in seconds after which the registration will be automatically expunged by the database. |
    

## Output

The body of the exchange is populated with a `DatabaseChangeEvent` when an event is received.
Not much metadata is attached (if notiftRowIds=true, then you will get the ROWID).  If you wish to get the data you must then
query the oracle database using that RowId.

[DatabaseChangeEvent Javadoc](http://download.oracle.com/otn_hosted_doc/jdeveloper/905/jdbc-javadoc/oracle/jdbc/dcn/DatabaseChangeEvent.html)

## Building And Installing

To build this project use

    mvn install

For more help see the Apache Camel documentation:

    http://camel.apache.org/writing-components.html
    
## Oracle Docs

- https://docs.oracle.com/cd/E11882_01/java.112/e16548/dbchgnf.htm#JJDBC28815
