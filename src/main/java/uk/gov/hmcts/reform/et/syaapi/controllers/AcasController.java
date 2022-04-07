package uk.gov.hmcts.reform.et.syaapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.et.syaapi.model.AcasCertificate;
import uk.gov.hmcts.reform.et.syaapi.service.AcasService;

import java.util.List;

@RestController
public class AcasController {

    @Autowired
    private AcasService acasService;

    @PostMapping("/acas")
    public List<AcasCertificate> sendAcasNumbers(@RequestBody String[] body) throws Exception {
      return acasService.getCertificates(body);
    }

    @GetMapping("/acas")
    public String test(){

        return "hello world!";

    }
}
