package com.raza.kisanmitra.controller;
import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.service.FarmerService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
@Controller public class AuthController {
 private final FarmerService farmers; private final LocationCatalog locations;
 public AuthController(FarmerService farmers,LocationCatalog locations){this.farmers=farmers;this.locations=locations;}
 @GetMapping("/login") String login(){return "login";}
 @GetMapping("/register") String registerForm(Model m){m.addAttribute("farmer",new FarmerUser());addLocations(m,null);return "register";}
 @PostMapping("/register") String register(@Valid @ModelAttribute("farmer") FarmerUser farmer,BindingResult br,Model m){
  String state=locations.canonicalState(farmer.getState());
  if(state==null)br.rejectValue("state","location.state.invalid","Choose a state from the list.");
  else{
   farmer.setState(state);
   String district=locations.canonicalDistrict(state,farmer.getDistrict());
   if(district==null)br.rejectValue("district","location.district.invalid","Choose a district from the selected state.");
   else farmer.setDistrict(district);
  }
  if(br.hasErrors()){addLocations(m,farmer.getState());return "register";}
  try{farmers.register(farmer);return "redirect:/login?registered";}catch(IllegalArgumentException e){m.addAttribute("registrationError",e.getMessage());addLocations(m,farmer.getState());return "register";}
 }
 private void addLocations(Model model,String state){model.addAttribute("states",locations.states());model.addAttribute("districts",locations.districts(state));}
}
