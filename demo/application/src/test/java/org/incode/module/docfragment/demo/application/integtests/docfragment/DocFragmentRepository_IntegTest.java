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
package org.incode.module.docfragment.demo.application.integtests.docfragment;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Throwables;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.xactn.TransactionService;

import org.isisaddons.module.fakedata.dom.FakeDataService;

import org.incode.module.docfragment.demo.application.fixture.scenarios.DemoAppFixture;
import org.incode.module.docfragment.demo.application.integtests.DocFragmentModuleIntegTestAbstract;
import org.incode.module.docfragment.dom.impl.DocFragment;
import org.incode.module.docfragment.dom.impl.DocFragmentRepository;
import org.incode.module.docfragment.fixture.scenario.DocFragmentData;
import org.incode.module.docfragment.fixture.teardown.DocFragmentModuleTearDown;

import static org.assertj.core.api.Assertions.assertThat;
import static org.incode.module.docfragment.demo.application.integtests.docfragment.DocFragmentRepository_IntegTest.FindByObjectTypeAndNameAndAtPath.causalChainContains;

public class DocFragmentRepository_IntegTest extends DocFragmentModuleIntegTestAbstract {

    @Inject
    FixtureScripts fixtureScripts;
    @Inject
    FakeDataService fakeDataService;
    @Inject
    TransactionService transactionService;
    @Inject
    DocFragmentRepository repository;

    public static class ListAll extends DocFragmentRepository_IntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            fixtureScripts.runFixtureScript(new DocFragmentModuleTearDown(), null);
            DocFragmentData.PersistScript fs = new DocFragmentData.PersistScript();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final List<DocFragment> all = repository.listAll();

            // then
            assertThat(all).hasSize(fs.getObjects().size());

            DocFragment domainObject = wrap(all.get(0));
            assertThat(domainObject.getName()).isEqualTo(fs.getObjects().get(0).getName());
        }

        @Test
        public void whenNone() throws Exception {

            // given
            FixtureScript fs = new DocFragmentModuleTearDown();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final List<DocFragment> all = repository.listAll();

            // then
            assertThat(all).hasSize(0);
        }
    }

    public static class Create extends DocFragmentRepository_IntegTest {

        @Test
        public void happyCase() throws Exception {

            // given
            FixtureScript fs = new DocFragmentModuleTearDown();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final DocFragmentData random = fakeDataService.enums().anyOf(DocFragmentData.class);
            random.createWith(repository);

            // then
            final List<DocFragment> all = repository.listAll();
            assertThat(all).hasSize(1);
        }

        @Test
        public void whenAlreadyExists() throws Exception {

            // given
            FixtureScript fs = new DocFragmentModuleTearDown();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            final DocFragmentData random = fakeDataService.enums().anyOf(DocFragmentData.class);
            random.createWith(repository);
            transactionService.nextTransaction();

            // then
            expectedExceptions.expectCause(causalChainContains(SQLIntegrityConstraintViolationException.class));

            // when
            random.createWith(repository);
            transactionService.nextTransaction();
        }

    }

    public static class FindByObjectTypeAndNameAndApplicableToAtPath extends DocFragmentRepository_IntegTest {

        @Test
        public void when_exact_match() throws Exception {

            // given
            FixtureScript fs = new DemoAppFixture();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final DocFragmentData data = DocFragmentData.Customer_hello_FRA;
            final DocFragment docFragment = repository
                    .findByObjectTypeAndNameAndApplicableToAtPath(
                            data.getObjectType(), data.getName(), data.getAtPath());

            // then
            assertThat(docFragment.getObjectType()).isEqualTo(data.getObjectType());
            assertThat(docFragment.getName()).isEqualTo(data.getName());
            assertThat(docFragment.getAtPath()).isEqualTo(data.getAtPath());
        }

        @Test
        public void when_fallback_match() throws Exception {

            // given
            FixtureScript fs = new DemoAppFixture();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final DocFragmentData data = DocFragmentData.Customer_goodbye_GLOBAL;
            final DocFragment docFragment = repository
                    .findByObjectTypeAndNameAndApplicableToAtPath(
                            data.getObjectType(), data.getName(), "/ITA");

            // then
            assertThat(docFragment.getObjectType()).isEqualTo(data.getObjectType());
            assertThat(docFragment.getName()).isEqualTo(data.getName());
            assertThat(docFragment.getAtPath()).isEqualTo(data.getAtPath());
        }

        @Test
        public void when_no_match() throws Exception {

            // given
            FixtureScript fs = new DemoAppFixture();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final DocFragmentData data = DocFragmentData.Customer_goodbye_GLOBAL;
            final DocFragment docFragment = repository
                    .findByObjectTypeAndNameAndApplicableToAtPath(
                            data.getObjectType(), "unknown", data.getAtPath());

            // then
            assertThat(docFragment).isNull();
        }


    }

    public static class FindByObjectTypeAndNameAndAtPath extends DocFragmentRepository_IntegTest {

        @Test
        public void when_match() throws Exception {

            // given
            FixtureScript fs = new DemoAppFixture();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final DocFragmentData data = DocFragmentData.Customer_hello_FRA;
            final DocFragment docFragment = repository
                    .findByObjectTypeAndNameAndAtPath(
                            data.getObjectType(), data.getName(), data.getAtPath());

            // then
            assertThat(docFragment.getObjectType()).isEqualTo(data.getObjectType());
            assertThat(docFragment.getName()).isEqualTo(data.getName());
            assertThat(docFragment.getAtPath()).isEqualTo(data.getAtPath());
        }

        @Test
        public void when_no_match() throws Exception {

            // given
            FixtureScript fs = new DemoAppFixture();
            fixtureScripts.runFixtureScript(fs, null);
            transactionService.nextTransaction();

            // when
            final DocFragmentData data = DocFragmentData.Customer_goodbye_GLOBAL;
            final DocFragment docFragment = repository
                    .findByObjectTypeAndNameAndAtPath(
                            data.getObjectType(), data.getName(), "/ITA");

            // then
            assertThat(docFragment).isNull();
        }

        static Matcher<? extends Throwable> causalChainContains(final Class<?> cls) {
            return new TypeSafeMatcher<Throwable>() {
                @Override
                protected boolean matchesSafely(Throwable item) {
                    final List<Throwable> causalChain = Throwables.getCausalChain(item);
                    for (Throwable throwable : causalChain) {
                        if(cls.isAssignableFrom(throwable.getClass())){
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText("exception with causal chain containing " + cls.getSimpleName());
                }
            };
        }
    }

}