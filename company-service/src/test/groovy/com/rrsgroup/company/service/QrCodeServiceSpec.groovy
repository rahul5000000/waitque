package com.rrsgroup.company.service

import com.rrsgroup.common.dto.UserDto
import com.rrsgroup.common.util.ImageWrapper
import com.rrsgroup.company.entity.Company
import com.rrsgroup.company.entity.QrCode
import com.rrsgroup.company.repository.QrCodeRepository
import spock.lang.Specification

import java.awt.image.BufferedImage
import java.time.LocalDateTime

class QrCodeServiceSpec extends Specification {

    QrCodeRepository qrCodeRepository = Mock()
    QrCodeService qrCodeService = new QrCodeService(qrCodeRepository)

    def "generateQrCodes should create and persist given number of QR codes"() {
        given:
        def company = new Company()
        def user = Mock(UserDto) {
            getUserId() >> { -> "createdBy" }
        }
        def now = LocalDateTime.now()
        int count = 5

        when:
        def result = qrCodeService.generateQrCodes(count, company, user)

        then:
        1 * qrCodeRepository.saveAllAndFlush(_ as List<QrCode>) >> { List<QrCode> qrCodes ->
            assert qrCodes.get(0).size() == count
            assert qrCodes.get(0).every { it.company == company }
            assert qrCodes.get(0).every { it.createdBy == user.userId }
            assert qrCodes.get(0).every { it.updatedBy == user.userId }
            assert qrCodes.get(0).every { it.createdDate != null }
            assert qrCodes.get(0).every { it.updatedDate != null }
            return qrCodes.get(0) // return same list back
        }
        result.size() == count
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
