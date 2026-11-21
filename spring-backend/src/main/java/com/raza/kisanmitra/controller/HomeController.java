package com.raza.kisanmitra.controller;
import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.service.FarmerService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller public class HomeController {
 private final FarmerService farmers; public HomeController(FarmerService farmers){this.farmers=farmers;}
 @GetMapping({"/","/dashboard"}) String dashboard(Authentication auth,Model m){FarmerUser f=farmers.byEmail(auth.getName());m.addAttribute("farmer",f);return "dashboard";}
}
