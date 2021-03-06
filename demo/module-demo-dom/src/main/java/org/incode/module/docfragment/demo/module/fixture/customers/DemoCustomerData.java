/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.incode.module.docfragment.demo.module.fixture.customers;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.registry.ServiceRegistry2;

import org.incode.module.docfragment.demo.module.dom.impl.customers.DemoCustomer;
import org.incode.module.fixturesupport.dom.data.DemoData;
import org.incode.module.fixturesupport.dom.data.DemoDataPersistAbstract;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
@Getter
@Accessors(chain = true)
public enum DemoCustomerData implements DemoData<DemoCustomerData, DemoCustomer> {

    Mr_Joe_Bloggs("Mr", "Joe", "Bloggs", "/"),
    Ms_Joanna_Smith("Ms", "Joanna", "Smith", "/ITA"),
    Mrs_Betty_Flintstone("Mrs", "Betty", "Flintstone", "/FRA"),
    ;

    private final String title;
    private final String firstName;
    private final String lastName;
    private final String atPath;

    @Programmatic
    public DemoCustomer asDomainObject() {
        return DemoCustomer.builder()
                .title(title)
                .firstName(firstName)
                .lastName(lastName)
                .atPath(atPath)
                .build();
    }

    @Programmatic
    public DemoCustomer persistUsing(final ServiceRegistry2 serviceRegistry) {
        return Util.persist(this, serviceRegistry);
    }

    @Programmatic
    public DemoCustomer findUsing(final ServiceRegistry2 serviceRegistry) {
        return Util.firstMatch(this, serviceRegistry);
    }

    public static class PersistScript extends DemoDataPersistAbstract<PersistScript, DemoCustomerData, DemoCustomer> {
        public PersistScript() {
            super(DemoCustomerData.class);
        }
    }

}
