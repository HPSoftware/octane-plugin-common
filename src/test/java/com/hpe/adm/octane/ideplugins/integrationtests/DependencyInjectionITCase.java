/*
 * Copyright 2017 EntIT Software LLC, a Micro Focus company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.adm.octane.ideplugins.integrationtests;

import com.google.inject.Inject;
import com.hpe.adm.octane.ideplugins.services.TestService;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettingsProvider;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DependencyInjectionITCase extends IntegrationTestBase {

    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;

    @Inject
    private TestService testService;

    @Test
    public void testInjection(){
        //Test DI
        assertNotNull(connectionSettingsProvider);
        assertNotNull(testService);
    }

}
