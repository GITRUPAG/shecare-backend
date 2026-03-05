package com.app.shecare.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.app.shecare.mongo.document.CommunityReport;

public interface CommunityReportRepository
        extends MongoRepository<CommunityReport, String> {
}
