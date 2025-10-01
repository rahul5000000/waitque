package com.rrsgroup.company.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rrsgroup.common.dto.AdminUserDto;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.QrCode;
import com.rrsgroup.company.repository.QrCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class QrCodeService {
    private final QrCodeRepository qrCodeRepository;

    @Autowired
    public QrCodeService(QrCodeRepository qrCodeRepository) {
        this.qrCodeRepository = qrCodeRepository;
    }

    public List<QrCode> generateQrCodes(Integer count, Company company, UserDto createdBy) {
        LocalDateTime now = LocalDateTime.now();
        List<QrCode> qrCodesToSave = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> QrCode.builder().company(company).qrCode(UUID.randomUUID()).createdDate(now).createdBy(createdBy.getUserId()).updatedDate(now).updatedBy(createdBy.getUserId()).build())
                .toList();
        return qrCodeRepository.saveAllAndFlush(qrCodesToSave);
    }

    public BufferedImage generateQRCodeImage(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generated QR code", e);
        }
    }
}
