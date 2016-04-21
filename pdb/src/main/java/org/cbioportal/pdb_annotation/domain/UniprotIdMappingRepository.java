package org.cbioportal.pdb_annotation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Selcuk Onur Sumer
 */
public interface UniprotIdMappingRepository
    extends JpaRepository<UniprotIdMapping, UniprotIdMappingPK>
{
    List<UniprotIdMapping> findByUniprotId(String uniprotId);
    List<UniprotIdMapping> findByUniprotAcc(String uniprotAcc);
    List<UniprotIdMapping> findByEntrezGeneId(long entrezGeneId);
}
