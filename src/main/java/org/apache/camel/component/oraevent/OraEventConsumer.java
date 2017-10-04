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


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The OraEventConsumer consumer.
 */
public class OraEventConsumer extends DefaultConsumer implements DatabaseChangeListener {
    private static final Logger LOG = LoggerFactory.getLogger(OraEventConsumer.class);
    private final OraEventEndpoint endpoint;
    private OracleConnection dbConnection;
    private DatabaseChangeRegistration dcr;

    public OraEventConsumer(OraEventEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        dbConnection = (OracleConnection)endpoint.initJdbc();

        Properties prop = new Properties();

        if(endpoint.getDCN_IGNORE_DELETEOP() != null){
            prop.setProperty(OracleConnection.DCN_IGNORE_DELETEOP, endpoint.getDCN_IGNORE_DELETEOP());
        }
        if(endpoint.getDCN_IGNORE_INSERTOP() != null){
            prop.setProperty(OracleConnection.DCN_IGNORE_INSERTOP, endpoint.getDCN_IGNORE_INSERTOP());
        }
        if(endpoint.getDCN_IGNORE_UPDATEOP() != null){
            prop.setProperty(OracleConnection.DCN_IGNORE_UPDATEOP, endpoint.getDCN_IGNORE_UPDATEOP());
        }
        if(endpoint.getDCN_NOTIFY_CHANGELAG() != null){
            prop.setProperty(OracleConnection.DCN_NOTIFY_CHANGELAG, endpoint.getDCN_NOTIFY_CHANGELAG());
        }
        if(endpoint.getDCN_NOTIFY_ROWIDS() != null){
            prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS, endpoint.getDCN_NOTIFY_ROWIDS() );
        }
        if(endpoint.getDCN_QUERY_CHANGE_NOTIFICATION() != null){
            prop.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION, endpoint.getDCN_QUERY_CHANGE_NOTIFICATION());
        }
        if(endpoint.getNTF_LOCAL_HOST() != null){
            prop.setProperty(OracleConnection.NTF_LOCAL_HOST, endpoint.getNTF_LOCAL_HOST());
        }
        if(endpoint.getNTF_LOCAL_TCP_PORT() != null){
            prop.setProperty(OracleConnection.NTF_LOCAL_TCP_PORT, endpoint.getNTF_LOCAL_TCP_PORT());
        }
        if(endpoint.getNTF_QOS_RELIABLE() != null){
            prop.setProperty(OracleConnection.NTF_QOS_RELIABLE, endpoint.getNTF_QOS_RELIABLE());
        }
        if(endpoint.getNTF_QOS_PURGE_ON_NTFN() != null){
            prop.setProperty(OracleConnection.NTF_QOS_RELIABLE, endpoint.getNTF_QOS_PURGE_ON_NTFN());
        }
        if(endpoint.getNTF_TIMEOUT() != null){
            prop.setProperty(OracleConnection.NTF_TIMEOUT, endpoint.getNTF_TIMEOUT());
        }

        // first step: create a registration on the server:
        dcr = dbConnection.registerDatabaseChangeNotification(prop);
        try{
            dcr.addListener(this);

            // second step: add objects in the registration:
            Statement stmt = dbConnection.createStatement();
            // associate the statement with the registration:
            ((OracleStatement)stmt).setDatabaseChangeRegistration(dcr);
            ResultSet rs = stmt.executeQuery(endpoint.getQuery());
            while (rs.next())
            {}
            String[] tableNames = dcr.getTables();
            for(int i=0;i<tableNames.length;i++)
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Oracle DatabaseChangeEvent " + tableNames[i]+ " is part of the registration.");
                }
            rs.close();
            stmt.close();

        }catch(SQLException ex) {
            // if an exception occurs, we need to close the registration in order
            // to interrupt the thread otherwise it will be hanging around.
            if(dbConnection != null)
                dbConnection.unregisterDatabaseChangeNotification(dcr);
            throw ex;
        }
        finally {
            try {
                // Note that we close the connection!
                dbConnection.close();
            }
            catch(Exception innerex){ throw innerex; }
        }

    }


    @Override
    protected void doStop() throws Exception {
        if (dbConnection != null) {
            dbConnection.unregisterDatabaseChangeNotification(dcr);
            dbConnection.close();
        }
    }

    @Override
    public void onDatabaseChangeNotification(DatabaseChangeEvent databaseChangeEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Oracle DatabaseChangeEvent eventType: {}, regId: {}", new Object[]{databaseChangeEvent.getEventType().getCode(), databaseChangeEvent.getRegId()});
        }

        Exchange exchange = endpoint.createExchange();
        Message msg = exchange.getIn();
//        msg.setHeader("channel", channel);
        msg.setBody(databaseChangeEvent);

        try {
            getProcessor().process(exchange);
        } catch (Exception ex) {
            String cause = "Unable to process incoming notification from Oracle eventType: " + databaseChangeEvent.getEventType().getCode() + " , regId: "+ databaseChangeEvent.getRegId();
            getExceptionHandler().handleException(cause, ex);
        }
    }
}
