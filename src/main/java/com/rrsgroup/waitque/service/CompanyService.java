package com.rrsgroup.waitque.service;

import com.rrsgroup.waitque.entity.Company;
import com.rrsgroup.waitque.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
