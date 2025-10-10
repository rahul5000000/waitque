package com.rrsgroup.company.service

import com.rrsgroup.common.domain.UserRole
import com.rrsgroup.common.dto.SystemUserDto
import com.rrsgroup.common.util.ImageWrapper
import com.rrsgroup.company.dto.QrCodeDto
import com.rrsgroup.company.entity.Company
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.LocalDateTime

class QrCodeServiceSpec extends Specification {

    WebClient webClient = Mock()
    def requestHeadersUriSpec = Mock(RequestHeadersUriSpec)
    def requestHeadersSpec = Mock(RequestHeadersSpec)
    def responseSpec = Mock(ResponseSpec)
    QrCodeService qrCodeService = new QrCodeService(webClient)

    def setup() {
        qrCodeService.customerServiceBaseUrl = "http://customer-service"
    }

    def "generateQrCodes should call WebClient with correct URL and return list"() {
        given:
        def company = new Company(id: 123L)
        def user = new SystemUserDto("456", "System", "User", 'system@test.com', "system", UserRole.SYSTEM)
        def qrCode1 = UUID.randomUUID()
        def qrCode2 = UUID.randomUUID()
        def qrCodes = [
                new QrCodeDto(1L, 123L, 1L, qrCode1, LocalDateTime.now(), LocalDateTime.now(), "1", "1"),
                new QrCodeDto(1L, 123L, 1L, qrCode2, LocalDateTime.now(), LocalDateTime.now(), "1", "1")]

        when:
        def result = qrCodeService.generateQrCodes(2, company, user)

        then:
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri("http://customer-service/api/system/qrcodes?count=2&companyId=123&userId=456") >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(_ as ParameterizedTypeReference<List<QrCodeDto>>) >> Mono.just(qrCodes)
        0 * _  // ensure no unexpected interactions

        result.size() == 2
        result[0].qrCode() == qrCode1
        result[1].qrCode() == qrCode2
    }

    def "generateQrCodes should propagate exceptions from WebClient"() {
        given:
        def company = new Company(id: 10L)
        def user = new SystemUserDto("456", "System", "User", 'system@test.com', "system", UserRole.SYSTEM)

        when:
        qrCodeService.generateQrCodes(1, company, user)

        then:
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri(_) >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(_ as ParameterizedTypeReference<List<QrCodeDto>>) >> { throw new RuntimeException("Remote error") }

        def ex = thrown(RuntimeException)
        ex.message == "Remote error"
    }

    def "generateQRCodeImage should return a valid BufferedImage"() {
        given:
        String text = "Hello QR"
        int width = 200
        int height = 200

        when:
        ImageWrapper image = qrCodeService.generateQRCodeImage(text, width, height)

        then:
        image != null
        image.getImage() != null
        image.getImage().width == width
        image.getImage().height == height
    }

    def "generateQRCodeImage should throw RuntimeException for invalid input"() {
        when:
        qrCodeService.generateQRCodeImage(null, 200, 200)

        then:
        def ex = thrown(RuntimeException)
        ex.message.contains("Failed to generated QR code")
    }
}
