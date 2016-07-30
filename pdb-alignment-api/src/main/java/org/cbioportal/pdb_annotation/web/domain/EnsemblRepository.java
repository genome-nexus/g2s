package org.cbioportal.pdb_annotation.web.domain;

import java.util.List;
import javax.transaction.Transactional;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 
 * @author Juexin Wang
 *
 */
@Transactional
public interface EnsemblRepository extends JpaRepository<Alignment, Long> {
    public List<Ensembl> findByEnsemblId(String ensemblId);
}
