package com.rrsgroup.common;

import com.rrsgroup.common.entity.Email;

public record EmailRequest(Email toEmail, String subject, String htmlBody) {
}
