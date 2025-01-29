package com.dianping.controller;

import com.dianping.pojo.Result;
import com.dianping.pojo.SeckillVoucher;
import com.dianping.pojo.Voucher;
import com.dianping.service.VoucherService;
import com.dianping.service.impl.SeckillVoucherService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/voucher")
public class VoucherController {
    @Resource
    private VoucherService voucherService;
    @Resource
    private SeckillVoucherService seckillVoucherService;

    /**
     * add new voucher
     */
    @PostMapping
    public Result addVoucher (@RequestBody Voucher voucher) {
        Long id = voucher.getId();
        voucherService.addVoucher(id);
        return Result.ok(voucher.getId());
    }

    /**
     * add new sckill voucher
     * @param voucher
     * @return
     */
    @PostMapping("/seckill")
    public Result addSeckillVoucher (@RequestBody Voucher voucher) {
        seckillVoucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop (@PathVariable("shopId") Long shopId) {
        Voucher voucher = voucherService.queryVoucherOfShop(shopId);
        return Result.ok(voucher);
    }

    @GetMapping("/seckill/{id}")
    public Result getById(@PathVariable("id") Long id) {
        SeckillVoucher seckillVoucher = seckillVoucherService.getByVoucherId(id);
        return Result.ok(seckillVoucher);
    }



}
