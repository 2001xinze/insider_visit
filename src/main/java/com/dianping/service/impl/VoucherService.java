package com.dianping.service.impl;

import com.dianping.mapper.VoucherMapper;
import com.dianping.pojo.Voucher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VoucherService implements com.dianping.service.VoucherService {
    @Resource
    private VoucherMapper voucherMapper;

    @Override
    public void addVoucher(Long id) {
        voucherMapper.addVoucher(id);
    }

    @Override
    public Voucher queryVoucherOfShop(Long shopId) {
        return voucherMapper.queryVoucherOfShop(shopId);
    }
}
