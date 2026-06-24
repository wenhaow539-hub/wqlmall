package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import com.hmall.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class orderStatusListener {

    private final ICartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(name = "trade.topic", type = "topic"),
            key = "order.create"
    ))
    public void listenerPaySuccess(Map<String, Object> msg){
        Collection<Long> itemIds = (Collection<Long>) msg.get("itemIds");
        Long userId = ((Number) msg.get("userId")).longValue();
        UserContext.setUser(userId);
        try{
            cartService.removeByItemIds(itemIds);
        } finally {
            UserContext.removeUser();
        }
    }
}
