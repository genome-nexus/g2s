package org.cbioportal.pdb_annotation.web.domain;

import java.util.List;

import javax.transaction.Transactional;

import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 
 * @author Juexin Wang
 *
 */
@Transactional
public interface EnsemblRepository extends JpaRepository<Ensembl, Long>{
    public List<Ensembl> findByEnsemblId(String ensemblId);
}
