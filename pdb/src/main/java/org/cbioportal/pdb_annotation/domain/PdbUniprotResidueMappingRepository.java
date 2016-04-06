package org.cbioportal.pdb_annotation.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Selcuk Onur Sumer
 */
public interface PdbUniprotResidueMappingRepository
    extends CrudRepository<PdbUniprotResidueMapping, PdbUniprotResidueMappingPK>
{
    List<PdbUniprotResidueMapping> findByAlignmentId(long alignmentId);

    @Query("select max(m.alignmentId) from #{#entityName} m")
    long getMaxId();
}
