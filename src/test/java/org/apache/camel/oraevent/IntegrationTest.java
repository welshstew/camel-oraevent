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
package org.apache.camel.oraevent;


import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.main.Main;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

public class IntegrationTest {

    private Main main;
    private BasicDataSource ds;

    @Before
    public void setUp() throws Exception {

        ds = new BasicDataSource();
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setUrl("jdbc:oracle:thin:@localhost:1521:ORCL");
        ds.setUsername("flex");
        ds.setPassword("Password1!");
        ds.setAccessToUnderlyingConnectionAllowed(true);

        main = new Main();
        main.bind("ds", ds);
        main.addRouteBuilder(buildConsumer());

    }

    RouteBuilder buildConsumer() {
        RouteBuilder builder = new RouteBuilder() {

            @Override
            public void configure() throws Exception {
                fromF("oraevent://select * from dept where deptno='45'?dataSource=ds")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                String hello = "";
                            }
                        })
                    .to("log:org.apache.camel.oraevent.OraEventConsumer?level=INFO");
            }
        };

        return builder;
    }


    @Test
    public void waitHere() throws Exception {
        main.run();
        Thread.sleep(60000);
    }
}
