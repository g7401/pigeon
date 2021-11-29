package io.g740.commons.exception.component;

import com.google.common.base.Strings;
import io.g740.commons.api.Response;
import io.g740.commons.exception.BasicErrorCodeI18N;
import io.g740.commons.exception.BasicException;
import io.g740.commons.exception.IErrorCodeI18N;
import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

/**
 * @author : zxiuwu
 */
@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler implements ResponseBodyAdvice<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @Autowired
    private MessageSourceComponent messageSourceComponent;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Response handleException(Exception exception, Locale locale) {
        IErrorCodeI18N errorCodeI = null;
        String[] errorParams = null;
        if (exception instanceof BasicException) {
            errorCodeI = ((BasicException) exception).getErrorCode();
            errorParams = ((BasicException) exception).getErrorParams();
        } else {
            errorCodeI = BasicErrorCodeI18N.UNKNOWN_ERROR;
        }

        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(exception.getMessage()).append("\r\n");
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        if (stackTraceElements != null && stackTraceElements.length > 0) {
            int size = (stackTraceElements.length > 10 ? 10 : stackTraceElements.length);
            for (int i = 0; i < size; i++) {
                errorMessage.append(stackTraceElements[i]).append("\r\n");
            }
        }

        LOGGER.error(errorMessage.toString());

        return buildResponse(errorCodeI, exception, errorParams);
    }

    /**
     * IO错误
     *
     * @param exception
     * @param locale
     * @return
     */
    @ExceptionHandler(IOException.class)
    @ResponseBody
    public Response handleIOException(Exception exception, Locale locale) {
        if (exception instanceof ClientAbortException) {
            LOGGER.error(exception.getMessage());
            return null;
        }
        LOGGER.error(exception.getMessage(), exception);
        IErrorCodeI18N errorCodeI = BasicErrorCodeI18N.IO_ERROR;
        return buildResponse(errorCodeI, exception, null);
    }

    /**
     * SQL错误
     *
     * @param exception
     * @param locale
     * @return
     */
    @ExceptionHandler(SQLException.class)
    @ResponseBody
    public Response handleSQLException(Exception exception, Locale locale) {
        LOGGER.error(exception.getMessage(), exception);
        IErrorCodeI18N errorCodeI = BasicErrorCodeI18N.SQL_ERROR;
        return buildResponse(errorCodeI, exception, null);
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (body instanceof Response) {
            Response bodyResponse = (Response) body;
            if (!bodyResponse.isSuccessful()) {
                serverHttpResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {

        }
        return body;

    }

    /**
     * @param errorCodeI
     * @param exception
     * @param errorParams
     * @return
     */
    private Response buildResponse(IErrorCodeI18N errorCodeI, Exception exception, String[] errorParams) {
        Integer errorCode = errorCodeI.getCode();
        String errorCodeSymbol = errorCodeI.getCodeSymbol();
        String errorMessage = this.messageSourceComponent.getMessage(errorCodeI.getCodeSymbol(), errorParams);
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            errorMessage = exception.getMessage();
        } else {
            if (!Strings.isNullOrEmpty(exception.getMessage())) {
                errorMessage += " \n " + exception.getMessage();
            }
        }

        return Response.buildFailure(errorCode, errorMessage);
    }

}



