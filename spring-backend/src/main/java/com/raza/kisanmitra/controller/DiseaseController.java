package com.raza.kisanmitra.controller;
import com.raza.kisanmitra.model.FarmerUser;
import com.raza.kisanmitra.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
@Controller public class DiseaseController {
 private final MlClient ml; private final FarmerService farmers;
 @Value("${plant-health.api-url:}") String apiUrl; @Value("${plant-health.api-key:abcd}") String apiKey;
 public DiseaseController(MlClient ml,FarmerService farmers){this.ml=ml;this.farmers=farmers;}
 @GetMapping("/disease") String form(Authentication a,Model m){m.addAttribute("farmer",farmers.byEmail(a.getName()));if(apiKey.isBlank()||apiKey.equals("abcd"))m.addAttribute("apiNotConfigured",true);return "disease";}
 @PostMapping(value="/disease",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) String detect(Authentication a,@RequestParam("image") MultipartFile image,Model m){m.addAttribute("farmer",farmers.byEmail(a.getName()));
  if(apiKey.isBlank()||apiKey.equals("abcd")){m.addAttribute("apiNotConfigured",true);return "disease";}
  if(image.isEmpty()||image.getSize()>8*1024*1024){m.addAttribute("error","Choose a leaf image smaller than 8 MB.");return "disease";}
  if(apiUrl.isBlank()){m.addAttribute("error","The plant health service URL is not configured.");return "disease";}
  try{Map<?,?> raw=ml.disease(image,apiUrl,apiKey);java.util.Map<String,Object> result=new java.util.HashMap<>();
   result.put("disease",first(raw,"disease","disease_name","label","prediction"));
   result.put("treatment",first(raw,"treatment","treatment_hint","advice"));
   Object confidence=raw.get("confidence");Double percent=null;
   try{if(confidence instanceof Number n)percent=n.doubleValue();else if(confidence!=null)percent=Double.parseDouble(confidence.toString().replace("%",""));if(percent!=null&&percent<=1)percent*=100;if(percent!=null)percent=Math.max(0,Math.min(100,percent));}catch(NumberFormatException ignored){}
   result.put("confidencePercent",percent);m.addAttribute("result",result);
  }catch(Exception e){m.addAttribute("error","We could not reach the plant health service. Please try again in a moment.");}return "disease";
 }
 private Object first(Map<?,?> map,String... keys){for(String key:keys)if(map.get(key)!=null)return map.get(key);return null;}
}
