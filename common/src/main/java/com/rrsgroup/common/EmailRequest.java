package com.rrsgroup.common;

import com.rrsgroup.common.domain.EmailTemplate;
import com.rrsgroup.common.entity.Email;

public record EmailRequest(Email toEmail, EmailTemplate template, String htmlBody) {
}
