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

import java.util.Map;
import java.util.Set;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.CamelContextHelper;
import org.apache.camel.util.IntrospectionSupport;

import javax.sql.DataSource;

/**
 * Represents the component that manages {@link OraEventEndpoint}.
 */
public class OraEventComponent extends UriEndpointComponent {

    private DataSource dataSource;
    @Metadata(label = "advanced", defaultValue = "true")
    private boolean usePlaceholder = true;

    public OraEventComponent() {
        super(OraEventEndpoint.class);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        DataSource target = null;

        // endpoint options overrule component configured datasource
        DataSource ds = resolveAndRemoveReferenceParameter(parameters, "dataSource", DataSource.class);
        if (ds != null) {
            target = ds;
        }
        String dataSourceRef = getAndRemoveParameter(parameters, "dataSourceRef", String.class);
        if (target == null && dataSourceRef != null) {
            target = CamelContextHelper.mandatoryLookup(getCamelContext(), dataSourceRef, DataSource.class);
        }
        if (target == null) {
            // fallback and use component
            target = dataSource;
        }
        if (target == null) {
            // check if the registry contains a single instance of DataSource
            Set<DataSource> dataSources = getCamelContext().getRegistry().findByType(DataSource.class);
            if (dataSources.size() > 1) {
                throw new IllegalArgumentException("Multiple DataSources found in the registry and no explicit configuration provided");
            } else if (dataSources.size() == 1) {
                target = dataSources.stream().findFirst().orElse(null);
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("DataSource must be configured");
        }

        String parameterPlaceholderSubstitute = getAndRemoveParameter(parameters, "placeholder", String.class, "#");

        String query = remaining;
        if (usePlaceholder) {
            query = query.replaceAll(parameterPlaceholderSubstitute, "?");
        }

        String dcnIgnoreDeleteOp = getAndRemoveParameter(parameters, "ignoreDeleteOp", String.class);
        String dcnIgnoreInsertOp = getAndRemoveParameter(parameters, "ignoreInsertOp", String.class);
        String dcnIgnoreUpdateOp = getAndRemoveParameter(parameters, "ignoreUpdateOp", String.class);
        String dcnNotifyChangeLag = getAndRemoveParameter(parameters, "notifyChangeLag", String.class);
        String dcnNotifyRowIds = getAndRemoveParameter(parameters, "notifyRowIds", String.class);
        String dcnQueryChangeNotification = getAndRemoveParameter(parameters, "queryChangeNotification", String.class);
        String ntfLocalHost = getAndRemoveParameter(parameters, "localHost", String.class);
        String ntfLocalTcpPort = getAndRemoveParameter(parameters, "localTcpPort", String.class);
        String ntfQosPurgeOnNtfn = getAndRemoveParameter(parameters, "qosPurgeOnNtfn", String.class);
        String ntfQosReliable = getAndRemoveParameter(parameters, "qosReliable", String.class);
        String ntfTimeout = getAndRemoveParameter(parameters, "ntfTimeout", String.class);


        OraEventEndpoint endpoint = new OraEventEndpoint(uri, this, target, query);

        //TODO: finish this off

        if(dcnIgnoreDeleteOp == null){
            dcnIgnoreDeleteOp = "false";
        }

        if(dcnIgnoreInsertOp == null){
            dcnIgnoreInsertOp = "false";
        }

        if(dcnIgnoreUpdateOp == null){
            dcnIgnoreUpdateOp = "false";
        }

        if(dcnNotifyRowIds ==null){
            dcnNotifyRowIds = "true";
        }

        if(dcnQueryChangeNotification == null){
            dcnQueryChangeNotification = "true";
        }

        endpoint.setDCN_IGNORE_DELETEOP(dcnIgnoreDeleteOp);
        endpoint.setDCN_IGNORE_INSERTOP(dcnIgnoreInsertOp);
        endpoint.setDCN_IGNORE_UPDATEOP(dcnIgnoreUpdateOp);
        endpoint.setDCN_NOTIFY_ROWIDS(dcnNotifyRowIds);
        endpoint.setDCN_QUERY_CHANGE_NOTIFICATION(dcnQueryChangeNotification);

        return endpoint;
    }

    /**
     * Sets the DataSource to use to communicate with the database.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Sets whether to use placeholder and replace all placeholder characters with ? sign in the SQL queries.
     * <p/>
     * This option is default <tt>true</tt>
     */
    public void setUsePlaceholder(boolean usePlaceholder) {
        this.usePlaceholder = usePlaceholder;
    }

    public boolean isUsePlaceholder() {
        return usePlaceholder;
    }
}
