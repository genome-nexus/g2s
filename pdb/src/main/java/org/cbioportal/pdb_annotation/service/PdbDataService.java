package org.cbioportal.pdb_annotation.service;

import org.cbioportal.pdb_annotation.domain.PdbHeader;

/**
 * @author Selcuk Onur Sumer
 */
public interface PdbDataService
{
    PdbHeader getPdbHeader(String pdbId);
}
