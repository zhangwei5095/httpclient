/*
 * $HeadURL$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.client.protocol;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthState;
import org.apache.http.protocol.HttpContext;

/**
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 *
 * @version $Revision$
 * 
 * @since 4.0
 */
public class RequestProxyAuthentication implements HttpRequestInterceptor {

    private static final Log LOG = LogFactory.getLog(RequestProxyAuthentication.class);
    
    public RequestProxyAuthentication() {
        super();
    }
    
    public void process(final HttpRequest request, final HttpContext context) 
            throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }

        if (request.containsHeader(AUTH.PROXY_AUTH_RESP)) {
            return;
        }
        
        // Obtain authentication state
        AuthState authState = (AuthState) context.getAttribute(
                HttpClientContext.PROXY_AUTH_STATE);
        if (authState == null) {
            return;
        }

        AuthScheme authScheme = authState.getAuthScheme();
        if (authScheme == null) {
            return;
        }
        
        Credentials creds = authState.getCredentials();
        if (creds == null) {
            LOG.debug("User credentials not available");
            return;
        }
        if (authState.getAuthScope() != null || !authScheme.isConnectionBased()) {
            try {
                request.addHeader(authScheme.authenticate(creds, request));
            } catch (AuthenticationException ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Proxy authentication error: " + ex.getMessage());
                }
            }
        }
    }
    
}
