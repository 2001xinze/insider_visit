package com.dianping.service;

import com.dianping.pojo.Voucher;


public interface VoucherService {
    void addVoucher(Long id);

    Voucher queryVoucherOfShop(Long shopId);
}
