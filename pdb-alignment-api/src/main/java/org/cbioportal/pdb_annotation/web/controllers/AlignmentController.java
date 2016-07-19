package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.AlignmentDAO;
import org.cbioportal.pdb_annotation.web.models.EnsemblDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class AlignmentController {
	
	@RequestMapping("/StructureMappingQuery")
	  @ResponseBody
	  public List<Alignment> findByEnsemblidinAlignment(String ensemblid) {
	    
		List<Alignment> list = new ArrayList<Alignment>();
	    try {
	      Iterator<Alignment> it = alignmentDao.findByEnsemblId(ensemblid).iterator();
	       while(it.hasNext()){
	    	   Alignment alignment = (Alignment)it.next();
	    	   list.add(alignment);
	       }	
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	    return list;
	    
	  }
	
	
	@RequestMapping("/ProteinIdentifierRecognitionQuery")
	  @ResponseBody
	  public boolean isExistedEnsemblidinAlignment(String ensemblid) {
	    try {
	      Iterator<Alignment> it = alignmentDao.findByEnsemblId(ensemblid).iterator(); 
	      if(it.hasNext()){
	    	   return true;
	       }else{
	    	   return false;
	       }
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	    }
	    return false;
	  }
	  
	  
	  
	  
	  
	  // ------------------------
	  // PRIVATE FIELDS
	  // ------------------------

	  @Autowired
	  private AlignmentDAO alignmentDao;
	  
	  //@Autowired
	  //private EnsemblDAO ensemblDao;
	  
	


}
