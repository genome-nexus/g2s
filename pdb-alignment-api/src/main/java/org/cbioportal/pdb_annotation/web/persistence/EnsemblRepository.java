package org.cbioportal.pdb_annotation.web.persistence;

import javax.transaction.Transactional;

import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.springframework.data.repository.CrudRepository;

@Transactional
public interface EnsemblRepository extends CrudRepository<Alignment, Long>{

	public Iterable<Ensembl> findByEnsemblid(String ensemblid);
}
