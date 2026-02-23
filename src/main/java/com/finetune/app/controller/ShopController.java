package com.finetune.app.controller;

import com.finetune.app.model.Shop;
import com.finetune.app.service.ShopService;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/shops")
public class ShopController {
   private final ShopService shopService;

    // Constructor injection (preferred)
  public ShopController(ShopService shopService) {
      this.shopService = shopService;
  }

  @GetMapping()
  public List<Shop> getShops() {
    return shopService.getAllShops();
  }

  @GetMapping("/{id}")
  public Shop getShop(@PathVariable Long id) {
    return shopService.getShop(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Shop createShop(@RequestBody Shop shop) {
    return shopService.createShop(shop);
  }

  @PutMapping("/{id}")
  public Shop updateShop(@PathVariable Long id, @RequestBody Shop shop) {
    return shopService.updateShop(id, shop);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteShop(@PathVariable Long id) {
    shopService.deleteShop(id);
  }


  // Upload or update shop branding (logo and name)
  @PostMapping("/branding")
  public ResponseEntity<?> uploadShopBranding(
      @RequestParam("shopName") String shopName,
      @RequestPart(value = "logo", required = false) MultipartFile logoFile) {
    try {
      String logoUrl = null;
      if (logoFile != null && !logoFile.isEmpty()) {
        // Save logo to persistent directory
        String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "shops";
        Files.createDirectories(Paths.get(uploadsDir));
        String ext = logoFile.getOriginalFilename() != null && logoFile.getOriginalFilename().contains(".") ? logoFile.getOriginalFilename().substring(logoFile.getOriginalFilename().lastIndexOf('.')) : "";
        String filename = UUID.randomUUID().toString() + ext;
        Path filePath = Paths.get(uploadsDir, filename);
        logoFile.transferTo(filePath);
        logoUrl = "/uploads/shops/" + filename;
      }
      // Save or update shop (assuming single shop for branding)
      Shop shop = shopService.getOrCreateMainShop();
      shop.setName(shopName);
      if (logoUrl != null) {
        shop.setLogoUrl(logoUrl);
      }
      shopService.saveShop(shop);

      // Automatically generate and save agreement template HTML with shop logo
      String defaultAgreementText = "<h2>Binding Mounting Agreement</h2>" +
        "<p>By signing this agreement, you acknowledge and accept the risks associated with ski/snowboard binding mounting and adjustment. You agree to release and hold harmless the shop and its employees from any liability for injuries or damages resulting from the use of your equipment.</p>" +
        "<ul><li>You have provided accurate information about your equipment and intended use.</li>" +
        "<li>You understand that improper use or adjustment of bindings can result in injury.</li>" +
        "<li>You have inspected your equipment and accept its condition.</li></ul>" +
        "<p>Please read the full agreement and sign below.</p>";

      String html = "<div style='text-align:center;'>" +
        (shop.getLogoUrl() != null ? "<img src='" + shop.getLogoUrl() + "' alt='Shop Logo' style='max-height:80px; margin-bottom:16px;'/>" : "") +
        "<h1 style='margin:0;'>" + shop.getName() + "</h1>" +
        "</div>" +
        defaultAgreementText;

      try {
        java.nio.file.Path dir = java.nio.file.Paths.get(System.getProperty("user.dir"), "uploads", "agreements");
        java.nio.file.Files.createDirectories(dir);
        String filename = "agreement-shop-" + shop.getId() + ".html";
        java.nio.file.Path filePath = dir.resolve(filename);
        java.nio.file.Files.writeString(filePath, html);
      } catch (Exception e) {
        e.printStackTrace();
      }

      return ResponseEntity.ok(shop);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload logo: " + e.getMessage());
    }
  }
}

