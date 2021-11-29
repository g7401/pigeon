package io.g740.pigeon.init.config;

import io.g740.commons.api.RequestIdLogFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.*;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.stream.Collectors;

/**
 * @author bbottong
 */
@SuppressWarnings("unchecked")
@Configuration
public class GeneralConfigurator {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralConfigurator.class);

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setThreadNamePrefix("SCH-");
        threadPoolTaskScheduler.setPoolSize(10);
        return threadPoolTaskScheduler;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        Duration connectTimeoutDuration = Duration.ofSeconds(30);
        Duration readTimeoutDuration = Duration.ofSeconds(60);
        RestTemplate restTemplate =
                builder.setConnectTimeout(connectTimeoutDuration).setReadTimeout(readTimeoutDuration).build();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
                InputStream bodyInputStream = response.getBody();
                InputStreamReader isr = new InputStreamReader(bodyInputStream, StandardCharsets.UTF_8);
                BufferedReader bf = new BufferedReader(isr);
                String errorMsg = bf.lines().collect(Collectors.joining());
                LOGGER.error("RestTemplate error msg: {}", errorMsg);
                switch (statusCode.series()) {
                    case CLIENT_ERROR:
                        throw new HttpClientErrorException(statusCode, response.getStatusText(), response.getHeaders(), this.getResponseBody(response), this.getCharset(response));
                    case SERVER_ERROR:
                        throw new HttpServerErrorException(statusCode, response.getStatusText(), response.getHeaders(), this.getResponseBody(response), this.getCharset(response));
                    default:
                        throw new RestClientException(
                                String.format("Rest Template Error, %s, %s, %s, %s",
                                        statusCode,
                                        response.getStatusText(),
                                        response.getHeaders(),
                                        new String(this.getResponseBody(response), this.getCharset(response))));
                }
            }
        });

        return restTemplate;
    }

    @PreDestroy
    public void onShutDown() {
        LOGGER.info("closing application context..let's do the final resource cleanup");
    }

    @Bean
    public FilterRegistrationBean requestIdLogFilterRegistration() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new RequestIdLogFilter());
        filterRegistrationBean.setName("requestIdLogFilter");
        filterRegistrationBean.setOrder(0);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }
}