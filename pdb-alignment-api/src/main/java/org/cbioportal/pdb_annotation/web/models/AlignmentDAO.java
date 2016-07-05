package org.cbioportal.pdb_annotation.web.models;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface AlignmentDAO extends CrudRepository<Alignment, Long>{
		
	public Iterable<Alignment> findByEnsemblid(String ensemblid);
	

}
