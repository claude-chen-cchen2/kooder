/**
 * Copyright (c) 2021, OSChina (oschina.net@gmail.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitee.kooder.gitee;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gitee.kooder.models.Relation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author zhanggx
 */
public class Issue {

    public static final String STATE_OPEN = "open";
    public static final String STATE_PROGRESSING = "progressing";
    public static final String STATE_CLOSED = "closed";
    public static final String STATE_REJECTED = "rejected";

    private Integer id;
    private String htmlUrl;
    private String state;
    private String title;
    private String body;
    private List<String> labels;
    private User user;
    private Repository repository;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private Date createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssX")
    private Date updatedAt;

    /**
     * Turn to kooder issue
     * @return
     */
    public com.gitee.kooder.models.Issue toKooderIssue() {
        com.gitee.kooder.models.Issue iss = new com.gitee.kooder.models.Issue();
        iss.setId(getId());
        iss.setIdent(this.getRepository().getId() + "_" + this.getId());
        iss.setRepository(new Relation(this.getRepository().getId(), this.getRepository().getName(), this.getRepository().getUrl()));
        iss.setOwner(new Relation(this.getUser().getId(), this.getUser().getName(), this.getUser().getHtmlUrl()));
        iss.setTitle(this.getTitle());
        iss.setDescription(this.getBody());
        iss.setUrl(this.getHtmlUrl());
        iss.setLabels(new ArrayList<>(this.getLabels()));
        iss.setCreatedAt(this.getCreatedAt().getTime());
        iss.setUpdatedAt(this.getUpdatedAt().getTime());
        iss.setState((STATE_OPEN.equals(this.getState()) || STATE_PROGRESSING.equals(this.getState()))
                ?com.gitee.kooder.models.Issue.STATE_OPENED:com.gitee.kooder.models.Issue.STATE_CLOSED);
        return iss;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
