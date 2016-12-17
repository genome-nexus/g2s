package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Inputsequence;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * Control the input sequence to blast
 * 
 * @author Juexin wang
 * 
 */
@Controller
public class InputsequenceController {

    @GetMapping("/input")
    public ModelAndView inputForm(Model model) {
        model.addAttribute("inputsequence", new Inputsequence());
        return new ModelAndView("input");
    }   
    
    @PostMapping("/input")
    public ModelAndView resultBack(@ModelAttribute @Valid Inputsequence inputsequence, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return new ModelAndView ("input");
        } 
        
        //is client behind something?
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
               ipAddress = request.getRemoteAddr();
        }
        
        inputsequence.setId(ipAddress);
        //inputsequence.setSequence(inputsequence.getSequence());
        
        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand= new PdbScriptsPipelineRunCommand();        
        List<Alignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);
        
        List<Residue> residues = new ArrayList<Residue> ();
        int inputAA=0;
        if(!inputsequence.getResidueNum().equals("")){
            inputAA = Integer.parseInt(inputsequence.getResidueNum());
        }
         
        for(Alignment ali:alignments){
            //if getResidueNum is empty, then return alignments
            //else, return residues
            if(inputsequence.getResidueNum().equals("") || (inputAA>=ali.getSeqFrom() && inputAA<=ali.getSeqTo())){
                Residue re = new Residue(); 
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore(ali.getBitscore());
                re.setChain(ali.getChain());
                re.setSeqAlign(ali.getSeqAlign());
                re.setSeqFrom(ali.getSeqFrom());
                re.setSeqId(ali.getSeqId());
                re.setSeqTo(ali.getSeqTo());
                re.setEvalue(ali.getEvalue());
                re.setIdentity(ali.getIdentity());
                re.setIdentp(ali.getIdentp());
                re.setMidlineAlign(ali.getMidlineAlign());
                re.setPdbAlign(ali.getPdbAlign());
                re.setPdbFrom(ali.getPdbFrom());
                re.setPdbId(ali.getPdbId());
                re.setPdbNo(ali.getPdbNo());
                re.setPdbSeg(ali.getPdbSeg());
                re.setPdbTo(ali.getPdbTo());
                if(! (inputsequence.getResidueNum().equals(""))){
                    re.setResidueName(ali.getPdbAlign().substring(inputAA-ali.getSeqFrom(), inputAA-ali.getSeqFrom()+1));
                    re.setResidueNum(new Integer(ali.getPdbFrom()+(inputAA-ali.getSeqFrom())).toString());                   
                }
                residues.add(re);
            }
        }       
        return new ModelAndView ("/result","residues", residues);
    }
    
    @GetMapping("/api")
    public ModelAndView apiInfo() {
        return new ModelAndView ("api");
    }
    
    @GetMapping("/about")
    public ModelAndView aboutInfo() {
        return new ModelAndView ("about");
    }
    
    @GetMapping("/contact")
    public ModelAndView contactInfo() {
        return new ModelAndView ("contact");
    }
    
}
