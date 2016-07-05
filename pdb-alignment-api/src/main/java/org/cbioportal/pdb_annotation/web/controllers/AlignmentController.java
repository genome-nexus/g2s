package org.cbioportal.pdb_annotation.web.controllers;

import java.util.Iterator;

import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.AlignmentDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AlignmentController {
	
	
	@RequestMapping("/get-by-ensemblid")
	  @ResponseBody
	  public String findByEnsemblid(String ensemblid) {
	    String alignmentid;
	    String pdbno;	  
	    String pdbid;
		String pdbfrom;
		String pdbto;
		String ensemblfrom;
		String ensemblto;
		String ensemblalign;
		String pdbalign;
		String midlinealign;
	    
		String outstr="";
	    try {
	      Iterator<Alignment> it = alignmentDao.findByEnsemblid(ensemblid).iterator();
	       while(it.hasNext()){
	    	   Alignment alignment = (Alignment)it.next();
	    	   alignmentid = String.valueOf(alignment.getAlignmentid());
	 	      ensemblid=alignment.getEnsemblid();
	 	      ensemblfrom = String.valueOf(alignment.getEnsemblfrom());
	 	      ensemblto = String.valueOf(alignment.getEnsemblto());
	 	      pdbno = alignment.getPdbno();
	 	      pdbfrom = String.valueOf(alignment.getPdbfrom());
	 	      pdbto = String.valueOf(alignment.getPdbto());
	 	      ensemblalign = alignment.getEnsemblalign();
	 	      pdbalign = alignment.getPdbalign();  
	 	      outstr=outstr+"The alignmentid is: " + alignmentid + "&nbsp" + ensemblid + "&nbsp" + ensemblfrom + "&nbsp" + ensemblto + "&nbsp" + pdbno + "&nbsp" 
	 		    		+ pdbfrom + "&nbsp" + pdbto + "&nbsp" + ensemblalign + "&nbsp" + pdbalign + "<br>";
	       }
	        	
	    }
	    catch (Exception ex) {
	    	ex.printStackTrace();
	      return "Alignment not found\n";
	    }
	    return outstr;
	  }
	  
	  
	  
	  // ------------------------
	  // PRIVATE FIELDS
	  // ------------------------

	  @Autowired
	  private AlignmentDAO alignmentDao;
	  
	


}
