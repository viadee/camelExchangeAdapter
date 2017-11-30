/**
 * Copyright � 2017, viadee Unternehmensberatung GmbH All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. 3. All advertising materials mentioning features or use of this software must display the following
 * acknowledgement: This product includes software developed by the viadee Unternehmensberatung GmbH. 4. Neither the
 * name of the viadee Unternehmensberatung GmbH nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <viadee Unternehmensberatung GmbH> ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package de.viadee.msExchange;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.IdFormat;
import microsoft.exchange.webservices.data.core.enumeration.property.DefaultExtendedPropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.MapiPropertyType;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.item.Task;
import microsoft.exchange.webservices.data.core.service.schema.TaskSchema;
import microsoft.exchange.webservices.data.misc.id.AlternateId;
import microsoft.exchange.webservices.data.misc.id.AlternateIdBase;
import microsoft.exchange.webservices.data.property.definition.ExtendedPropertyDefinition;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

/**
 * The Exchange producer.
 */
public class TaskGetterProducer extends ExchangeProducer {

    private static final transient Logger LOG = LoggerFactory.getLogger(TaskGetterProducer.class);

    private ExchangeService exService;

    public TaskGetterProducer(ExchangeEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        exService = super.getExService();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        Exchange messageExchange = getEndpoint().createExchange();
        messageExchange.getIn().setHeader("Accept", "application/json");

        LOG.debug("Calling MS Exchange");

        boolean openTasks = getEndpoint().isOpenTasks();

        FindItemsResults<Item> tasksOfResp = getAllTasksOfResponsible(getEndpoint().getName(), openTasks);
        List<ExchangeTask> taskList = mapToOwnTask(tasksOfResp);

        // Converting to JSON File
        ObjectMapper om = new ObjectMapper();
        ObjectWriter ow = om.writer().withDefaultPrettyPrinter();

        String jsonString = ow.writeValueAsString(taskList);
        exchange.getIn().setBody(jsonString);

        LOG.debug(exchange.getIn().getBody().toString());
    }

    private List<ExchangeTask> mapToOwnTask(FindItemsResults<Item> openTasks) throws ServiceLocalException {
        List<ExchangeTask> exTaskList = new ArrayList<ExchangeTask>();
        for (Item item : openTasks) {
            String itemId = item.getId().toString();
            String itemOWAurl = "";
            try {
                itemOWAurl = createURL(itemId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            exTaskList.add(new ExchangeTask(
                    itemId,
                    item.getSubject(),
                    item.getDateTimeCreated(),
                    item.getImportance(),
                    ((Task) item).getOwner(),
                    itemOWAurl,
                    ((Task) item).getStatus()));
        }
        return exTaskList;
    }

    /**
     * Creates URL to open tasks in OWA
     * 
     * @throws Exception
     * 
     */
    private String createURL(String id) throws Exception {
        String owaId = getOwaId(id, exService);
        StringBuilder sb = new StringBuilder();
        // TODO insert URL to OWA. e.g. https://example.com/owa/?ae=Item&a=Open&t=IPM.Task
        sb.append("https://insert-URL.com/owa/?ae=Item&a=Open&t=IPM.Task");
        sb.append("id=").append(owaId);
        return sb.toString();
    }

    private String getOwaId(String id, ExchangeService ser) throws Exception {
        String userMail = getEndpoint().getMail();
        AlternateId ewsId = new AlternateId(IdFormat.EwsId, id, userMail);
        AlternateIdBase owaId = ser.convertId(ewsId, IdFormat.OwaId);
        return ((AlternateId) owaId).getUniqueId();
    }

    /**
     * Returns all tasks of user, either all or just those with status open
     * 
     * @param username
     * @param justOpenTasks
     *            true to show only open tasks; else false
     * @return Tasks in Format FindItemResults<Item>
     * @throws Exception
     */
    public FindItemsResults<Item> getAllTasksOfResponsible(String username, boolean justOpenTasks) throws Exception {
        Folder.bind(exService, WellKnownFolderName.Tasks);
        SearchFilter namefilter = new SearchFilter.IsEqualTo(TaskSchema.Owner, username);

        SearchFilter filter;
        if (justOpenTasks) { // sets filter for open tasks
            ExtendedPropertyDefinition pidLidTaskStatus = new ExtendedPropertyDefinition(
                    DefaultExtendedPropertySet.Task, 0x00008101, MapiPropertyType.Integer); // Specifies the status of a
                                                                                            // task
            SearchFilter.IsEqualTo pidfilter = new SearchFilter.IsEqualTo(pidLidTaskStatus,
                    ExchangeConstants.STATUS_NOTSTARTED);
            filter = new SearchFilter.SearchFilterCollection(LogicalOperator.And, namefilter, pidfilter); // Concating
                                                                                                          // filter
        } else
            filter = namefilter;

        FindItemsResults<Item> findResults = exService.findItems(WellKnownFolderName.Tasks, filter, new ItemView(1000));
        if (findResults.iterator().hasNext()) {
            exService.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
            for (Item item : findResults.getItems()) {
                LOG.debug("id==========" + item.getId());
                LOG.debug("sub==========" + item.getSubject());
            }
            LOG.debug("Anzahl der gefundenen Aufgaben" + findResults.getTotalCount());
        } else {
            findResults = new FindItemsResults<Item>();
            findResults.setTotalCount(0);
        }

        return findResults;
    }

}
