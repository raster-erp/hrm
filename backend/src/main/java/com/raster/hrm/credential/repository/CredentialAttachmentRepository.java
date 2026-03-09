package com.raster.hrm.credential.repository;

import com.raster.hrm.credential.entity.CredentialAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CredentialAttachmentRepository extends JpaRepository<CredentialAttachment, Long> {

    List<CredentialAttachment> findByCredentialId(Long credentialId);
}
