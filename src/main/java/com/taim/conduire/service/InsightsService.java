package com.taim.conduire.service;

import com.taim.conduire.domain.RepoData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface InsightsService {

    HttpEntity<String> getAllHeadersEntity(String userAccessToken);

    void showAvailableAPIHits(HttpHeaders responseHeaders);

    Map<String, List<String>> getRepositoryReviewComments(RepoData repoData);

    String getCodeQualityEnhancementsInsights(RepoData repoData);

    StringBuilder processPomXMLFile(RepoData repoData) throws IOException;
}
