package org.cbioportal.pdb_annotation.web.models;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface EnsemblDAO extends CrudRepository<Alignment, Long>{

	public Iterable<Ensembl> findByEnsemblId(String ensemblid);
}
