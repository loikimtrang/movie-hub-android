package com.movie_hub.android.data.local.room;

import com.movie_hub.android.data.model.room.OrderEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockOrder {
    public static List<OrderEntity> orders(){
        List<OrderEntity> results = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i <10; i++) {
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setId((long) i);
            orderEntity.setName("" + i);
            orderEntity.setSyncStatus(random.nextBoolean());
            results.add(orderEntity);
        }
        return results;
    }
}
