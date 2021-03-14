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
package com.gitee.kooder.indexer;

import com.gitee.kooder.core.KooderConfig;
import io.gitea.ApiClient;
import io.gitea.ApiException;
import io.gitea.Configuration;
import io.gitea.api.AdminApi;
import io.gitea.api.OrganizationApi;
import io.gitea.api.RepositoryApi;
import io.gitea.auth.ApiKeyAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check and Index all of Gitea data for first time
 * @author Winter Lau<javayou@gmail.com>
 */
public class GiteaIndexThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger("[gitea]");

    private final static int itemsPerPage = 20;

    private String gsearch_url;
    private String system_hook_url;
    private String secret_token;
    private ApiClient client;

    public static void main(String[] args) {
        new GitlabIndexThread().start();
    }

    public GiteaIndexThread() {
        this.gsearch_url = KooderConfig.getProperty("http.url");
        this.system_hook_url = gsearch_url + "/gitea";
        this.secret_token = KooderConfig.getProperty("gitea.secret_token", "gsearch");

        //Gitea Configuration
        String apiKey = KooderConfig.getProperty("gitea.apikey");
        this.client = Configuration.getDefaultApiClient();
        // Configure API key authorization: AccessToken
        ApiKeyAuth AccessToken = (ApiKeyAuth) this.client.getAuthentication("AccessToken");
        AccessToken.setApiKey(apiKey);
        // Uncomment the following line to set a prefix for the API key, e.g. "Token" (defaults to null)
        //AccessToken.setApiKeyPrefix("Token");

    }

    @Override
    public void run() {
        long ct = System.currentTimeMillis();
        try {
            AdminApi adminApi = new AdminApi();
            adminApi.adminGetAllOrgs(1, 10000);

            OrganizationApi orgApi = new OrganizationApi();

            RepositoryApi repoApi = new RepositoryApi();


            log.info("Gitea data initialize finished in {} ms.", System.currentTimeMillis() - ct);
        } catch(ApiException e) {
            log.error("Failed to initialize gitlab data.", e);
        }
    }

}
