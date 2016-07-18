package org.cbioportal.pdb_annotation.web.persistence;

import javax.transaction.Transactional;

import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.springframework.data.repository.CrudRepository;

@Transactional
public interface AlignmentRepository extends CrudRepository<Alignment, Long>{
		
	public Iterable<Alignment> findByEnsemblid(String ensemblid);	

}
