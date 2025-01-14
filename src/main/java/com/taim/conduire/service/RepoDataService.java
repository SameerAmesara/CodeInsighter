package com.taim.conduire.service;

import com.taim.conduire.domain.RepoData;
import com.taim.conduire.domain.UserData;
import com.taim.conduire.service.common.AbstractService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

public interface RepoDataService extends AbstractService<RepoData, Integer> {

    String getRepoData(UserData userData);

    String dumpRepoData(UserData userData);

    RepoData findByGithubRepoId(Integer githubRepoId);

    List<RepoData> findByUserId(Integer userId);

    Map<String, Integer> getRepositoryLanguages(RepoData repoData);

    Map<String, Integer> getRepoContributors(RepoData repoData);

    String getRepoLOC(RepoData repoData);

    Integer getRepositoryPRs(RepoData repoData);

    String getParentRepo(RepoData repoData);

    Integer getRepositoryForksCount(RepoData repoData);

    HttpEntity<?> getAllHeadersEntity(String userAccessToken);

    void showAvailableAPIHits(HttpHeaders headers);
}
