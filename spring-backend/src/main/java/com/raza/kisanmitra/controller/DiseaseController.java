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
import java.util.ArrayList;
import java.util.List;
@Controller public class DiseaseController {
 private static final List<String> SUPPORTED_CROPS=List.of("tomato","potato","maize");
 private final MlClient ml; private final FarmerService farmers;
 @Value("${plant-health.api-url:}") String apiUrl; @Value("${plant-health.api-key:}") String apiKey;
 public DiseaseController(MlClient ml,FarmerService farmers){this.ml=ml;this.farmers=farmers;}
 @GetMapping("/disease") String form(Authentication a,Model m){m.addAttribute("farmer",farmers.byEmail(a.getName()));m.addAttribute("crops",SUPPORTED_CROPS);if(apiKey.isBlank()||apiUrl.isBlank())m.addAttribute("apiNotConfigured",true);return "disease";}
 @PostMapping(value="/disease",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) String detect(Authentication a,@RequestParam("image") MultipartFile image,@RequestParam("crop") String crop,Model m){m.addAttribute("farmer",farmers.byEmail(a.getName()));m.addAttribute("crops",SUPPORTED_CROPS);m.addAttribute("selectedCrop",crop);
  if(apiKey.isBlank()||apiUrl.isBlank()){m.addAttribute("apiNotConfigured",true);return "disease";}
  if(!SUPPORTED_CROPS.contains(crop.toLowerCase(java.util.Locale.ROOT))){m.addAttribute("error","Choose tomato, potato, or maize.");return "disease";}
  if(image.isEmpty()||image.getSize()>8*1024*1024){m.addAttribute("error","Choose a leaf image smaller than 8 MB.");return "disease";}
  try{Map<?,?> raw=ml.disease(image,crop.toLowerCase(java.util.Locale.ROOT),apiUrl,apiKey);java.util.Map<String,Object> result=new java.util.HashMap<>();
   Object disease=raw.get("disease");
   result.put("disease",disease==null?"No plant detected":disease.toString().replace('_',' '));
   result.put("status",raw.get("result"));
   Map<?,?> treatment=raw.get("treatment") instanceof Map<?,?> t?t:Map.of();
   List<String> advice=new ArrayList<>();
   addAdvice(advice,"Cure",treatment.get("cure"));addAdvice(advice,"Prevention",treatment.get("prevention"));
   Object sprays=treatment.get("recommended_sprays");
   if(sprays instanceof List<?> list&&!list.isEmpty())advice.add("Recommended sprays: "+String.join(", ",list.stream().map(Object::toString).toList()));
   result.put("treatment",advice.isEmpty()?"Review the result with a local agriculture professional.":String.join(" ",advice));
   result.put("confidenceBelowThreshold",Boolean.TRUE.equals(raw.get("confidence_below_threshold")));
   Object confidence=raw.get("confidence");Double percent=null;
   try{if(confidence instanceof Number n)percent=n.doubleValue();else if(confidence!=null)percent=Double.parseDouble(confidence.toString().replace("%",""));if(percent!=null&&percent<=1)percent*=100;if(percent!=null)percent=Math.max(0,Math.min(100,percent));}catch(NumberFormatException ignored){}
   result.put("confidencePercent",percent);m.addAttribute("result",result);
  }catch(Exception e){m.addAttribute("error","We could not reach the plant health service. Please try again in a moment.");}return "disease";
 }
 private void addAdvice(List<String> advice,String label,Object value){if(value!=null&&!value.toString().isBlank())advice.add(label+": "+value);}
}
