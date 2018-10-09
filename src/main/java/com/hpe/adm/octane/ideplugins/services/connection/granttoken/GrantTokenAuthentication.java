/*
 * © 2017 EntIT Software LLC, a Micro Focus company, L.P.
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

package com.hpe.adm.octane.ideplugins.services.connection.granttoken;

import com.hpe.adm.nga.sdk.authentication.Authentication;
import com.hpe.adm.octane.ideplugins.services.connection.IdePluginsOctaneHttpClient;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;

/**
 * Used by {@link IdePluginsOctaneHttpClient}
 */
public class GrantTokenAuthentication implements Authentication {

    public GrantTokenAuthentication() {
    }

    @Override
    public String getAuthenticationString() {
        return null;
    }

    @Override
    public String getClientHeader() {
        return ServiceModule.CLIENT_TYPE.name();
    }

}