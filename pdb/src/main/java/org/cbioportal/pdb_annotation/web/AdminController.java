package org.cbioportal.pdb_annotation.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cbioportal.pdb_annotation.service.PdbDataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Selcuk Onur Sumer
 */
//@RestController // shorthand for @Controller, @ResponseBody
//@CrossOrigin(origins="*") // allow all cross-domain requests
//@RequestMapping(value = "/admin/")
public class AdminController
{
    @Autowired
    @Qualifier("residueMappingFromMA")
    private PdbDataImportService dataImportServiceFromMA;

    @Autowired
    @Qualifier("residueMappingFromSifts")
    private PdbDataImportService dataImportServiceFromSifts;

    @ApiOperation(value = "repopulate the PDB database from the local file",
        nickname = "repopulatePdbDBFromMA")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success",
            response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/repopulate/fromMA",
        method = RequestMethod.GET,
        produces = "application/json")
    public String repopulatePdbDBFromMA()
    {
        // TODO make it async if possible
        dataImportServiceFromMA.importData();

        return "initialized PDB data import from mutation assessor";
    }

    @ApiOperation(value = "repopulate the PDB database from the local file",
        nickname = "repopulatePdbDBFromSifts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success",
            response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/repopulate/fromSifts",
        method = RequestMethod.GET,
        produces = "application/json")
    public String repopulatePdbDBFromSifts()
    {
        // TODO make it async if possible
        dataImportServiceFromSifts.importData();

        return "initialized PDB data import from sifts";
    }
}
