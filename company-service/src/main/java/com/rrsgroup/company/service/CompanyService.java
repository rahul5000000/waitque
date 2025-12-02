package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.dto.SuperUserDto;
import com.rrsgroup.common.dto.UserDto;
import com.rrsgroup.common.entity.Email;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.common.exception.RecordNotFoundException;
import com.rrsgroup.company.domain.EmailStatus;
import com.rrsgroup.company.domain.FileStage;
import com.rrsgroup.company.domain.UploadFileType;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.entity.CompanyEmail;
import com.rrsgroup.company.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {
    private final CompanyRepository repository;

    @Autowired
    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    public Company createCompany(Company request, SuperUserDto user) {
        LocalDateTime now = LocalDateTime.now();

        request.getEmails().forEach(companyEmail -> {
            if(companyEmail.getType() == null) {
                throw new IllegalRequestException("Email type must be specified");
            }
            companyEmail.setStatus(EmailStatus.ACTIVE);
            setAuditFieldsOnNewEmail(companyEmail.getEmail(), user, now);
        });

        return repository.save(request);
    }

    private void setAuditFieldsOnNewEmail(Email email, UserDto user, LocalDateTime now) {
        email.setCreatedBy(user.getUserId());
        email.setUpdatedBy(user.getUserId());
        email.setCreatedDate(now);
        email.setUpdatedDate(now);
    }

    public Page<Company> getListOfCompanies(int limit, int page, String sortField, SortDirection sortDir) {
        Pageable pageable = PageRequest.of(
                page,
                limit,
                sortDir == SortDirection.ASC ? Sort.by(sortField).ascending() : Sort.by(sortField).descending());
        return repository.findAll(pageable);
    }

    public Optional<Company> getCompany(Long companyId) {
        return repository.findById(companyId);
    }

    public Company updateCompany(Company updateRequest, UserDto user) {
        Optional<Company> existingCompanyOptional = getCompany(updateRequest.getId());

        if(existingCompanyOptional.isEmpty()) {
            throw new RecordNotFoundException("Company not found with that ID");
        }

        Company existingCompany = existingCompanyOptional.get();

        if(updateRequest.getId() == null) {
            throw new IllegalUpdateException("Id must be set to update the record");
        }

        List<CompanyEmail> newEmails = updateRequest.getEmails().stream().filter(companyEmail -> existingCompany.getEmails().stream().noneMatch(existingCompanyEmail -> existingCompanyEmail.getType().equals(companyEmail.getType()) && existingCompanyEmail.getStatus().equals(companyEmail.getStatus()) && existingCompanyEmail.getEmail().getEmail().equals(companyEmail.getEmail().getEmail()))).toList();
        List<CompanyEmail> deletedEmails = existingCompany.getEmails().stream().filter(existingCompanyEmail -> updateRequest.getEmails().stream().noneMatch(companyEmail -> companyEmail.getType().equals(existingCompanyEmail.getType()) && companyEmail.getStatus().equals(existingCompanyEmail.getStatus()) && companyEmail.getEmail().getEmail().equals(existingCompanyEmail.getEmail().getEmail()))).toList();

        LocalDateTime now = LocalDateTime.now();
        newEmails.forEach(companyEmail -> {
            if(companyEmail.getType() == null) {
                throw new IllegalRequestException("Email type must be specified");
            }
            companyEmail.setStatus(EmailStatus.ACTIVE);
            setAuditFieldsOnNewEmail(companyEmail.getEmail(), user, now);
        });

        deletedEmails.forEach(deletedEmail -> {
            deletedEmail.setStatus(EmailStatus.INACTIVE);
            setAuditFieldsOnUpdatedEmail(deletedEmail.getEmail(), user, now);
        });

        updateRequest.getEmails().addAll(deletedEmails);

        return repository.save(updateRequest);
    }

    private void setAuditFieldsOnUpdatedEmail(Email email, UserDto updatedByUser, LocalDateTime now) {
        email.setUpdatedBy(updatedByUser.getUserId());
        email.setUpdatedDate(now);
    }

    public String getBucketKeyForFileAndStage(Company company, UploadFileType fileType, String fileName, FileStage stage) {
        if(fileType == UploadFileType.LOGO) {
            return stage.toString() + "/" + fileType.getFolder()+ "/" + company.getId() + "/" + fileName;
        } else {
            throw new IllegalRequestException("UploadFileType " + fileType + " is not supported");
        }
    }
}
