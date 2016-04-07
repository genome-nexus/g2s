package org.cbioportal.pdb_annotation.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cbioportal.pdb_annotation.service.PdbDataImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Selcuk Onur Sumer
 */
@RestController // shorthand for @Controller, @ResponseBody
//@CrossOrigin(origins="*") // allow all cross-domain requests
@RequestMapping(value = "/admin/")
public class AdminController
{
    private PdbDataImportService dataImportService;

    @Autowired
    public AdminController(PdbDataImportService dataImportService)
    {
        this.dataImportService = dataImportService;
    }

    @ApiOperation(value = "repopulate the PDB database from the local file",
        nickname = "repopulatePdbDB")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success",
            response = String.class),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/repopulate",
        method = RequestMethod.GET,
        produces = "application/json")
    public String repopulatePdbDB()
    {
        dataImportService.importData();

        return "PDB data import initialized";
    }
}
