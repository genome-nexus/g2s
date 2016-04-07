package org.cbioportal.pdb_annotation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Selcuk Onur Sumer
 */
public interface PdbUniprotAlignmentRepository extends JpaRepository<PdbUniprotAlignment, Long>
{
    List<PdbUniprotAlignment> findByPdbId(String pdbId);
    List<PdbUniprotAlignment> findByUniprotId(String uniprotId);
}
