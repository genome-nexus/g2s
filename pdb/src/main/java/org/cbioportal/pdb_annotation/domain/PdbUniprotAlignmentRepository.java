package org.cbioportal.pdb_annotation.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author Selcuk Onur Sumer
 */
public interface PdbUniprotAlignmentRepository extends CrudRepository<PdbUniprotAlignment, Long>
{
	List<PdbUniprotAlignment> findByPdbId(String pdbId);
}
