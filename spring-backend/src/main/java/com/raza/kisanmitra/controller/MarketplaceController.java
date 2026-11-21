package com.raza.kisanmitra.controller;
import com.raza.kisanmitra.model.*;
import com.raza.kisanmitra.repository.CropListingRepository;
import com.raza.kisanmitra.service.FarmerService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@Controller @RequestMapping("/marketplace") public class MarketplaceController {
 private final CropListingRepository listings; private final FarmerService farmers;
 public MarketplaceController(CropListingRepository listings,FarmerService farmers){this.listings=listings;this.farmers=farmers;}
 @GetMapping String browse(Authentication a,@RequestParam(defaultValue="") String crop,Model m){FarmerUser f=farmers.byEmail(a.getName());m.addAttribute("farmer",f);m.addAttribute("crop",crop);
  List<CropListing> rows;if(crop.isBlank())rows=listings.findByStatusAndLocationDistrictIgnoreCaseOrderByCreatedAtDesc(CropListing.Status.ACTIVE,f.getDistrict());else rows=listings.findByStatusAndLocationDistrictIgnoreCaseAndCropNameContainingIgnoreCaseOrderByCreatedAtDesc(CropListing.Status.ACTIVE,f.getDistrict(),crop);
  if(rows.isEmpty()&&!crop.isBlank())rows=listings.findByStatusAndCropNameContainingIgnoreCaseOrderByCreatedAtDesc(CropListing.Status.ACTIVE,crop);m.addAttribute("listings",rows);return "marketplace";}
 @GetMapping("/new") String newForm(Authentication a,Model m){FarmerUser f=farmers.byEmail(a.getName());CropListing l=new CropListing();l.setSellerName(f.getUsername());l.setLocationState(f.getState());l.setLocationDistrict(f.getDistrict());m.addAttribute("farmer",f);m.addAttribute("listing",l);return "listing-form";}
 @PostMapping("/new") String create(Authentication a,@Valid @ModelAttribute("listing") CropListing listing,BindingResult br,Model m){FarmerUser f=farmers.byEmail(a.getName());m.addAttribute("farmer",f);if(br.hasErrors())return "listing-form";listing.setOwnerUserId(f.getId());listing.setStatus(CropListing.Status.ACTIVE);listings.save(listing);return "redirect:/marketplace";}
 @GetMapping("/{id}") String detail(Authentication a,@PathVariable Long id,Model m){FarmerUser f=farmers.byEmail(a.getName());CropListing l=listings.findById(id).orElseThrow();m.addAttribute("farmer",f);m.addAttribute("listing",l);m.addAttribute("isOwner",Objects.equals(f.getId(),l.getOwnerUserId()));return "listing-detail";}
 @PostMapping("/{id}/sold") String sold(Authentication a,@PathVariable Long id){FarmerUser f=farmers.byEmail(a.getName());CropListing l=listings.findById(id).orElseThrow();if(!Objects.equals(f.getId(),l.getOwnerUserId()))throw new org.springframework.security.access.AccessDeniedException("Only the listing owner can mark it sold");l.setStatus(CropListing.Status.SOLD);listings.save(l);return "redirect:/marketplace/"+id;}
}
