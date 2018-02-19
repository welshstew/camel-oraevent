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

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

public class IntegrationTest extends CamelTestSupport {

    private BasicDataSource ds;
    JdbcTemplate jdbc;
    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        ds = new BasicDataSource();

        ClassLoader classLoader = getClass().getClassLoader();
        Properties p = new Properties();
        p.load(new FileReader(new File(classLoader.getResource("oraevent.properties").getFile())));

        ds.setDriverClassName(p.getProperty("jdbc.driverClassName"));
        ds.setUrl(p.getProperty("jdbc.url"));
        ds.setUsername(p.getProperty("jdbc.username"));
        ds.setPassword(p.getProperty("jdbc.password"));
        ds.setAccessToUnderlyingConnectionAllowed(true);

        jdbc = new JdbcTemplate(ds);
        try {
            jdbc.execute("DROP TABLE DEPT");
        }catch(Exception ex){
            log.info("DEPT table didn't exist");
        }
        jdbc.execute("CREATE TABLE DEPT(DEPTNO NUMBER,DNAME VARCHAR2(50))");

        //bind the beans
        jndi.bind("ds", ds);
        jndi.bind("jdbcTemplate", jdbc);
        return jndi;
    }


    @Override
    protected RoutesBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() {
                fromF("oraevent://select * from dept where deptno='45'?dataSource=ds")
                        .to("mock:dbevent");
            }
        };
    }

    @Test
    public void testEventFired() throws Exception {
        MockEndpoint me = (MockEndpoint) context.getEndpoint("mock:dbevent");
        me.expectedMessageCount(1);
        jdbc.execute("insert into dept (deptno,dname) values ('45','cool dept')");
        Thread.sleep(1000);
        me.assertIsSatisfied();
    }

    @After
    public void tearDown(){
        JdbcTemplate jdbcTemplate = (JdbcTemplate) context.getRegistry().lookupByName("jdbcTemplate");
        jdbc.execute("DROP TABLE DEPT");
    }
}
