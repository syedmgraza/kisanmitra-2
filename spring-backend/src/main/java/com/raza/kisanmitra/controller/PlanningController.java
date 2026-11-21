package com.raza.kisanmitra.controller;
import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.*;
@Controller public class PlanningController {
 private static final Logger log=LoggerFactory.getLogger(PlanningController.class);
 private final MlClient ml; private final FarmerService farmers;
 public PlanningController(MlClient ml,FarmerService farmers){this.ml=ml;this.farmers=farmers;}
 @GetMapping("/recommendation") String rec(Authentication auth,Model m){m.addAttribute("farmer",farmers.byEmail(auth.getName()));m.addAttribute("seasons",List.of("Kharif","Rabi","Whole Year","Summer","Winter"));return "recommendation";}
 @PostMapping("/recommendation") String recommend(Authentication auth,@RequestParam String season,Model m){log.info("Received crop recommendation form submission for season {}.",season);FarmerUser f=farmers.byEmail(auth.getName());m.addAttribute("farmer",f);m.addAttribute("seasons",List.of("Kharif","Rabi","Whole Year","Summer","Winter"));m.addAttribute("selectedSeason",season);
  try{Map<?,?> res=ml.recommend(Map.of("state",f.getState(),"district",f.getDistrict(),"season",season,"land_size",f.getLandSizeHectares()));m.addAttribute("result",res);}
  catch(Exception e){log.warn("Crop recommendation request to ML service failed.",e);m.addAttribute("serviceError","Crop recommendation failed: "+errorDetail(e));}return "recommendation";}
 @GetMapping("/yield") String yield(Authentication auth,Model m){FarmerUser f=farmers.byEmail(auth.getName());m.addAttribute("farmer",f);m.addAttribute("seasons",List.of("Kharif","Rabi","Whole Year","Summer","Winter"));m.addAttribute("fertilizers",List.of("Recommended","Less than recommended","More than recommended"));return "yield";}
 @PostMapping("/yield") String predict(Authentication auth,@RequestParam String season,@RequestParam String crop,@RequestParam(defaultValue="800") double rainfall,@RequestParam(defaultValue="Recommended") String fertilizer,Model m){FarmerUser f=farmers.byEmail(auth.getName());m.addAttribute("farmer",f);m.addAttribute("seasons",List.of("Kharif","Rabi","Whole Year","Summer","Winter"));m.addAttribute("fertilizers",List.of("Recommended","Less than recommended","More than recommended"));m.addAttribute("selectedSeason",season);m.addAttribute("crop",crop);m.addAttribute("rainfall",rainfall);m.addAttribute("fertilizer",fertilizer);
  try{m.addAttribute("result",ml.predictYield(Map.of("state",f.getState(),"district",f.getDistrict(),"season",season,"crop",crop,"rainfall",rainfall,"fertilizer_choice",fertilizer,"land_size",f.getLandSizeHectares())));}
  catch(Exception e){log.warn("Yield prediction request to ML service failed.",e);m.addAttribute("serviceError","Yield prediction failed: "+errorDetail(e));}return "yield";}
 @GetMapping("/weather") String weather(Authentication auth,Model m,@RequestParam(defaultValue="Kharif") String season){FarmerUser f=farmers.byEmail(auth.getName());m.addAttribute("farmer",f);m.addAttribute("seasons",List.of("Kharif","Rabi","Whole Year","Summer","Winter"));m.addAttribute("selectedSeason",season);try{m.addAttribute("weather",ml.weather(f.getState(),season));}catch(Exception e){log.warn("Weather request to ML service failed.",e);m.addAttribute("serviceError","Weather details are temporarily unavailable.");}return "weather";}
 private String errorDetail(Exception e){
  String detail;
  if(e instanceof WebClientResponseException response){
   String body=response.getResponseBodyAsString();
   detail="HTTP "+response.getStatusCode().value()+(body==null||body.isBlank()?"":": "+body);
  }else detail=e.getClass().getSimpleName()+(e.getMessage()==null?"":": "+e.getMessage());
  return detail.length()>600?detail.substring(0,600)+"…":detail;
 }
}
