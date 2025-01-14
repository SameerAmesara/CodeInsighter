package com.taim.conduire.service.impl;

import com.taim.conduire.domain.RepoData;
import com.taim.conduire.domain.UserData;
import com.taim.conduire.repository.RepoDataRepository;
import com.taim.conduire.service.UserDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RepoDataServiceImplTest {

    @Mock
    private RepoDataRepository repository;

    @Mock
    private UserDataService userDataService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RepoDataServiceImpl repoDataService;

    @Test
    public void testFindByGithubRepoId_Positive() {
        // Mock RepoData object with ID 1
        RepoData mockRepoData = new RepoData();
        mockRepoData.setGithubRepoId(1);

        when(repository.findByGithubRepoId(1)).thenReturn(mockRepoData);

        RepoData foundRepoData = repoDataService.findByGithubRepoId(1);

        assertEquals(mockRepoData, foundRepoData);
    }

    @Test
    public void testFindByGithubRepoId_Negative() {
        final int dummyRepoId = 2;
        when(repository.findByGithubRepoId(dummyRepoId)).thenReturn(null);

        RepoData foundRepoData = repoDataService.findByGithubRepoId(dummyRepoId);

        assertNull(foundRepoData);
    }

    @Test
    public void testFindByUserId_Positive() {
        // Mocking user ID and creating a list of RepoData for that user
        final int userId = 123;
        List<RepoData> repoList = new ArrayList<>();
        repoList.add(new RepoData());
        repoList.add(new RepoData());

        when(repository.findByUserId(userId)).thenReturn(repoList);

        List<RepoData> foundRepoList = repoDataService.findByUserId(userId);

        assertEquals(2, foundRepoList.size());
    }

    @Test
    public void testFindByUserId_Negative() {
        // Mocking user ID and providing an empty list of RepoData
        final int userId = 456;
        List<RepoData> emptyRepoList = new ArrayList<>();

        when(repository.findByUserId(userId)).thenReturn(emptyRepoList);

        List<RepoData> foundRepoList = repoDataService.findByUserId(userId);

        assertTrue(foundRepoList.isEmpty());
    }

    @Test
    public void testGetRepoData_Positive() {
        // Mocking UserData object
        UserData userData = new UserData();
        userData.setUserName("exampleUser");
        userData.setUserAccessToken("exampleToken");

        // Mocking a response from GitHub API
        String responseBody = "[{\"id\":1,\"name\":\"repo1\"},{\"id\":2,\"name\":\"repo2\"}]";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-RateLimit-Limit", "5000");
        responseHeaders.set("X-RateLimit-Remaining", "4999");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(responseHeaders).body(responseBody);

        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenReturn(responseEntity);

        String repoData = repoDataService.getRepoData(userData);

        assertNotNull(repoData);
        // Assertions based on the expected response from the GitHub API
        assertTrue(repoData.contains("repo1"));
        assertTrue(repoData.contains("repo2"));
    }

    @Test
    public void testGetRepoData_Negative() {
        // Mocking UserData object with missing access token
        UserData userData = new UserData();
        userData.setUserName("exampleUser");
        // No access token set

        // Simulating a failure in retrieving data from GitHub API by throwing an exception
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND)); // Simulate 404 Not Found

        String repoData;
        try {
            repoData = repoDataService.getRepoData(userData);
            fail("Expected an exception due to API call failure, but no exception was thrown.");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
            // Add further assertions or handling based on the expected behavior when the API call fails
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testGetRepositoryPRs() {
        // Mocking required data
        RepoData repoData = new RepoData();
        repoData.setName("exampleRepo");
        repoData.setUserId(1);
        UserData userData = new UserData();
        when(userDataService.getOne(anyInt())).thenReturn(userData);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-RateLimit-Limit", "5000");
        responseHeaders.set("X-RateLimit-Remaining", "4999");

        // Mocking REST response
        String mockJsonResponse = "[{\"some\":\"data\"},{\"another\":\"data\"}]";
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok().headers(responseHeaders).body(mockJsonResponse);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Invoke the method being tested
        Integer prCount = repoDataService.getRepositoryPRs(repoData);

        // Verifying interactions and assertions
        verify(userDataService, times(1)).getOne(anyInt());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));


        int expectedPRCount = 2; // Assuming 2 items in the mocked JSON array
        assert prCount != null;
        assert prCount.equals(expectedPRCount);
    }

    @Test
    public void testGetRepositoryForksCount() {
        // Mocking required data
        RepoData repoData = new RepoData();
        repoData.setName("exampleRepo");
        repoData.setUserId(1);

        UserData userData = new UserData();
        when(userDataService.getOne(anyInt())).thenReturn(userData);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-RateLimit-Limit", "5000");
        responseHeaders.set("X-RateLimit-Remaining", "4999");

        // Mocking REST response
        String mockJsonResponse = "{\"forks_count\": 5}"; // Simulating JSON response
        ResponseEntity<String> mockResponseEntity = ResponseEntity.ok().headers(responseHeaders).body(mockJsonResponse);;
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponseEntity);

        // Invoke the method being tested
        Integer forksCount = repoDataService.getRepositoryForksCount(repoData);

        // Verifying interactions and assertions
        verify(userDataService, times(1)).getOne(anyInt());
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));

        // Assert the returned forks count matches the mocked JSON response
        int expectedForksCount = 5;
        assert forksCount != null;
        assert forksCount.equals(expectedForksCount);
    }

    @Test
    public void testGetRepositoryLanguages_Positive() {
        // Mocking RepoData object
        RepoData repoData = new RepoData();
        repoData.setName("exampleRepo");
        repoData.setUserId(1);

        // Mocking UserData and ResponseEntity
        UserData userData = new UserData();
        userData.setUserAccessToken("dummyAccessToken");

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-RateLimit-Limit", "5000");
        responseHeaders.set("X-RateLimit-Remaining", "4999");

        Map<String, Integer> languagesMap = Collections.singletonMap("Java", 100); // Example language map

        ResponseEntity<Map> responseEntity = ResponseEntity.ok()
                .headers(responseHeaders)
                .body(languagesMap);

        // Mocking UserDataService and RestTemplate
        when(userDataService.getOne(anyInt())).thenReturn(userData);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Call the method to test
        Map<String, Integer> result = repoDataService.getRepositoryLanguages(repoData);

        // Assertions
        assertNotNull(result);
        assertTrue(result.containsKey("Java"));
        assertEquals(100, result.get("Java"));
    }

    @Test
    public void testGetRepositoryLanguages_Negative() {
        // Mocking RepoData object for a non-existent repository
        RepoData repoData = new RepoData();
        repoData.setName("nonexistentRepo");
        repoData.setUserId(1);

        // Mocking a non-existent repository response from GitHub API
        String apiUrl = "https://api.github.com/repos/username/nonexistentRepo/languages";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-RateLimit-Limit", "5000");
        responseHeaders.set("X-RateLimit-Remaining", "4999");
        ResponseEntity<Map> responseEntity = ResponseEntity.notFound().headers(responseHeaders).build();

        when(userDataService.getOne(anyInt())).thenReturn(new UserData());

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        // Call the method to test
        Map<String, Integer> languages = repoDataService.getRepositoryLanguages(repoData);

        // Assertions
        assertNull(languages);
    }

    @Test
    public void testGetParentRepo_Positive() {
        // Mocking RepoData object for a forked repository
        RepoData forkedRepoData = new RepoData();
        forkedRepoData.setName("forkedRepo");
        forkedRepoData.setUserId(1);
        forkedRepoData.setIsFork(true);

        // Mocking a response from GitHub API for a forked repository's parent
        String apiUrl = "https://api.github.com/repos/username/forkedRepo";
        String responseBody = "{\"source\": {\"full_name\": \"username/parentRepo\"}}";
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-RateLimit-Limit", "5000");
        responseHeaders.set("X-RateLimit-Remaining", "4999");
        ResponseEntity<String> responseEntity = ResponseEntity.ok().headers(responseHeaders).body(responseBody);

        when(userDataService.getOne(1)).thenReturn(new UserData());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(responseEntity);

        String parentRepo = repoDataService.getParentRepo(forkedRepoData);
        assertEquals("username/parentRepo", parentRepo);
    }

    @Test
    public void testGetParentRepo_Negative() {
        // Mocking RepoData object for a non-forked repository
        RepoData nonForkedRepoData = new RepoData();
        nonForkedRepoData.setName("nonForkedRepo");
        nonForkedRepoData.setUserId(1);
        nonForkedRepoData.setIsFork(false);

        // The method should return the same name as it's not a forked repository
        String parentRepo = repoDataService.getParentRepo(nonForkedRepoData);

        assertEquals("nonForkedRepo", parentRepo);
    }

    @Test
    public void testGetRepoLOC_Positive() {
        // Mocking RepoData object
        RepoData repoData = new RepoData();
        repoData.setName("exampleRepo");
        repoData.setUserId(1);

        final int expectedLinesOfCode = 1000;

        // Mocking a response from the external API for repository lines of code (LOC)
        String apiUrl = "https://api.codetabs.com/v1/loc?github=exampleRepo";
        List<Map<String, Object>> locArrMap = new ArrayList<>();
        Map<String, Object> language1 = new HashMap<>();
        language1.put("language", "Total");
        language1.put("linesOfCode", expectedLinesOfCode);
        locArrMap.add(language1);

        when(restTemplate.getForObject(apiUrl, List.class)).thenReturn(locArrMap);

        String repoLOC = repoDataService.getRepoLOC(repoData);

        assertEquals("1000", repoLOC);
    }

    @Test
    public void testGetRepoLOC_Negative() {
        // Mocking RepoData object
        RepoData repoData = new RepoData();
        repoData.setName("largeRepo");
        repoData.setUserId(1);

        // Simulating an error response from the external API for a large repository
        String apiUrl = "https://api.codetabs.com/v1/loc?github=username/largeRepo";

        when(userDataService.getOne(1)).thenReturn(new UserData());
        when(restTemplate.getForObject(apiUrl, List.class)).thenThrow(new RuntimeException("Repository > 500 MB"));

        String repoLOC = repoDataService.getRepoLOC(repoData);

        assertEquals("Repo > 500 MB", repoLOC);
    }

    @Test
    public void testGetRepoContributors_Positive() {
        // Mocking RepoData object
        RepoData repoData = new RepoData();
        repoData.setName("exampleRepo");
        repoData.setUserId(1);

        // Mocking a response from GitHub API for repository contributors
        String apiUrl = "https://api.github.com/repos/username/exampleRepo/contributors";
        List<Map<String, Object>> contributors = Arrays.asList(
                createContributorMap("John", 10),
                createContributorMap("Alice", 15)
        );
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("X-RateLimit-Limit", "5000");
        responseHeaders.set("X-RateLimit-Remaining", "4999");
        ResponseEntity<List> responseEntity = ResponseEntity.ok().headers(responseHeaders).body(contributors);

        when(userDataService.getOne(1)).thenReturn(new UserData());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(List.class)))
                .thenReturn(responseEntity);

        Map<String, Integer> repoContributors = repoDataService.getRepoContributors(repoData);

        assertNotNull(repoContributors);
        assertEquals(2, repoContributors.size());
        assertTrue(repoContributors.containsKey("John"));
        assertTrue(repoContributors.containsKey("Alice"));
        assertEquals(10, repoContributors.get("John"));
        assertEquals(15, repoContributors.get("Alice"));
    }

    @Test
    public void testGetRepoLOCRepoTooBigException() {
        // Mocking required data
        RepoData repoData = new RepoData();
        repoData.setName("repoName"); // Set the repo name for testing

        // Simulating HttpClientErrorException with a specific response body
        String responseBody = "{\"message\": \"Repo Size > 500 MB\"}";
        HttpClientErrorException mockException = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST, "Bad Request", responseBody.getBytes(), null);

        when(restTemplate.getForObject(anyString(), eq(List.class)))
                .thenThrow(mockException);

        // Invoke the method being tested
        String result = repoDataService.getRepoLOC(repoData);

        // Verify interactions and assertions
        verify(restTemplate, times(1)).getForObject(anyString(), eq(List.class));
        assert result.equals("Repo > 500 MB");
    }

    @Test
    public void testGetRepoLOCGenericException() {
        // Mocking required data
        RepoData repoData = new RepoData();
        repoData.setName("repoName"); // Set the repo name for testing

        // Simulating a generic exception
        when(restTemplate.getForObject(anyString(), eq(List.class)))
                .thenThrow(new RuntimeException("An error occurred"));

        // Invoke the method being tested
        String result = repoDataService.getRepoLOC(repoData);

        // Verify interactions and assertions
        verify(restTemplate, times(1)).getForObject(anyString(), eq(List.class));
        assert result.equals("Repo > 500 MB"); // As the catch block returns this string for any exception
    }


    private Map<String, Object> createContributorMap(String name, int contributions) {
        return Map.of("login", name, "contributions", contributions);
    }
}
