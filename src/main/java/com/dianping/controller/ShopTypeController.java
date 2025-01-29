package com.dianping.controller;

import com.dianping.pojo.Result;
import com.dianping.pojo.ShopType;
import com.dianping.service.ShopTypeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private ShopTypeService shopTypeService;

    @GetMapping("list")
    public Result queryTypeList () {
        List<ShopType> typeList = shopTypeService.queryShopType();
        return Result.ok(typeList);
    }

}
