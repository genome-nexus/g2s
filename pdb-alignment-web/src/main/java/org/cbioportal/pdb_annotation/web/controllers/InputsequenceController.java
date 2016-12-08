package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Inputsequence;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        inputsequence.setSequence(inputsequence.getSequence());
        
        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand= new PdbScriptsPipelineRunCommand();        
        List<Alignment> alignments = pdbScriptsPipelineRunCommand.runBlast(inputsequence);
        
        return new ModelAndView ("/result","alignments", alignments);
    }
    
}
