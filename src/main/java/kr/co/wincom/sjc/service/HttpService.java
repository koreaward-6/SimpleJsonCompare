package kr.co.wincom.sjc.service;

import kr.co.wincom.sjc.dto.ResultDto;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class HttpService {

    private final static String APPLICATION_JSON = "application/json";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(21))
            .build();

    private HttpService() {

    }

    public static HttpService getInstance() {
        return SingletonHelper.SINGLETON;
    }

    public CompletableFuture<ResultDto> call(String method, String url, String bodyData) throws Exception {
        return httpClient.sendAsync(this.request(method, url, bodyData), HttpResponse.BodyHandlers.ofString())
                .thenApply(httpResponse -> new ResultDto(httpResponse.body(), null))
                .exceptionally(t -> new ResultDto(null, t.getMessage()));
    }

    private HttpRequest request(String method, String url, String bodyData) {
        HttpRequest.BodyPublisher bodyPayload = StringUtils.isBlank(bodyData) ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(bodyData);

        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", APPLICATION_JSON)
                .header("Accept", APPLICATION_JSON)
                .method(method, bodyPayload)
                .timeout(Duration.ofSeconds(21))
                .build();
    }

    private static class SingletonHelper {
        private static final HttpService SINGLETON = new HttpService();
    }
}
