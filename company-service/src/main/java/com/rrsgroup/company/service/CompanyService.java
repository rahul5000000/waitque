package com.rrsgroup.company.service;

import com.rrsgroup.common.domain.SortDirection;
import com.rrsgroup.common.exception.IllegalRequestException;
import com.rrsgroup.common.exception.IllegalUpdateException;
import com.rrsgroup.company.domain.FileStage;
import com.rrsgroup.company.domain.UploadFileType;
import com.rrsgroup.company.entity.Company;
import com.rrsgroup.company.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompanyService {
    private final CompanyRepository repository;

    @Autowired
    public CompanyService(CompanyRepository repository) {
        this.repository = repository;
    }

    public Company createCompany(Company request) {
        return repository.save(request);
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

    public Company updateCompany(Company updateRequest) {
        if(updateRequest.getId() == null) {
            throw new IllegalUpdateException("Id must be set to update the record");
        }
        return repository.save(updateRequest);
    }

    public String getBucketKeyForFileAndStage(Company company, UploadFileType fileType, String fileName, FileStage stage) {
        if(fileType == UploadFileType.LOGO) {
            return stage.toString() + "/" + company.getId() + "/" + fileType.getFolder() + "/" + fileName;
        } else {
            throw new IllegalRequestException("UploadFileType " + fileType + " is not supported");
        }
    }
}
