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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonRootName;

import microsoft.exchange.webservices.data.core.enumeration.property.Importance;
import microsoft.exchange.webservices.data.core.enumeration.service.TaskStatus;

@JsonRootName("Task")
public class ExchangeTask {

    private String id_ext;

    private String subject;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date createdDate;

    private int priority;

    private String owner;

    private String owaUrl;

    private String status;

    public ExchangeTask(String id_ext, String subject, Date createdDate, Importance importance, String owner,
            String owaUrl, TaskStatus status) {
        this.id_ext = id_ext;
        this.subject = subject;
        this.createdDate = createdDate;
        this.priority = evaluatePriority(importance);
        this.owner = owner;
        this.owaUrl = owaUrl;
        this.status = evaluateStatus(status);
    }

    private int evaluatePriority(Importance importance) {
        int priority = 0;
        switch (importance) {
            case Low:
                priority = 3;
                break;
            case Normal:
                priority = 2;
                break;
            case High:
                priority = 1;
                break;
        }

        return priority;
    }

    private String evaluateStatus(TaskStatus taskStatus) {
        String status;

        if (taskStatus.equals(TaskStatus.NotStarted)) {
            status = "open";
        } else if (taskStatus.equals(TaskStatus.InProgress) || taskStatus.equals(TaskStatus.WaitingOnOthers)) {
            status = "doing";
        } else {
            status = "closed";
        }
        return status;
    }

    public String getId_ext() {
        return id_ext;
    }

    public void setId_ext(String id_ext) {
        this.id_ext = id_ext;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOWAUrl() {
        return owaUrl;
    }

    public void getOWAUrl(String owaUrl) {
        this.owaUrl = owaUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
