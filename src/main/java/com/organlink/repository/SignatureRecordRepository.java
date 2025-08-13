package com.organlink.repository;

import com.organlink.entity.SignatureRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, Long> {
}
