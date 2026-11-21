package com.raza.kisanmitra.controller;
import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.service.FarmerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@Controller public class AuthController {
 private static final List<String> STATES=List.of("Andaman And Nicobar Island","Andhra Pradesh","Arunachal Pradesh","Assam","Bihar","Chandigarh","Chhattisgarh","Dadra And Nagar Haveli","Daman And Diu","Delhi","Goa","Gujarat","Haryana","Himachal Pradesh","Jammu And Kashmir","Jharkhand","Karnataka","Kerala","Laddak","Madhya Pradesh","Maharashtra","Manipur","Meghalaya","Mizoram","Nagaland","Odisha","Puducherry","Punjab","Rajasthan","Sikkim","Tamil Nadu","Telangana","Tripura","Uttar Pradesh","Uttarakhand","West Bengal");
 private final FarmerService farmers; public AuthController(FarmerService farmers){this.farmers=farmers;}
 @GetMapping("/login") String login(){return "login";}
 @GetMapping("/register") String registerForm(Model m){m.addAttribute("farmer",new FarmerUser());m.addAttribute("states",STATES);return "register";}
 @PostMapping("/register") String register(@Valid @ModelAttribute("farmer") FarmerUser farmer,BindingResult br,Model m){
  if(br.hasErrors()){m.addAttribute("states",STATES);return "register";}
  try{farmers.register(farmer);return "redirect:/login?registered";}catch(IllegalArgumentException e){m.addAttribute("registrationError",e.getMessage());m.addAttribute("states",STATES);return "register";}
 }
}
