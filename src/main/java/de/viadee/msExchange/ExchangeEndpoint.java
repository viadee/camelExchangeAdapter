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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;

/**
 * Represents an Exchange endpoint.
 */
@UriEndpoint(scheme = "ms-exchange", title = "Microsoft Exchange", syntax = "ms-exchange:type", producerOnly = true, label = "ms-exchange")
public class ExchangeEndpoint extends DefaultEndpoint {

    @UriParam
    @Metadata(required = "true")
    private ProducerType type;

    @UriPath
    @Metadata(required = "true")
    private String url;

    @UriParam
    @Metadata(required = "true", secret = true)
    private String username;

    @UriParam
    @Metadata(required = "true", secret = true)
    private String password;

    @UriParam
    @Metadata(required = "true", secret = true)
    private String name;

    @UriParam
    @Metadata(required = "false")
    private String id_ext;

    @UriParam
    @Metadata(required = "false", secret = true)
    private String mail;

    @UriParam
    @Metadata(required = "false")
    private boolean openTasks;

    public ExchangeEndpoint(String endpointUri, ExchangeComponent component) {
        super(endpointUri, component);
    }

    @Override
    public ExchangeProducer createProducer() throws Exception {
        if (type == ProducerType.GETTASKS) {
            return new TaskGetterProducer(this);
        } else if (type == ProducerType.UPDATETASK) {
            return new TaskUpdateProducer(this);
        }
        throw new IllegalArgumentException("Producer does not support type: " + type);
    }

    /**
     * Method is not supported
     */
    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Consumer is not supported");
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Username of account
     * 
     * @param username
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Password of Account
     * 
     * @param password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Name of the user
     * 
     * @return
     */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Type of Producer to be produced
     * 
     */
    public void setType(ProducerType type) {
        this.type = type;
    }

    public ProducerType getType() {
        return type;
    }

    /**
     * E-Mail address of user
     */
    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Definies, if only open tasks should be returned. Default = True
     */
    public boolean isOpenTasks() {
        return openTasks;
    }

    public void setOpenTasks(boolean openTasks) {
        this.openTasks = openTasks;
    }

}
