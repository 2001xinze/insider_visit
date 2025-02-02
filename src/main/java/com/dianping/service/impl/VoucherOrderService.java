package com.dianping.service.impl;

import com.dianping.mapper.VoucherOrderMapper;
import com.dianping.pojo.Result;
import com.dianping.pojo.VoucherOrder;
import com.dianping.service.SckillVoucherService;
import com.dianping.utils.RedisIdWorker;
import com.dianping.utils.UserHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class VoucherOrderService implements com.dianping.service.VoucherOrderService {
    @Resource
    private VoucherOrderMapper voucherOrderMapper;
    @Resource
    SckillVoucherService sckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    @Override
//    public Result seckillVoucher(Long voucherId) {
//        // 1.query the sckillvoucher
//        SeckillVoucher seckillVoucher = sckillVoucherService.getByVoucherId(voucherId);
//        // 2.check if the sckill begins
//        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
//            // have not begun
//            return Result.fail("the sckill has not begun");
//        }
//        // 3.check if the sckill ends
//        if (seckillVoucher.getEndTime().isBefore((LocalDateTime.now()))) {
//            return Result.fail("the sckill has ended");
//        }
//        // 4.check if the inventory is enough
//        if (seckillVoucher.getStock() < 1) {
//            //stock not enough
//            return Result.fail("the stock is not enough");
//        }
//        // 5.one person only get one order
//        // 5.1 user id
//        Long userId = UserHolder.getUser().getId();
//
////        // create the lock object
////        SimpleRedisLock lock = new SimpleRedisLock("order" + userId, stringRedisTemplate);
////        // get the key
////        boolean isLock = lock.tryLock(1200); //超时时间
//
//        // here we use redisson to get lock
//        RLock lock = redissonClient.getLock("lock:order" + userId);
//        boolean isLock = lock.tryLock();
//
//        // check if you get the lock successfully
//        if (!isLock) {
//            // not successful
//            return Result.fail("one person, one order");
//        }
//        try {
//            VoucherOrderService proxy = (VoucherOrderService) AopContext.currentProxy();
//            return proxy.createVoucherOrder(voucherId);
//        } finally {
//            // release the lock
//            lock.unlock();
//        }
//    }

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private BlockingQueue<VoucherOrder> orderTasks = new ArrayBlockingQueue<>(1024 * 1024);
    private static final ExecutorService SECKILL_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();

    @PostConstruct
    private void init () {
        SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
    }
    private class VoucherOrderHandler implements Runnable {
        @Override
        public void run () {
            while (true) {
                try {
                    // 1.get the info of the blocking queue
                    VoucherOrder voucherOrder = orderTasks.take();
                    // 2.create the order
                    handleVoucherOrder(voucherOrder);
                } catch (Exception e) {
                    log.error("process the order wrongly");
                }
            }
        }
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        // get the userId
        Long userId = voucherOrder.getUserId();
        // get the lock
        RLock lock = redissonClient.getLock("lock:order" + userId);
        boolean isLock = lock.tryLock();

        // check if you get the lock successfully
        if (!isLock) {
            // not successful
            log.error("one person get one order");
            return;
        }
        try {
            VoucherOrderService proxy = (VoucherOrderService) AopContext.currentProxy();
            proxy.createVoucherOrder(voucherOrder);
        } finally {
            // release the lock
            lock.unlock();
        }
    }

    private VoucherOrderService proxy;
    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1.get the userid
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        // Execute the lua script
        Long result = stringRedisTemplate.execute(SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId)
        );
        int r = result.intValue();
        // 2.check if r == 0
        if (r != 0) {
            // 2.1 not 0, meaning that the user has ordered
            return Result.fail(r == 1 ? "stock not enough" : "can order only once");
        }
        // 2.1.Create an order
        VoucherOrder voucherOrder = new VoucherOrder();
        // 2.2 order Id
        voucherOrder.setId(orderId);
        // 2.3 user Id
        voucherOrder.setUserId(userId);
        // 2.4 voucher Id
        voucherOrder.setVoucherId(voucherId);

        // save it to block queue
        orderTasks.add(voucherOrder);

        // 3.get the proxy object
        proxy = (VoucherOrderService) AopContext.currentProxy();
        // 3.return the order
        return Result.ok(orderId);
    }

    @Transactional
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long voucherId = voucherOrder.getId();
        Long userId = voucherOrder.getUserId();
        synchronized (userId.toString().intern()) {
            int count = voucherOrderMapper.count(userId, voucherId);
            // 5.2 check if the order exists
            if (count > 0) {
                // the user has bought the seckill voucher
                log.error("The user has bought the voucher once");
                return;
            }
             // 6.Fastener inventory
            boolean success = sckillVoucherService.update(voucherId);
            if (!success) {
                log.error("the sckill has ended");
                return;
            }

            // 7.4 save the order to database
            voucherOrderMapper.save(voucherOrder);
        }
    }









}
