package com.rrsgroup.company.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.util.ImageWrapper;
import com.rrsgroup.company.dto.QrCodeDto;
import com.rrsgroup.company.entity.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class QrCodeService {
    private final WebClient webClient;

    @Value("${microservices.customer-service.base-url}")
    private String customerServiceBaseUrl;

    @Autowired
    public QrCodeService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<QrCodeDto> generateQrCodes(Integer count, Company company, UserDto createdBy) {
        return webClient.get()
                .uri(customerServiceBaseUrl + "/api/system/qrcodes?count="+count+"&companyId="+company.getId()+"&userId="+createdBy.getUserId())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<QrCodeDto>>() {})
                .block();
    }

    public ImageWrapper generateQRCodeImage(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            return new ImageWrapper(MatrixToImageWriter.toBufferedImage(bitMatrix));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generated QR code", e);
        }
    }
}
