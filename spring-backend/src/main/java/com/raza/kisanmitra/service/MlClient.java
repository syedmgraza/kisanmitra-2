package com.raza.kisanmitra.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service public class MlClient {
 private final WebClient client;
 private final String serviceApiKey;
 public MlClient(@Value("${ml.service-url:http://localhost:8000}") String url,@Value("${ml.service-api-key:}") String serviceApiKey){client=WebClient.builder().baseUrl(url).build();this.serviceApiKey=serviceApiKey;}
 public Map<?,?> recommend(Map<String,Object> request){return client.post().uri("/recommend").header("x-ml-service-key",serviceApiKey).contentType(MediaType.APPLICATION_JSON).bodyValue(request).retrieve().bodyToMono(Map.class).block();}
 public Map<?,?> predictYield(Map<String,Object> request){return client.post().uri("/predict-yield").header("x-ml-service-key",serviceApiKey).contentType(MediaType.APPLICATION_JSON).bodyValue(request).retrieve().bodyToMono(Map.class).block();}
 public Map<?,?> weather(String state,String season){return client.get().uri(uri->uri.path("/weather").queryParam("state",state).queryParam("season",season).build()).header("x-ml-service-key",serviceApiKey).retrieve().bodyToMono(Map.class).block();}
 public String health(){return client.get().uri("/health").retrieve().bodyToMono(String.class).block();}
 public Map<?,?> disease(MultipartFile file,String crop,String url,String apiKey){
   return WebClient.builder().build().post().uri(url).headers(h->h.setBearerAuth(apiKey)).contentType(MediaType.MULTIPART_FORM_DATA)
    .header("Idempotency-Key",java.util.UUID.randomUUID().toString())
    .body(org.springframework.web.reactive.function.BodyInserters.fromMultipartData("image",new org.springframework.core.io.ByteArrayResource(read(file)){
      @Override public String getFilename(){return file.getOriginalFilename()==null?"leaf-upload":file.getOriginalFilename();}
    }).with("crop",crop))
    .retrieve().bodyToMono(Map.class).block(java.time.Duration.ofSeconds(10));
 }
 private byte[] read(MultipartFile f){try{return f.getBytes();}catch(java.io.IOException e){throw new IllegalStateException("Could not read the uploaded image",e);}}
}
