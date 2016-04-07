package org.cbioportal.pdb_annotation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @author Selcuk Onur Sumer
 */
public interface PdbUniprotResidueMappingRepository
    extends JpaRepository<PdbUniprotResidueMapping, PdbUniprotResidueMappingPK>
{
    List<PdbUniprotResidueMapping> findByAlignmentId(long alignmentId);

    @Query("select max(m.alignmentId) from #{#entityName} m")
    Long getMaxId();
}
