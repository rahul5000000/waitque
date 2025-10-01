package com.rrsgroup.common.exception

import com.rrsgroup.common.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolation
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Path
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import spock.lang.Specification

class GlobalExceptionHandlerSpec extends Specification {
    def handler = new GlobalExceptionHandler()

    def "handleValidationConstraintViolations returns BAD_REQUEST with violation messages"() {
        given:
        def fieldPath = Mock(Path) {
            toString() >> { -> "fieldName" }
        }
        def violation = Mock(ConstraintViolation) {
            getPropertyPath() >> { -> fieldPath }
            getMessage() >> { -> "must not be blank" }
        }
        def ex = new ConstraintViolationException([violation] as Set)
        def request = Mock(HttpServletRequest) {
            getMethod() >> "POST"
            getRequestURI() >> "/api/test"
        }

        when:
        ResponseEntity<ErrorResponse> response = handler.handleValidationConstraintViolations(ex, request)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.status == HttpStatus.BAD_REQUEST.value()
        response.body.error == HttpStatus.BAD_REQUEST.reasonPhrase
        response.body.message.contains("'fieldName' must not be blank")
        response.body.path == "POST /api/test"
    }

    def "handleValidationConstraintViolations handles exception in violation parsing"() {
        given:
        def ex = Mock(ConstraintViolationException) {
            getConstraintViolations() >> { throw new RuntimeException("boom") }
        }
        def request = Mock(HttpServletRequest) {
            getMethod() >> "GET"
            getRequestURI() >> "/api/error"
        }

        when:
        ResponseEntity<ErrorResponse> response = handler.handleValidationConstraintViolations(ex, request)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.message == "Unknown error"
        response.body.path == "GET /api/error"
    }

    def "handleIllegalUpdateException returns CONFLICT with exception message"() {
        given:
        def ex = new IllegalUpdateException("Illegal update attempted")
        def request = Mock(HttpServletRequest) {
            getMethod() >> "PUT"
            getRequestURI() >> "/api/resource/1"
        }

        when:
        ResponseEntity<ErrorResponse> response = handler.handleIllegalUpdateException(ex, request)

        then:
        response.statusCode == HttpStatus.CONFLICT
        response.body.status == HttpStatus.CONFLICT.value()
        response.body.error == HttpStatus.CONFLICT.reasonPhrase
        response.body.message == "Illegal update attempted"
        response.body.path == "PUT /api/resource/1"
    }

    def "handleIllegalRequestException returns BAD_REQUEST with exception message"() {
        given:
        def ex = new IllegalRequestException("Illegal request attempted")
        def request = Mock(HttpServletRequest) {
            getMethod() >> "PUT"
            getRequestURI() >> "/api/resource/1"
        }

        when:
        ResponseEntity<ErrorResponse> response = handler.handleIllegalRequestException(ex, request)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.status == HttpStatus.BAD_REQUEST.value()
        response.body.error == HttpStatus.BAD_REQUEST.reasonPhrase
        response.body.message == "Illegal request attempted"
        response.body.path == "PUT /api/resource/1"
    }

    def "handleRecordNotFoundException returns NOT_FOUND with exception message"() {
        given:
        def ex = new RecordNotFoundException("Record not found")
        def request = Mock(HttpServletRequest) {
            getMethod() >> "GET"
            getRequestURI() >> "/api/resource/1"
        }

        when:
        ResponseEntity<ErrorResponse> response = handler.handleRecordNotFoundException(ex, request)

        then:
        response.statusCode == HttpStatus.NOT_FOUND
        response.body.status == HttpStatus.NOT_FOUND.value()
        response.body.error == HttpStatus.NOT_FOUND.reasonPhrase
        response.body.message == "Record not found"
        response.body.path == "GET /api/resource/1"
    }

    def "handleGenericException returns INTERNAL_SERVER_ERROR with exception message"() {
        given:
        def ex = new Exception("Generic exception")
        def request = Mock(HttpServletRequest) {
            getMethod() >> "GET"
            getRequestURI() >> "/api/resource/1"
        }

        when:
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request)

        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
        response.body.status == HttpStatus.INTERNAL_SERVER_ERROR.value()
        response.body.error == HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase
        response.body.message == "Generic exception"
        response.body.path == "GET /api/resource/1"
    }
}
