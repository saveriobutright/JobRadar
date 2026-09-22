package io.github.saveriobutright.jobradar.api;

import io.github.saveriobutright.jobradar.jobs.InvalidSearchCriteriaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidSearchCriteriaException.class)
    public ProblemDetail handleInvalidSearchCriteria(
            InvalidSearchCriteriaException exception,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );

        problem.setTitle("Invalid request");
        problem.setInstance(
                URI.create(request.getRequestURI())
        );

        return problem;
    }
}
