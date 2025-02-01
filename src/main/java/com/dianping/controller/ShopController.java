package com.dianping.controller;

import com.dianping.pojo.Result;
import com.dianping.pojo.Shop;
import com.dianping.service.ShopService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopController {
    @Resource
    public ShopService shopService;

    /*
    query shop info by id
     */
    @GetMapping("/{id}")
    public Result queryShopById (@PathVariable("id") Long id) {
        return shopService.queryById(id);
    }

    /*
    add shop info
     */
    @PostMapping
    public Result saveShop (@RequestBody Shop shop) {
        // save the info to database
        shopService.save(shop);
        return Result.ok(shop.getId());
    }
    /*
    update the shop info
     */
    @PutMapping
    public Result updateShop (@RequestBody Shop shop) {
        return shopService.update(shop);
    }

    @GetMapping("/of/type")
    public Result queryShopByType (@RequestParam("typeId") Integer typeId,
                                   @RequestParam(value = "current", defaultValue = "1") Integer current,
                                   @RequestParam(value = "x", required = false) Double x,
                                   @RequestParam(value = "y", required = false) Double y
    ) {
        return  shopService.queryShopByType(typeId, current, x, y);

    }

}
