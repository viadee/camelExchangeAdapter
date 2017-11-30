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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.service.ConflictResolutionMode;
import microsoft.exchange.webservices.data.core.enumeration.service.TaskStatus;
import microsoft.exchange.webservices.data.core.service.item.Task;
import microsoft.exchange.webservices.data.property.complex.ItemId;

public class TaskUpdateProducer extends ExchangeProducer {

    private static final transient Logger LOG = LoggerFactory.getLogger(TaskUpdateProducer.class);

    private ExchangeService exService;

    public TaskUpdateProducer(Endpoint endpoint) {
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
        String id = exchange.getIn().getHeader("id_ext").toString();
        String newStatus = exchange.getIn().getHeader("new_Status").toString();
        changeStatusOfTask(id, newStatus);
    }

    /**
     * Changes the status of a task
     * 
     * @param id
     * @param newStatus
     * @throws Exception
     */
    public void changeStatusOfTask(String id, String newStatus) throws Exception {
        Task taskToUpdate = getTaskById(id);
        if (taskToUpdate != null) {
            TaskStatus newTaskStatus = getTaskStatusOfString(newStatus);
            taskToUpdate.setStatus(newTaskStatus);
            taskToUpdate.update(ConflictResolutionMode.AutoResolve);
        } else
            LOG.error("Task with id " + id + " not found");
    }

    /**
     * Maps the string given status to the TaskStatus of Java EWS APi
     * 
     * @param newStatus
     * @return TaskStatus status
     */
    private TaskStatus getTaskStatusOfString(String newStatus) {
        TaskStatus taskStatus;
        switch (newStatus) {
            case "open":
                taskStatus = TaskStatus.NotStarted;
                break;
            case "doing":
                taskStatus = TaskStatus.InProgress;
                break;
            default:
                taskStatus = TaskStatus.Completed;
                break;
        }
        return taskStatus;
    }

    /**
     * Identifies the Task by a string given id.
     * 
     * @param id
     * @return task (nullable)
     * @throws Exception
     */
    public Task getTaskById(String id) throws Exception {
        Task task = Task.bind(exService, new ItemId(id));
        return task;

    }

}
