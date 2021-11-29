package io.g740.commons.api;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bbottong
 */
public class RequestIdLogFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestIdLogFilter.class);

    public static final String REQUEST_ID_KEY = "requestId";
    public static final String REQUEST_URI_KEY = "requestUri";
    public static final String REQUEST_PARAMETERS_KEY = "requestParameters";
    public static final String LOG_LEVEL_KEY = "logLevel";

    /**
     * 1st dimension - domain, 1st dimension - timestamp in milliseconds
     */
    private final Map<Long, Long> currentMillisecondsWatermark = new ConcurrentHashMap<>(100);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String requestIdValue = String.valueOf(generateRandomUid());
        MDC.put(REQUEST_ID_KEY, requestIdValue);

        String logLevelValue = servletRequest.getParameter(LOG_LEVEL_KEY);
        if (!Strings.isNullOrEmpty(logLevelValue)) {
            MDC.put(LOG_LEVEL_KEY, logLevelValue.toUpperCase());
        }

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            MDC.put(REQUEST_URI_KEY, httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI());
            MDC.put(REQUEST_PARAMETERS_KEY, JSONObject.toJSONString(httpServletRequest.getParameterMap()));
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.remove(REQUEST_ID_KEY);
            MDC.remove(LOG_LEVEL_KEY);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void destroy() {

    }

    public synchronized Long generateRandomUid() {
        Date now = new Date();
        Long nowTimeInMilliseconds = now.getTime();
        Long newWatermark = 1L;

        Long lastWatermark = this.currentMillisecondsWatermark.get(nowTimeInMilliseconds);
        if (lastWatermark != null) {
            newWatermark = lastWatermark + 1L;

            // 1 millisecond内超出负载，借用下一个millisecond
            if (newWatermark > 999L) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    LOGGER.info(e.getMessage(), e);
                } finally {
                    Date newNow = new Date();
                    Long newNowTimeInMilliseconds = newNow.getTime();
                    if (newNowTimeInMilliseconds > nowTimeInMilliseconds) {
                        now = newNow;
                        nowTimeInMilliseconds = newNowTimeInMilliseconds;
                    } else {
                        throw new RuntimeException("1 millisecond exceeds 999 uid, and cannot borrow from next 1 millisecond");
                    }
                }
            }

            // 清除历史毫秒水位
            if (this.currentMillisecondsWatermark.size() > 100) {
                this.currentMillisecondsWatermark.clear();
            }
        }

        Long result = nowTimeInMilliseconds * 1000 + newWatermark;
        this.currentMillisecondsWatermark.put(nowTimeInMilliseconds, newWatermark);
        return result;
    }
}
