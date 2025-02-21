package com.dianping.controller;

import com.dianping.pojo.Result;
import com.dianping.service.VoucherOrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Resource
    private VoucherOrderService voucherOrderService;

    /**
     *
     * @param voucherId
     * @return
     */
    @PostMapping("/seckill/{id}")
    public Result seckillVoucher (@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }

    /**
     *
     * @param voucherId
     * @return
     */
    @PostMapping("/{id}")
    public Result normalVoucher (@PathVariable("id") Long voucherId) {
        return Result.fail("the function is not finished");
    }
}
