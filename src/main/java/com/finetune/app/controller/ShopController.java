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
}

