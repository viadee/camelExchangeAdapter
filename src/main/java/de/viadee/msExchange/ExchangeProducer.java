/**
 * Copyright � 2017, viadee Unternehmensberatung GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the viadee Unternehmensberatung GmbH.
 * 4. Neither the name of the viadee Unternehmensberatung GmbH nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <viadee Unternehmensberatung GmbH> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.viadee.msExchange;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.misc.TraceFlags;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.misc.ITraceListener;

public abstract class ExchangeProducer extends DefaultProducer {

    private static final transient Logger LOG = LoggerFactory.getLogger(ExchangeProducer.class);

    private ExchangeService exService;

    public ExchangeProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public ExchangeEndpoint getEndpoint() {
        return (ExchangeEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {

        // Connecting to Exchange Server
        if (exService == null)
            exService = connectViadirectURL(getEndpoint().getUrl(), getEndpoint().getUsername(),
                    getEndpoint().getPassword());

        if (log.isInfoEnabled()) {
            log.info("Creating and starting MS-Exchange Producer using url: {}", exService.getUrl().toString());
        }

    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

    }

    public static ExchangeService connectViadirectURL(String url_str, String user, String password)
            throws URISyntaxException {

        ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010_SP2); // TODO enter the Exchange
                                                                                         // Version you're using

        service.setCredentials(new WebCredentials(user, password));
        service.setUrl(new URI(url_str));
        service.setTraceEnabled(true);
        service.setTraceFlags(EnumSet.allOf(TraceFlags.class));
        service.setTraceListener(new ITraceListener() {

            @Override
            public void trace(String traceType, String traceMessage) {
                LOG.debug("Type:" + traceType + " Message:" + traceMessage);
            }
        });

        return service;
    }

    public ExchangeService getExService() {
        return exService;
    }

}