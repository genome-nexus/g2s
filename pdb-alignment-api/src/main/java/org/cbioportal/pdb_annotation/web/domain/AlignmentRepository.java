package org.cbioportal.pdb_annotation.web.domain;

import java.util.List;
import javax.transaction.Transactional;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Juexin Wang
 *
 */
@Transactional
public interface AlignmentRepository extends JpaRepository<Alignment, Long> {
    public List<Alignment> findByEnsemblId(String ensemblId);
}
