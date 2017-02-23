package org.cbioportal.pdb_annotation.web.domain;

import java.util.List;

import org.cbioportal.pdb_annotation.web.models.Statistics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatisticsRepository extends JpaRepository<Statistics,Long>{
    //Find newest update
    public List<Statistics> findTop2ByOrderByIdDesc();
}
