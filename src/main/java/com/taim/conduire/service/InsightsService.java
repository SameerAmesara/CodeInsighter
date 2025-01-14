package com.taim.conduire.service;

import com.taim.conduire.domain.RepoData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface InsightsService {

    HttpEntity<String> getAllHeadersEntity(String userAccessToken);

    void showAvailableAPIHits(HttpHeaders responseHeaders);

    Map<String, List<String>> getRepositoryReviewComments(RepoData repoData);

    String getCommonCodeMistakesInsights(RepoData repoData);

    Map<String, List<String>> getDevPRCode(RepoData repoData);

    String getCodeQualityEnhancementsInsights(RepoData repoData);

    String getDependencyVersionInsights(RepoData repoData) throws IOException;

    String getBugDetectionInApplicationFlowInsights(RepoData repoData);

    String getCustomCodeLintingInsights(RepoData repoData);

    String getTestCaseMinimizationInsights(RepoData repoData);

    String getRepositoryPRsCollab(RepoData repoData) throws IOException, InterruptedException;

    String getAdvancedCodeSearchInsight(RepoData repoData, String input);

}
