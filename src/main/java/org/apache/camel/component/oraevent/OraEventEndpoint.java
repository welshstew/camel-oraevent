/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.oraevent;

import oracle.jdbc.OracleConnection;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.util.URISupport;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Properties;

/**
 * The oraevent component allows for producing/consuming Oracle events related to the LISTEN/NOTIFY commands.
 *
 */
@UriEndpoint(firstVersion = "2.20.0", scheme = "oraevent", title = "Oracle Database Notification Event", syntax = "oraevent:query?datasource=dataSource", consumerClass = OraEventConsumer.class, label = "database,sql")
public class OraEventEndpoint extends DefaultEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(OraEventEndpoint.class);


    @UriPath(description = "Sets the SQL query to perform. You can externalize the query by using file: or classpath: as prefix and specify the location of the file.")
    @Metadata(required = "true")
    private String query;

    @UriParam(description = "the oracle dataSource")
    @Metadata(required = "true")
    private DataSource datasource;

    @UriParam(description = "If set to true, DELETE operations will not generate any database change event", defaultValue = "false")
    private String DCN_IGNORE_DELETEOP;

    @UriParam(description = "If set to true, INSERT operations will not generate any database change event.", defaultValue = "false")
    private String DCN_IGNORE_INSERTOP;

    @UriParam(description = "If set to true, UPDATE operations will not generate any database change event.", defaultValue = "false")
    private String DCN_IGNORE_UPDATEOP;

    @UriParam(description = "Specifies the number of transactions by which the client is willing to lag behind.  Note: If this option is set to any value other than 0, then ROWID level granularity of information will not be available in the events, even if the DCN_NOTIFY_ROWIDS option is set to true.", defaultValue = "0")
    private String DCN_NOTIFY_CHANGELAG;

    @UriParam(description = "Database change events will include row-level details, such as operation type and ROWID.", defaultValue = "true")
    private String DCN_NOTIFY_ROWIDS;

    @UriParam(description = "Activates query change notification instead of object change notification. Note: This option is available only when running against an 11.0 database.", defaultValue = "true")
    private String DCN_QUERY_CHANGE_NOTIFICATION;

    @UriParam(description = "Specifies the IP address of the computer that will receive the notifications from the server.")
    private String NTF_LOCAL_HOST;

    @UriParam(description = "Specifies the TCP port that the driver should use for the listener socket.")
    private String NTF_LOCAL_TCP_PORT;

    @UriParam(description = "Specifies if the registration should be expunged on the first notification event.", defaultValue = "false")
    private String NTF_QOS_PURGE_ON_NTFN;

    @UriParam(description = "Specifies whether or not to make the notifications persistent, which comes at a performance cost.", defaultValue = "false")
    private String NTF_QOS_RELIABLE;

    @UriParam(description = "Specifies the time in seconds after which the registration will be automatically expunged by the database.")
    private String NTF_TIMEOUT;

    private final String uri;

    private OracleConnection dbConnection;

    public OraEventEndpoint(String uri, OraEventComponent component)  {
        super(uri, component);
        this.uri = uri;
    }

    public OraEventEndpoint(String uri, OraEventComponent component, DataSource dataSource, String registrationQuery) {
        super(uri, component);
        this.uri = uri;
        this.datasource = dataSource;
        this.query = registrationQuery;
    }

    public final OracleConnection initJdbc() throws Exception {
        OracleConnection oracleConnection = null;
        Connection conn;
        Properties props = new Properties();
        props.putAll(URISupport.parseQuery(uri));
        conn = this.getDatasource().getConnection();
        if (conn.isWrapperFor(OracleConnection.class)) {
            oracleConnection = conn.unwrap(OracleConnection.class);
        }
        return oracleConnection;
    }

    @Override
    public Producer createProducer() throws Exception {
        return null;
    }

    @Override
    protected String createEndpointUri() {
        // Make sure it's properly encoded
        return "oraevent:" + UnsafeUriCharactersEncoder.encode(query);
    }


    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        OraEventConsumer consumer = new OraEventConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getDCN_IGNORE_DELETEOP() {
        return DCN_IGNORE_DELETEOP;
    }

    public void setDCN_IGNORE_DELETEOP(String DCN_IGNORE_DELETEOP) {
        this.DCN_IGNORE_DELETEOP = DCN_IGNORE_DELETEOP;
    }

    public String getDCN_IGNORE_INSERTOP() {
        return DCN_IGNORE_INSERTOP;
    }

    public void setDCN_IGNORE_INSERTOP(String DCN_IGNORE_INSERTOP) {
        this.DCN_IGNORE_INSERTOP = DCN_IGNORE_INSERTOP;
    }

    public String getDCN_IGNORE_UPDATEOP() {
        return DCN_IGNORE_UPDATEOP;
    }

    public void setDCN_IGNORE_UPDATEOP(String DCN_IGNORE_UPDATEOP) {
        this.DCN_IGNORE_UPDATEOP = DCN_IGNORE_UPDATEOP;
    }

    public String getDCN_NOTIFY_CHANGELAG() {
        return DCN_NOTIFY_CHANGELAG;
    }

    public void setDCN_NOTIFY_CHANGELAG(String DCN_NOTIFY_CHANGELAG) {
        this.DCN_NOTIFY_CHANGELAG = DCN_NOTIFY_CHANGELAG;
    }

    public String getDCN_NOTIFY_ROWIDS() {
        return DCN_NOTIFY_ROWIDS;
    }

    public void setDCN_NOTIFY_ROWIDS(String DCN_NOTIFY_ROWIDS) {
        this.DCN_NOTIFY_ROWIDS = DCN_NOTIFY_ROWIDS;
    }

    public String getDCN_QUERY_CHANGE_NOTIFICATION() {
        return DCN_QUERY_CHANGE_NOTIFICATION;
    }

    public void setDCN_QUERY_CHANGE_NOTIFICATION(String DCN_QUERY_CHANGE_NOTIFICATION) {
        this.DCN_QUERY_CHANGE_NOTIFICATION = DCN_QUERY_CHANGE_NOTIFICATION;
    }

    public String getNTF_LOCAL_HOST() {
        return NTF_LOCAL_HOST;
    }

    public void setNTF_LOCAL_HOST(String NTF_LOCAL_HOST) {
        this.NTF_LOCAL_HOST = NTF_LOCAL_HOST;
    }

    public String getNTF_LOCAL_TCP_PORT() {
        return NTF_LOCAL_TCP_PORT;
    }

    public void setNTF_LOCAL_TCP_PORT(String NTF_LOCAL_TCP_PORT) {
        this.NTF_LOCAL_TCP_PORT = NTF_LOCAL_TCP_PORT;
    }

    public String getNTF_QOS_PURGE_ON_NTFN() {
        return NTF_QOS_PURGE_ON_NTFN;
    }

    public void setNTF_QOS_PURGE_ON_NTFN(String NTF_QOS_PURGE_ON_NTFN) {
        this.NTF_QOS_PURGE_ON_NTFN = NTF_QOS_PURGE_ON_NTFN;
    }

    public String getNTF_QOS_RELIABLE() {
        return NTF_QOS_RELIABLE;
    }

    public void setNTF_QOS_RELIABLE(String NTF_QOS_RELIABLE) {
        this.NTF_QOS_RELIABLE = NTF_QOS_RELIABLE;
    }

    public String getNTF_TIMEOUT() {
        return NTF_TIMEOUT;
    }

    public void setNTF_TIMEOUT(String NTF_TIMEOUT) {
        this.NTF_TIMEOUT = NTF_TIMEOUT;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public DataSource getDatasource() {
        return datasource;
    }
    /**
     * To connect using the given {@link javax.sql.DataSource} instead of using hostname and port.
     */
    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }
}