package io.github.domgarlor.jobtracker.shared;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.github.domgarlor.jobtracker.cv.DuplicateCvVersionException;
import io.github.domgarlor.jobtracker.cv.UnsupportedCvFileException;

/**
 * Translates domain exceptions into RFC 9457 "problem detail" responses. Validation errors (400) are already handled
 * by {@link ResponseEntityExceptionHandler}.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	ProblemDetail handleNotFound(NotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(DuplicateCvVersionException.class)
	ProblemDetail handleDuplicateCvVersion(DuplicateCvVersionException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setProperty("existingVersionId", ex.getExistingVersionId());
		return problem;
	}

	@ExceptionHandler(ConflictException.class)
	ProblemDetail handleConflict(ConflictException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
	}

	/** Two simultaneous requests can both pass the service checks; the database unique constraints decide. */
	@ExceptionHandler(DataIntegrityViolationException.class)
	ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "The data conflicts with an existing record");
	}

	@ExceptionHandler(UnsupportedCvFileException.class)
	ProblemDetail handleUnsupportedFile(UnsupportedCvFileException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.getMessage());
	}

}
